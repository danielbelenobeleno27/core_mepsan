package com.core.app.protocols;

import com.butter.bean.CatalogoBean;
import com.butter.bean.EquipoDao;
import com.butter.bean.Main;
import com.butter.bean.PredeterminadaBean;
import com.butter.bean.TareaProgramada;
import com.butter.bean.Utils;
import com.core.app.NeoService;
import static com.core.app.NeoService.sutidao;
import com.core.app.server.NotificacionSocket;
import com.core.commons.cliente.ClientSerial;
import com.core.commons.cliente.ClientTcp;
import com.core.commons.cliente.Cliente;
import com.core.database.DAOException;
import com.core.database.impl.SurtidorDao;
import com.core.print.services.PrinterFacade;
import com.google.gson.JsonObject;
import com.neo.app.bean.AConstant;
import com.neo.app.bean.Autorizacion;
import com.neo.app.bean.BaseControllerProtocols;
import static com.neo.app.bean.BaseControllerProtocols.ESTA_PROCESANDO;
import static com.neo.app.bean.BaseControllerProtocols.TIENE_PETICION;
import com.neo.app.bean.Cara;
import com.neo.app.bean.Manguera;
import com.neo.app.bean.Precio;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.Totalizador;
import com.neo.app.bean.Venta;
import com.neo.app.bean.VentaParcial;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MepsanController extends BaseControllerProtocols {

    public MepsanProtocol protocolo;
    SurtidorDao sdao = new SurtidorDao();
    EquipoDao edao = new EquipoDao();

    private static final TreeMap<Byte, String> ESTADOS_SURTIDOR = new TreeMap<>();
    private static final TreeMap<Byte, String> ESTADOS_MANGUERA = new TreeMap<>();

    //FINALIZA VALORES DE LOS ESTADOS
    public static final int CONSTANTE_FIND_MANGUERA = 0x0F;
    public static final int CONSTANTE_FIND_REPUESTA = 0xF0;

    private static final byte ESTADO_MANGUERA_COLGADO = 0X00;
    private static final byte ESTADO_MANGUERA_DESCOLGADO = 0X10;

    //Estados del surtidor
    private static final byte SURTIDOR_ESTADO_NO_PROGRAMADO = 0X00;

    private static final byte SURTIDOR_ESTADO_REINICIADO = 0X01;
    private static final byte SURTIDOR_ESTADO_AUTORIZADO = 0X02;
    private static final byte SURTIDOR_ESTADO_DESPACHO = 0X04;
    private static final byte SURTIDOR_ESTADO_FIN_DESPACHO = 0X05;
    private static final byte SURTIDOR_ESTADO_VOL_MAX_ALCANZADO = 0X06;
    private static final byte SURTIDOR_ESTADO_APAGADO = 0X07;
    private static final byte SURTIDOR_ESTADO_EN_PAUSA = (byte) 0X0B;

    public static byte SURTIDOR_ESTADO = SURTIDOR_ESTADO_NO_PROGRAMADO;

    //Estados de las mangueras
    static final byte MANGUERA_ESTADO = 0x00;
    private static final byte MANGUERA_ESTADO_COLGADA = 0X00;
    private static final byte MANGUERA_ESTADO_DESCOLGADA = 0X10;

    private static final int TIEMPO_ESPERA = 100;
    private static final int TIEMPO_ENTRE_COMANDO = 200;
    private static final int TIEMPO_REVISION_MAX = 1000;
    private static final int TIEMPO_ESPERA_TOTALIZADORES = 300;

    private Long grupoJornada = null;
    public static final int PREGUNTA_TOTALIZADOR_VOLUMEN = 0;
    public static final int PREGUNTA_TOTALIZADOR_IMPORTE = 1;

    private String ip;
    private int puerto;

    int listaPrecio1 = 1;
    int listaPrecio2 = 2;

    private Cliente client;
    PrinterFacade facade;

    SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_DATETIME_FULL);
    SimpleDateFormat sdfFull = new SimpleDateFormat(AConstant.FORMAT_DATETIME_FULL);
    SimpleDateFormat sdfBasic = new SimpleDateFormat(AConstant.FORMAT_DATETIME_AM);

    //Server de notificaciones
    public MepsanController(Surtidor surtidor) {

        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_NO_PROGRAMADO, "SURTIDOR NO PROGRAMADO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_REINICIADO, "SURTIDOR REINICIADO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_AUTORIZADO, "SURTIDOR AUTORIZADO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_DESPACHO, "SURTIDOR EN DESPACHO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_FIN_DESPACHO, "SURTIDOR EN FIN DE DESPACHO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_VOL_MAX_ALCANZADO, "SURTIDOR VOL MAX ALCANZADO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_APAGADO, "SURTIDOR APAGADO");
        ESTADOS_SURTIDOR.put(SURTIDOR_ESTADO_EN_PAUSA, "SURTIDOR EN PAUSA");

        ESTADOS_MANGUERA.put(MANGUERA_ESTADO_COLGADA, "MANGUERA COLGADA");
        ESTADOS_MANGUERA.put(MANGUERA_ESTADO_DESCOLGADA, "MANGUERA DESCOLGADA");

        ip = surtidor.getIp();
        puerto = surtidor.getPort();

        this.surtidor = surtidor;
        facade = new PrinterFacade();

        switch (surtidor.getControlador()) {
            case NeoService.CONTROLADOR_IP:
                client = new ClientTcp(ip, puerto);
                NeoService.setLog("SURTIDOR CONECTADO POR IP");
                break;
            case NeoService.CONTROLADOR_RS_232:
                client = new ClientSerial(ip, puerto, NeoService.DATABIT, NeoService.STOPBIT, NeoService.PARITY, surtidor.isDebugEstado());
                NeoService.setLog("SURTIDOR CONECTADO POR SERIAL");
                break;
            default:
                NeoService.setLog("EL CONTROLADOR DEL SURTIDOR NO ESTA CONFIGURADO CORRECTAMENTE");
                throw new AssertionError();
        }

        protocolo = new MepsanProtocol(client);
        NeoService.setLog("Iniciando protocolo Mepsan");
    }

    @Override
    public void run() {
        while (true) {
            sdao.getDebugEstado(surtidor);
            try {
                if (!TIENE_PETICION.get()) {
                    ESTA_PROCESANDO.set(true);
                    getEstados();
                }
                ESTA_PROCESANDO.set(false);
                pauseCore(TIEMPO_ENTRE_COMANDO);
            } catch (Exception a) {
                Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, a);
                pauseCore(TIEMPO_REVISION_MAX);
            }
        }
    }

    boolean conectado = false;

    public boolean existeManguera(int surtidorId, int mangueraId) {
        return surtidores.get(surtidorId) != null;
    }

    public void getEstados() {
        for (Map.Entry<Integer, Cara> caras : surtidor.getCaras().entrySet()) {
            Cara cara = caras.getValue();
            try {
                consultarEstado(surtidor, cara);
                if (!conectado) {
                    conectado = true;
                }
                if (!NeoService.SURTIDOR_ERROR) {
                    NeoService.SURTIDOR_ERROR = false;
                    JsonObject json = new JsonObject();
                    json.addProperty("tipo", 1);
                    json.addProperty("subtipo", NeoService.SUB_COMANDO_NEOAPP_CONEXION_ONLINE);
                    json.addProperty("mensaje", "Online");
                    NotificacionSocket.publishrr(1, json);
                }
            } catch (IOException ex) {
                NeoService.SURTIDOR_ERROR = true;
                NotificacionSocket.publish(1, "Perdida comunicacion S" + surtidor.getId() + " C" + cara.getNumero() + " IP: " + ip + ":" + puerto);
            } catch (Exception ex) {
                Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int validarGrado(Cara cara, int mangueraSurtidor) {
        int existeGrado = -1;
        for (Map.Entry<Integer, Manguera> mangueras : cara.getMangueras().entrySet()) {
            Manguera manguera = mangueras.getValue();
            if (manguera.getGrado() == mangueraSurtidor) {
                existeGrado = manguera.getGrado();
                break;
            }
        }
        return existeGrado;
    }

    public void consultarEstado(Surtidor surtidor, Cara cara) throws Exception {
        int estado = -1;
        ArrayList<Byte> respuesta = protocolo.estadoManguera(surtidor, cara.getNumero(), TIEMPO_ESPERA);
        if (respuesta != null && respuesta.size() > 1) {
            estado = Byte.toUnsignedInt((byte) (respuesta.get(10) & CONSTANTE_FIND_REPUESTA));
            byte estadoSurtidor = respuesta.get(4);
            int mangueraSurtidor = respuesta.get(10) & CONSTANTE_FIND_MANGUERA;
            if (surtidor.isDebugEstado()) {
                NeoService.setLog(" ESTADO INT : -> " + estado + " MANGUERA SURTIDOR " + mangueraSurtidor + " " + (String.format("%02x", (byte) estado).toUpperCase() + " "));
            }
            NeoService.setLog(" ESTADO SURTIDOR ->  " + (String.format("%02x", estadoSurtidor).toUpperCase() + " "));

            if (estado == ESTADO_MANGUERA_DESCOLGADO && (estadoSurtidor == SURTIDOR_ESTADO_DESPACHO || estadoSurtidor == SURTIDOR_ESTADO_EN_PAUSA)) {
                estado = SURTIDOR_ESTADO_DESPACHO;
            } else if (estado == ESTADO_MANGUERA_DESCOLGADO && estadoSurtidor == SURTIDOR_ESTADO_AUTORIZADO) {
                estado = SURTIDOR_ESTADO_AUTORIZADO;
            } else if (estado == ESTADO_MANGUERA_COLGADO && cara.getEstado() == SURTIDOR_ESTADO_DESPACHO) {
                estado = SURTIDOR_ESTADO_FIN_DESPACHO;
            }

            if (validarGrado(cara, mangueraSurtidor) == -1) {
                NeoService.setLog(NeoService.ANSI_RED + "-> GRADO DESCONOCIDO " + mangueraSurtidor + NeoService.ANSI_RESET);
            } else {
                for (Map.Entry<Integer, Manguera> entry : cara.getMangueras().entrySet()) {
                    Integer key = entry.getKey();
                    Manguera value = entry.getValue();
                    if (value.getGrado() == validarGrado(cara, mangueraSurtidor)) {
                        cara.setMagueraactual(value);
                        break;
                    }
                }
                validarFinVenta(surtidor, cara, estado);
                procesarEstadocara(surtidor, cara, estado, mangueraSurtidor);
            }

        }
    }

    void procesarEstadoEspera(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) {
        NeoService.setLog("->  ESTADO MANGUERA #".concat(cara.getMagueraactual().getId() + "").concat(" COLGADO"));

        if (NeoService.AUTORIZACION.containsKey(cara.getNumero())) {
            Date ahora = new Date();
            Date ultimavez = NeoService.AUTORIZACION.get(cara.getNumero());
            long lapso = ahora.getTime() - ultimavez.getTime();
            int tiempoDuracionTag;
            try {
                tiempoDuracionTag = sdao.getTiempoDuracionTag() * 1000;
            } catch (Exception | DAOException e) {
                tiempoDuracionTag = 10000;
            }

            if (lapso >= tiempoDuracionTag) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MESSAN: " + NeoService.ANSI_RESET + NeoService.ANSI_YELLOW + "*BORRANDO LECTURAS DE LOS TAG... ESPERE*" + NeoService.ANSI_RESET);
                NeoService.PERSONA_AUTORIZA_ID.set(0);
                NeoService.REGISTRO_PERSONA.put(cara.getNumero(), 0L);
                NeoService.AUTORIZACION.remove(cara.getNumero());
            }
        }

        if (surtidor.isDebugEstado()) {
            NeoService.setLog(NeoService.ANSI_CYAN + "MESSAN: " + NeoService.ANSI_RESET + "[STATUS]: EQUIPOID: " + Main.credencial.getId() + "  CARA(" + surtidor.getId() + "," + cara.getNumero() + ") EN ESPERA " + sdfFull.format(new Date()));
        }

        grupoJornada = sutidao.getGrupoJornada();
        if (grupoJornada == 0) {
            ejecucionTareasProgramadas(cara.getNumero());
        }

        boolean iniciando = false;
        for (Map.Entry<Integer, Manguera> mangueras : cara.getMangueras().entrySet()) {
            Manguera manguera = mangueras.getValue();
            if (!manguera.isTieneTotalizadores()) {
                iniciando = true;
                validaTotalizadores(null, surtidor, cara);
            }
        }

        if (iniciando && grupoJornada > 0) {
            for (Map.Entry<Integer, Manguera> mangueras : cara.getMangueras().entrySet()) {
                Manguera manguera = mangueras.getValue();
                try {
                    sdao.registrarTotalizadores(surtidor.getId(), cara.getNumero(), grupoJornada, manguera);
                } catch (DAOException ex) {
                    Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (cara.getEstado() != ESTADO_MANGUERA_COLGADO) {
            NeoService.setLog(NeoService.ANSI_CYAN + "->  MEPSAN: " + NeoService.ANSI_RESET + "SE SOLICITA TOTALIZADORES PORQUE VIENE DIFERENTE ESTADO");
            validaTotalizadores(null, surtidor, cara);
        }

        cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_ESPERA);
        cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_ESPERA);

        cara.setEstado(ESTADO_MANGUERA_COLGADO);
        cara.setMagueraactual(null);
        sutidao.guardarEstado(surtidor, cara);

        if (sdao.getAutorizacionesEnCaraVencimiento(cara.getNumero(), NeoService.TIEMPO_BORRAR_AUTORIZACION) >= 1) {
            sdao.borrarAutorizacionesEnCaraTiempo(cara.getNumero(), NeoService.TIEMPO_BORRAR_AUTORIZACION);
            facade.conFormato(cara.getNumero() + ". SE HA CANCELADO UNA PRE-AUTORIZACION EN LA CARA " + cara.getNumero() + "\r\nFECHA: " + sdfBasic.format(new Date()) + "\r\n");
        }
    }

    private void ejecucionTareasProgramadas(int cara) {
        try {
            LinkedHashSet<TareaProgramada> tareas = sdao.getTareasProgramadas();
            if (!tareas.isEmpty()) {
                NeoService.setLog(NeoService.ANSI_CYAN + "**** MEPSAN: " + NeoService.ANSI_RESET + "BUSCO FACTOR DE PRECIO");
                int factorPrecio = sdao.getFactorPrecio(surtidor.getId());
                for (TareaProgramada tarea : tareas) {
                    if (tarea.getCara() == cara) {
                        long precio = Utils.calculeCantidadInversa(tarea.getPrecioOriginal(), factorPrecio);
                        NeoService.setLog(NeoService.ANSI_CYAN + "****** MEPSAN: " + NeoService.ANSI_RESET + "APLICANDO CAMBIO DE PRECIO A CARA=" + tarea.getCara() + " MANGUERA= " + tarea.getManguera() + " GRADO= " + tarea.getGrado() + " VALOR ORIGINAL =" + tarea.getPrecioOriginal() + " FACTOR PRECIO = " + factorPrecio + " VALOR FACTOR = " + precio);
                        ESTA_PROCESANDO.set(false);
                        actualizaMultipreciosPrecios(precio, 1, tarea.getGrado(), tarea.getCara(), false, tarea.getPrecioOriginal());
                        sdao.setTareaProgramada(tarea.getId());
                        TIENE_PETICION.set(false);
                        NeoService.setLog(NeoService.ANSI_CYAN + "**--** MEPSAN: " + NeoService.ANSI_RESET + "CAMBIO APLICADO");
                    }
                }
            }
        } catch (DAOException ex) {
            Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void validarFinVenta(Surtidor surtidor, Cara cara, int estado) {
        if (estado == ESTADO_MANGUERA_COLGADO && sutidao.getVentaCurso(surtidor, cara)) {
            try {
                int cantidad = sdao.getCantidadDespachoVentaCurso(cara);
                if (cantidad != 0) {
                    estado = SURTIDOR_ESTADO_FIN_DESPACHO;
                } else {
                    sdao.getEliminaVentaCurso(cara);
                }
            } catch (DAOException ex) {
                Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void procesarEstadoDescolgado(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) throws Exception {
        NeoService.setLog("-> ESTADO MANGUERA #".concat(cara.getMagueraactual().getId() + "").concat(" DESCOLGADO"));
        if (!sutidao.getVentaCurso(surtidor, cara)) {
            boolean existeSaltoLectura = false;
            for (Map.Entry<Integer, Manguera> entry : cara.getMangueras().entrySet()) {
                Manguera manguera = entry.getValue();
                if (manguera.isTieneSaltoLectura()) {
                    existeSaltoLectura = true;
                    break;
                }
            }
            boolean autorizado = false;
            if (!existeSaltoLectura) {
                autorizado = procesarProcesoAutorizacion(surtidor, cara, estado, mangueraSurtidor);
                if (!autorizado) {
                    NeoService.setLog("NO SE AUTORIZA LA CARA #".concat(cara.getNumero() + ""));
                } else {
                    cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_AUTORIZADO);
                    cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_AUTORIZADO);
                    cara.setEstado(SURTIDOR_ESTADO_AUTORIZADO);
                    sutidao.guardarEstado(surtidor, cara, validarGrado(cara, mangueraSurtidor));
                }
            }

            if (!autorizado && cara.getEstado() != ESTADO_MANGUERA_DESCOLGADO) {
                cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_AUTORIZACION);
                cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_AUTORIZACION);
                cara.setEstado(ESTADO_MANGUERA_DESCOLGADO);
                sutidao.guardarEstado(surtidor, cara, validarGrado(cara, mangueraSurtidor));
            }
        }
    }

    void procesarEstadoAutorizado(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) {
        NeoService.setLog("ESTADO MANGUERA #".concat(cara.getMagueraactual().getId() + "").concat(" AUTORIZADO"));
        if (cara.getEstado() != SURTIDOR_ESTADO_AUTORIZADO) {
            cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_AUTORIZADO);
            cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_AUTORIZADO);
            cara.setEstado(SURTIDOR_ESTADO_AUTORIZADO);
            sutidao.guardarEstado(surtidor, cara, validarGrado(cara, mangueraSurtidor));
        }
    }

    void validarInactividadRumbo(Cara cara) {
        if (cara.getMagueraactual() != null && cara.getMagueraactual().getVenta() != null && cara.getMagueraactual().getVenta().getAutorizacionToken() != null) {
            Autorizacion token = cara.getMagueraactual().getVenta().getAutorizacionToken();
            if (token.getProveedorId() == NeoService.AUTORIZACION_PROVEEDOR_RUMBO) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "VALIDANDO AUTORIZACION DE INACTIVIDAD RUMBO...");
                JsonObject parametrosRumbo = sutidao.getParametrosRumbo();
                if (parametrosRumbo != null) {
                    Venta ventaST = cara.getMagueraactual().getVenta();
                    if (ventaST.getFechaFin() != null) {
                        int tiempo = parametrosRumbo.get("timeout").getAsJsonObject().get("inactivitySale").getAsInt();
                        Date ahora = new Date();
                        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "INACTIVIDAD CONFIGURADA A " + tiempo + " SEGUNDOS");
                        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "LAPSO... " + (ahora.getTime() - ventaST.getFechaFin().getTime()) + " SEGUNDOS");
                        if (ahora.getTime() - ventaST.getFechaFin().getTime() >= (tiempo * 1000)) {
                            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "SURTIDOR DETENIDO POR RUMBO ANTIFRAUDE!");
                            try {
                                protocolo.detener(surtidor, cara.getNumero(), TIEMPO_ENTRE_COMANDO);
                            } catch (Exception ex) {
                                Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    void recuperarVentaCara(Surtidor surtidor, Cara cara) {
        if (cara.getMagueraactual() == null || cara.getMagueraactual().getVenta() == null) {
            Venta venta;
            try {
                venta = sutidao.getVentaPorCara(cara);
                for (Map.Entry<Integer, Manguera> entry : cara.getMangueras().entrySet()) {
                    Manguera value = entry.getValue();
                    if (value.getGrado() == venta.getGrado()) {
                        cara.setMagueraactual(value);
                        break;
                    }
                }
                if (surtidor.isDebugEstado()) {
                    NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "VENTA ACTUAL ES " + venta.getId());
                }
                cara.getMagueraactual().setVenta(venta);
            } catch (Exception | DAOException ex) {
                Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    VentaParcial obtenerVentaDisplay(Surtidor surtidor, Cara cara) throws Exception {
        VentaParcial vpartial = protocolo.ventaEnCurso(surtidor, cara, TIEMPO_ENTRE_COMANDO);
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   = VENTA CURSO VOLUMEN: " + vpartial.getVolumenCalculado());
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   = VENTA CURSO IMPORTE: " + vpartial.getVolumenReal());
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   = VENTA CURSO PRECIO: " + vpartial.getPrecio());

        if (cara.getMagueraactual().getVenta().getVentaCantidad() >= 0 && vpartial.getVolumenCalculado() == 0) {
            int veces = 0;
            while (true) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "CONSULTANDO NUEVAMENTE PORQUE NO ME RESPONDIO LA VENTA CURSO 0");
                vpartial = protocolo.ventaEnCurso(surtidor, cara, TIEMPO_ENTRE_COMANDO);
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   = VENTA CURSO PRECIO: " + vpartial.getPrecio());
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   = VENTA CURSO VOLUMEN: " + vpartial.getVolumenCalculado());
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   = VENTA CURSO IMPORTE: " + vpartial.getVolumenReal());

                veces++;
                if (veces == 4 || vpartial.getVolumenCalculado() != 0) {
                    break;
                }
            }
        }
        return vpartial;
    }

    void procesarAutorizacionPredeterminacion(Surtidor surtidor, Cara cara, int mangueraSurtidor, Autorizacion autorizacion) throws Exception {
        PredeterminadaBean predeterminada = sdao.getPredeterminada(cara.getNumero());
        if (predeterminada == null) {
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "CANTIDAD MAXIMA VOLUMEN " + autorizacion.getMontoMaximo());
            if (autorizacion.getCantidadMaxima() > 0) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "ENTRO EN VOLUMEN CON UNA TRANSACCION PREVIA");
                int factorPredeterminacion;
                try {
                    factorPredeterminacion = sdao.getFactorPredeterminacion(surtidor.getId());
                } catch (DAOException ex) {
                    factorPredeterminacion = 1;
                }
                long precioPredeteminar = (long) (autorizacion.getCantidadMaxima() * factorPredeterminacion);
                protocolo.setPredeterminarVolumen(surtidor, cara.getNumero(), precioPredeteminar, TIEMPO_ENTRE_COMANDO);
            }

            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "CANTIDAD MAXIMA EN MONTO " + autorizacion.getMontoMaximo());
            if (autorizacion.getMontoMaximo() > 0) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "ENTRO EN MONTO");
                protocolo.setPredeterminarDinero(surtidor, cara.getNumero(), (int) autorizacion.getMontoMaximo(), TIEMPO_ENTRE_COMANDO);
            }
        } else {
            if (!predeterminada.isPrecio()) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "ENTRO EN UNA PREDETERMINACION EN VOLUMEN");

                int factorPredeterminacion;
                try {
                    factorPredeterminacion = sdao.getFactorPredeterminacion(surtidor.getId());
                } catch (DAOException ex) {
                    factorPredeterminacion = 1;
                }

                long precioPredeteminar = (int) predeterminada.getValor() * factorPredeterminacion;
                protocolo.setPredeterminarVolumen(surtidor, cara.getNumero(), precioPredeteminar, TIEMPO_ENTRE_COMANDO);

            } else {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "SE ESTÁ TRATANDO DE PREDETERMINAR EN PRECIO... " + predeterminada.getValor());
                protocolo.setPredeterminarDinero(surtidor, cara.getNumero(), (int) predeterminada.getValor(), TIEMPO_ENTRE_COMANDO);
            }
        }
    }

    void procesarEstadoDespachoSurtidor(Surtidor surtidor, Cara cara, int mangueraSurtidor) throws Exception {
        VentaParcial vpartial = obtenerVentaDisplay(surtidor, cara);
        Venta ventaDespacho1;
        if (cara.getMagueraactual().getVenta() != null) {
            ventaDespacho1 = cara.getMagueraactual().getVenta();
        } else {
            ventaDespacho1 = new Venta();
            ventaDespacho1.setId(0);
            ventaDespacho1.setSurtidorTipoId(Venta.ORIGEN_TIPO.SURTIDOR);
            ventaDespacho1.setSurtidorId(surtidor.getUniqueId());
            ventaDespacho1.setCara(cara.getNumero());
            ventaDespacho1.setGrado(cara.getMagueraactual().getGrado());
            ventaDespacho1.setManguera(cara.getMagueraactual().getId());
            ventaDespacho1.setProductoId(cara.getMagueraactual().getProductoId());
            ventaDespacho1.setConfiguracionId(cara.getMagueraactual().getConfiguracionId());
            ventaDespacho1.setPlaca("XXXXXX");
            ventaDespacho1.setAcumuladoImporteInicial(cara.getMagueraactual().getRegistroSurtidorVentas());
            ventaDespacho1.setAcumuladoVolumenInicial(cara.getMagueraactual().getRegistroSurtidorVolumen());
        }

        if (vpartial.getVolumenReal() != 0 && vpartial.getVolumenReal() != ventaDespacho1.getVentaImporte()) {
            ventaDespacho1.setFechaFin(new Date());
            ventaDespacho1.setVentaImporte(vpartial.getVolumenReal());
            ventaDespacho1.setVentaCantidad(vpartial.getVolumenCalculado());
            sutidao.setEstadoVenta(ventaDespacho1, Surtidor.SURTIDORES_ESTADO.DEPACHO);
        } else {
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "NO SE ACTUALIZA LA BD PORQUE TIENE EL MISMO VALOR EN LA CANTIDAD");
        }

        cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_DESPACHO);
        cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_DESPACHO);
        cara.setEstado(SURTIDOR_ESTADO_DESPACHO);
        sutidao.guardarEstado(surtidor, cara, validarGrado(cara, mangueraSurtidor));
    }

    void procesarFinVenta(Surtidor surtidor, Cara cara) throws Exception {
        if (cara.getEstado() != SURTIDOR_ESTADO_FIN_DESPACHO) {
            if (surtidor.isDebugEstado()) {
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "Manguera(" + surtidor.getId() + ", " + cara.getNumero() + ") en Fin de venta");
            }
            recuperarVentaCara(surtidor, cara);
            if (cara.getMagueraactual() != null) {
                VentaParcial ventaFinal = obtenerVentaDisplay(surtidor, cara);
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   % VENTA VOLUMEN REAL: " + ventaFinal.getVolumenReal());
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   % VENTA VALOR ACTUAL: " + ventaFinal.getVolumenReal());
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "   % VENTA PRECIO ES: " + ventaFinal.getPrecio());

                Venta ventaDespacho2 = cara.getMagueraactual().getVenta();
                ventaDespacho2.setVentaImporte(ventaFinal.getVolumenReal());
                ventaDespacho2.setActualImporte(ventaFinal.getVolumenReal());
                ventaDespacho2.setActualVolumenReal(ventaFinal.getVolumen());

                ventaDespacho2.setFechaFin(new Date());
                ventaDespacho2.setActualVolumen(ventaFinal.getVolumen());

                Totalizador totalizador = totalizadoresSurtidor(cara, ventaDespacho2.getGrado(), 5);
                ventaDespacho2.setAcumuladoImporteFinal(totalizador.getAcumuladoVenta());
                ventaDespacho2.setAcumuladoVolumenFinal(totalizador.getAcumuladoVolumen());

                sutidao.setEstadoVenta(ventaDespacho2, Surtidor.SURTIDORES_ESTADO.VENTA_FINALIZADA);

                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + " Sistema  Acumulado Volumen: " + cara.getMagueraactual().getRegistroSistemaVolumen());
                NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + " Surtidor Acumulado Volumen: " + cara.getMagueraactual().getRegistroSurtidorVolumen());

                cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_FINALIZADA_PEOT);
                cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_FINALIZADA_PEOT);
                cara.setEstado(SURTIDOR_ESTADO_FIN_DESPACHO);
                sutidao.guardarEstado(surtidor, cara, cara.getMagueraactual().getGrado());

                cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_FINALIZADA_FEOT);
                cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_FINALIZADA_FEOT);
                cara.setEstado(SURTIDOR_ESTADO_FIN_DESPACHO);
                sutidao.guardarEstado(surtidor, cara);

                NeoService.PERSONA_AUTORIZA_ID.set(0);
                NeoService.REGISTRO_PERSONA.put(cara.getNumero(), 0L);
                cara.setMagueraactual(null);
            }
        }

    }

    void procesarEstadoDespacho(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) {
        NeoService.setLog("ESTADO MANGUERA #".concat(cara.getMagueraactual().getId() + "").concat(" DESPACHO"));

        //VALIDACION RUMBO
        validarInactividadRumbo(cara);
        //RECUPERACION VENTA
        recuperarVentaCara(surtidor, cara);

        try {
            procesarEstadoDespachoSurtidor(surtidor, cara, mangueraSurtidor);
        } catch (Exception ex) {
            Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    boolean procesarProcesoAutorizacion(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) throws Exception {
        int grado = validarGrado(cara, mangueraSurtidor);
        CatalogoBean bloqueo = sutidao.getBloqueo(surtidor.getId(), cara.getNumero(), grado);
        grupoJornada = sutidao.getGrupoJornada();

        if (bloqueo == null && grupoJornada > 0) {
            NeoService.setLog(NeoService.ANSI_PURPLE + "->> MEPSAN: " + NeoService.ANSI_RESET + "SIN BLOQUEO");
            long promotorAutoriza = validarPersonaAutoriza(cara, grado);
            if (promotorAutoriza > 0) {
                Totalizador iniciales = totalizadoresSurtidor(cara, grado, 3);
                if (iniciales != null) {
                    Autorizacion autorizacion = sutidao.getAutorizacion(cara.getNumero(), grado);
                    boolean autorizado = true;

                    NeoService.AUTORIZACION_REQUIERE_VEHICULO = sdao.getCaraRequierePlaca(cara.getMagueraactual().getId());
                    if (NeoService.AUTORIZACION_REQUIERE_VEHICULO) {
                        autorizado = false;
                        if (autorizacion != null && autorizacion.getPlacaVehiculo() != null) {
                            autorizado = true;
                        } else {
                            NeoService.setLog(NeoService.ANSI_RED + "MANGUERA " + cara.getMagueraactual().getId() + " REQUIERE PLACA" + NeoService.ANSI_RESET);
                        }
                    }
                    if (autorizacion != null) {
                        sdao.actualizaAutorizacion(autorizacion.getId());
                        procesarAutorizacionPredeterminacion(surtidor, cara, mangueraSurtidor, autorizacion);
                    }

                    if (autorizado) {
                        try {
                            protocolo.autorizar(surtidor, cara.getNumero(), TIEMPO_ENTRE_COMANDO);
                        } catch (Exception e) {
                            Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        crearVenta0(cara, iniciales, promotorAutoriza, autorizacion, grado);
                        sdao.borrarPredeterminada(cara.getNumero());
                    }

                    NeoService.PERSONA_AUTORIZA_ID.set(0);
                    NeoService.REGISTRO_PERSONA.put(cara.getNumero(), 0L);
                    NeoService.AUTORIZACION.remove(cara.getNumero());

                    pauseCore(TIEMPO_ENTRE_COMANDO);
                    return true;
                } else {
                    NeoService.setLog(NeoService.ANSI_RED + "->>SIN TOTALIZADORES INICIALES ->" + NeoService.ANSI_RESET);
                }
            } else {
                NeoService.setLog(NeoService.ANSI_PURPLE + "->>SIN ID DE PROMOTOR" + NeoService.ANSI_RESET);

            }

        } else if (grupoJornada <= 0) {
            facade.conFormato(cara.getNumero() + ". NO SE HA INICIADO UNA JORNADA");
        } else if (bloqueo != null) {
            facade.conFormato("BLOQUEO DE MANGUERA " + bloqueo.getValor() + ". " + bloqueo.getDescripcion() + "\r\n");
        }
        return false;
    }

    @Override
    public Precio actualizaMultipreciosPrecios(long precio, int lista, int grado, int cara, boolean returnTotalizador, long precioOriginal) {
        Precio resul = new Precio();
        resul.setId(-1);
        NeoService.setLog(NeoService.ANSI_PURPLE + "->>[actualizaMultipreciosPrecios]" + NeoService.ANSI_RESET);

        try {
            TIENE_PETICION.set(true);
            while (ESTA_PROCESANDO.get()) {
                NeoService.setLog(NeoService.ANSI_PURPLE + "->>ESTA_PROCESANDO" + NeoService.ANSI_RESET);
            }

            resul.setId(protocolo.setPrecio(surtidor, cara, precio, TIEMPO_ENTRE_COMANDO));

            ArrayList<Byte> results = protocolo.estadoManguera(surtidor, cara, TIEMPO_ENTRE_COMANDO);
            Utils.printArray("ESTADO PARA ACTUALIZAR PRECIO", results);
            for (Map.Entry<Integer, Manguera> entry : surtidor.getCaras().get(cara).getMangueras().entrySet()) {
                Manguera manguera = entry.getValue();
                if (manguera.getGrado() == grado) {
                    SurtidorDao dao = new SurtidorDao();
                    dao.actualizarPrecioProucto(manguera.getProductoId(), lista, precioOriginal, true);
                }
            }
            if (returnTotalizador) {
                Cara ocara = new Cara();
                ocara.setNumero(cara);
                Totalizador[] totales = getTotalizadoresByCara(ocara);
                for (int i = 0; i < totales.length; i++) {
                    if (i == grado) {
                        resul.setPrecio(totales[i].getPrecio());
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            TIENE_PETICION.set(false);
        }
        return resul;
    }

    void crearVenta0(Cara cara, Totalizador iniciales, long personaAutoriza, Autorizacion autorizacion, int grado) {

        Venta ventaAutorizada = new Venta();
        ventaAutorizada.setId(0);
        ventaAutorizada.setAutorizacionToken(autorizacion);
        ventaAutorizada.setManguera(cara.getMagueraactual().getId());
        ventaAutorizada.setFechaInicio(new Date());
        ventaAutorizada.setFechaFin(new Date());
        ventaAutorizada.setProductoId(cara.getMagueraactual().getProductoId());
        ventaAutorizada.setClienteId(NeoService.CLIENTES_VARIOS_ID);

        ventaAutorizada.setSurtidorTipoId(Venta.ORIGEN_TIPO.SURTIDOR);
        NeoService.setLog(NeoService.ANSI_CYAN + " ** MEPSAN: " + NeoService.ANSI_RESET + "VOLUMEN DE LA MANGUERA " + cara.getMagueraactual().getRegistroSurtidorVolumen());

        if (iniciales != null) {
            ventaAutorizada.setAcumuladoImporteInicial(iniciales.getAcumuladoVenta());
            ventaAutorizada.setAcumuladoVolumenInicial(iniciales.getAcumuladoVolumen());
            ventaAutorizada.setActualPrecio(iniciales.getPrecio());
        } else {
            ventaAutorizada.setAcumuladoImporteInicial(cara.getMagueraactual().getRegistroSurtidorVentas());
            ventaAutorizada.setAcumuladoVolumenInicial(cara.getMagueraactual().getRegistroSurtidorVolumen());
        }

        ventaAutorizada.setConfiguracionId(cara.getMagueraactual().getConfiguracionId());
        ventaAutorizada.setSurtidorId(surtidor.getId());
        ventaAutorizada.setCara(cara.getNumero());
        ventaAutorizada.setGrado(cara.getMagueraactual().getGrado());
        ventaAutorizada.setOperadorId(personaAutoriza);
        ventaAutorizada.setJornadaId(grupoJornada);
        if (autorizacion != null) {
            ventaAutorizada.setAutorizacionToken(autorizacion);
            ventaAutorizada.setClienteId(autorizacion.getIdentificadorCLiente());
            ventaAutorizada.setPlaca(autorizacion.getPlacaVehiculo());
        } else {
            NeoService.setLog(NeoService.ANSI_CYAN + "-> MEPSAN: " + NeoService.ANSI_RESET + "NO EN ENCONTRO UNA PREAUTORIZACIÓN EN EL SISTEMA EN LA CARA " + cara.getNumero() + " GRADO " + grado);
        }

        long id = sutidao.setEstadoVenta(ventaAutorizada, Surtidor.SURTIDORES_ESTADO.AUTORIZACION);
        ventaAutorizada.setId(id);
        cara.getMagueraactual().setVenta(ventaAutorizada);
        NeoService.setLog(NeoService.ANSI_CYAN + "->> MEPSAN: " + NeoService.ANSI_RESET + "AUTORIZACION COMPLETA CARA " + cara.getNumero() + " ###################################################### ");
    }

    Totalizador totalizadoresSurtidor(Cara cara, int grado, int intentos) {
        Totalizador iniciales = null;
        int i = 0;
        Totalizador[] totalizadoresSurtidor = getTotalizadores(cara);
        NeoService.setLog("totalizadoresSurtidor[i]" + " " + totalizadoresSurtidor[i].getAcumuladoVolumen() + " i = " + i + " Grado = " + grado);

        if (totalizadoresSurtidor == null || totalizadoresSurtidor.length == 0) {
            int intento1 = 0;
            while (intento1 == intentos) {
                totalizadoresSurtidor = getTotalizadores(cara);
                if (totalizadoresSurtidor != null) {
                    break;
                }
                pauseCore(100);
                client.reconect();
                intento1++;
            }
        }
        int factorPrecio;
        try {
            factorPrecio = sdao.getFactorPrecio(surtidor.getId());
        } catch (DAOException ex) {
            factorPrecio = 1;
        }

        long precio1 = Utils.calculeCantidad(totalizadoresSurtidor[i].getPrecio(), factorPrecio);
        long precio2 = Utils.calculeCantidad(totalizadoresSurtidor[i].getPrecio2(), factorPrecio);

        iniciales = totalizadoresSurtidor[i];
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "");
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "=========================IDENTIFICANDO EL GRADO/PRECIO ==============================");
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "CORE: " + NeoService.VERSION_NAME);
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "C" + cara.getNumero() + " M " + cara.getMagueraactual().getId() + " G " + grado + " venta    " + totalizadoresSurtidor[i].getAcumuladoVenta());
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "C" + cara.getNumero() + " M " + cara.getMagueraactual().getId() + " G " + grado + " volumen  " + totalizadoresSurtidor[i].getAcumuladoVolumen());
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "C" + cara.getNumero() + " M " + cara.getMagueraactual().getId() + " G " + grado + " precio 1 " + totalizadoresSurtidor[i].getPrecio());
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "C" + cara.getNumero() + " M " + cara.getMagueraactual().getId() + " G " + grado + " precio 2 " + totalizadoresSurtidor[i].getPrecio2());
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "C" + cara.getNumero() + " M " + cara.getMagueraactual().getId() + " G " + grado + " precio 1 Factorizado " + precio1);
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "C" + cara.getNumero() + " M " + cara.getMagueraactual().getId() + " G " + grado + " precio 2 Factorizado " + precio2);
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "=====================================================================================");
        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "");

        sdao.actualizarPrecioProucto(cara.getMagueraactual().getProductoId(), listaPrecio1, precio1, true);
        sdao.actualizarPrecioProucto(cara.getMagueraactual().getProductoId(), listaPrecio2, precio2, true);

        cara.getMagueraactual().setProductoPrecio(totalizadoresSurtidor[i].getPrecio());
        cara.getMagueraactual().setProductoPrecio2(totalizadoresSurtidor[i].getPrecio2());

        return iniciales;
    }

    long validarPersonaAutoriza(Cara cara, int grado) {
        long personaAutoriza = 0;
        Autorizacion autorizacion = sutidao.getAutorizacion(cara.getNumero(), grado);
        if (autorizacion != null && autorizacion.getPromotorId() > 0) {
            personaAutoriza = autorizacion.getPromotorId();
        } else {
            NeoService.METODO_AUTORIZACION_TAG = sdao.getParametroInt("tipo_autorizacion");
            switch (NeoService.METODO_AUTORIZACION_TAG) {
                case NeoService.METODO_AUTORIZACION_SIN_TAG:
                    NeoService.setLog(NeoService.ANSI_CYAN + "[->] MEPSAN: " + NeoService.ANSI_RESET + "METODO DE AUTORIZACION SIN_TAG (JORNADA)");
                    personaAutoriza = sutidao.getPrimeroTurno();
                    break;
                case NeoService.METODO_AUTORIZACION_TAG_GLOBAL:
                    NeoService.setLog(NeoService.ANSI_CYAN + " MEPSAN: " + NeoService.ANSI_RESET + "METODO DE AUTORIZACION TAG_GLOBAL");
                    long persona = NeoService.PERSONA_AUTORIZA_ID.get();
                    boolean enTurno = sutidao.existeUsuarioEnTurno(persona);
                    if (enTurno) {
                        personaAutoriza = persona;
                    }
                    break;
                case NeoService.METODO_AUTORIZACION_TAG_CARA:
                    NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN : " + NeoService.ANSI_RESET + "METODO DE AUTORIZACION POR CARA. CARA " + cara.getNumero());
                    if (NeoService.REGISTRO_PERSONA.containsKey(cara.getNumero()) && NeoService.REGISTRO_PERSONA.get(cara.getNumero()) > 0) {
                        long persona1 = NeoService.REGISTRO_PERSONA.get(cara.getNumero());
                        NeoService.setLog(NeoService.ANSI_CYAN + "->  MEPSAN: " + NeoService.ANSI_RESET + "REGISTRO_PERSONA_ID " + persona1);
                        boolean enTurno1 = sutidao.existeUsuarioEnTurno(persona1);
                        NeoService.setLog(NeoService.ANSI_CYAN + "   MEPSAN: " + NeoService.ANSI_RESET + "REGISTRO_PERSONA_TURNO " + enTurno1);
                        if (enTurno1) {
                            personaAutoriza = persona1;
                        }
                    } else {
                        NeoService.setLog(NeoService.ANSI_CYAN + "   MEPSAN: " + NeoService.ANSI_RESET + "SIN REGISTRO EN CACHE");
                    }
                    break;
            }
        }
        return personaAutoriza;
    }

    void procesarEstadocara(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) throws Exception {
        switch (estado) {
            case ESTADO_MANGUERA_COLGADO:
                procesarEstadoEspera(surtidor, cara, estado, mangueraSurtidor);
                break;
            case ESTADO_MANGUERA_DESCOLGADO:
                procesarEstadoDescolgado(surtidor, cara, estado, mangueraSurtidor);
                break;
            case SURTIDOR_ESTADO_AUTORIZADO:
                procesarEstadoAutorizado(surtidor, cara, estado, mangueraSurtidor);
                break;
            case SURTIDOR_ESTADO_DESPACHO:
                procesarEstadoDespacho(surtidor, cara, estado, mangueraSurtidor);
                break;
            case SURTIDOR_ESTADO_FIN_DESPACHO:
                procesarFinVenta(surtidor, cara);
                break;
            default:
                NeoService.setLog(NeoService.ANSI_RED + "ESTADO MANGUERA #".concat(mangueraSurtidor + "").concat(" DESCONOCIDO ") + NeoService.ANSI_RESET);
                break;

        }
    }

    @Override
    public Totalizador[] getTotalizadoresByCara(Cara cara) {
        return getTotalizadores(cara);
    }

    public Totalizador[] getTotalizadores(Cara cara) {
        Totalizador[] totalizadoresSurtidorMulti = null;
        try {
            int maximo = 0;
            for (Map.Entry<Integer, Manguera> m : cara.getMangueras().entrySet()) {
                maximo++;
            }

            totalizadoresSurtidorMulti = new Totalizador[maximo];
            maximo = 0;
            for (Map.Entry<Integer, Manguera> mangueras : cara.getMangueras().entrySet()) {
                Manguera manguera = mangueras.getValue();
                Totalizador totalizadorVol = protocolo.consigueTotalizadores(surtidor, cara.getNumero(), manguera.getGrado(), TIEMPO_ESPERA_TOTALIZADORES, PREGUNTA_TOTALIZADOR_VOLUMEN);
                if (totalizadorVol == null) {
                    totalizadorVol = protocolo.consigueTotalizadores(surtidor, cara.getNumero(), manguera.getGrado(), TIEMPO_ESPERA_TOTALIZADORES, PREGUNTA_TOTALIZADOR_VOLUMEN);
                }
                Totalizador totalizadorImporte = protocolo.consigueTotalizadores(surtidor, cara.getNumero(), manguera.getGrado(), TIEMPO_ESPERA_TOTALIZADORES, PREGUNTA_TOTALIZADOR_IMPORTE);
                if (totalizadorImporte == null) {
                    totalizadorImporte = protocolo.consigueTotalizadores(surtidor, cara.getNumero(), manguera.getGrado(), TIEMPO_ESPERA_TOTALIZADORES, PREGUNTA_TOTALIZADOR_IMPORTE);
                }
                Long precio = protocolo.getPrecio(surtidor, cara.getNumero(), manguera.getGrado(), TIEMPO_ESPERA);
                if (precio == 0) {
                    precio = protocolo.getPrecio(surtidor, cara.getNumero(), manguera.getGrado(), TIEMPO_ESPERA);
                }

                Totalizador totales = new Totalizador();
                totales.setAcumuladoVolumen(totalizadorVol.getAcumuladoVolumen());
                totales.setAcumuladoVenta(totalizadorImporte.getAcumuladoVenta());
                totales.setPrecio(precio);
                totales.setPrecio2(precio);
                totales.setCara(cara.getNumero());
                totales.setGrado(manguera.getGrado());
                totales.setManguera(manguera.getId());
                totalizadoresSurtidorMulti[maximo] = totales;
            }
        } catch (Exception ex) {
            return null;
        }
        return totalizadoresSurtidorMulti;
    }

    public void validaTotalizadores(Totalizador[] totalizadoresSurtidorMulti, Surtidor surtidor, Cara cara) {
        try {
            if (totalizadoresSurtidorMulti == null) {
                totalizadoresSurtidorMulti = getTotalizadores(cara);
            }

            Totalizador[] totalizadoresSistema = sutidao.getMultiTotalizadores(surtidor.getUniqueId(), cara.getNumero());
            int i = 0;
            for (Totalizador totalizador : totalizadoresSistema) {
                if (totalizadoresSurtidorMulti[i] != null) {
                    cara.getMangueras().get(totalizador.getManguera()).setRegistroSistemaVentas(totalizador.getAcumuladoVenta());
                    cara.getMangueras().get(totalizador.getManguera()).setRegistroSistemaVolumen(totalizador.getAcumuladoVolumen());
                    cara.getMangueras().get(totalizador.getManguera()).setRegistroSurtidorVentas(totalizadoresSurtidorMulti[i].getAcumuladoVenta());
                    cara.getMangueras().get(totalizador.getManguera()).setRegistroSurtidorVolumen(totalizadoresSurtidorMulti[i].getAcumuladoVolumen());
                    cara.getMangueras().get(totalizador.getManguera()).setProductoPrecio(totalizadoresSurtidorMulti[i].getPrecio());
                    cara.getMangueras().get(totalizador.getManguera()).setProductoPrecio2(totalizadoresSurtidorMulti[i].getPrecio2());

                    //ACTUALIZA EL PRECIO EN LA BD
                    int factorPrecio;
                    try {
                        factorPrecio = sdao.getFactorPrecio(surtidor.getId());
                    } catch (DAOException ex) {
                        factorPrecio = 1;
                    }

                    long precio1 = Utils.calculeCantidad(cara.getMangueras().get(totalizador.getManguera()).getProductoPrecio(), factorPrecio);
                    long precio2 = Utils.calculeCantidad(cara.getMangueras().get(totalizador.getManguera()).getProductoPrecio2(), factorPrecio);

                    sdao.actualizarPrecioProucto(cara.getMangueras().get(totalizador.getManguera()).getProductoId(), listaPrecio1, precio1, true);
                    sdao.actualizarPrecioProucto(cara.getMangueras().get(totalizador.getManguera()).getProductoId(), listaPrecio2, precio2, true);

                    long diferencia = totalizadoresSurtidorMulti[i].getAcumuladoVolumen() - totalizador.getAcumuladoVolumen();

                    NeoService.setLog(NeoService.ANSI_CYAN + " MEPSAN: " + NeoService.ANSI_RESET + "COMPARADOR DE VOLUMENES");
                    NeoService.setLog(NeoService.ANSI_CYAN + "  MEPSAN: " + NeoService.ANSI_RESET + ""
                            + "SURTIDOR: " + totalizadoresSurtidorMulti[i].getAcumuladoVolumen() + " VS "
                            + "SISTEMA:  " + totalizador.getAcumuladoVolumen() + " "
                            + " = DIFF " + diferencia
                    );

                    if (totalizador.getAcumuladoVolumen() == totalizadoresSurtidorMulti[i].getAcumuladoVolumen() || (diferencia <= 10 && diferencia >= -10)) {
                        cara.getMangueras().get(totalizador.getManguera()).setTieneTotalizadores(true);
                        cara.getMangueras().get(totalizador.getManguera()).setTieneSaltoLectura(false);
                        sdao.nivelaDecimales(cara.getMangueras().get(totalizador.getManguera()), totalizadoresSurtidorMulti[i].getAcumuladoVolumen());
                    } else {
                        NeoService.setLog(NeoService.ANSI_CYAN + "  MEPSAN: " + NeoService.ANSI_RESET + "EXISTE UN SALTO DE LECTURA EN " + surtidor.getUniqueId() + " C" + cara.getNumero() + " M" + cara.getMangueras().get(totalizador.getManguera()).getId());
                        cara.getMangueras().get(totalizador.getManguera()).setTieneTotalizadores(true);
                        cara.getMangueras().get(totalizador.getManguera()).setTieneSaltoLectura(true);
                        Manguera mang = cara.getMangueras().get(totalizador.getManguera());
                        sutidao.saveSaltoLectura(mang);
                        NotificacionSocket.publish(1, "Salto de lectura en S" + surtidor.getId() + " M" + cara.getMangueras().get(totalizador.getManguera()).getId());
                        boolean imprimir = false;
                        if (NeoService.sharedPreference.get(AConstant.PREFERENCE_SALTO_LECTURA) == null) {
                            NeoService.sharedPreference.put(AConstant.PREFERENCE_SALTO_LECTURA, totalizadoresSurtidorMulti[i].getAcumuladoVolumen());
                            imprimir = true;
                        } else {
                            long temp1 = (long) NeoService.sharedPreference.get(AConstant.PREFERENCE_SALTO_LECTURA);
                            long temp2 = totalizadoresSurtidorMulti[i].getAcumuladoVolumen();
                            if (temp1 != temp2) {
                                NeoService.sharedPreference.put(AConstant.PREFERENCE_SALTO_LECTURA, totalizadoresSurtidorMulti[i].getAcumuladoVolumen());
                                imprimir = true;
                            }
                        }
                        if (imprimir) {
                            facade.conFormato("EXISTE UN SALTO DE LECTURA EN MANGUERA " + cara.getMangueras().get(totalizador.getManguera()).getId() + "\r\n");
                        }
                    }
                } else {
                    NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + " GRADO NO ENCONTRADO " + i);
                }
                i++;
            }

        } catch (Exception ex) {
            NeoService.setLog(NeoService.ANSI_RED + "MEPSAN: " + NeoService.ANSI_RESET + " ERROR AL CONSULTAR LOS TOTALIZADORES " + ex.getMessage());
            Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
