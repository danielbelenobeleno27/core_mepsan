/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.awt.Color;

/**
 *
 * @author ASUS-PC
 */
public class Butter {

    static int VERSION_CODE;
    static String VERSION_NAME;
    public static boolean SYNC;

    public static final int ULTIMA_VENTA_MINUTOS = 3;
    public static final String PREFERENCE_IP_IMPRESORA = "impresora";
    public static final String PREFERENCE_SERIAL_PORT = "puertoserial";
    public static final String IP_IMPRESORA = "192.168.0.212";
    public static final int PORT_IMPRESORA = 9100;

    public static final String ACTIVE = "A";
    public static final String CONSECUTIVO_ESTADO_ACTIVO = "A";
    public static final String CONSECUTIVO_ESTADO_USO = "U";
    public static final String CONSECUTIVO_ESTADO_VENCIDO = "V";

    public static final String MODAL_CONSECUTIVOS = "modal_consecutivos";
    public static final String MODAL_REGISTRO_TAG = "modal_registrotag";
    public static final String MODAL_BODEGAS_LISTA = "modal_bodegas";
    public static final String MODAL_BODEGAS_PRODUCTOS = "modal_bodegas_productos";
    public static final String MODAL_TRANSLADOS = "modal_traslados";

    public static final String SERVER_DATABASE_HOST = "localhost";
    public static final String SERVER_DATABASE_PORT = "5432";
    public static final String SERVER_DATABASE_NAME = "mortadela";
    public static final String SERVER_DATABASE_USER = "neoline";
    public static final String SERVER_DATABASE_PASSWORD = "$Neo122018.";

    public static final String SIMBOLS_PRICE = "$";
    public static final String SIMBOLS_PERCENTAGE = "%";
    public static final String FORMAT_MONEY = "###,##0";
    public static final String FORMAT_MONEY_WITHOUT_ZERO = "###,###";

    public static final String FORMAT_DATE = "dd-MM-yyyy";
    public static final String FORMAT_DATE_SQL = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME_SQL = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATETIME_SQL_SSS = "dd HH:mm:ss SSS";
    public static final String FORMAT_DATETIME_24 = "dd-MM-yyyy HH:mm:ss";
    public static final String FORMAT_DATETIME_AM = "dd-MM-yyyy hh:mm:ss a";

    public static final String DATETIME_AM = "dd-MM-yyyy hh:mm:ss a";
    public static final String FORMAT_TIME_AM = "hh:mm:ss a";
    public static final String FORMAT_TIME_24 = "HH:mm";
    public static final String FORMAT_TIME_BASIC_AM = "hh:mm a";
    //2020-04-14 10:04:09.001
    public static final String FORMAT_FULL_DATE = "yyyy-MM-dd HH:mm:ss.SSS";
    //2001-07-04T12:08:56.235-0700
    public static final String FORMAT_FULL_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String FORMAT_PROCESS = "yyyyMMddHHmmssSSS";
    public static final String FORMAT_BASIC_DATETIME_AM = "dd-MM-yyyy hh:mm a";

    public static final String URL_IMAGEN_FONDO = "/com/butter/view/resources/fondo.png";
    public static final String URL_IMAGEN_FONDO_PEDIDO = "/com/butter/view/resources/fnd_pedido.png";

    public final static String HOST_END_POINT = "repositorio.neoline.co";
    public final static String END_POINT_VENTA = "http://" + HOST_END_POINT + ":6005/api/venta";
    public final static String END_POINT_CONSECUTIVOS = "http://" + HOST_END_POINT + ":6007/api/consecutivo/empresa";

    public final static String SECURE_END_POINT_EQUIPO = "https://" + HOST_END_POINT + ":7002/api/equipo";
    public final static String SECURE_END_POINT_EMPRESA = "https://" + HOST_END_POINT + ":7003/api/empresa/info";

    public final static String SECURE_END_POINT_EMPLEADOS_JORNADAS_FIN = "https://" + HOST_END_POINT + ":7004/api/jornada/fin";
    public final static String SECURE_END_POINT_EMPLEADOS_JORNADAS_INVENTARIO_INICIO = "https://" + HOST_END_POINT + ":7004/api/jornada/inventario";

    public final static String SECURE_END_POINT_EMPLEADOS_IDENTIFICADORES = "https://" + HOST_END_POINT + ":7004/api/identificador";

    public final static String SECURE_END_POINT_BODEGA = "https://" + HOST_END_POINT + ":7006/api/bodegas";
    public final static String SECURE_END_POINT_PERSONAL = "https://" + HOST_END_POINT + ":7004/api/persona";

    public final static String SECURE_END_POINT_PRODUCTOS_POS_ACUERDOS = "https://" + HOST_END_POINT + ":7005/api/producto/pos/acuerdos";

    public final static String SECURE_END_POINT_PRODUCTOS_PADRES = "https://" + HOST_END_POINT + ":7005/api/producto/pos/productos_ingredientes";
    public final static String SECURE_END_POINT_PRODUCTOS_DETALLES = "https://" + HOST_END_POINT + ":7005/api/producto/pos";
    public final static String SECURE_END_POINT_PRODUCTOS_DETALLES_FULL = "https://" + HOST_END_POINT + ":7005/api/producto/rest/pos/full";

    public final static String SECURE_END_POINT_PRODUCTOS_EMPRESAS = "https://" + HOST_END_POINT + ":7005/api/producto/productos_empresas";
    public final static String SECURE_END_POINT_BODEGAS_PRODUCTOS = "https://" + HOST_END_POINT + ":7005/api/producto/bodegas_productos";
    public final static String SECURE_END_POINT_CATEGORIAS = "https://" + HOST_END_POINT + ":7001/api/grupos/all";
    public final static String SECURE_END_POINT_MEDIOS_PAGOS = "https://" + HOST_END_POINT + ":7001/api/MediosPagos/empresas/all";

    public final static String SECURE_END_POINT_MOVIMENTO_VENTA = "https://" + HOST_END_POINT + ":7008/api/ventas";
    public final static String SECURE_END_POINT_MOVIMENTO = "https://" + HOST_END_POINT + ":7008/api/movimiento";

    public final static String SECURE_END_POINT_CONSECUTIVOS = "https://" + HOST_END_POINT + ":7001/api/consecutivo/pos/cambiar_estado";

    public final static String SECURE_END_POINT_JORNADAS = "http://localhost:8019/api/recibirLecturas";

    public final static String POST = "POST";
    public final static String GET = "GET";
    public final static String PUT = "PUT";

    public final static String TX_TIPO_JORNADA_CIERRE = "CIERRE_JORNADA";

    public final static int TIME_TO_REPORT = 10;

    public static boolean ENABLE_DEBUG = true;
    public static boolean DISABLE_DEBUG = false;

    public static boolean ENABLE_HTTPS = true;
    public static boolean DISABLE_HTTPS = false;

    public static boolean INICIAR_JORNADA = true;
    public static boolean FINALIZA_JORNADA = false;
    public static int MOVIMIENTO_TIPO_VENTA = 9;

    public static final Color COLOR_RED = Color.decode("#FF2121");
    public static final Color COLOR_GREEN = Color.decode("#20AD00");

    public static int CONTACTO_TIPO_CORREO = 1;
    public static int CONTACTO_TIPO_TELEFONO = 2;
    public static int CONTACTO_TIPO_DIRECCION = 3;
    public static int CONTACTO_TIPO_SITIO_WEB = 4;
    public static int CONTACTO_TIPO_SOCIAL = 5;

    public static String SIN_GRUPO = "SIN CLASIFICACION";
    public static String CATEGORIA = "categoria";
    public static String PRODUCTOS = "productos";

    public static int ACCESO_REGISTRO_TAG = 32;
    public static int ACCESO_TRASLADO_BODEGA = 33;
    public static int ACCESO_REIMPRIMIR_RECIBOS = 34;
    public static String FILE_PROPERTIES = "config.properties";

}
