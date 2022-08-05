package com.butter.bean;

import static com.butter.bean.Butter.GET;
import com.core.app.NeoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class ClientWS {

    SimpleDateFormat sdf;
    int status = -1;
    int timeout = 10000;

    public ClientWS() {
        sdf = new SimpleDateFormat(Butter.DATETIME_AM);
    }

    public int getStatus() {
        return status;
    }

    /**
     * @param funcion: Es que metodo ejecuto el webservice
     * @param method: POST, GET, PUT, DELETE
     * @param url: La url remota del servidor donde va a ejecutar la operacion
     * @param json: El objeto que va a procesar
     * @param DEBUG
     * @return El response del servidor
     */
    public JsonObject execute(boolean enableSSL, String funcion, String url, String method, JsonObject json, boolean DEBUG) throws WSException {
        JsonObject respuesta = null;
        try {

            if (enableSSL) {
                URL curl = new URL(url);
                HttpsURLConnection conn = (HttpsURLConnection) curl.openConnection();

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("content-Type", "application/json");

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [RESQ] " + json.toString());
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(json.toString());
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [GET ] " + url);
                    }
                }

                int status = conn.getResponseCode();
                String result = conn.getResponseMessage();

                InputStreamReader ir;
                BufferedReader reader;
                String sb;
                Gson gson = new GsonBuilder()
                        .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                        .setPrettyPrinting().create();
                if (status != 0) {
                    switch (status) {
                        case 200:
                        case 201:
                            ir = new InputStreamReader(conn.getInputStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [" + result + "]: ");
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta = gson.fromJson(sb, JsonObject.class);
                            respuesta.addProperty("success", true);
                            break;
                        default:
                            ir = new InputStreamReader(conn.getErrorStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [ERROR WS]: " + status + " -> " + result);
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta.addProperty("success", false);
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
                conn.setRequestProperty("content-Type", "application/json");

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [RESQ] " + json.toString());
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(json.toString());
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [GET ] " + url);
                    }
                }

                int status = conn.getResponseCode();
                String result = conn.getResponseMessage();
                InputStreamReader ir;
                BufferedReader reader;
                String sb;
                Gson gson = new GsonBuilder()
                        .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                        .setPrettyPrinting().create();
                if (status != 0) {
                    switch (status) {
                        case 200:
                        case 201:
                            ir = new InputStreamReader(conn.getInputStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [" + result + "]: ");
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta = gson.fromJson(sb, JsonObject.class);
                            respuesta.addProperty("success", true);
                            break;
                        default:
                            ir = new InputStreamReader(conn.getErrorStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [ERROR WS]: " + status + " -> " + result);
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta.addProperty("success", false);
                            break;
                    }
                }
            }
        } catch (MalformedURLException mlfexception) {
            NeoService.setLog("ERROR: MalformedURLException ClientWS -> " + mlfexception.getMessage());
        } catch (SocketTimeoutException ti) {
            throw new WSException("");
        } catch (IOException mlfexception) {
            NeoService.setLog("ERROR: IOException ClientWS -> " + mlfexception.getMessage());
        } catch (JsonSyntaxException exception) {
            NeoService.setLog("ERROR: JsonSyntaxException ClientWS -> " + exception.getMessage());
        } catch (Exception exception) {
            NeoService.setLog("ERROR: Exception ClientWS ->  " + exception.getMessage());
        }
        return respuesta;
    }

    /**
     *
     * @param enableSSL
     * @param funcion: Es que metodo ejecuto el webservice
     * @param credencial: Se require como autenticacion
     * @param method: POST, GET, PUT, DELETE
     * @param url: La url remota del servidor donde va a ejecutar la operacion
     * @param json: El objeto que va a procesar
     * @param DEBUG
     * @return El response del servidor
     */
    public JsonObject execute(boolean enableSSL, String funcion, CredencialBean credencial, String url, String method, JsonObject json, boolean DEBUG) throws WSException {
        JsonObject respuesta = null;

        try {
            if (enableSSL) {

                URL curl = new URL(url);
                HttpsURLConnection conn = (HttpsURLConnection) curl.openConnection();

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + credencial.getToken());
                conn.setRequestProperty("password", credencial.getPassword());

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("");
                        NeoService.setLog("");
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [POST] " + json.toString());
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(json.toString());
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [GET] " + url);
                    }
                }

                status = conn.getResponseCode();
                String result = conn.getResponseMessage();
                InputStreamReader ir;
                BufferedReader reader;
                String sb;
                Gson gson = new GsonBuilder()
                        .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                        .setPrettyPrinting().create();
                if (status != 0) {
                    switch (status) {
                        case 200:
                        case 201:
                            ir = new InputStreamReader(conn.getInputStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [" + result + "]: ");
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta = gson.fromJson(sb, JsonObject.class);
                            respuesta.addProperty("success", true);
                            break;
                        default:
                            ir = new InputStreamReader(conn.getErrorStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [ERROR WS]: " + status + " -> " + result);
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta.addProperty("success", false);
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
                conn.setRequestProperty("content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + credencial.getToken());
                conn.setRequestProperty("password", credencial.getPassword());

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [POST] " + json.toString());
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(json.toString());
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [GET] " + url);
                    }
                }

                status = conn.getResponseCode();
                String result = conn.getResponseMessage();
                InputStreamReader ir;
                BufferedReader reader;
                String sb;
                Gson gson = new GsonBuilder()
                        .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                        .setPrettyPrinting().create();
                if (status != 0) {
                    switch (status) {
                        case 200:
                        case 201:
                            ir = new InputStreamReader(conn.getInputStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [" + result + "]: ");
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta = gson.fromJson(sb, JsonObject.class);
                            respuesta.addProperty("success", true);
                            break;
                        default:
                            ir = new InputStreamReader(conn.getErrorStream());
                            reader = new BufferedReader(ir);
                            sb = reader.readLine();
                            if (DEBUG) {
                                NeoService.setLog("RX: [ERROR WS]: " + status + " -> " + result);
                                NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb)));
                            }
                            respuesta.addProperty("success", false);
                            break;
                    }
                }
            }
        } catch (MalformedURLException mlfexception) {
            NeoService.setLog("WS [ERR ]  MalformedURLException->" + url);
        } catch (SocketTimeoutException ti) {
            NeoService.setLog("WS [ERR ]  SocketTimeoutException->" + url);
        } catch (IOException mlfexception) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR ]  IOException ClientWS[" + status + "] -> [" + funcion + "] " + mlfexception.getMessage());
                if (!method.equals(GET)) {
                    NeoService.setLog("TX [POST] " + url);
                    NeoService.setLog("TX [POST] " + json.toString());
                } else {
                    NeoService.setLog("TX [GET ] " + url);
                }
            }
        } catch (JsonSyntaxException exception) {
            NeoService.setLog("ERROR: JsonSyntaxException ClientWS02 -> [" + funcion + "] " + exception.getMessage());
        } catch (Exception exception) {
            NeoService.setLog("ERROR: Exception ClientWS02 -> [" + funcion + "] " + exception.getMessage());
        }
        return respuesta;
    }

    public JsonObject executeToPicafuel(boolean enableSSL, String funcion, CredencialBean credencial, String url, String method, JsonObject json, boolean DEBUG) throws WSException {
        JsonObject respuesta = null;
        StringBuilder sb = new StringBuilder();
        int status;
        try {

            URL curl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) curl.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);

            TreeMap<String, String> header = new TreeMap<>();
            header.put("Content-Type", "application/json");
            header.put("Accept", "application/json");
            header.put("uuid", "a");
            header.put("fecha", new Date().toString());
            header.put("aplicacion", "test terpel");
            header.put("identificadorDispositivo", "12345");
            header.put("Authorization", "Bearer " + credencial.getToken());
            header.put("password", credencial.getPassword());

            for (Map.Entry<String, String> entry : header.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                conn.setRequestProperty(key, value);
            }

            if (!method.equals(GET)) {
                if (DEBUG) {
                    NeoService.setLog(".");
                    NeoService.setLog(".");
                    NeoService.setLog(".");
                    NeoService.setLog(".");
                    NeoService.setLog("TX [FUNC    ] " + funcion);
                    NeoService.setLog("TX [POST    ] " + url);

                    for (Map.Entry<String, String> entry : header.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        NeoService.setLog("TX [HEADER  ] " + key + ": " + value);
                    }
                    NeoService.setLog("TX [REQUEST ] " + json.toString());

                }
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();
            } else {
                if (DEBUG) {
                    NeoService.setLog("TX [GET] " + url);
                }
            }

            status = conn.getResponseCode();
            String result = conn.getResponseMessage();

            InputStreamReader ir;
            BufferedReader reader;
            Gson gson = new GsonBuilder()
                    .setDateFormat(Butter.FORMAT_FULL_DATE_ISO)
                    .setPrettyPrinting().create();
            if (status != 0) {
                switch (status) {
                    case 200:
                        respuesta = new JsonObject();
                        String line = null;
                        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        if (DEBUG) {
                            NeoService.setLog("RX: [" + result + "]: ");
                        }
                        try {
                            NeoService.setLog(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(sb.toString())));
                            respuesta = gson.fromJson(sb.toString(), JsonObject.class);
                        } catch (Exception a) {
                            NeoService.setLog("RX NO FORMAT JSON: ");
                            NeoService.setLog(sb.toString());
                        }
                        respuesta.addProperty("success", true);
                        break;
                    case 201:
                    case 401:
                    case 400:
                    case 404:
                    case 500:
                        BufferedReader bre = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
                        while ((line = bre.readLine()) != null) {
                            sb.append(line);
                        }
                        if (DEBUG) {
                            NeoService.setLog("RX [CODE    ] " + status + " ->" + result);
                            NeoService.setLog("RX [MENS    ] " + sb.toString());
                            NeoService.setLog(".");
                            NeoService.setLog(".");
                            NeoService.setLog(".");
                            NeoService.setLog(".");
                        }
                        break;
                    default:
                        NeoService.setLog("CODIGO DE ERROR " + status);
                        break;
                }
            }
        } catch (MalformedURLException mlfexception) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR (executeToPicafuel)]  MalformedURLException->" + url);
            }
        } catch (SocketTimeoutException ti) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR (executeToPicafuel)]  SocketTimeoutException->" + url);
            }
        } catch (IOException mlfexception) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR (executeToPicafuel)] IOException->" + url);
            }
        } catch (JsonSyntaxException exception) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR (executeToPicafuel)] JsonSyntaxException -> [" + funcion + "] " + exception.getMessage());
            }
        } catch (Exception exception) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR (executeToPicafuel)] Exception -> [" + funcion + "] " + exception.getMessage());
            }
        }
        return respuesta;
    }

    public JsonObject execute(boolean enableSSL, String funcion, CredencialBean credencial, String url, String method, String json, boolean DEBUG) throws WSException {
        JsonObject respuesta = null;
        try {
            if (enableSSL) {

                URL curl = new URL(url);
                HttpsURLConnection conn = (HttpsURLConnection) curl.openConnection();

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod(method);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + credencial.getToken());
                conn.setRequestProperty("password", credencial.getPassword());

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [POST] " + json);
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(json);
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [GET] " + url);
                    }
                }

                InputStreamReader ir = new InputStreamReader(conn.getInputStream());

                status = conn.getResponseCode();
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
                            respuesta = gson.fromJson(sb, JsonObject.class);
                            respuesta.addProperty("success", true);
                            break;
                        default:
                            NeoService.setLog("CODIGO DE ERROR " + status + ": " + funcion);
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
                conn.setRequestProperty("content-Type", "application/json");
                conn.setRequestProperty("authorization", "Bearer " + credencial.getToken());
                conn.setRequestProperty("password", credencial.getPassword());

                if (!method.equals(GET)) {
                    if (DEBUG) {
                        NeoService.setLog("TX [POST] " + url);
                        NeoService.setLog("TX [POST] " + json.toString());
                    }
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(json.toString());
                    wr.flush();
                } else {
                    if (DEBUG) {
                        NeoService.setLog("TX [GET] " + url);
                    }
                }

                InputStreamReader ir = new InputStreamReader(conn.getInputStream());

                status = conn.getResponseCode();
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
                            respuesta = gson.fromJson(sb, JsonObject.class);
                            break;
                        default:
                            NeoService.setLog("CODIGO DE ERROR " + status + ": " + funcion);
                            break;
                    }
                }
            }
        } catch (MalformedURLException mlfexception) {
            NeoService.setLog("WS [ERR ]  MalformedURLException->" + url);
        } catch (SocketTimeoutException ti) {
            NeoService.setLog("WS [ERR ]  SocketTimeoutException->" + url);
        } catch (IOException mlfexception) {
            if (DEBUG) {
                NeoService.setLog("WS [ERR ]  IOException ClientWS[" + status + "] -> [" + funcion + "] " + mlfexception.getMessage());
                if (!method.equals(GET)) {
                    NeoService.setLog("TX [POST] " + url);
                    NeoService.setLog("TX [POST] " + json.toString());
                } else {
                    NeoService.setLog("TX [GET ] " + url);
                }
            }
        } catch (JsonSyntaxException exception) {
            NeoService.setLog("ERROR: JsonSyntaxException ClientWS02 -> [" + funcion + "] " + exception.getMessage());
        } catch (Exception exception) {
            NeoService.setLog("ERROR: Exception ClientWS02 -> [" + funcion + "] " + exception.getMessage());
        }
        return respuesta;
    }

    public void setTimeOut(int i) {
        timeout = i;
    }

}
