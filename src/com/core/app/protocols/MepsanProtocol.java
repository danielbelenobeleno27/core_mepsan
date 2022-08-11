package com.core.app.protocols;

import com.butter.bean.Utils;
import static com.butter.bean.Utils.byteArrayToString;
import com.core.app.NeoService;
import static com.core.app.protocols.Mepsan.ACKPeticion;
import static com.core.app.protocols.MepsanController.CONSTANTE_FIND_MANGUERA;
import com.core.commons.cliente.Cliente;
import com.neo.app.bean.Cara;
import com.neo.app.bean.ProtocolsDto;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.Totalizador;
import com.neo.app.bean.VentaParcial;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MepsanProtocol extends BaseProtocols {

    public static final int TIEMPO_ESPERA_PULLING = 100;
    public static final int TIEMPO_ESPERA_VALIDACION = 100;

    private static final int CONSTANTE_FIND_TOTALIZADORES = 0x65;
    private static final int CONSTANTE_RESPUESTA_ERROR_TRAMA = 0x01;

    private static final String CONSULTA_TOTALIZADORES = "obtenerTotalizadores";
    private static final String ESTADO_MANGUERA = "estadoCara";
    private static final String VENTA_EN_CURSO = "ventaEnCurso";
    private static final String PREDETERMINAR_VOLUMEN = "predeterminarVolumen";
    private static final String PREDETERMINAR_DINERO = "predeterminarDinero";
    private static final String CAMBIAR_PRECIO = "cambiarPrecio";

    private static final int CONSTANTE_FIND_VALOR = 0x0F;
    private static final int CONSTANTE_FIND_COMANDO = 0xF0;

    private static final int FACTOR_PREDETERMINACION_VOLUMEN = 1000;
    private static final int FACTOR_PREDETERMINACION_DINERO = 1;

    private static byte id = -1;

    private static final Mepsan MEPSAN = new Mepsan();

    public MepsanProtocol(Cliente client) {
        this.client = client;
        this.ciclo = 0;
    }

    public byte[] pulling(Surtidor surtidor, int cara, String parent, int wait) throws Exception {
        byte[] txTrama = new byte[3];
        byte[] rxTrama = null;

        switch (parent) {
            case CONSULTA_TOTALIZADORES:
                rxTrama = new byte[14];
                break;
            case ESTADO_MANGUERA:
                rxTrama = new byte[15];
                break;
            case VENTA_EN_CURSO:
                rxTrama = new byte[22];
                break;
            default:
        }

        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x20;
        txTrama[2] = (byte) 0xFA;

        ProtocolsDto protodto = new ProtocolsDto(surtidor, "pulling", txTrama, rxTrama, surtidor.isEcho());
        protodto.setEsperaRespuesta(true);
        protodto.setDebug(surtidor.isDebugTrama());
        byte[] response = client.send(protodto, TIEMPO_ESPERA_PULLING);

        ACKPulling(surtidor, cara, parent, protodto, response, wait);

        return response;
    }

    public ArrayList<Byte> estadoManguera(Surtidor surtidor, int cara, int wait) throws Exception {
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[3];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x01; // 01 indica modo comando
        txTrama[3] = 0x01; // length de datos
        txTrama[4] = 0x00; // comando solicitud de estado
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, ESTADO_MANGUERA, txTrama, rxTrama, surtidor.isEcho());
        protodto.setEsperaRespuesta(true);
        protodto.setDebug(surtidor.isDebugTrama());
        byte[] response = client.send(protodto, wait);
        if (response != null && response.length > 0) {
            boolean valido = ACKPeticion(txTrama, rxTrama);
            if (valido) {
                if (surtidor.isDebugEstado()) {
                    NeoService.setLog(NeoService.ANSI_GREEN + "Respuesta del surtidor valida, enviando comando pulling" + NeoService.ANSI_RESET);
                }
                response = pulling(surtidor, cara, ESTADO_MANGUERA, wait);
            } else {
                if (surtidor.isDebugEstado()) {
                    NeoService.setLog(NeoService.ANSI_RED + "Respuesta del surtidor ERRORNEA, NO SE ENVIA COMANDO PULLING" + NeoService.ANSI_RESET);
                }
                response = new byte[1];
                response[0] = CONSTANTE_RESPUESTA_ERROR_TRAMA;
            }
        }
        ArrayList<Byte> respuesta = new ArrayList<>();
        if (response != null) {
            for (byte b : response) {
                respuesta.add(b);
            }
        }
        return respuesta;

    }

    public Long getPrecio(Surtidor surtidor, int cara, int grado, int wait) throws Exception {
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[3];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x01; // 01 indica modo comando
        txTrama[3] = 0x01; // length de datos
        txTrama[4] = 0x00; // comando solicitud de estado
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, ESTADO_MANGUERA, txTrama, rxTrama, surtidor.isEcho());
        protodto.setEsperaRespuesta(true);
        protodto.setDebug(true);
        byte[] response = client.send(protodto, wait);
        if (response != null && response.length > 0) {
            boolean valido = ACKPeticion(txTrama, rxTrama);
            if (valido) {
                NeoService.setLog(NeoService.ANSI_GREEN + "Respuesta del surtidor valida, enviando comando pulling" + NeoService.ANSI_RESET);
                response = pulling(surtidor, cara, ESTADO_MANGUERA, wait);
            } else {
                NeoService.setLog(NeoService.ANSI_RED + "Respuesta del surtidor ERRORNEA, NO SE ENVIA COMANDO PULLING" + NeoService.ANSI_RESET);
                response = new byte[1];
                response[0] = CONSTANTE_RESPUESTA_ERROR_TRAMA;
            }
        }
        if (response != null && response.length > 1) {
            String data = byteArrayToString(response, 7, 9);
            int mangueraSurtidor = response[10] & CONSTANTE_FIND_MANGUERA;
            if (mangueraSurtidor == grado) {
                NeoService.setLog(NeoService.ANSI_GREEN + " PRECIO SIN CONVERTIR " + data + NeoService.ANSI_RESET);
                NeoService.setLog(NeoService.ANSI_GREEN + " PRECIO DE LA MANGUERA " + mangueraSurtidor + " $" + Long.parseLong(data) + NeoService.ANSI_RESET);
                return Long.parseLong(data);
            }
        }
        return 0L;
    }

    public ProtocolsDto autorizar(Surtidor surtidor, int cara, int wait) throws Exception {
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[3];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x01; // 01 indica modo comando
        txTrama[3] = 0x01; // length de datos
        txTrama[4] = 0x06; // comando solicitud de estado
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, "autorizar", txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(true);
        protodto.setEsperaRespuesta(surtidor.isEcho());

        rxTrama = client.send(protodto, wait);

        boolean valido = ACKPeticion(txTrama, rxTrama);
        if (valido) {
            NeoService.setLog(NeoService.ANSI_GREEN + "Respuesta del surtidor valida" + NeoService.ANSI_RESET);
        }

        return protodto;
    }

    public boolean detener(Surtidor surtidor, int cara, int wait) throws Exception {
        boolean result;
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[1];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x01; // 01 indica modo comando
        txTrama[3] = 0x01; // length de datos
        txTrama[4] = 0x08; // comando solicitud de estado
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, "detenerManguera", txTrama, rxTrama, surtidor.isEcho());
        if (surtidor.isEcho()) {
            protodto.setEsperaRespuesta(true);
        }
        client.send(protodto, wait);
        result = true;

        return result;
    }

    public VentaParcial ventaEnCurso(Surtidor surtidor, Cara cara, int wait) throws Exception {
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[9];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara.getNumero() - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x01; // 01 indica modo comando
        txTrama[3] = 0x01; // length de datos
        txTrama[4] = 0x04; // comando solicitud de estado
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, VENTA_EN_CURSO, txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(surtidor.isDebugTrama());
        protodto.setEsperaRespuesta(true);
        VentaParcial venta = new VentaParcial();
        String dataVolumen = "0";
        String dataImporte = "0";
        String dataPrecio = "0";
        byte[] response = client.send(protodto, wait);
        if (response != null && response.length > 0) {
            response = pulling(surtidor, cara.getNumero(), VENTA_EN_CURSO, wait);
        }
        
        if (response == null || response.length == 0) {
            response = pulling(surtidor, cara.getNumero(), VENTA_EN_CURSO, wait);
        }

        if (response != null && response.length > 2) {
            dataVolumen += byteArrayToString(response, 4, 7);
            dataImporte += byteArrayToString(response, 8, 11);
            dataPrecio += byteArrayToString(response, 14, 16);

            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.fill("=", Utils.PAGE_SIZE));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.text_between("CARA:", cara.getNumero() + ""));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.text_between("MANGUERA:", cara.getMagueraactual().getId() + ""));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.text_between("GRADO:", cara.getMagueraactual().getGrado() + ""));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.text_between("PRECIO:", dataPrecio + ""));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.text_between("IMPORTE:", dataImporte + ""));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.text_between("VOLUMEN CALCULADO:", dataVolumen + ""));
            NeoService.setLog(NeoService.ANSI_CYAN + "MEPSAN: " + NeoService.ANSI_RESET + Utils.fill("=", Utils.PAGE_SIZE));
        }

        venta.setVolumenCalculado(Long.parseLong(dataVolumen));
        venta.setPrecio(Long.parseLong(dataPrecio));
        venta.setVolumenReal(Long.parseLong(dataImporte));
        venta.setVolumen(Long.parseLong(dataVolumen));

        return venta;
    }

    public Totalizador consigueTotalizadores(Surtidor surtidor, int cara, int manguera, int wait, int tipoTotalizador) throws Exception {
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[3];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x65;
        txTrama[3] = 0x01;// LENGTH DE DATOS

        if (tipoTotalizador == 0) {
            txTrama[4] = 0;
        } else {
            txTrama[4] = 0x10;
        }
        txTrama[4] = (byte) (txTrama[4] | manguera); //TOTALIZADORES POR VOLUMEN el 0 antes de 01
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, CONSULTA_TOTALIZADORES, txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(true);
        protodto.setEsperaRespuesta(true);

        byte[] receive = client.send(protodto, wait);
        if (receive != null && receive.length == 0) {
            NeoService.pauseMS(80);
            NeoService.setLog(NeoService.ANSI_RED + "ERROR EN CONSULTA DE TOTALIZADORES , REINTENTANDO..." + NeoService.ANSI_RESET);
            receive = client.send(protodto, wait);
        }
        boolean valido = ACKPeticion(txTrama, rxTrama);
        if (valido) {
            NeoService.setLog(NeoService.ANSI_GREEN + "Respuesta del surtidor valida, enviando comando pulling" + NeoService.ANSI_RESET);
            receive = pulling(surtidor, cara, CONSULTA_TOTALIZADORES, wait);
            if (receive.length > 0) {
                Totalizador totalizadores = new Totalizador();
                String data = byteArrayToString(receive, 5, 9);
                if (tipoTotalizador == MepsanController.PREGUNTA_TOTALIZADOR_VOLUMEN) {
                    NeoService.setLog(NeoService.ANSI_PURPLE + "TOTALIZADORES VOLUMEN " + data + NeoService.ANSI_RESET);
                    totalizadores.setAcumuladoVolumen(Long.parseLong(data));
                }
                if (tipoTotalizador == MepsanController.PREGUNTA_TOTALIZADOR_IMPORTE) {
                    NeoService.setLog(NeoService.ANSI_PURPLE + "TOTALIZADORES IMPORTE " + data + NeoService.ANSI_RESET);
                    totalizadores.setAcumuladoVenta(Long.parseLong(data));
                }
                return totalizadores;
            } else {
                NeoService.setLog(NeoService.ANSI_RED + "Respuesta del surtidor ERRORNEA, DESPUES DE VALIDAR TOTALIZADORES" + NeoService.ANSI_RESET);
            }
        } else {
            NeoService.setLog(NeoService.ANSI_RED + "Respuesta del surtidor ERRORNEA, NO SE ENVIA COMANDO PULLING" + NeoService.ANSI_RESET);
        }

        return null;
    }

    public int setPredeterminarVolumen(Surtidor surtidor, int cara, long galones, int wait) throws Exception {
        byte[] txTrama = new byte[12];
        byte[] rxTrama = new byte[2];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x03; // comando de predeterminacion por volumen
        txTrama[3] = 0x04; // length de datos
        galones *= FACTOR_PREDETERMINACION_VOLUMEN;

        char[] arrayGalones = Utils.stringToByteArray(String.valueOf(galones), 8);

        byte data0 = (byte) Integer.parseInt(arrayGalones[0] + "" + arrayGalones[1], 16);
        byte data1 = (byte) Integer.parseInt(arrayGalones[2] + "" + arrayGalones[3], 16);
        byte data2 = (byte) Integer.parseInt(arrayGalones[4] + "" + arrayGalones[5], 16);
        byte data3 = (byte) Integer.parseInt(arrayGalones[6] + "" + arrayGalones[7], 16);

        txTrama[4] = data0;
        txTrama[5] = data1;
        txTrama[6] = data2;
        txTrama[7] = data3;

        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, PREDETERMINAR_VOLUMEN, txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(true);
        protodto.setEsperaRespuesta(true);

        client.send(protodto, wait);

        return 0;
    }

    public int setPredeterminarDinero(Surtidor surtidor, int cara, long dinero, int wait) throws Exception {
        byte[] txTrama = new byte[12];
        byte[] rxTrama = new byte[2];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x04; // comando de predeterminacion por valor
        txTrama[3] = 0x04; // length de datos
        dinero *= FACTOR_PREDETERMINACION_DINERO;

        char[] arrayDinero = Utils.stringToByteArray(String.valueOf(dinero), 8);

        byte data0 = (byte) Integer.parseInt(arrayDinero[0] + "" + arrayDinero[1], 16);
        byte data1 = (byte) Integer.parseInt(arrayDinero[2] + "" + arrayDinero[3], 16);
        byte data2 = (byte) Integer.parseInt(arrayDinero[4] + "" + arrayDinero[5], 16);
        byte data3 = (byte) Integer.parseInt(arrayDinero[5] + "" + arrayDinero[6], 16);

        txTrama[4] = data0;
        txTrama[5] = data1;
        txTrama[6] = data2;
        txTrama[7] = data3;

        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, PREDETERMINAR_DINERO, txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(true);
        protodto.setEsperaRespuesta(true);

        client.send(protodto, wait);

        return 0;
    }

    public int setPrecio(Surtidor surtidor, int cara, long precio, int wait) throws Exception {
        byte[] txTrama = new byte[14];
        byte[] rxTrama = new byte[3];

        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x05; // comando de cambio de precio
        txTrama[3] = 0x06; // length de datos

        char[] arrayPrecio = Utils.stringToByteArray(String.valueOf(precio), 6);

        byte data0 = (byte) Integer.parseInt(arrayPrecio[0] + "" + arrayPrecio[1], 16);
        byte data1 = (byte) Integer.parseInt(arrayPrecio[2] + "" + arrayPrecio[3], 16);
        byte data2 = (byte) Integer.parseInt(arrayPrecio[4] + "" + arrayPrecio[5], 16);

        txTrama[4] = data0;
        txTrama[5] = data1;
        txTrama[6] = data2;
        txTrama[7] = data0;
        txTrama[8] = data1;
        txTrama[9] = data2;

        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, CAMBIAR_PRECIO, txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(true);
        protodto.setEsperaRespuesta(true);

        client.send(protodto, wait);

        return 0;
    }

    private static byte getIdTransaccion() {
        if (id > 15) {
            id = 0;
        }
        id++;
        return id;
    }

    public void ACKPulling(Surtidor surtidor, int cara, String parent, ProtocolsDto protodto, byte[] response, int wait) {
        switch (parent) {
            case CONSULTA_TOTALIZADORES:
                validarTotalizadores(protodto, response, 5, wait);
                break;
            default:
        }
    }

    private void validarTotalizadores(ProtocolsDto protocolDto, byte[] response, int reintentos, int wait) {
        int intentos = 0;
        try {
            boolean errorTrama = false;
            if (response.length > 0 && response[2] != CONSTANTE_FIND_TOTALIZADORES) {
                NeoService.setLog(NeoService.ANSI_RED + " RESPUESTA INDICE 0X65 DE TOTALIZADORES" + NeoService.ANSI_RESET);
                response = new byte[1];
                response[0] = CONSTANTE_RESPUESTA_ERROR_TRAMA;
                errorTrama = true;
            }
            if (response.length == 0 || errorTrama) {
                while (intentos < reintentos) {
                    NeoService.millisecondsPause(TIEMPO_ESPERA_VALIDACION);
                    NeoService.setLog(NeoService.ANSI_YELLOW + "RESPUESTA INCORRECTA DE TOTALIZADORES, SE VUELVE A CONSULTAR" + NeoService.ANSI_RESET);
                    response = client.send(protocolDto, wait);
                    if (response[2] == CONSTANTE_FIND_TOTALIZADORES) {
                        break;
                    }
                    client.reconect();
                    NeoService.millisecondsPause(80);
                    intentos++;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(MepsanProtocol.class.getName()).log(Level.SEVERE, null, e);
            NeoService.setLog(e.getMessage());
        }
    }
}
