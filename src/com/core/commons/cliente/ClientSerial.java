package com.core.commons.cliente;

import com.core.app.NeoService;
import com.core.app.protocols.Mepsan;
import com.fazecast.jSerialComm.SerialPort;
import com.neo.app.bean.ProtocolsDto;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSerial extends Cliente {

    SerialPort Port;
    String setComPort;
    int baudRate;
    int numDataBit;
    int numStopBit;
    int parity;
    byte[] inbuffer;
    boolean conect = false;
    boolean debug = false;
    static final String TIPO_TX = "TX";
    static final String TIPO_RX = "RX";

    public ClientSerial(String setComPort, int baudRate, int numDataBit, int numStopBit, int parity, boolean debug) {
        this.setComPort = setComPort;
        this.baudRate = baudRate;
        this.numDataBit = numDataBit;
        this.numStopBit = numStopBit;
        this.parity = parity;
        this.debug = debug;
        while (!conect) {
            try {
                Port = SerialPort.getCommPort(setComPort);
                Port.setBaudRate(baudRate);
                Port.setNumDataBits(numDataBit);
                Port.setNumStopBits(numStopBit);
                Port.setParity(parity);
                Port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
                Port.clearDTR();
                conect = Port.openPort();
                if (debug) {
                    NeoService.setLog("[INFO (ClientSerial)]: " + setComPort + " Conectado?: " + conect);
                }
            } catch (Exception ex) {
                conect = false;
                if (debug) {
                    NeoService.setLog("[ERROR (ClientSerial)]: No fue posible conectase al puerto lazo: " + setComPort + " -> " + ex.toString());
                }
            }
        }
    }

    /**
     *
     * @param protocol
     * @param time
     * @return
     * @throws Exception
     */
    @Override
    public byte[] send(ProtocolsDto protocol, int time) throws Exception {
        long id = new Date().getTime();
        sendTrama(id, protocol);
        if (time > 0) {
            NeoService.pauseMS(time);
        }
        return receiveTrama(id, protocol);
    }

    @Override
    public void reconect() {
        try {
            if (debug) {
                NeoService.setLog("Reconectando al puerto " + setComPort);
            }
            NeoService.setLog("A: Reconectando al puerto " + setComPort);
            Port.closePort();
            conect = Port.openPort();
            if (debug) {
                NeoService.setLog("Puerto [" + setComPort + "] reconectado correctamente");
            }
        } catch (Exception ex) {
            conect = false;
            NeoService.setLog("Error al conectarse al puerto");
        }
    }

    public void sendTrama(long id, ProtocolsDto protocol) {
        try {
            Port.writeBytes(protocol.getTxTrama(), protocol.getTxTrama().length);
            Port.getOutputStream().flush();

            if (protocol.isDebug()) {
                procesarTramaRespuesta(id, protocol, TIPO_TX, protocol.getTxTrama(), false);
            }
        } catch (IOException e) {
            Logger.getLogger(ClientSerial.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public byte[] receiveTrama(long id, ProtocolsDto protocol) {
        byte[] response = new byte[0];
        try {
            if (protocol.isEsperaRespuesta()) {
                response = protocol.getRxTrama();
                int cantbyte = Port.readBytes(response, response.length);
                //RESPUESTA CABECERA 0x50
                if (cantbyte > 0 && response[0] != protocol.getTxTrama()[0] && response[cantbyte - 1] != 0xFA) {
                    cantbyte = 0;
                    NeoService.setLog(protocol.getTitulo() + " " + NeoService.ANSI_RED + "[" + id + "] " + protocol.getOrigen().getIp() + ":" + protocol.getOrigen().getPort() + " V." + NeoService.VERSION_NAME + " [RX  SERIAL]: RESPUESTA NO VALIDA DEL SURTIDOR " + NeoService.ANSI_RESET);
                    procesarTramaRespuesta(id, protocol, TIPO_RX, response, true);
                }
                if (cantbyte != 0 && protocol.isDebug()) {
                    procesarTramaRespuesta(id, protocol, TIPO_RX, response, false);
                } else {
                    NeoService.setLog(protocol.getTitulo() + " " + NeoService.ANSI_RED + "[" + id + "] " + protocol.getOrigen().getIp() + ":" + protocol.getOrigen().getPort() + " V." + NeoService.VERSION_NAME + " [RX  SERIAL]: NO HAY RESPUESTA DEL SURTIDOR" + NeoService.ANSI_RESET);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ClientSerial.class.getName()).log(Level.SEVERE, null, e);
        }
        return response;
    }

    void procesarTramaRespuesta(long id, ProtocolsDto protocol, String tipo, byte[] tramaX, boolean isError) {
        String trama = "";
        for (byte b : tramaX) {
            trama = trama.concat(String.format("%02x", b).toUpperCase() + " ");
        }
        if (protocol.getTitulo().equals("pulling")) {
            NeoService.setLog(protocol.getTitulo() + " " + NeoService.ANSI_CYAN + "[" + id + "] " + protocol.getOrigen().getIp() + ":" + protocol.getOrigen().getPort() + " V." + NeoService.VERSION_NAME + " [" + tipo + "  SERIAL]: " + trama + NeoService.ANSI_RESET);
        } else {
            NeoService.setLog(protocol.getTitulo() + " " + (isError ? NeoService.ANSI_RED : NeoService.ANSI_GREEN) + "[" + id + "] " + protocol.getOrigen().getIp() + ":" + protocol.getOrigen().getPort() + " V." + NeoService.VERSION_NAME + " [[" + tipo + "  SERIAL]: " + trama + NeoService.ANSI_RESET);
        }
    }
}
