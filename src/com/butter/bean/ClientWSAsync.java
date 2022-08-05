/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import static com.butter.bean.Butter.GET;
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * @author novus
 */
public class ClientWSAsync extends Thread {

    private final String funcion;
    private final CredencialBean credencial;
    private final String url;
    private final String method;
    private final String request;
    private JsonObject response;
    private final boolean DEBUG;
    private final boolean ensableSsl;
    private boolean isArray = false;
    private int errorCodigo;
    private String errorMensaje;
    
    public ClientWSAsync(boolean ensableSsl, String funcion, CredencialBean credencial, String url, String method, JsonObject json, boolean DEBUG) {
        this.funcion = funcion;
        this.credencial = credencial;
        this.url = url;
        this.method = method;
        if (json != null) {
            this.request = json.toString();
        } else {
            this.request = null;
        }
        this.DEBUG = DEBUG;
        this.ensableSsl = ensableSsl;
    }

    public ClientWSAsync(boolean ensableSsl, String funcion, CredencialBean credencial, String url, String method, String json, boolean DEBUG) {
        this.funcion = funcion;
        this.credencial = credencial;
        this.url = url;
        this.method = method;
        this.request = json;
        this.DEBUG = DEBUG;
        this.ensableSsl = ensableSsl;
    }

    public ClientWSAsync(boolean ensableSsl, String funcion, CredencialBean credencial, String url, String method, JsonObject json, boolean DEBUG, boolean isArray) {
        this.funcion = funcion;
        this.credencial = credencial;
        this.url = url;
        this.method = method;
        if (json != null) {
            this.request = json.toString();
        } else {
            this.request = null;
        }
        this.DEBUG = DEBUG;
        this.ensableSsl = ensableSsl;
        this.isArray = isArray;
    }

    @Override
    public void run() {
        try {
            if (ensableSsl) {
                executeHttps();
            } else {
                executeHttp();
            }
        } catch (WSException ex) {
            Logger.getLogger(ClientWSAsync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void executeHttps() throws WSException {
        JsonObject respuesta = null;
        try {

            URL curl = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) curl.openConnection();

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("content-Type", "application/json");
            conn.setRequestProperty("authorization", "Bearer " + credencial.getToken());
            conn.setRequestProperty("password", credencial.getPassword());

            if (!method.equals(GET)) {
                if (DEBUG) {
                    NeoService.setLog("TX [POST][FUNC] " + funcion);
                    NeoService.setLog("TX [POST][URL ] " + url);
                    NeoService.setLog("TX [POST][BODY] " + request);
                }
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(request);
                wr.flush();
            } else {
                if (DEBUG) {
                    NeoService.setLog("TX [GET][FUNC] " + funcion);
                    NeoService.setLog("TX [GET][URL ] " + url);
                }
            }

            InputStreamReader ir = new InputStreamReader(conn.getInputStream());

            int status = conn.getResponseCode();
            String result = conn.getResponseMessage();

            if (status != 0) {
                switch (status) {
                    case 200:
                    case 201:
                    case 401:
                        BufferedReader reader = new BufferedReader(ir);
                        String sb = reader.readLine();
                        if (DEBUG) {
                            NeoService.setLog("RX: [" + result + "]: ");
                            NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                        }
                        Gson gson = new GsonBuilder()
                                .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                                .setPrettyPrinting().create();

                        if (isArray) {
                            JsonArray responseArray = gson.fromJson(sb, JsonArray.class);
                            response = new JsonObject();
                            response.add("data", responseArray);
                        } else {
                            response = gson.fromJson(sb, JsonObject.class);
                        }

                        NeoService.setLog("");
                        break;
                    default:
                        errorCodigo = status;
                        errorMensaje = "Respuesta no esperada en el servidor";
                        NeoService.setLog("CODIGO DE ERROR " + status + ": " + funcion);
                        break;
                }
            }
        } catch (MalformedURLException ex) {
            errorMensaje = "ERROR: MalformedURLException ClientWSAsync -> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (SocketTimeoutException ex) {
            errorMensaje = "ERROR: TimeoutExeption ClientWSAsync-> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (IOException ex) {
            errorMensaje = "ERROR: IOException ClientWSAsync-> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (JsonSyntaxException ex) {
            errorMensaje = "ERROR: JsonSyntaxException ClientWSAsync-> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (Exception ex) {
            errorMensaje = "ERROR: Exception ClientWSAsync-> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        }
    }

    public void executeHttp() throws WSException {
        JsonObject respuesta = null;
        try {

            URL curl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) curl.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("content-Type", "application/json");
            conn.setRequestProperty("authorization", "Bearer " + credencial.getToken());
            conn.setRequestProperty("password", credencial.getPassword());

            if (!method.equals(GET)) {
                if (DEBUG) {
                    NeoService.setLog("TX [POST][URL ] " + url);
                    NeoService.setLog("TX [POST][BODY] " + request);
                }
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(request);
                wr.flush();
            } else {
                if (DEBUG) {
                    NeoService.setLog("TX [GET][URL ]  " + url);
                }
            }

            InputStreamReader ir = new InputStreamReader(conn.getInputStream());

            int status = conn.getResponseCode();
            String result = conn.getResponseMessage();

            if (status != 0) {
                switch (status) {
                    case 200:
                    case 201:
                    case 401:
                        //NeoService.setLog("Servicio creado en firebase");
                        BufferedReader reader = new BufferedReader(ir);
                        String sb = reader.readLine();
                        if (DEBUG) {
                            NeoService.setLog("RX: [" + result + "]: ");
                            NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                        }
                        Gson gson = new GsonBuilder()
                                .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                                .setPrettyPrinting().create();

                        if (isArray) {
                            JsonArray responseArray = gson.fromJson(sb, JsonArray.class);
                            response.add("data", responseArray);
                        } else {
                            response = gson.fromJson(sb, JsonObject.class);
                        }

                        NeoService.setLog("");
                        break;
                    default:
                        errorCodigo = status;
                        errorMensaje = "RESPONSE[" + status + "]: RESPUESTA NO ESPERADA EN EL SERVIDOR. FUNCION: " + funcion;
                        NeoService.setLog(errorMensaje);
                        break;
                }
            }
        } catch (MalformedURLException ex) {
            errorMensaje = "ERROR: MalformedURLException -> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (SocketTimeoutException ex) {
            errorMensaje = "ERROR: TimeoutExeption -> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (IOException ex) {
            errorMensaje = "ERROR: IOException -> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (JsonSyntaxException ex) {
            errorMensaje = "ERROR: JsonSyntaxException -> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        } catch (Exception ex) {
            errorMensaje = "ERROR: Exception -> " + ex.getMessage();
            NeoService.setLog(errorMensaje);
        }
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

}
