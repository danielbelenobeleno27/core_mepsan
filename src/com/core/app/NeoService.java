package com.core.app;

import com.butter.bean.Butter;
import com.core.app.bean.socket.SocketExternal;
import com.butter.bean.CredencialBean;
import com.butter.bean.EquipoDao;
import com.butter.bean.Main;
import com.butter.bean.ModuleViewInterface;
import com.butter.bean.Utils;
import com.core.app.bean.service.ControllerSync;
import com.core.app.protocols.DispositivoController;
import com.core.app.protocols.Dispositivos;
import com.core.app.protocols.MepsanController;
import com.fazecast.jSerialComm.SerialPort;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.BaseControllerProtocols;
import com.core.app.server.ServerComandoWS;
import com.core.database.DAOException;
import com.core.database.Postgrest;
import com.core.database.impl.SurtidorDao;
import com.neo.app.bean.Cara;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * @author ASUS-PC
 */
public class NeoService {

    public static final String APLICATION_FULL_NAME = "Lazo Express Core";
    public static final String APLICATION_NAME = "lazo_express_core";
    public static final int VERSION_CODE = 1;
    public static boolean SURTIDOR_ERROR = false;
    public static final String VERSION_NAME = "Beta 3 C" + VERSION_CODE;

    public final static int MINIMO_CARA = 1;
    public final static int MAXIMO_CARA = 30;

    public final static int MINIMO_MANGUERA = 1;
    public final static int MAXIMO_MANGUERA = 50;

    public static final int CONTROLADOR_IP = 1;
    public static final int CONTROLADOR_RS_232 = 2;
    public static int CONTROLADOR;

    public static boolean VALIDA_PREDETERMINACION_PORFAMILIA = false;
    public static boolean PUBLICAR_MQTT_ESTADO_SURTIDOR = false;
    public static boolean RECIBIR_NOTIFICACIONES_MQTT = false;
    public static boolean PUBLICAR_SOCKET_ESTADO_SURTIDOR = false;

    public static boolean DEBUG_NOTIFICACION_INTERNA = true;

    public static TreeMap<Long, Integer> HOMOLOGACION_CONEXION_CARA = new TreeMap<Long, Integer>();
    static SimpleDateFormat sdfMS = new SimpleDateFormat(Butter.FORMAT_DATETIME_SQL_SSS);

    public static final int PROTOCOLO_PUMCONTROL = 1;
    public static final int PROTOCOLO_MEPSAN = 2;

    public final static boolean LOCALHOUSE = false;

    public static String LECTOR_RFID_COM = "ttyS0";
    public static String LECTOR_RFID2 = "rfid2_puerto";
    public static String SURTIDOR_COMMPORT;
    public static int BAUDRATE = 9600;
    public static final int DATABIT = 8;
    public static final int STOPBIT = 1;
    public static final int PARITY = SerialPort.ODD_PARITY;

    /* TIEMPO DE TIMEOUT */
    public final static int TIMEOUT_SICOM = 1000;
    public final static int TIMEOUT_CONEXION_SOCKET = 1000;

    public static final String CONTROLLER_HAST = "00:00:00:00:01";

    public static int PORT_SERVER_WS_API = 8000;
    public static final int PORT_SOCKET_COMANDOS = 8001;
    public static final int TO_NEOAPP_EE = 7002;

    public static int PUERTO_POS_LAZO = 10000;
    public static int[] PORT_IBUTTON = new int[6];

    public static final boolean TIENE_IMPRESORA = true;
    public static String IP_IMPRESORA = "10.35.84.46";

    public static int PORT_IMPRESORA = 9100;
    public static int TIPO_IMPRESORA = 1; //1:TCP(DEFAULT), 2:SERIAL	
    public static final int TIPO_IMPRESORA_TCP = 1;
    public static final int TIPO_IMPRESORA_SISTEMA = 2;

    public static final String IP_PANTALLA = "192.168.0.204";
    public static final int PORT_PANTALLA = 5000;

    public final static int TOTALIZADOR_ORIGEN_BD = 1;
    public static int TOTALIZADOR_ORIGEN = 0;

    public static Postgrest db;
    public static SurtidorDao sutidao;
    public static boolean SYNC = false;

    public static final String FACTOR_VOLUMEN = "fvo";
    public static final String FACTOR_PRECIO = "fpr";
    public static final String FACTOR_IMPORTE = "fim";
    public static final String FACTOR_INVENTARIO = "fin";

    public final static String UNIDAD_MEDIDA = "GL";

    public final static int SURTIDORES_PUBLIC_ESTADO_ID_ESPERA = 100;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_AUTORIZACION = 101;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_AUTORIZADO = 102;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_DESPACHO = 103;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_DESPACHO2 = 104;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_FINALIZADA_PEOT = 105;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_FINALIZADA_FEOT = 106;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_PARADA = 107;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_ERROR = 108;
    public final static int SURTIDORES_PUBLIC_ESTADO_ID_DETENIDA = 109;

    public final static String SURTIDORES_PUBLIC_ESTADO_DS_ESPERA = "idle";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_AUTORIZACION = "'authorization_in_progresss'";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_AUTORIZADO = "'authorization_in_progresss'";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_DESPACHO = "fueling";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_DESPACHO2 = "fueling";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_FINALIZADA_PEOT = "terminated";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_FINALIZADA_FEOT = "terminated";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_PARADA = "paused";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_ERROR = "error";
    public final static String SURTIDORES_PUBLIC_ESTADO_DS_DETENIDA = "error";

    public static final int SUB_COMANDO_NEOAPP_TAG_REFID = 1;
    public static final int SUB_COMANDO_NEOAPP_TAG_IBUTTON = 2;
    public static final int SUB_COMANDO_NEOAPP_CONEXION_ONLINE = 3;

    public static TreeMap<Integer, Surtidor> surtidores = new TreeMap<>();

    public static TreeMap<String, Object> sharedPreference = new TreeMap<>();

    public static ModuleViewInterface module;

    public static TreeMap<Long, DispositivoController> dispositivos;
    static ControllerSync controlSync;

    public static final String COMMAND_APP_STATUS = "status";
    public static final String COMMAND_APP_AUTHORIZATION = "authorization";
    public static final String PREFERENCE_AUTORIZACION = "autorizaciontoken";

    public static AtomicLong PERSONA_AUTORIZA_ID = new AtomicLong(0);
    public static TreeMap<Integer, Long> REGISTRO_PERSONA = new TreeMap<>();
    public static TreeMap<Integer, Date> AUTORIZACION = new TreeMap<>();
    public static TreeMap<Integer, String> REGISTRO_VEHICULO = new TreeMap<>();

    public static final int METODO_AUTORIZACION_SIN_TAG = 1;
    public static final int METODO_AUTORIZACION_TAG_GLOBAL = 2;
    public static final int METODO_AUTORIZACION_TAG_CARA = 3;

    public static int METODO_AUTORIZACION_TAG = METODO_AUTORIZACION_TAG_GLOBAL;

    public static String NUMERO_SURTIDOR = "0";
    public static int DATABASE_VERSION = 0;
    public static String DATABASE_LOCAL_HOST = "localhost";
    public final static String DATABASE_LOCAL_PORT = "5432";
    public final static String DATABASE_LOCAL_NAME = "lazoexpresscore";
    public final static String DATABASE_LOCAL_USER = "pilotico";
    public final static String DATABASE_LOCAL_PASSWORD = "$2y$12$UWpxiZi3UaF7ZyKeySCpB.5Z5FfRtAAkgYuQz.m4qnLUFR7CmTOu";

    public static final int AUTORIZACION_PROVEEDOR_RUMBO = 3;
    public static int TIEMPO_BORRAR_AUTORIZACION = 30;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {

        try {
            DATABASE_LOCAL_HOST = args[0];
        } catch (Exception e) {
            DATABASE_LOCAL_HOST = "localhost";
        }

        try {
            NUMERO_SURTIDOR = args[1];
        } catch (Exception e) {
            NUMERO_SURTIDOR = "*";
        }

        try {
            PORT_SERVER_WS_API = Integer.parseInt(args[2]);
        } catch (Exception e) {
            PORT_SERVER_WS_API = 8000;
        }

        db = new Postgrest();
        sutidao = new SurtidorDao();

        NeoService.setLog("***************************************************");
        NeoService.setLog("[INFO (main)] VERSION_NAME -> " + VERSION_NAME);
        NeoService.setLog("[INFO (main)] VERSION_CODE -> " + VERSION_CODE);
        NeoService.setLog("***************************************************");

        EquipoDao dao = new EquipoDao();
        dao.actualizarBasededatos();

        NeoService.setLog("[INFO (main)] BASE DE DATOS -> " + NeoService.DATABASE_LOCAL_HOST);

        NeoService.DATABASE_VERSION = dao.getParametroInt("version_coredb");
        NeoService.setLog("[INFO (main)] VERSION BASE DE DATOS -> " + NeoService.DATABASE_VERSION);

        String ibuttomMultiple = dao.getParametroString("ibutton_puerto");
        int x = 0;
        for (int j : NeoService.PORT_IBUTTON) {
            NeoService.PORT_IBUTTON[x] = 0;
            x++;
        }
        if (ibuttomMultiple.contains(";")) {
            String[] arr = ibuttomMultiple.split(";");
            int i = 0;
            for (String ib4 : arr) {
                NeoService.setLog("IB4:" + ib4);
                try {
                    NeoService.PORT_IBUTTON[i] = Integer.parseInt(ib4);
                    i++;
                } catch (Exception ex) {
                    Logger.getLogger(NeoService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            try {
                NeoService.PORT_IBUTTON[0] = Integer.parseInt(ibuttomMultiple);
            } catch (Exception ex) {
                Logger.getLogger(NeoService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int j : NeoService.PORT_IBUTTON) {
            NeoService.setLog("->IB4:" + j);
        }
        NeoService.setLog("[INFO (main)] PORT_IBUTTON -> " + ibuttomMultiple);

        NeoService.CONTROLADOR = dao.getParametroInt("controlador");
        NeoService.setLog("[INFO (main)] CONTROLADOR -> " + NeoService.CONTROLADOR + " (1:IP, 2:RS232)");

        NeoService.DEBUG_NOTIFICACION_INTERNA = dao.getParametroBoolean("debug_notificacion_interna");
        NeoService.setLog("[INFO (main)] DEBUG_NOTIFICACION_INTERNA -> " + NeoService.DEBUG_NOTIFICACION_INTERNA);

        NeoService.PUBLICAR_MQTT_ESTADO_SURTIDOR = dao.getParametroBoolean("mqtt_activar");
        NeoService.setLog("[INFO (main)] PUBLICAR_MQTT_ESTADO_SURTIDOR -> " + NeoService.PUBLICAR_MQTT_ESTADO_SURTIDOR);

        NeoService.PUBLICAR_SOCKET_ESTADO_SURTIDOR = dao.getParametroBoolean("socket_externo_activar");
        NeoService.setLog("[INFO (main)] PUBLICAR_SOCKET_ESTADO_SURTIDOR -> " + NeoService.PUBLICAR_SOCKET_ESTADO_SURTIDOR);

        NeoService.RECIBIR_NOTIFICACIONES_MQTT = dao.getParametroBoolean("push_mqtt_activar");
        NeoService.setLog("[INFO (main)] RECIBIR_NOTIFICACIONES_MQTT -> " + NeoService.RECIBIR_NOTIFICACIONES_MQTT);

        NeoService.LECTOR_RFID_COM = dao.getParametroString("lector_rfid");
        NeoService.setLog("[INFO (main)] LECTOR_RFID_COM -> " + NeoService.LECTOR_RFID_COM);

        NeoService.IP_IMPRESORA = dao.getParametroString("impresora");
        NeoService.setLog("[INFO (main)] IP_IMPRESORA -> " + NeoService.IP_IMPRESORA);

        NeoService.TIPO_IMPRESORA = dao.getParametroInt("tipo_impresora");
        NeoService.setLog("[INFO (main)] TIPO_IMPRESORA -> " + NeoService.TIPO_IMPRESORA + " (1:TCP, 2:SISTEMA)");

        NeoService.METODO_AUTORIZACION_TAG = dao.getParametroInt("tipo_autorizacion");
        NeoService.setLog("[INFO (main)] METODO_AUTORIZACION_TAG -> " + NeoService.METODO_AUTORIZACION_TAG + " (1:SIN_TAG, 2:TAG_GLOBAL, 3:TAG_CARA)");

        NeoService.LECTOR_RFID2 = dao.getParametroString("rfid2_puerto");
        NeoService.setLog("[INFO (main)] LECTOR_RFID2 -> " + NeoService.LECTOR_RFID2 + " lector RFID Version 2");

        int temp = dao.getParametroInt("tiempo_borrar_autorizacion");
        if (temp > 0) {
            NeoService.TIEMPO_BORRAR_AUTORIZACION = temp;
        }
        NeoService.setLog("[INFO (main)] TIEMPO_BORRAR_AUTORIZACION -> " + NeoService.TIEMPO_BORRAR_AUTORIZACION + " SEG");

        NeoService.TOTALIZADOR_ORIGEN = dao.getParametroInt("totalizador_origen");
        NeoService.setLog("[INFO (main)] TOTALIZADOR_ORIGEN -> " + NeoService.TOTALIZADOR_ORIGEN + " (1=Origen BaseDatos, default=Surtidor)");

        CredencialBean credencial = new CredencialBean();
        credencial.setSerial(Utils.getSystemDevice());
        credencial.setAlmacenamiento(Utils.getSystemStore());
        credencial.setMac(Utils.getSystemNetwork());
        Main.credencial = credencial;
        try {
            Main.credencial = dao.findToken(Main.credencial);
            if (Main.credencial.isAutorizado() || true) {
                NeoService neoService = new NeoService();
                neoService.cargarIcono();
                neoService.init();

                controlSync = new ControllerSync();
                controlSync.setDaemon(true);
                controlSync.setPriority(Thread.MAX_PRIORITY);
                controlSync.start();

                if (PUBLICAR_SOCKET_ESTADO_SURTIDOR) {
                    SocketExternal socketserver = new SocketExternal();
                    socketserver.start();
                }
            } else {
                NeoService.setLog("Licencia sin validar");
            }
        } catch (DAOException ex) {
            Logger.getLogger(NeoService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init() {

        try {
            db.getConn();
            surtidores = sutidao.getSurtidor(NUMERO_SURTIDOR);
        } catch (DAOException a) {
            NeoService.setLog(a.getMessage());
        }

        try {
            // DISPOSITIVOS RFID v2
            if (LECTOR_RFID2 != null && !LECTOR_RFID2.trim().equals("") && !LECTOR_RFID2.trim().equals("0")) {
                DispositivoController.RfidV2(LECTOR_RFID2);
            }

            //DISPOSITIVOS RFID v1
            DispositivoController.RFid(LECTOR_RFID_COM);

            // IBUTTON TCP
            for (int i : NeoService.PORT_IBUTTON) {
                if (i > 0) {
                    Dispositivos.IB4Tcp(i);
                }
            }

            try {
                //CUALQUIER DISPOSITIVO
                dispositivos = sutidao.getDispositivos();
            } catch (Exception e) {
                NeoService.setLog("[INFO (main)] ERROR AL OBTENER DISPOSITIVOS -> " + e.getMessage());
            }

            // SERVIDOR HTTP
            NeoService.setLog("SERVER API WS " + PORT_SERVER_WS_API);
            ServerComandoWS serverapi = new ServerComandoWS();
            serverapi.setPriority(Thread.MAX_PRIORITY);
            serverapi.setDaemon(true);
            serverapi.start();
            NeoService.setLog("[INFO (main)] CANTIDAD SURTIDORES -> " + surtidores.size());

            for (Map.Entry<Integer, Surtidor> entry1 : surtidores.entrySet()) {
                Integer key = entry1.getKey();
                Surtidor value = entry1.getValue();

                if (value.getControlador() == 1) {
                    NeoService.setLog("[INFO (main)] SURTIDOR " + key + " -> " + " [TCP    ]" + value.getIp() + ":" + value.getPort());
                } else {
                    NeoService.setLog("[INFO (main)] SURTIDOR " + key + " -> " + " [SERIAL ]" + value.getIp());
                }
            }

            /* Multi Surtidores :) */
            for (Map.Entry<Integer, Surtidor> entry : surtidores.entrySet()) {
                Surtidor surtidor = entry.getValue();
                surtidor.setProtocolo(PROTOCOLO_MEPSAN);

                MepsanController control = new MepsanController(surtidor);
                control.start();

                surtidor.setControl(control);
            }

        } catch (NumberFormatException e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public static Connection obtenerConexion() {
        return db.getConn();
    }

    public static void pause(int i) {
        try {
            Thread.sleep(1000 * i);
        } catch (InterruptedException ex) {
            Logger.getLogger(NeoService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cargarIcono() {

        TrayIcon trayIcon;
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            ImageIcon image = new javax.swing.ImageIcon(getClass().getResource("/com/core/view/resources/fuel.png"));

            ActionListener listener = (ActionEvent e) -> {
                System.exit(0);
            };
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem2 = new MenuItem();
            defaultItem2.addActionListener(listener);
            defaultItem2.setLabel("Salir");
            popup.add(defaultItem2);

            trayIcon = new TrayIcon(image.getImage(), APLICATION_FULL_NAME + " v" + VERSION_NAME, popup);
            trayIcon.addActionListener(listener);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                NeoService.setLog(e.getMessage());
            }
        }
    }

    public static void millisecondsPause(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException ex) {
            Logger.getLogger(NeoService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int HomologacionCara(int cara) {
        try {
            int caraB = (int) NeoService.HOMOLOGACION_CONEXION_CARA.get((long) cara);
            return caraB;
        } catch (Exception ex) {
            NeoService.setLog("ex: " + ex.getMessage());
            NeoService.setLog(NeoService.ANSI_RED + "HOMOLOGACION CARA ERR -> : " + cara + NeoService.ANSI_RESET);
            Logger.getLogger(NeoService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cara;
    }

    public static void setLog(String txt) {
        System.out.println(sdfMS.format(new Date()) + " " + txt);
    }
}
