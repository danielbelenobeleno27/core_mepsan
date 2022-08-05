/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.bean.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.core.app.NeoService;
import com.neo.app.bean.ResponseSicom;
import com.core.database.DAOException;
import com.core.database.Postgrest;
import com.core.database.impl.SurtidorDao;
import com.core.print.services.PrinterFacade;
import com.core.screen.service.SplashService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 *
 * @author ASUS-PC
 */
public class ClientWS {

    Gson gson;

    public ClientWS() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setPrettyPrinting().create();
    }

    public JsonObject execute(String url, JsonObject json) {
        JsonObject respuesta = null;
        String server = url;
        try {

            URL curl = new URL(url);
            HttpURLConnection conn;

            server = curl.getHost();

            conn = (HttpURLConnection) curl.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(json.toString());
            wr.flush();

            InputStreamReader ir = new InputStreamReader(conn.getInputStream());

            int status = conn.getResponseCode();

            if (status != 0) {
                switch (status) {
                    case 200:
                        BufferedReader reader = new BufferedReader(ir);
                        String sb = reader.readLine();
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .setPrettyPrinting().create();
                        respuesta = gson.fromJson(sb, JsonObject.class);
                        break;
                    case 401:
                        //client side error
                        NeoService.setLog("Error occurred :");
                        break;
                    case 501:
                        //server side error
                        NeoService.setLog("Notification Response : [ errorCode=ServerError ] TokenId : ");
                        break;
                    case 503:
                        //server side error
                        NeoService.setLog("Notification Response : FCM Service is Unavailable  TokenId : ");
                        break;
                    default:
                        break;
                }
            }

        } catch (MalformedURLException mlfexception) {
            NeoService.setLog("Error occurred while sending push Notification!.." + mlfexception.getMessage());
            PrinterFacade fc = new PrinterFacade();
            fc.conFormato("NO SE PUDO CONECTAR A:" + url);
        } catch (IOException mlfexception) {
            NeoService.setLog("Reading URL, Error occurred while sending push Notification!.." + mlfexception.getMessage());
            // PrinterFacade fc = new PrinterFacade();
            // fc.conFormato("SIN CONEXIÓN PARA...\r\n" + server);
        } catch (Exception exception) {
            NeoService.setLog("Error occurred while sending push Notification!.." + exception.getMessage());
            PrinterFacade fc = new PrinterFacade();
            fc.conFormato("RESULTADO DE CONEXIÓN: " + exception.getMessage());
        }
        return respuesta;
    }

    public ResponseSicom valideChip(String chip, char cara) {

        //chip = "7F0000006E81F006";
        ResponseSicom response = null;
        SplashService sp = new SplashService();
        try {
            //UN CHIP A8000000312DE406
            //OTRO CHIP 3A25400007DA5F21
            URL curl = new URL("http://node.neoline.co:8000/valide");

            HttpURLConnection conn;
            conn = (HttpURLConnection) curl.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(NeoService.TIMEOUT_SICOM);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String json = "{\"idrom\":\"" + chip + "\"}";
            NeoService.setLog(json);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json);
            wr.flush();

            int status = conn.getResponseCode();

            if (status != 0) {
                switch (status) {
                    case 200:
                        InputStreamReader ir = new InputStreamReader(conn.getInputStream());
                        BufferedReader reader = new BufferedReader(ir);
                        String sb = reader.readLine();
                        Gson gson = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .setPrettyPrinting().create();
                        response = gson.fromJson(sb, ResponseSicom.class);
                        break;
                    case 404:
                        NeoService.setLog("Vehiculo no encontrado en sicon y neocentral");
                        break;
                    case 401:
                        //client side error
                        NeoService.setLog("Error occurred :");
                        break;
                    case 501:
                        //server side error
                        NeoService.setLog("Notification Response : [ errorCode=ServerError ] TokenId : ");
                        break;
                    case 503:
                        //server side error
                        NeoService.setLog("Notification Response : FCM Service is Unavailable  TokenId : ");
                        break;
                    default:
                        break;
                }
            }

        } catch (JsonSyntaxException | IOException exception) {
            NeoService.setLog("Error !.." + exception.getMessage());
            //PrinterFacade fc = new PrinterFacade();
            //fc.conFormato("ERROR DE CONSULTA");
        } catch (Exception exception) {
            NeoService.setLog("Error.." + exception.getMessage());
            //PrinterFacade fc = new PrinterFacade();
            //fc.conFormato("ERROR DE CONSULTA");
        } finally {

            if (response == null) {
                NeoService.setLog("Vehiculo no encontrado");
                PrinterFacade fc = new PrinterFacade();
                fc.conFormato("VEHICULO NO ENCONTRADO EN SICOM");
                sp.showPlaca(1, cara, "NOCHIP");
            } else {
                sp.showPlaca(1, cara, response.getPlaca());
            }
        }

        return response;

    }

    private HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                HostnameVerifier hv
                        = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("com.example.com", session);
            }
        };
        return hostnameVerifier;
    }

}
