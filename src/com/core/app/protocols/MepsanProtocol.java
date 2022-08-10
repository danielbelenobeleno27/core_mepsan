package com.core.app.protocols;

import com.butter.bean.Utils;
import com.core.app.NeoService;
import com.core.commons.cliente.Cliente;
import com.neo.app.bean.Cara;
import com.neo.app.bean.ProtocolsDto;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.Totalizador;
import com.neo.app.bean.VentaParcial;
import java.util.ArrayList;

public class MepsanProtocol extends BaseProtocols {

    public static final int TIEMPO_ESPERA_PULLING = 100;
    public static final int TIEMPO_ESPERA_VALIDACION = 100;
    
    private static final int CONSTANTE_FIND_TOTALIZADORES = 0x65;
    
    private static final String CONSULTA_TOTALIZADORES = "obtenerTotalizadores";
    private static final String ESTADO_MANGUERA = "estadoCara";
    private static final String VENTA_EN_CURSO = "ventaEnCurso";
    private static final String PREDETERMINAR_VOLUMEN = "predeterminarVolumen";
    private static final String PREDETERMINAR_DINERO = "predeterminarDinero";
    private static final String CAMBIAR_PRECIO = "cambiarPrecio";
    
    private static final  int CONSTANTE_FIND_VALOR = 0x0F;
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
            default:
        }

        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x20; 
        txTrama[2] = (byte) 0xFA; 

        ProtocolsDto protodto = new ProtocolsDto(surtidor, "pulling", txTrama, rxTrama, surtidor.isEcho());
        protodto.setEsperaRespuesta(true);
        protodto.setDebug(true);
        byte[] response = client.send(protodto, TIEMPO_ESPERA_PULLING);
        
        ACKPulling(parent, protodto, response, wait);
        
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
        protodto.setDebug(true);
        byte[] response = client.send(protodto, wait);
        
        boolean valido = ACKPeticion(txTrama, rxTrama);
        if (valido) {
            NeoService.setLog(NeoService.ANSI_GREEN + "Respuesta del surtidor valida, enviando comando pulling" + NeoService.ANSI_RESET);
            response = pulling(surtidor, cara, ESTADO_MANGUERA, wait);
        }
        
        ArrayList<Byte> respuesta = new ArrayList<>();

        for (byte b : response) {
            respuesta.add(b);
        }

        return respuesta;
        
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
        
        client.send(protodto, wait);

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

        byte[] receive = client.send(protodto, wait);
        
        return null;
    }

    public Totalizador[] consigueTotalizadores(Surtidor surtidor, int cara, int manguera, int wait) throws Exception {
        byte[] txTrama = new byte[9];
        byte[] rxTrama = new byte[3];
        
        byte idTransaccion = getIdTransaccion();
        txTrama[0] = 0x50;
        txTrama[0] = (byte) (txTrama[0] | cara - 1);
        txTrama[1] = 0x30;
        txTrama[1] = (byte) (txTrama[1] | idTransaccion);
        txTrama[2] = 0x65;
        txTrama[3] = 0x01;
        txTrama[4] = 0x00;
        txTrama[4] = (byte) (txTrama[4] | manguera);
        txTrama = MEPSAN.calcularCheckSum(txTrama);

        ProtocolsDto protodto = new ProtocolsDto(surtidor, CONSULTA_TOTALIZADORES, txTrama, rxTrama, surtidor.isEcho());
        protodto.setDebug(true);
        protodto.setEsperaRespuesta(true);
        
        byte[] receive = client.send(protodto, wait);
        
        boolean valido = ACKPeticion(txTrama, rxTrama);
        if (valido) {
            NeoService.setLog(NeoService.ANSI_GREEN + "Respuesta del surtidor valida, enviando comando pulling" + NeoService.ANSI_RESET);
            receive = pulling(surtidor, cara, CONSULTA_TOTALIZADORES, wait);
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
        byte[] rxTrama = new byte[2];
       
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

   private static byte getIdTransaccion(){
       if (id > 15) {
            id = 0;
        }
        id++;
        return id;
   }
   
   public boolean ACKPeticion(byte[] tx, byte[] rx){
       boolean valido = false;
       if (tx[0] == rx[0] && (tx[1] & (byte) 0x0F) == (rx[1] & (byte) 0x0F) && rx[2] == (byte) 0xFA) {
           valido = true;
       }
       return valido;
   }
   
   public void ACKPulling(String parent, ProtocolsDto protodto, byte[] response, int wait) {
       
       switch (parent) {
           case CONSULTA_TOTALIZADORES:
               validarTotalizadores(protodto, response, 5, wait);
               break;
           case ESTADO_MANGUERA:
               break;
           default:
       }
   }

   private byte[] validarTotalizadores(ProtocolsDto protocolDto, byte[] response, int reintentos, int wait) {
       int intentos = 0;
       try {
           while (intentos < reintentos) {
               NeoService.millisecondsPause(TIEMPO_ESPERA_VALIDACION);
               NeoService.setLog(NeoService.ANSI_YELLOW + "RESPUESTA INCORRECTA DE TOTALIZADORES, SE VUELVE A CONSULTAR" + NeoService.ANSI_RESET);
               response = client.send(protocolDto, wait);
               if (response[2] == CONSTANTE_FIND_TOTALIZADORES) {
                   break;
               }
               intentos++;
           }
       } catch (Exception e) {
           NeoService.setLog(e.getMessage());
       }
       return response;
   }

   private byte[] validarEstadoCara(ProtocolsDto protodto, byte[] response) {
       return response;
   }
}
