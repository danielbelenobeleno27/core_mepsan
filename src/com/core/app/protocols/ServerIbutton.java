/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.protocols;

import com.core.app.NeoService;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS-PC
 */
public class ServerIbutton extends Thread {

    ServerSocket welcomeSocket;
    Socket connectionSocket;
    String clientSentence;
    String capitalizedSentence;
    boolean conectado;
    DataOutputStream outToClient;
    int port;
    String mensaje = "";

    public ServerIbutton(int port) {
        this.port = port;
        setTimeout(() -> cerrar(), 10);

    }

    public void conectar() {
        try {
            welcomeSocket = new ServerSocket(port);
            conectado = true;
        } catch (BindException ex) {
            NeoService.setLog("PUERTO OCUPADO PARA EL SERVIDOR IBUTTON PUERTO:" + port);
            System.exit(23);
        } catch (IOException ex) {
            Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void desconectar() {
        try {

            if (connectionSocket != null) {
                connectionSocket.close();
                connectionSocket = null;
            }
            if (connectionSocket != null) {
                outToClient.close();
                outToClient = null;
            }
            if (welcomeSocket != null) {
                welcomeSocket.close();
                welcomeSocket = null;
            }

        } catch (IOException ex) {
            Logger.getLogger(ServerIbutton.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            conectado = false;
        }
    }

    public void cerrar() {
        desconectar();
        setTimeout(() -> cerrar(), 10);
    }

    @Override
    public void run() {

        while (true) {
            conectar();
            while (conectado) {

                clientSentence = "";
                BufferedReader inFromClient = null;
                try {
                    //NeoService.setLog("ESPERANDO CONEXION....!");
                    if (connectionSocket == null || connectionSocket.isClosed()) {
                        connectionSocket = welcomeSocket.accept();
                        // NeoService.setLog("NUEVO CLIENTE CONECTADO....!");
                    }

                    inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    int caract = -1;
                    int timeoutMillis = 1000;
                    long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
                    while (System.currentTimeMillis() < maxTimeMillis) {
                        caract = inFromClient.read();
                        if (caract == -1) {
                            break;
                        }
                    }
                    while (caract != '~') {
                        NeoService.setLog(clientSentence);
                        clientSentence += (char) caract;
                        caract = inFromClient.read();
                        if (caract == -1) {
                            break;
                        }
                    }
                    NeoService.setLog(clientSentence);
                    Dispositivos.notificaIbutton(clientSentence);
                    connectionSocket.close();
                } catch (Exception a) {

                    if (mensaje.equals(a.getMessage())) {
                        mensaje = a.getMessage();
                        NeoService.setLog(mensaje);
                    }
                    connectionSocket = null;
                }
            }
        }
    }

    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay * 1000);
                runnable.run();
            } catch (InterruptedException e) {
                NeoService.setLog(e.getMessage());
            }
        }).start();
    }

    List<Integer> sendCommand(byte[] comando) {
        List<Integer> arreglo = new ArrayList<>();
        NeoService.setLog("Enviando...");
        return arreglo;
    }

}
