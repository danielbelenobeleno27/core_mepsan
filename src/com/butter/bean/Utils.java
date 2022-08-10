/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import com.core.app.NeoService;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import javax.swing.JTextField;

public class Utils {

    static ArrayList<String> datos = new ArrayList<>();
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static final String TXT_LEFT = "TXT_LEFT";
    public static final String TXT_CENTER = "TXT_CENTER";
    public static final String TXT_RIGHT = "TXT_RIGHT";
    public static final String TXT_BETWEEN = "TXT_BETWEEN";
    public static final String TXT_UNIFORM = "TXT_UNIFORM";

    public static final String PAD_LEFT = "PAD_LEFT";
    public static final String PAD_RIGHT = "PAD_RIGHT";

    public static final int PAGE_SIZE = 45;

    public static String align(String str, String align) {
        int length = str.length();
        String line;
        int leftSpaces = PAGE_SIZE - length;
        switch (align) {
            case TXT_CENTER:
                leftSpaces /= 2;
                if (PAGE_SIZE % 2 != 0) {
                    if (length % 2 != 0) {
                        line = fill(" ", leftSpaces).concat(str).concat(fill(" ", leftSpaces));
                    } else {
                        line = fill(" ", leftSpaces).concat(str).concat(fill(" ", leftSpaces + 1));
                    }
                } else {
                    if (length % 2 != 0) {
                        line = fill(" ", leftSpaces).concat(str).concat(fill(" ", leftSpaces + 1));
                    } else {
                        line = fill(" ", leftSpaces).concat(str).concat(fill(" ", leftSpaces));
                    }
                }
                break;
            case TXT_RIGHT:
                line = fill(" ", leftSpaces).concat(str);
                break;
            case TXT_LEFT:
                line = str;
                break;
            default:
                line = "";
                break;
        }
        return line;
    }

    public static String text_between(String str1, String str2) {
        int leftSpaces = PAGE_SIZE - (str1.length() + str2.length());
        return str1.concat(fill(" ", leftSpaces).concat(str2));
    }

    public static String fill(String str, int spaces) {
        if (str.length() <= spaces) {
            return str + fill(str, spaces - 1);
        } else {
            return "";
        }
    }

    public static String format_cols(String[] obj, int anchos[]) {
        String line = "";
        int leftSpace;
        int length;
        for (int i = 0; i < obj.length; i++) {
            length = obj[i].length();
            leftSpace = anchos[i] - length;
            if (i == 0) {
                line += obj[i].concat(fill(" ", leftSpace));
            } else {
                line += fill(" ", leftSpace).concat(obj[i]);
            }
        }
        return line;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static void printBytes(byte[] response) {
        String trama = "";
        for (byte x : response) {
            trama += (String.format("%02x", x).toUpperCase() + " ");
        }
        NeoService.setLog(trama);
    }

    public static char[] stringToByteArray(String str, int length_bytes) {
        String aux = "";
        for (int i = 0; i < length_bytes; i++) {
            aux += "0";
        }
        return (aux.substring(0, aux.length() - str.length()).concat(str)).toCharArray();
    }

    public static String byteArrayToString(byte[] array) {
        String aux = "";
        for (byte b : array) {
            aux += (String.format("%02x", b).toUpperCase());
        }
        return aux;
    }

    public static String byteArrayToString(byte[] array, int init, int end) {
        StringBuilder data = new StringBuilder();
        int y = 0;
        for (byte b : array) {
            if (y >= init && y <= end) {
                data.append(String.format("%02x", b).toUpperCase());
            }
            y++;
        }
        return data.toString();
    }

    public static byte[] byteListToByteArray(ArrayList<Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static String getFormat(byte b) {
        return (String.format("%02x", b).toUpperCase() + " ");
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isSolaris() {
        return (OS.contains("sunos"));
    }

    public static String getSystemDevice() {
        try {
            String OSName = System.getProperty("os.name").toLowerCase();
            if (OSName.contains("windows")) {
                return (getWindowsMotherboardSerialNumber());
            }
            if (OSName.contains("mac")) {
                return (getMacMotherBoardSerialNumber());
            } else {
                return (getLinuxMotherBoardSerialNumber());
            }
        } catch (Exception E) {
            NeoService.setLog("System MotherBoard Exp : " + E.getMessage());
            return null;
        }
    }

    public static String getSystemStore() {
        try {
            String OSName = System.getProperty("os.name").toLowerCase();;
            if (OSName.contains("windows")) {
                return (getLinuxStorage());
            } else {
                return (getLinuxStorage());
            }
        } catch (Exception E) {
            NeoService.setLog("System MotherBoard Exp : " + E.getMessage());
            return null;
        }
    }

    public static String getSystemNetwork() {
        try {
            String OSName = System.getProperty("os.name").toLowerCase();;
            if (OSName.contains("windows")) {
                return (getLinuxMac());
            } else {
                return (getLinuxMac());
            }
        } catch (Exception E) {
            NeoService.setLog("System MotherBoard Exp : " + E.getMessage());
            return null;
        }
    }

    private static String getWindowsMotherboardSerialNumber() {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs
                    = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_BaseBoard\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.SerialNumber \n"
                    + "    exit for  ' do the first cpu only! \n"
                    + "Next \n";

            fw.write(vbs);
            fw.close();

            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception E) {
            NeoService.setLog("Windows MotherBoard Exp : " + E.getMessage());
        }
        return result.trim();
    }

    private static String getLinuxMotherBoardSerialNumber() {
        String command = "cat /proc/cpuinfo";
        String result;
        String serial = "";
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = stdInput.readLine()) != null) {
                if (result.contains("Serial")) {
                    serial = result.split(":")[1].trim();
                }
            }
        } catch (IOException ex) {
            NeoService.setLog("Linux Motherboard Exp : " + ex.getMessage());
            serial = null;
        }
        return serial;
    }

    private static String getMacMotherBoardSerialNumber() {
        String command = "ioreg -l";
        String result;
        String serial = null;
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = stdInput.readLine()) != null) {
                if (serial == null && result.contains("IOPlatformSerialNumber")) {
                    serial = result.trim().split("=")[1].trim().replace('\"', ' ').trim();
                    NeoService.setLog(serial);
                }
            }
        } catch (IOException ex) {
            NeoService.setLog("Mac Motherboard Exp : " + ex.getMessage());
            serial = null;
        }
        return serial;
    }

    private static String getLinuxStorage() {
        //String command = "cat /proc/cpuinfo | tail -1 | cut -c11-100 && cat /sys/block/mmcblk0/device/cid && cat /sys/class/net/eth0/address";
        //String command2[] = {"cat /sys/block/mmcblk0/device/cid", "cat /sys/class/net/eth0/address", "cat /proc/cpuinfo | tail -1 | cut -c11-100"};
        String command = "cat /sys/block/mmcblk0/device/cid";
        String result;
        String serial = "";
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = stdInput.readLine()) != null) {
                serial = result.trim();
            }
        } catch (IOException ex) {
            NeoService.setLog("Linux Motherboard Exp : " + ex.getMessage());
            serial = null;
        }
        return serial;
    }

    private static String getLinuxMac() {
        String command = "cat /sys/class/net/eth0/address";
        String result;
        String mac = "";
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = stdInput.readLine()) != null) {
                mac = result.trim();
            }
        } catch (IOException ex) {
            NeoService.setLog("Linux Motherboard Exp : " + ex.getMessage());
            mac = null;
        }
        return mac;
    }

    public static String getLapsoTiempo(Date start, Date end) {
        long diffInSeconds = (end.getTime() - start.getTime()) / 1000;
        long diff[] = new long[]{0, 0, 0, 0};
        diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        diff[0] = (diffInSeconds = (diffInSeconds / 24));

        String time = "";
        if (diff[0] > 1) {
            time += diff[0] + " ds ";
        } else if (diff[0] == 1) {
            time += diff[0] + " d ";
        }

        if (diff[1] > 1) {
            time += diff[1] + " hs ";
        } else if (diff[1] == 1) {
            time += diff[1] + " h ";
        }

        if (diff[2] > 1) {
            time += diff[2] + " mins ";
        } else if (diff[2] == 1) {
            time += diff[2] + " min ";
        }

        if (diff[0] == 0 && diff[1] == 0 && diff[2] == 0) {
            time += diff[3] + " seg ";
        }

        return time;
        /*
        return (String.format(
                "%d day%s, %d hour%s, %d minute%s, %d second%s ago",
                diff[0],
                diff[0] > 1 ? "s" : "",
                diff[1],
                diff[1] > 1 ? "s" : "",
                diff[2],
                diff[2] > 1 ? "s" : "",
                diff[3],
                diff[3] > 1 ? "s" : ""));
         */
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static float potencia(float base, int exponente) {
        float result = 1;
        int cont = exponente;
        while (cont > 0) {
            result *= base;
            cont--;
        }
        return result;
    }

    public static void soloNumero(KeyEvent evt) {
        char[] p = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        int b = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] == evt.getKeyChar()) {
                b = 1;
            }
        }
        if (b == 0) {
            evt.consume();
        }
    }

    public static void decimalFomatTextField(JTextField jTextField1) {

        DecimalFormat f = new DecimalFormat("###,###.##");
        if (jTextField1.getText().length() > 2) {
            int value = Integer.parseInt(jTextField1.getText().toString().replace(".", ""));
            jTextField1.setText(String.valueOf(f.format(value)));
        }
        if (jTextField1.getText().length() == 0) {
            jTextField1.setText(String.valueOf(f.format(0.0)));
        }
    }

    public static String str_pad(String input, int length, String pad, String sense) {
        if (input == null) {
            NeoService.setLog("");
        }
        int resto_pad = length - input.length();
        String padded = "";

        if (resto_pad <= 0) {
            return input;
        }

        if (sense.equals("STR_PAD_RIGHT")) {
            padded = input;
            padded += _fill_string(pad, resto_pad);
        } else if (sense.equals("STR_PAD_LEFT")) {
            padded = _fill_string(pad, resto_pad);
            padded += input;
        } else // STR_PAD_BOTH
        {
            int pad_left = (int) Math.ceil(resto_pad / 2);
            int pad_right = resto_pad - pad_left;

            padded = _fill_string(pad, pad_left);
            padded += input;
            padded += _fill_string(pad, pad_right);
        }
        return padded;
    }

    protected static String _fill_string(String pad, int resto) {
        boolean first = true;
        String padded = "";

        if (resto >= pad.length()) {
            for (int i = resto; i >= 0; i = i - pad.length()) {
                if (i >= pad.length()) {
                    if (first) {
                        padded = pad;
                    } else {
                        padded += pad;
                    }
                } else {
                    if (first) {
                        padded = pad.substring(0, i);
                    } else {
                        padded += pad.substring(0, i);
                    }
                }
                first = false;
            }
        } else {
            padded = pad.substring(0, resto);
        }
        return padded;
    }

    public static Date stringToDate(String asString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Butter.FORMAT_DATETIME_SQL);
            return sdf.parse(asString);
        } catch (Exception a) {
            return null;
        }

    }

    /**
     * Se utiliza para optener la cantidad correcta aplicandole el facor
     *
     * @param cantidad
     * @param factor
     * @return
     */
    public static long calculeCantidad(long cantidad, int factor) {
        long result;
        if (factor < 0) {
            result = cantidad / (factor * -1);
        } else {
            result = cantidad * factor;
        }
        return result;
    }

    public static double calculeCantidadDouble(long cantidad, int factor) {
        double result;
        if (factor < 0) {
            result = (double) cantidad / (double) (factor * -1);
        } else {
            result = (double) cantidad * (double) factor;
        }
        return result;
    }

    /**
     * Se utiliza para optener la cantidad correcta con el factor inverso
     *
     * @param cantidad
     * @param factor
     * @return
     */
    public static long calculeCantidadInversa(long cantidad, int factor) {
        long result;
        if (factor < 0) {
            result = cantidad * (factor * -1);
        } else {
            result = cantidad / factor;
        }
        return result;
    }

    public static double calculeCantidadInversaDouble(long cantidad, int factor) {
        double result;
        if (factor < 0) {
            result = (double) cantidad * (double) (factor * -1);
        } else {
            result = (double) cantidad / (double) factor;
        }
        return result;
    }

    public static float calculeCantidadInversaFloat(float cantidad, int factor) {
        float result;
        if (factor < 0) {
            result = (float) cantidad * (float) (factor * -1);
        } else {
            result = (float) cantidad / (float) factor;
        }
        return result;
    }

    public static void validarUuid(HttpExchange t, String identificadorProceso, String name) {
        try {
            UUID uuid = UUID.fromString(identificadorProceso);
        } catch (IllegalArgumentException exception) {
            ErrorResponse error = new ErrorResponse();
            error.setTipo(ErrorResponse.ERROR_NEGOCIO);
            error.setCodigo(ErrorResponse.ERROR_40021_ID);
            error.setMensaje(ErrorResponse.ERROR_40021_DESC_PARA + name);
            error.setFechaProceso(new Date().toString());
            responseError(t, error);
        }
    }

    public static void validarString(HttpExchange t, String text, String name) {
        try {
            if (null == text || text.equals("")) {
                throw new IllegalArgumentException();
            }
            //ivandariosanchezf
        } catch (IllegalArgumentException exception) {
            ErrorResponse error = new ErrorResponse();
            error.setTipo(ErrorResponse.ERROR_NEGOCIO);
            error.setCodigo(ErrorResponse.ERROR_40021_ID);
            error.setMensaje(ErrorResponse.ERROR_40021_DESC_PARA + name);
            error.setFechaProceso(new Date().toString());
            responseError(t, error);
        }
    }

    public static void validarInt(HttpExchange t, String text, String name) {
        try {
            int num = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            ErrorResponse error = new ErrorResponse();
            error.setTipo(ErrorResponse.ERROR_NEGOCIO);
            error.setCodigo(ErrorResponse.ERROR_40021_ID);
            error.setMensaje(ErrorResponse.ERROR_40021_DESC_PARA + name);
            error.setFechaProceso(new Date().toString());
            responseError(t, error);
        }
    }

    public static void validarDouble(HttpExchange t, String text, String name) {
        try {
            double num = Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            ErrorResponse error = new ErrorResponse();
            error.setTipo(ErrorResponse.ERROR_NEGOCIO);
            error.setCodigo(ErrorResponse.ERROR_40021_ID);
            error.setMensaje(ErrorResponse.ERROR_40021_DESC_PARA + name);
            error.setFechaProceso(new Date().toString());
            responseError(t, error);
        }
    }

    public static void validarInt(HttpExchange t, String text, String name, int minimo, int maximo) {
        try {
            int num = Integer.parseInt(text);
            if (num < minimo || num > maximo) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException exception) {
            ErrorResponse error = new ErrorResponse();
            error.setTipo(ErrorResponse.ERROR_NEGOCIO);
            error.setCodigo(ErrorResponse.ERROR_40021_ID);
            error.setMensaje(ErrorResponse.ERROR_40021_DESC_PARA + name);
            error.setFechaProceso(new Date().toString());
            responseError(t, error);
        } catch (IllegalArgumentException exception) {
            ErrorResponse error = new ErrorResponse();
            error.setTipo(ErrorResponse.ERROR_NEGOCIO);
            error.setCodigo(ErrorResponse.ERROR_40021_ID);
            error.setMensaje(ErrorResponse.ERROR_40021_DESC_PARA + name);
            error.setFechaProceso(new Date().toString());
            responseError(t, error);
        }

    }

    public static boolean validarDate(String text) {
        boolean result = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Butter.FORMAT_FULL_DATE_ISO);
            //NeoService.setLog(sdf.format(new Date()));
            Date fecha = sdf.parse(text);
            result = true;
        } catch (ParseException exception) {
        }
        return result;
    }

    public static boolean validarUuid(String text) {
        boolean result = false;
        try {
            UUID uuid = UUID.fromString(text);
            result = true;
        } catch (IllegalArgumentException exception) {
        }
        return result;
    }

    public static void responseError(HttpExchange t, ErrorResponse error) {
        try {

            Headers respHeaders = t.getResponseHeaders();
            respHeaders.add("content-type", "application/json");
            if (error.getStatusCode() == null || error.getStatusCode() == 0) {
                t.sendResponseHeaders(400, error.toString().getBytes("UTF-8").length);
            } else {
                t.sendResponseHeaders(error.getStatusCode(), error.toString().getBytes("UTF-8").length);
            }
            try (OutputStream os = t.getResponseBody()) {
                os.write(error.toString().getBytes("UTF-8"));
                os.flush();
            }

        } catch (IOException ex) {
            NeoService.setLog(ex.getMessage());

        }

    }

    public static void responseStatus(HttpExchange t, int status, JsonObject data) {
        try {

            Headers respHeaders = t.getResponseHeaders();
            respHeaders.add("content-type", "application/json");
            t.sendResponseHeaders(status, data.toString().getBytes("UTF-8").length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(data.toString().getBytes("UTF-8"));
                os.flush();
            }

        } catch (IOException ex) {
            NeoService.setLog(ex.getMessage());

        }

    }

    public static void responseError(HttpExchange t, int status, JsonObject error) {
        try {

            Headers respHeaders = t.getResponseHeaders();
            respHeaders.add("content-type", "application/json");
            t.sendResponseHeaders(status, error.toString().getBytes("UTF-8").length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(error.toString().getBytes("UTF-8"));
                os.flush();
            }

        } catch (IOException ex) {
            NeoService.setLog(ex.getMessage());

        }

    }

    public static void printArray(String title, byte[] result) {
        String trama = "";
        for (int d = 0; d < result.length; d++) {
            trama += (String.format("%02x", result[d]).toUpperCase() + " ");
        }
        NeoService.setLog(title + " " + trama);
    }

    public static void printArray(String title, ArrayList<Byte> result) {
        String trama = "";
        for (Byte byte1 : result) {
            trama += (String.format("%02x", byte1).toUpperCase() + " ");
        }
        NeoService.setLog(title + " " + trama);
    }

}
