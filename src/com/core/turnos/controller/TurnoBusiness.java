package com.core.turnos.controller;

import com.neo.app.bean.ProductoBean;
import com.neo.app.bean.Totalizador;
import com.google.gson.Gson;
import com.core.app.NeoService;
import com.neo.app.bean.Cara;
import com.neo.app.bean.Manguera;
import com.core.turnos.bean.TurnosResponse;
import com.core.database.impl.TurnoDao;
import com.neo.app.bean.Persona;
import com.neo.app.bean.Surtidor;
import com.core.app.protocols.MepsanController;
import com.core.database.impl.PersonaDao;
import com.core.database.DAOException;
import com.core.database.Postgrest;
import com.core.database.impl.SurtidorDao;
import com.core.turnos.bean.SurtidorInventario;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class TurnoBusiness {

    boolean conectado;
    Postgrest db;
    Gson gson = new Gson();

    TurnosResponse iniciarJornada(String identificacion, String pin, int surt) {
        TurnosResponse respuesta = new TurnosResponse();
        try {
            PersonaDao pdao = new PersonaDao();
            Persona persona = pdao.getPersonaByIdentificacion(identificacion);

            Surtidor surtidor = NeoService.surtidores.get(surt).control.surtidor;

            boolean surtidorEsperando = true;
            Object estadoActual = null;
            if (!surtidor.getCaras().isEmpty()) {
                for (Map.Entry<Integer, Cara> entry1 : surtidor.getCaras().entrySet()) {
                    Integer key1 = entry1.getKey();
                    Cara cara = entry1.getValue();
                    if (cara.getEstado() != MepsanController.SURTIDORES_ESTADO_ESPERA) {
                        surtidorEsperando = false;
                        estadoActual = cara.getEstado();
                    }
                }
            } else {
                for (Map.Entry<Integer, Manguera> entry1 : surtidor.getMangueras().entrySet()) {
                    Integer key1 = entry1.getKey();
                    Manguera manguera = entry1.getValue();
                    if (manguera.getEstado() != Surtidor.SURTIDORES_ESTADO.ESPERA) {
                        surtidorEsperando = false;
                        estadoActual = manguera.getEstado();
                    }
                }
            }

            ArrayList<SurtidorInventario> inventarios = new ArrayList<>();
            if (surtidorEsperando) {
                if (persona == null || surtidor == null) {
                    respuesta.setSuccess(false);
                    respuesta.setMensaje("Datos incorrectos");
                } else {
                    if (!surtidor.getCaras().isEmpty()) {
                        for (Map.Entry<Integer, Cara> entry1 : surtidor.getCaras().entrySet()) {
                            Integer key1 = entry1.getKey();
                            Cara cara = entry1.getValue();
                            Totalizador[] totales = NeoService.surtidores.get(surt).control.getTotalizadoresByCara(cara);

                            SurtidorDao ppdao = new SurtidorDao();

                            for (int i = 0; i < totales.length; i++) {
                                for (Map.Entry<Integer, Manguera> entry : cara.getMangueras().entrySet()) {
                                    Object key = entry.getKey();
                                    Manguera mangue = entry.getValue();
                                    if (mangue.getGrado() == i) {
                                        mangue.setRegistroSurtidorVentas(totales[i].getAcumuladoVenta());
                                        mangue.setRegistroSurtidorVolumen(totales[i].getAcumuladoVolumen());

                                        ProductoBean producto = ppdao.getProductoByMangueta(cara.getNumero(), mangue.getId());

                                        SurtidorInventario invetn = new SurtidorInventario();
                                        invetn.setSurtidor(surt);
                                        invetn.setManguera(mangue.getId());
                                        invetn.setAcumuadoVentasInicial(totales[i].getAcumuladoVenta());
                                        invetn.setAcumuadoVolumenInicial(totales[i].getAcumuladoVolumen());
                                        invetn.setProductoDescripcion(producto.getDescripcion());
                                        inventarios.add(invetn);

                                    }
                                }
                            }

                        }
                    } else {
                        for (Map.Entry<Integer, Manguera> entry1 : surtidor.getMangueras().entrySet()) {
                            Integer key1 = entry1.getKey();
                            Manguera manguera = entry1.getValue();
                            Totalizador total = NeoService.surtidores.get(surt).control.consigueTotalizadores(manguera.getConfiguracionId());
                            manguera.setRegistroSurtidorVentas(total.getAcumuladoVenta());
                            manguera.setRegistroSurtidorVolumen(total.getAcumuladoVolumen());
                        }
                    }
                    TurnoDao dao = new TurnoDao();
                    respuesta = dao.iniciarJornada(persona, surtidor);
                    respuesta.setInventario(inventarios);

                }
                respuesta.setPersona(persona);
                respuesta.setSurtidor(surtidor);

            } else {
                respuesta.setSuccess(false);
                respuesta.setMensaje("Surtidor debe estar en ESPERA. Estado actual " + estadoActual);
            }

            return respuesta;
        } catch (DAOException ex) {

            Logger.getLogger(TurnoFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public TurnosResponse finalizarJornada(String identificacion) {
        TurnosResponse respuesta = new TurnosResponse();
        try {
            PersonaDao pdao = new PersonaDao();
            Persona persona = pdao.getPersonaByIdentificacion(identificacion);

            if (persona == null) {

                respuesta.setSuccess(false);
                respuesta.setMensaje("Datos incorrectos");
            } else {

                Surtidor surtidor = pdao.getSurtidorByJornada(persona);
                if (surtidor == null) {
                    respuesta.setSuccess(false);
                    respuesta.setMensaje("Datos incorrectos");
                } else {

                    surtidor = NeoService.surtidores.get(surtidor.getId()).control.surtidor;

                    boolean surtidorEsperando = true;
                    Object estadoActual = null;
                    if (!surtidor.getCaras().isEmpty()) {
                        for (Map.Entry<Integer, Cara> entry1 : surtidor.getCaras().entrySet()) {
                            Cara cara = entry1.getValue();
                            if (cara.getEstado() != MepsanController.SURTIDORES_ESTADO_ESPERA) {
                                surtidorEsperando = false;
                                estadoActual = cara.getEstado();
                            }
                        }
                    }

                    if (surtidorEsperando) {
                        if (!pdao.tieneVentaEnCurso(persona)) {

                            TurnoDao dao = new TurnoDao();
                            TurnosResponse inventarios = dao.consultaJornada(persona);
                            TurnosResponse acumulados = dao.consultaAcumulado(inventarios.getId(), surtidor);
                            respuesta = dao.finalizarJornada(persona, surtidor);

                            respuesta.setId(inventarios.getId());
                            respuesta.setFactorInventario(inventarios.getFactorInventario());
                            respuesta.setFactorPrecio(inventarios.getFactorPrecio());
                            respuesta.setFactorVolumen(inventarios.getFactorVolumen());
                            respuesta.setAcumuadoVentas(inventarios.getAcumuadoVentas());
                            respuesta.setAcumuadoVolumen(inventarios.getAcumuadoVolumen());
                            respuesta.setFechaInicio(inventarios.getFechaInicio());
                            respuesta.setCantidad(inventarios.getCantidad());

                            respuesta.setInventario(acumulados.getInventario());

                        } else {
                            respuesta.setSuccess(false);
                            respuesta.setMensaje("Tiene una venta en curso");
                        }
                    } else {
                        respuesta.setSuccess(false);
                        respuesta.setMensaje("Surtidor debe estar en ESPERA. Estado actual " + estadoActual);
                    }

                }
            }
            respuesta.setPersona(persona);

            return respuesta;
        } catch (DAOException ex) {

            Logger.getLogger(TurnoFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    TurnosResponse consultaJornada(String identificacion) {
        TurnosResponse respuesta = new TurnosResponse();
        try {
            PersonaDao pdao = new PersonaDao();
            Persona persona = pdao.getPersonaByIdentificacion(identificacion);
            if (persona == null) {
                respuesta.setSuccess(false);
                respuesta.setMensaje("Datos incorrectos");
            } else {
                TurnoDao dao = new TurnoDao();
                respuesta = dao.consultaJornada(persona);
                TurnosResponse acumulados = dao.consultaAcumulado(respuesta.getId(), null);
                respuesta.setInventario(acumulados.getInventario());
            }
            respuesta.setPersona(persona);

            return respuesta;
        } catch (DAOException ex) {

            Logger.getLogger(TurnoFacade.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
