package com.core.app.protocols;

import com.butter.bean.EquipoDao;
import com.butter.bean.Main;
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
import com.neo.app.bean.BaseControllerProtocols;
import com.neo.app.bean.Cara;
import com.neo.app.bean.Manguera;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.Totalizador;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public static final int PREGUNTA_TOTALIZADOR_IMPORTE = 0x0B;

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

    public void consultarEstado(Surtidor surtidor, Cara cara) throws Exception {
        int estado = -1;
        ArrayList<Byte> respuesta = protocolo.estadoManguera(surtidor, cara.getNumero(), TIEMPO_ESPERA);
        if (respuesta != null && respuesta.size() > 0) {
            estado = Byte.toUnsignedInt((byte) (respuesta.get(10) & CONSTANTE_FIND_REPUESTA));
            int mangueraSurtidor = respuesta.get(10) & CONSTANTE_FIND_MANGUERA;

            NeoService.setLog(" ESTADO INT : -> " + estado + " MANGUERA SURTIDOR " + mangueraSurtidor + " " + (String.format("%02x", (byte) estado).toUpperCase() + " "));
            boolean existeGrado = false;
            for (Map.Entry<Integer, Manguera> mangueras : cara.getMangueras().entrySet()) {
                Manguera manguera = mangueras.getValue();
                if (manguera.getGrado() == mangueraSurtidor) {
                    existeGrado = true;
                    break;
                }
            }
            if (!existeGrado) {
                NeoService.setLog(NeoService.ANSI_RED + "-> GRADO DESCONOCIDO " + mangueraSurtidor + NeoService.ANSI_RESET);
            } else {
                procesarEstadocara(surtidor, cara, estado, mangueraSurtidor);
            }

        }
    }

    void procesarEstadoEspera(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) {
        NeoService.setLog("->  ESTADO MANGUERA #".concat(mangueraSurtidor + "").concat(" COLGADO"));

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
            //TODO: ejecucionTareasProgramadas(cara.getNumero());
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

    void procesarEstadoDescolgado(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) {
        NeoService.setLog("ESTADO MANGUERA #".concat(mangueraSurtidor + "").concat(" DESCOLGADO"));

        cara.setPublicEstadoId(NeoService.SURTIDORES_PUBLIC_ESTADO_ID_AUTORIZACION);
        cara.setPublicEstadoDescripcion(NeoService.SURTIDORES_PUBLIC_ESTADO_DS_AUTORIZACION);
        cara.setEstado(ESTADO_MANGUERA_DESCOLGADO);
        sutidao.guardarEstado(surtidor, cara);
    }

    void procesarEstadocara(Surtidor surtidor, Cara cara, int estado, int mangueraSurtidor) {
        switch (estado) {
            case ESTADO_MANGUERA_COLGADO:
                procesarEstadoEspera(surtidor, cara, estado, mangueraSurtidor);
                break;
            case ESTADO_MANGUERA_DESCOLGADO:
                procesarEstadoDescolgado(surtidor, cara, estado, mangueraSurtidor);
                break;
            default:
                NeoService.setLog(NeoService.ANSI_RED + "ESTADO MANGUERA #".concat(mangueraSurtidor + "").concat(" DESCONOCIDO ") + NeoService.ANSI_RESET);
                break;

        }
    }

    public void validaTotalizadores(Totalizador[] totalizadoresSurtidorMulti, Surtidor surtidor, Cara cara) {
        try {
            if (totalizadoresSurtidorMulti == null) {
                totalizadoresSurtidorMulti = new Totalizador[cara.getMangueras().size()];
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
                    totalizadoresSurtidorMulti[manguera.getGrado()].setAcumuladoVolumen(totalizadorVol.getAcumuladoVolumen());
                    totalizadoresSurtidorMulti[manguera.getGrado()].setAcumuladoVenta(totalizadorImporte.getAcumuladoVenta());
                    totalizadoresSurtidorMulti[manguera.getGrado()].setPrecio(precio);
                    totalizadoresSurtidorMulti[manguera.getGrado()].setPrecio2(precio);
                }
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

                    NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "COMPARADOR DE VOLUMENES");
                    NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + ""
                            + "SURTIDOR: " + totalizadoresSurtidorMulti[i].getAcumuladoVolumen() + " VS "
                            + "SISTEMA:  " + totalizador.getAcumuladoVolumen() + " "
                            + " = DIFF " + diferencia
                    );

                    if (totalizador.getAcumuladoVolumen() == totalizadoresSurtidorMulti[i].getAcumuladoVolumen() || (diferencia <= 10 && diferencia >= -10)) {
                        cara.getMangueras().get(totalizador.getManguera()).setTieneTotalizadores(true);
                        cara.getMangueras().get(totalizador.getManguera()).setTieneSaltoLectura(false);
                        sdao.nivelaDecimales(cara.getMangueras().get(totalizador.getManguera()), totalizadoresSurtidorMulti[i].getAcumuladoVolumen());
                    } else {
                        NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + "EXISTE UN SALTO DE LECTURA EN " + surtidor.getUniqueId() + " C" + cara.getNumero() + " M" + cara.getMangueras().get(totalizador.getManguera()).getId());
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
            Logger.getLogger(MepsanController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
