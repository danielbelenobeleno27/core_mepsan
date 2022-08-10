package com.core.app.protocols;

import com.butter.bean.EquipoDao;
import com.butter.bean.MqttNotify;
import com.butter.bean.Utils;
import com.core.app.NeoService;
import com.core.app.server.NotificacionSocket;
import com.core.commons.cliente.ClientSerial;
import com.core.commons.cliente.ClientTcp;
import com.core.commons.cliente.Cliente;
import com.core.database.impl.SurtidorDao;
import com.core.print.services.PrinterFacade;
import com.google.gson.JsonObject;
import com.neo.app.bean.AConstant;
import com.neo.app.bean.BaseControllerProtocols;
import com.neo.app.bean.Cara;
import com.neo.app.bean.Surtidor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MepsanController extends BaseControllerProtocols{
    
    public MepsanProtocol protocolo;
    SurtidorDao sdao = new SurtidorDao();
    EquipoDao edao = new EquipoDao();
    
    private static final TreeMap<Byte, String> ESTADOS_SURTIDOR = new TreeMap<>();
    private static final TreeMap<Byte, String> ESTADOS_MANGUERA = new TreeMap<>();
   
    //FINALIZA VALORES DE LOS ESTADOS
    public static final int CONSTANTE_FIND_CARA = 0x0F;
    public static final int CONSTANTE_FIND_REPUESTA = 0xF0;

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
    MqttNotify notificacion;

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
        
        estado = respuesta.get(3) % CONSTANTE_FIND_REPUESTA;
        
        byte[] array = Utils.byteListToByteArray(respuesta);
        
        String str = Utils.byteArrayToString(array);
        
        NeoService.setLog(str);
        
    }

}
