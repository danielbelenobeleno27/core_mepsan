/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.protocols;

import com.ProtocolsRFID;
import com.RFIDConect;
import com.butter.bean.EquipoDao;
import com.butter.bean.Main;
import com.core.app.NeoService;
import static com.core.app.NeoService.AUTORIZACION;
import static com.core.app.NeoService.PERSONA_AUTORIZA_ID;
import static com.core.app.NeoService.SUB_COMANDO_NEOAPP_TAG_REFID;
import static com.core.app.NeoService.sutidao;
import com.core.app.bean.service.ClientWSAsync;
import com.core.app.server.NotificacionSocket;
import com.core.database.DAOException;
import com.core.database.impl.SurtidorDao;
import com.core.print.services.PrinterFacade;
import com.google.gson.JsonObject;
import com.neo.app.bean.Autorizacion;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author desarrollador
 */
public class Dispositivos {

    public static void IB4Tcp(int port) {
        ServerIbutton severIbutton = new ServerIbutton(port);
        severIbutton.setPriority(Thread.MAX_PRIORITY);
        severIbutton.setDaemon(true);
        severIbutton.start();
        NeoService.setLog("[INFO (Dispositivos)] IB4 INICIADO [" + port + "]");
    }

    public static void notificaIbutton(String trama) {
        if (trama.startsWith("I")) {
            boolean conectado = trama.toCharArray()[3] == 'N';
            if (!conectado) {
                try {
                    String chipIbutton = trama.substring(3, 19);
                    NeoService.setLog("ENVIANDO COMANDO 9: " + chipIbutton);
                    int puerto = Integer.parseInt(trama.toCharArray()[1] + "");
                    long personaId = NeoService.sutidao.existUsuarioSinEstado(chipIbutton);

                    int cara = puerto;
                    try {
                        String valor = NeoService.sutidao.getParametros("ibutton_cara");
                        String puertos[] = valor.split(";");
                        if (valor.length() > 0 && puertos.length > 0) {
                            for (String puertoconf : puertos) {
                                String[] p = puertoconf.split("=");
                                int port = Integer.parseInt(p[0]);
                                if (port == puerto) {
                                    cara = Integer.parseInt(p[1].replaceAll("C", ""));
                                    break;
                                }
                            }
                        }
                    } catch (DAOException ex) {
                        NeoService.setLog("LA CONFIGURACION DE PUERTOS IBUTTON INCORRECTA [ibutton_cara]");
                        NeoService.setLog("");
                        Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        NeoService.setLog("LA CONFIGURACION DE PUERTOS IBUTTON INCORRECTA [ibutton_cara]");
                        Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    NeoService.setLog("ENVIANDO LECTURA IBUTTON A CARA_:" + cara + " PERSONA:" + personaId + " " + chipIbutton);

                    notificacionDispositivoEmpleados("ibutton", chipIbutton, personaId, cara);
                    if (personaId > 0) {
                        if (NeoService.REGISTRO_PERSONA == null) {
                            NeoService.REGISTRO_PERSONA = new TreeMap<>();
                        }
                        NeoService.REGISTRO_PERSONA.put(cara, personaId);
                    }
                    EquipoDao dao = new EquipoDao();
                    NeoService.PUBLICAR_SOCKET_ESTADO_SURTIDOR = dao.getParametroBoolean("socket_externo_activar");
                    if (NeoService.PUBLICAR_SOCKET_ESTADO_SURTIDOR) {
                        JsonObject json = new JsonObject();
                        json.addProperty("tipo", 3);
                        json.addProperty("cara", cara);
                        json.addProperty("subtipo", NeoService.SUB_COMANDO_NEOAPP_TAG_IBUTTON);
                        json.addProperty("mensaje", chipIbutton);
                        NotificacionSocket.publishrr(1, json);
                    }
                } catch (Exception e) {
                    Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, e);
                    NeoService.setLog("com.core.app.bean.socket.ServerIbutton.notificaIbutton()" + e.getMessage());
                }
            }
        }
    }

    public static void notificacionDispositivoEmpleados(String medio, String data, Long promotor, int cara) {

        String funcion = "NOTIFICANDO EMPLEADOS";
        String url = "http://localhost:10000/api/identificadorPromotor";// + medio;
        String method = "POST";
        boolean DEBUG = false;
        JsonObject request = new JsonObject();

        SurtidorDao sutidao = new SurtidorDao();
        promotor = sutidao.existUsuarioSinEstado(data);
        String promotorNombre = sutidao.existUsuarioSinEstadoNombre(data);

        NeoService.PERSONA_AUTORIZA_ID.set(promotor);
        request.addProperty("medio", medio);
        request.addProperty("promotorId", promotor);
        request.addProperty("promotorNombre", promotorNombre);
        request.addProperty("promotorIdentificador", data);
        request.addProperty("cara", cara);

        ClientWSAsync async = new ClientWSAsync(funcion, url, method, request, DEBUG);
        JsonObject respuesta = async.esperaRespuesta();
        if (respuesta != null) {
            NeoService.setLog("NOTIFICACION ENVIADA");
        }

    }

    static public void ejecutarConsultaMedio(String medio, String data, Long promotor, int cara, int surtidor) {

        String funcion = "CONSULTANDO MEDIO";

        SurtidorDao sdao = new SurtidorDao();
        String server = "";
        try {
            server = sdao.getParametrosWacher("HOST_SERVER");
        } catch (DAOException ex) {
            Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
        }

        String url = "https://" + server + ":7001/api/vehiculo/autorizacion-cargue/idrom";
        String method = "POST";
        boolean DEBUG = true;
        JsonObject request = new JsonObject();
        request.addProperty("identificadorDominio", Main.credencial.getEmpresa().getDominioId());
        request.addProperty("identificadorNegocio", Main.credencial.getEmpresa().getNegocioId());
        request.addProperty("identificadorEmpresa", Main.credencial.getEmpresas_id());
        request.addProperty("identificadorPromotor", promotor);
        request.addProperty("identificadorEquipo", Main.credencial.getEquipos_id());
        if (cara > 0) {
            request.addProperty("identificadorCara", cara);
        } else {
            cara = 1;
        }
        request.addProperty("data", data);

        ClientWSAsync async = new ClientWSAsync(funcion, url, method, request, DEBUG);
        JsonObject respuesta = async.esperaRespuesta();
        int status = async.getStatus();
        if (respuesta != null) {
            Autorizacion auto = new Autorizacion();
            long PICAFUEL_PROVEEDOR = 1;
            boolean PREVENTA = true;
            String ESTADO = "A";
            int GRADO = 0;

            UUID uuid = UUID.randomUUID();

            JsonObject autoriz = new JsonObject();
            autoriz.addProperty("identificadorProceso", uuid.toString());
            autoriz.addProperty("nombreCliente", respuesta.get("data").getAsJsonObject().get("cliente").getAsString());
            autoriz.addProperty("identificadorCliente", respuesta.get("data").getAsJsonObject().get("identificador_cliente").getAsString());
            autoriz.addProperty("documentoIdentificacionCliente", respuesta.get("data").getAsJsonObject().get("identificacion_cliente").getAsString());
            autoriz.addProperty("placaVehiculo", respuesta.get("data").getAsJsonObject().get("placa_vehiculo").getAsString());
            autoriz.addProperty("montoMaximo", 0);
            autoriz.addProperty("identificadorNegocio", Main.credencial.getEmpresa().getNegocioId());

            if (respuesta.get("data").getAsJsonObject().get("paga_recaudo").getAsString().equals("S")) {
                autoriz.addProperty("isRecaudo", true);
                autoriz.addProperty("recaudar", respuesta.get("data").getAsJsonObject().get("recaudo_litro").getAsDouble());
                autoriz.addProperty("identificadorContrato", respuesta.get("data").getAsJsonObject().get("contrato_id").getAsDouble());
                autoriz.addProperty("empresaRecaudo", respuesta.get("data").getAsJsonObject().get("empresa_recaudo_final").getAsDouble());
                autoriz.addProperty("financiera", respuesta.get("data").getAsJsonObject().get("financiera").getAsString());
                autoriz.addProperty("cantidadMaxima", respuesta.get("data").getAsJsonObject().get("maximo_combustible").getAsString());
            }
            try {
                //SI NO LO RECIBE EL POS o EL POS ESTÁ INACTIVO CONTINUA
                notificacionDispositivo(medio, data, promotor, cara, autoriz);
            } catch (Exception e) {
            }
            NeoService.setLog("*****************************************************");
            NeoService.setLog("com.core.app.bean.socket.ServerIbutton.ejecutarConsultaMedio()");
            NeoService.setLog(autoriz.toString());
            NeoService.setLog("*****************************************************");
            boolean exito = sdao.registrarAutorizacion(autoriz, PICAFUEL_PROVEEDOR, PREVENTA, ESTADO, surtidor, cara, GRADO, medio, data);
            NeoService.setLog("AUTORIZACION WS [" + exito + "] CARA = " + cara + " AL VEHICULO " + respuesta.get("data").getAsJsonObject().get("placa_vehiculo").getAsString());
            NeoService.REGISTRO_VEHICULO.put(cara, respuesta.get("data").getAsJsonObject().get("placa_vehiculo").getAsString());
        } else {
            PrinterFacade print = new PrinterFacade();
            if (status == 404) {
                print.conFormato("CHIP SIN AUTORIZAR: " + data + "\r\n");
            } else {
                print.conFormato("ERROR EN LA CONEXIÓN DE RED\r\nVERIFIQUE E INTENTE NUEVAMENTE\r\nCHIP: " + data + "\r\n");
            }
        }
    }

    public static void notificacionDispositivo(String medio, String data, Long promotor, int cara, JsonObject json) {
        String funcion = "NOTIFICANDO DISPOSITIVOS";
        String url = "http://localhost:" + NeoService.PUERTO_POS_LAZO + "/api/dispositivosnotificaciones";// + medio;
        String method = "POST";
        boolean DEBUG = true;
        JsonObject request = new JsonObject();

        SurtidorDao sutidao = new SurtidorDao();
        promotor = sutidao.existUsuarioSinEstado(data);
        String promotorNombre = sutidao.existUsuarioSinEstadoNombre(data);

        request.addProperty("cara", cara);
        request.addProperty("promotorId", promotor);
        request.addProperty("promotorNombre", promotorNombre);
        request.addProperty("clienteId", 0);
        request.addProperty("medio", medio);
        request.addProperty("placa", json.get("placaVehiculo").getAsString());
        request.addProperty("clienteNombre", json.get("nombreCliente").getAsString());

        ClientWSAsync async = new ClientWSAsync(funcion, url, method, request, DEBUG);
        async.setTimeout(5000);
        JsonObject respuesta = async.esperaRespuesta();
        if (respuesta != null) {
            NeoService.setLog("NOTIFICACION ENVIADA");
        }
    }

    public static void servicioRFIDUsb(String com, int carita) {
        ProtocolsRFID runtime2 = new ProtocolsRFID(com, new RFIDConect() {
            @Override
            public void setData(boolean valide, String lectura) {
                NeoService.setLog("METODO DE LECTURA DE TAG SIN IMPLEMENTAR");
            }

            @Override
            public void setData(boolean bln, String lectura, int cara) {

                long usuario = sutidao.existUsuario(lectura);
                if (usuario > 0) {
                    AUTORIZACION.put(cara, new Date());
                    PERSONA_AUTORIZA_ID.set(usuario);
                    NeoService.REGISTRO_PERSONA.put(cara, usuario);
                } else {
                    NeoService.setLog("PERSONA NO EXISTE EN LA BASE DE DATOS");
                }

                NeoService.setLog("CARA " + carita + " PERSONA_AUTORIZA_ID >>>" + PERSONA_AUTORIZA_ID);
                JsonObject json = new JsonObject();
                json.addProperty("tipo", 3);
                json.addProperty("cara", cara);
                json.addProperty("subtipo", SUB_COMANDO_NEOAPP_TAG_REFID);
                json.addProperty("mensaje", lectura);
                NotificacionSocket.publishrr(1, json);

                NeoService.setLog("ENVIANDO DATOS A LA VISTA");
                Dispositivos.notificacionDispositivoEmpleados("rfid", lectura, usuario, cara);
            }
        }, carita);
        runtime2.setDaemon(true);
        runtime2.start();
    }

    public static void NotificacionRfidV2(boolean valide, String text) {
        if (valide) {
            text = text.trim();

            boolean conectado = false;
            try {
                conectado = text.toCharArray()[0] == '@';
            } catch (Exception o) { //FA7DDB80
            }

            if (conectado) {
                int cara = Integer.parseInt(text.toCharArray()[1] + "");

                try {
                    String valor = sutidao.getParametros("rfid2_cara");
                    String puertos[] = valor.split(";");
                    for (String puertoconf : puertos) {
                        String[] p = puertoconf.split("=");
                        int port = Integer.parseInt(p[0]);
                        if (port == cara) {
                            cara = Integer.parseInt(p[1].replaceAll("C", ""));
                            break;
                        }
                    }
                } catch (DAOException ex) {
                    NeoService.setLog("LA CONFIGURACION DE PUERTOS RFID2 INCORRECTA [rfid2_cara]");
                    NeoService.setLog("");
                    Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    NeoService.setLog("LA CONFIGURACION DE PUERTOS RFID2 INCORRECTA [rfid2_cara]");
                    Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
                }

                String rfid2text = text.substring(3, 11);

                NeoService.setLog("RFID2 AUTORIZANDO LA CARA " + cara);
                NeoService.setLog("RFID2: " + rfid2text);

                long usuario = sutidao.existUsuario(rfid2text);
                if (usuario > 0) {
                    AUTORIZACION.put(cara, new Date());
                    PERSONA_AUTORIZA_ID.set(usuario);
                    NeoService.REGISTRO_PERSONA.put(cara, usuario);
                } else {
                    NeoService.setLog("PERSONA NO EXISTE EN LA BASE DE DATOS");
                }

                NeoService.REGISTRO_PERSONA.put(cara, usuario);

                NeoService.setLog("CARA " + cara + " PERSONA_AUTORIZA_ID >>>" + PERSONA_AUTORIZA_ID);
                JsonObject json = new JsonObject();
                json.addProperty("tipo", 3);
                json.addProperty("cara", cara);
                json.addProperty("subtipo", SUB_COMANDO_NEOAPP_TAG_REFID);
                json.addProperty("mensaje", rfid2text);
                NotificacionSocket.publishrr(1, json);

                NeoService.setLog("ENVIANDO DATOS A LA VISTA");
                Dispositivos.notificacionDispositivoEmpleados("rfid", rfid2text, usuario, cara);

            }

        } else {
            NeoService.setLog("TRAMA NO VALIDA PARA RFID2");
        }
    }
}
