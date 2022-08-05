/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.protocols;

import com.ProtocolsIbutton;
import com.ProtocolsRFID;
import com.ProtocolsRFID2;
import com.RFIDConect;
import com.core.app.NeoService;
import static com.core.app.NeoService.PERSONA_AUTORIZA_ID;
import static com.core.app.NeoService.SUB_COMANDO_NEOAPP_TAG_REFID;
import static com.core.app.NeoService.sutidao;
import com.core.app.server.NotificacionSocket;
import com.google.gson.JsonObject;

/**
 *
 * @author usuariod
 */
public class DispositivoController {

    public final static String RFID = "rfid";
    public final static String RFID_V2 = "rfidV2";
    public final static String IBUTTON = "ibutton";
    ProtocolsIbutton ibutton;
    ProtocolsRFID rfid;

    public DispositivoController(String tipo, String interfaz, String device) {
        switch (tipo) {
            case RFID:
                RFid(device);
                break;
            case RFID_V2:
                RfidV2(device);
                break;
            case IBUTTON:
                if (!interfaz.equals("tcp")) {
                    Ib4Serial(device);
                } else {
                    try {
                        Dispositivos.IB4Tcp(Integer.parseInt(device));
                    } catch (Exception e) {
                        NeoService.setLog("ERROR EN DISPOSITIVO : [ " + tipo + " ] [" + device + "] interface: " + interfaz);
                    }
                }
                break;
            default:
                NeoService.setLog("DISPOSITIVO NO SOPORTADO: [ " + tipo + " ] [" + device + "] interface: " + interfaz);
                throw new AssertionError();
        }
    }

    public static void Ib4Serial(String interfaz) {
        RFIDConect conectorIbutton = new RFIDConect() {
            @Override
            public void setData(boolean bln, String string, int i) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setData(boolean valide, String text) {
                if (valide) {
                    Dispositivos.notificaIbutton(text);
                }
            }
        };

        ProtocolsIbutton runtime1 = new ProtocolsIbutton(interfaz, conectorIbutton);
        runtime1.setDaemon(true);
        runtime1.start();
    }

    public static void RfidV2(String device) {
        RFIDConect conector = new RFIDConect() {
            @Override
            public void setData(boolean bln, String string, int i) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setData(boolean valide, String text) {
                Dispositivos.NotificacionRfidV2(valide, text);
            }
        };

        ProtocolsRFID2 runtime1 = new ProtocolsRFID2(device, conector);
        runtime1.setDaemon(true);
        runtime1.start();
    }

    public static void RFid(String COM) {
        if (COM != null && !COM.trim().equals("") && !COM.trim().equals("0")) {
            if (COM.contains(";")) {
                String[] ports = COM.split(";");
                String[] subcanal1 = ports[0].split("=");
                String[] subcanal2 = ports[1].split("=");
                Integer CARA_P0 = Integer.parseInt(subcanal1[0]);
                String PUERTO_P0 = subcanal1[1];

                Integer CARA_P1 = Integer.parseInt(subcanal2[0]);
                String PUERTO_P1 = subcanal2[1];

                Dispositivos.servicioRFIDUsb(PUERTO_P0, CARA_P0);
                Dispositivos.servicioRFIDUsb(PUERTO_P1, CARA_P1);

            } else {
                RFIDConect rfid = new RFIDConect() {
                    @Override
                    public void setData(boolean valide, String text) {

                        long usuario = sutidao.existUsuario(text);
                        if (usuario > 0) {
                            PERSONA_AUTORIZA_ID.set(usuario);
                        } else {
                            NeoService.setLog("PERSONA NO EXISTE EN LA BASE DE DATOS");
                        }

                        NeoService.setLog("PERSONA_AUTORIZA_ID >>>" + PERSONA_AUTORIZA_ID);
                        JsonObject json = new JsonObject();
                        json.addProperty("tipo", 3);
                        json.addProperty("subtipo", SUB_COMANDO_NEOAPP_TAG_REFID);
                        json.addProperty("mensaje", text);
                        NotificacionSocket.publishrr(1, json);

                        Dispositivos.notificacionDispositivoEmpleados("rfid", text, usuario, 0);
                    }

                    @Override
                    public void setData(boolean bln, String string, int i) {
                        this.setData(bln, string);
                    }
                };
                ProtocolsRFID runtime = new ProtocolsRFID(COM, rfid);
                runtime.setDaemon(true);
                runtime.start();
            }
        }
    }
}
