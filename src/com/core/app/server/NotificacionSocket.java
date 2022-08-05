/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.server;

import com.butter.bean.EquipoDao;
import com.google.gson.JsonObject;
import com.core.app.NeoService;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS-PC
 */
public class NotificacionSocket {

    private static final EquipoDao eado = new EquipoDao();

    @Deprecated
    static public void publish(int mensajeTipo, String text) {

        JsonObject json = new JsonObject();
        json.addProperty("tipo", 1);
        json.addProperty("icono", mensajeTipo);
        json.addProperty("mensaje", text);

        try {
            try (Socket clientSocket = new Socket()) {

                String host = eado.getParametroString("socket_notificaciones_host");
                int port = eado.getParametroInt("socket_notificaciones_port");

                clientSocket.connect(new InetSocketAddress(host, port), 500);
                DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());
                osw.write(json.toString().getBytes());
                osw.flush();
                osw.close();
            }
            if (NeoService.DEBUG_NOTIFICACION_INTERNA) {
                NeoService.setLog(json.toString());
            }
        } catch (IOException ex) {
            if (NeoService.DEBUG_NOTIFICACION_INTERNA) {
                NeoService.setLog("[SOCKET]: Sin publicar push: " + json.toString());
            }
        }
    }

    static public void publishrr(int mensajeTipo, JsonObject notify) {
        try {
            if (NeoService.DEBUG_NOTIFICACION_INTERNA) {
                NeoService.setLog("[INFO (publishrr)]: " + notify.toString());
            }
            Socket clientSocket = new Socket();
            //clientSocket.connect(new InetSocketAddress("192.168.0.14", 7002), 500);

            EquipoDao eado = new EquipoDao();
            String host = eado.getParametroString("socket_notificaciones_host");
            int port = eado.getParametroInt("socket_notificaciones_port");

            clientSocket.connect(new InetSocketAddress(host, port), 500);
            DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());
            osw.write((notify.toString()).getBytes());
            osw.flush();
            osw.close();
            clientSocket.close();
        } catch (IOException ex) {
            if (NeoService.DEBUG_NOTIFICACION_INTERNA) {
                NeoService.setLog("[ERROR (publishrr)]: Sin publicar notificaciones ->" + notify.toString());
            }
        }
    }

    List<Integer> sendCommand(byte[] comando) {
        List<Integer> arreglo = new ArrayList<>();
        NeoService.setLog("Enviando...");
        return arreglo;
    }

}
