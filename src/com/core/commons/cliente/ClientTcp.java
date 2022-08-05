package com.core.commons.cliente;

import com.core.app.NeoService;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.neo.app.bean.ProtocolsDto;
import com.core.app.server.NotificacionSocket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientTcp extends Cliente {

    byte[] response;
    Socket clientSocket;
    boolean conect = false;
    DataOutputStream out;
    DataInputStream in;
    private final static int CONSTANTE_FIND_COMANDO = 0xF0;

    String host;
    int port;

    public ClientTcp(String host, int port) {
        this.host = host;
        this.port = port;
        while (!conect) {
            try {
                clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(host, port), 800);
                out = new DataOutputStream(clientSocket.getOutputStream());
                in = new DataInputStream(clientSocket.getInputStream());
                conect = true;
            } catch (IOException ex) {
                conect = false;
                NotificacionSocket.publish(1, "Perdida comunicacion al socket TCP " + host + ":" + port);
            }
        }
    }

    /**
     *
     * @param host
     * @param port
     * @param TimeOut: the timeout value to be used in milliseconds.
     */
    public ClientTcp(String host, int port, int TimeOut) {
        this.host = host;
        this.port = port;
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, port), TimeOut);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            conect = true;
        } catch (IOException ex) {
            conect = false;
            NotificacionSocket.publish(1, "Perdida comunicacion al socket TCP " + host + ":" + port);
        }
    }

    /**
     *
     * @param protocol
     * @param timeout
     * @return
     * @throws IOException
     */
    @Override
    public synchronized byte[] send(ProtocolsDto protocol, int timeout) throws Exception {
        sendTrama(protocol);
        if (timeout > 0) {
            wait(timeout);
        }
        receiveTrama(protocol);
        return response;
    }

    public static int readInputStreamWithTimeout(InputStream is, byte[] b, int timeoutMillis, boolean cortarSiLlegaCero)
            throws IOException {
        int bufferOffset = 0;
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {

            int readLength = java.lang.Math.min(is.available(), b.length - bufferOffset);
            int readResult = is.read(b, bufferOffset, readLength);
            if (cortarSiLlegaCero) {
                if (readResult == -1 || readResult == 0) {
                    break;
                }
            } else {
                if (readResult == -1) {
                    break;
                }
            }
            bufferOffset += readResult;

        }
        return bufferOffset;
    }

    public boolean isConect() {
        return conect;
    }

    @Override
    public void reconect() {
        NeoService.setLog("ELIMINANDO CONEXION");
        try {
            if (clientSocket.isConnected()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            NeoService.setLog("Error al cerrar la conexion del Surtidor " + port);
        }
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, port), NeoService.TIMEOUT_CONEXION_SOCKET);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            conect = true;
        } catch (IOException ex) {
            conect = false;
            NeoService.setLog("Error al conectarse al puerto del Surtidor " + port);
        }
    }

    public void sendTrama(ProtocolsDto protocol) {
        long id = new Date().getTime();
        try {
            out.write(protocol.getTxTrama());
            out.flush();
            if (protocol.isDebug()) {
                String trama = "";
                for (byte b : protocol.getTxTrama()) {
                    trama += (String.format("%02x", b).toUpperCase() + " ");
                }
                NeoService.setLog(NeoService.ANSI_GREEN + "[" + id + "] " + protocol.getOrigen().getIp() + ":" + protocol.getOrigen().getPort() + " V." + NeoService.VERSION_NAME + " [TX  TCP]: " + trama + NeoService.ANSI_RESET);
            }
        } catch (IOException e) {
            Logger.getLogger(ClientSerial.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    public byte[] receiveTrama(ProtocolsDto protocol) {
        long id = new Date().getTime();
        response = new byte[0];
        try {
            if (protocol.isEsperaRespuesta()) {
                boolean cortarSiLlegaCero = true;
                if ((protocol.getTxTrama()[0] & CONSTANTE_FIND_COMANDO) == (byte) 0x50) {
                    wait(400);
                }
                if ((protocol.getTxTrama()[0] & CONSTANTE_FIND_COMANDO) == (byte) 0x20) {
                    wait(200);
                }
                response = protocol.getRxTrama();
                int readCount = readInputStreamWithTimeout(in, response, NeoService.TIMEOUT_CONEXION_SOCKET, cortarSiLlegaCero);
                if (readCount != 0) {
                    if (protocol.isDebug()) {
                        String trama = "";
                        for (byte b : response) {
                            trama += (String.format("%02x", b).toUpperCase() + " ");
                        }
                        NeoService.setLog(NeoService.ANSI_GREEN + "[" + id + "] " + protocol.getOrigen().getIp() + ":" + protocol.getOrigen().getPort() + " V." + NeoService.VERSION_NAME + " [RX  TCP]: " + trama + NeoService.ANSI_RESET);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientTcp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
}
