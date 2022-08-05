/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.bean.socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.core.app.NeoService;
import com.neo.app.bean.AConstant;
import com.neo.app.bean.VentaDetalles;
import com.core.database.impl.SurtidorDao;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 *
 *
 *
 * @author Jorge V
 *
 */
public class SocketExternal extends Thread {

    private Socket socket;
    private BufferedReader bufferDeEntrada = null;
    private DataOutputStream bufferDeSalida = null;
    Scanner teclado = new Scanner(System.in);
    final String COMANDO_TERMINACION = "}";

    public SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_DATETIME_FULL);

    public void levantarConexion(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto);
            mostrarTexto("Conectado a :" + socket.getInetAddress().getHostName());
        } catch (Exception e) {
            mostrarTexto("Excepción al levantar conexión: " + e.getMessage());
        }
    }

    public static void mostrarTexto(String s) {
        NeoService.setLog(s);
    }

    public void abrirFlujos() {
        try {
            bufferDeEntrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en la apertura de flujos");
        }
    }

    public void enviar(String s) {
        try {
            bufferDeSalida.write(s.getBytes());
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("IOException on enviar");
        }
    }

    public void cerrarConexion() {
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
            mostrarTexto("Conexión terminada");
        } catch (IOException e) {
            mostrarTexto("IOException on cerrarConexion()");
        } finally {

        }
    }

    public String ejecutarConexion(String ip, int puerto) {
        String respuesta;
        try {
            levantarConexion(ip, puerto);
            abrirFlujos();
            respuesta = recibirDatos();
        } finally {
            //   cerrarConexion();
        }
        return respuesta;
    }

    public String recibirDatos() {
        String st = "";
        char caracter = ' ';
        try {
            do {
                caracter = (char) bufferDeEntrada.read();
                if ((caracter == '{') || !st.equals("")) {
                    st += caracter;
                }
            } while (caracter != '}');
            mostrarTexto("\n[Servidor] => " + st);
        } catch (IOException e) {
            NeoService.setLog("...");
        } catch (Exception e) {
            NeoService.setLog("...");
        }
        return st;
    }

    public void escribirDatos(String entrada) {
        NeoService.setLog("[Usted] => " + entrada);
        if (entrada.length() > 0) {
            enviar(entrada);
        }
    }

    public void start() {
        SocketExternal cliente = new SocketExternal();

        String ip = "seguimientogps.co";
        String puerto = "8000";

        if (ip.length() <= 0) {
            ip = "localhost";
        }

        if (puerto.length() <= 0) {
            puerto = "5050";
        }

        while (true) {
            try {
                String peticiontext = cliente.ejecutarConexion(ip, Integer.parseInt(puerto));
                Gson gson = new Gson();
                JsonObject peticion = gson.fromJson(peticiontext, JsonObject.class);
                String response = "{}";
                if (peticion != null && !peticion.get("operation").isJsonNull()) {
                    switch (peticion.get("operation").getAsString()) {
                        case NeoService.COMMAND_APP_STATUS:
                            response = getStatus(peticion.get("fuel_process_id").getAsString(), peticion.get("pump_number").getAsInt());
                            break;
                        case NeoService.COMMAND_APP_AUTHORIZATION:
                            response = setAutorization(peticion.get("fuel_process_id").getAsString());
                            break;
                        default:
                            break;
                    }
                    cliente.escribirDatos(response);
                }
                cliente.cerrarConexion();
            } catch (JsonSyntaxException | NumberFormatException a) {
                NeoService.setLog(a.getMessage());
            } finally {
            }
        }
    }

    public String setAutorization(String peticion) {

        JsonObject object = new JsonObject();
        object.addProperty("fuel_process_id", peticion);
        object.addProperty("is_success", true);
        object.addProperty("timestamp", sdf.format(new Date()));
        object.addProperty("station_id", 154);
        object.addProperty("pump_number", 1);
        object.addProperty("nozzle_number", 2);
        return object.toString();

    }

    public String getStatus(String peticion, int cara) {

        JsonObject object = new JsonObject();
        object.addProperty("fuel_process_id", peticion);
        object.addProperty("is_success", true);
        object.addProperty("timestamp", sdf.format(new Date()));
        object.addProperty("statusId", 1);
        object.addProperty("status", "idle");
        object.addProperty("station_id", 154);
        object.addProperty("pump_number", 1);

        //CUANDO HAYAN VENTAS
        object.addProperty("vehicle_plate", "ABC123");
        object.addProperty("client_id", 1);
        object.addProperty("driver_id", "1045676558");

        //PRODUCTO DE LA NOZZLE
        object.addProperty("nozzle_number", 2);
        object.addProperty("fuel_type", 2);

//        object.addProperty("unit_price", 9900);
//        object.addProperty("total_quantity", 16.0);
//        object.addProperty("total_cost", 45000);

        SurtidorDao sutidao = new SurtidorDao();
        VentaDetalles parcial = sutidao.getVentaCara(1, cara, 1, "");
        if (parcial != null) {
            object.addProperty("total_quantity", parcial.getCantidad());
            object.addProperty("total_cost", parcial.getTotal());
            object.addProperty("unit_price", parcial.getPrecio());
        } else {
            object.addProperty("unit_price", 0);
            object.addProperty("total_quantity", 0);
            object.addProperty("total_cost", 0);
        }

        return object.toString();

    }
}
