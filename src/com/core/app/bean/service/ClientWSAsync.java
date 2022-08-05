/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.bean.service;

import com.butter.bean.Butter;
import static com.butter.bean.Butter.GET;
import com.butter.bean.Main;
import com.core.app.NeoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * @author novus
 */
public class ClientWSAsync extends Thread {

    private final String funcion;
    TreeMap<String, String> header = null;
    private String publicKey;
    private final String url;
    private final String method;
    private final String request;
    private JsonObject response;
    private final boolean DEBUG;
    private boolean isArray = false;
    private int status = 0;
    private int errorCodigo;
    private String errorMensaje;
    private JsonObject error;
    private int timeout = 15000;

    Gson gson = new GsonBuilder()
            .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
            .setPrettyPrinting().create();

    SimpleDateFormat sdf = new SimpleDateFormat(Butter.FORMAT_FULL_DATE_ISO);

    /**
     *
     * @param funcion
     * @param url
     * @param method
     * @param json
     * @param DEBUG
     * @param publicKey
     */
    public ClientWSAsync(String funcion, String url, String method, JsonObject json, boolean DEBUG, String publicKey) {

        this.funcion = funcion;
        this.url = url;
        this.method = method;
        this.request = json.toString();
        this.publicKey = publicKey;
        this.DEBUG = DEBUG;

    }

    public ClientWSAsync(String funcion, String url, String method, boolean DEBUG) {

        this.funcion = funcion;
        this.url = url;
        this.method = method;
        this.request = null;
        this.DEBUG = DEBUG;

    }

    /**
     *
     * @param funcion
     * @param url
     * @param method
     * @param json
     * @param DEBUG
     */
    public ClientWSAsync(String funcion, String url, String method, JsonObject json, boolean DEBUG) {
        this.funcion = funcion;
        this.url = url;
        this.method = method;
        if (json != null) {
            this.request = json.toString();
        } else {
            this.request = null;
        }
        this.DEBUG = DEBUG;
    }

    /**
     *
     * @param funcion
     * @param url
     * @param method
     * @param json
     * @param DEBUG
     */
    public ClientWSAsync(String funcion, String url, String method, String json, boolean DEBUG) {
        this.funcion = funcion;
        this.url = url;
        this.method = method;
        this.request = json;
        this.DEBUG = DEBUG;
    }

    /**
     *
     * @param funcion
     * @param url
     * @param method
     * @param json
     * @param DEBUG
     * @param isArray
     */
    public ClientWSAsync(String funcion, String url, String method, JsonObject json, boolean DEBUG, boolean isArray) {
        this.funcion = funcion;
        this.url = url;
        this.method = method;
        if (json != null) {
            this.request = json.toString();
        } else {
            this.request = null;
        }
        this.DEBUG = DEBUG;
        this.isArray = isArray;
    }

    public ClientWSAsync(String funcion, String url, String method, JsonObject json, boolean DEBUG, boolean isArray, TreeMap<String, String> header) {
        this.funcion = funcion;
        this.url = url;
        this.method = method;
        if (json != null) {
            this.request = json.toString();
        } else {
            this.request = null;
        }
        this.DEBUG = DEBUG;
        this.isArray = isArray;
        this.header = header;
    }

    @Override
    public void run() {
        try {
            response = execute();
        } catch (WSException ex) {
            Logger.getLogger(ClientWSAsync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JsonObject execute() throws WSException {
        long before = System.nanoTime();
        JsonObject respuesta = null;
        StringBuilder stb;
        try {

            if (url.contains("https")) {
                URL curl = new URL(url);
                HttpsURLConnection conn = (HttpsURLConnection) curl.openConnection();

                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);

                if (header == null) {
                    header = new TreeMap<>();
                    header.put("content-Type", "application/json");
                    if (Main.credencial != null && Main.credencial.getToken() != null) {
                        header.put("authorization", "Bearer " + Main.credencial.getToken());
                        header.put("password", Main.credencial.getPassword());
                    }
                    header.put("identificadorDispositivo", Main.credencial.getEquipos_id() + "");
                    header.put("aplicacion", NeoService.APLICATION_FULL_NAME);
                    header.put("versionApp", NeoService.APLICATION_NAME);
                    header.put("versionCode", NeoService.VERSION_CODE + "");
                    header.put("fecha", sdf.format(new Date()));
                    if (publicKey != null) {
                        header.put("key", publicKey);
                    }
                }

                header.entrySet().forEach((entry) -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    conn.setRequestProperty(key, value);
                });

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [TASK] " + funcion);
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [HEAD] \r\n");
                        for (Map.Entry<String, String> entry : header.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            NeoService.setLog(key + ": " + value);
                        };
                        NeoService.setLog("TX [RESQ] " + request.toString());
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(request.toString());
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [TASK] " + funcion);
                        NeoService.setLog("TX [GET ] " + url);
                    }
                }

                status = conn.getResponseCode();
                String result = conn.getResponseMessage();
                BufferedReader br;
                String line;

                if (status != 0) {
                    switch (status) {
                        case 200:
                        case 201:
                            //Main.LOGGER.info("Servicio creado en firebase");
                            InputStreamReader ir = new InputStreamReader(conn.getInputStream());
                            br = new BufferedReader(ir);

                            stb = new StringBuilder();
                            while ((line = br.readLine()) != null) {
                                stb.append(line);
                            }
                            if (DEBUG) {
                                NeoService.setLog("RX [(" + status + "): " + result + "]: \r\n");
                                NeoService.setLog("\r\n" + new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(stb.toString())));
                            }
                            if (isArray) {
                                JsonArray array = gson.fromJson(stb.toString(), JsonArray.class);
                                respuesta = new JsonObject();
                                respuesta.addProperty("sucess", true);
                                respuesta.add("data", array);
                            } else {
                                respuesta = gson.fromJson(stb.toString(), JsonObject.class);
                            }
                            break;
                        default:
                            InputStreamReader err = new InputStreamReader(conn.getErrorStream());
                            br = new BufferedReader(err);
                            stb = new StringBuilder();
                            while ((line = br.readLine()) != null) {
                                stb.append(line);
                            }

                            NeoService.setLog("ERROR RX [(" + status + "): " + stb.toString());
                            try {
                                error = gson.fromJson(stb.toString(), JsonObject.class);
                            } catch (Exception e) {
                                error = new JsonObject();
                            }
                            break;

                    }
                }
            } else {
                URL curl = new URL(url);
                HttpURLConnection conn;
                conn = (HttpURLConnection) curl.openConnection();

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);

                if (header == null) {
                    header = new TreeMap<>();
                    header.put("content-Type", "application/json");
                    if (Main.credencial != null && Main.credencial.getToken() != null) {
                        header.put("Authorization", "Bearer " + Main.credencial.getToken());
                        header.put("password", Main.credencial.getPassword());
                    }
                    header.put("identificadorDispositivo", Main.credencial.getEquipos_id() + "");
                    header.put("aplicacion", NeoService.APLICATION_FULL_NAME);
                    header.put("versionApp", NeoService.APLICATION_NAME);
                    header.put("versionCode", NeoService.VERSION_CODE + "");
                    header.put("fecha", sdf.format(new Date()));
                    if (publicKey != null) {
                        header.put("key", publicKey);
                    }
                }

                header.entrySet().forEach((entry) -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    conn.setRequestProperty(key, value);
                });
                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [TASK] " + funcion);
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [HEAD] \r\n");
                        for (Map.Entry<String, String> entry : header.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            NeoService.setLog(key + ": " + value);
                        }
                        NeoService.setLog("TX [RESQ] " + request);

                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(request);
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [TASK] " + funcion);
                        NeoService.setLog("TX [GET ] " + url);
                    }
                }

                status = conn.getResponseCode();
                String result = conn.getResponseMessage();
                BufferedReader br;
                String line;

                if (status != 0) {
                    switch (status) {
                        case 200:
                        case 201:
                            //Main.LOGGER.info("Servicio creado en firebase");
                            InputStreamReader ir = new InputStreamReader(conn.getInputStream());
                            br = new BufferedReader(ir);
                            stb = new StringBuilder();
                            while ((line = br.readLine()) != null) {
                                stb.append(line);
                            }
                            if (DEBUG) {
                                NeoService.setLog("RX [(" + status + "): " + result + "]: " + stb.toString());
                            }
                            if (isArray) {
                                JsonArray array = gson.fromJson(stb.toString(), JsonArray.class);
                                respuesta = new JsonObject();
                                respuesta.addProperty("sucess", true);
                                respuesta.add("data", array);
                            } else {
                                respuesta = gson.fromJson(stb.toString(), JsonObject.class);
                            }
                            break;
                        default:
                            InputStreamReader err = new InputStreamReader(conn.getErrorStream());
                            br = new BufferedReader(err);
                            stb = new StringBuilder();
                            while ((line = br.readLine()) != null) {
                                stb.append(line);
                            }

                            NeoService.setLog("ERROR RX [(" + status + "): " + stb.toString());
                            try {
                                error = gson.fromJson(stb.toString(), JsonObject.class);
                            } catch (Exception e) {
                                error = new JsonObject();
                            }

                            break;

                    }
                }
            }
        } catch (MalformedURLException mlfexception) {
            NeoService.setLog("ERROR: MalformedURLException ClientWS -> " + mlfexception.getMessage());
        } catch (SocketTimeoutException ti) {
            throw new WSException("ERROR: SocketTimeout in " + url);
        } catch (IOException mlfexception) {
            NeoService.setLog("ERROR: IOException ClientWS -> " + mlfexception.getMessage());
        } catch (JsonSyntaxException exception) {
            NeoService.setLog("ERROR: JsonSyntaxException ClientWS -> " + exception.getMessage());
        } catch (Exception exception) {
            NeoService.setLog("ERROR: Exception ClientWS ->  " + exception.getMessage());
        }
        long durationMs = (System.nanoTime() - before)/1_000_000;
        NeoService.setLog("TIMEPROCESS: ["+funcion+"] "+durationMs+"ms");
        return respuesta;
    }

    public JsonObject getResponse() {
        return response;
    }

    public void setResponse(JsonObject response) {
        this.response = response;
    }

    public int getErrorCodigo() {
        return errorCodigo;
    }

    public void setErrorCodigo(int errorCodigo) {
        this.errorCodigo = errorCodigo;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }

    public void setErrorMensaje(String errorMensaje) {
        this.errorMensaje = errorMensaje;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public JsonObject getError() {
        return error;
    }

    public void setError(JsonObject error) {
        this.error = error;
    }

    public JsonObject esperaRespuesta() {
        start();
        try {
            join();
            return response;
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientWSAsync.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

}
