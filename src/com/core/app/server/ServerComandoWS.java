package com.core.app.server;

import com.butter.bean.Butter;
import com.butter.bean.CatalogoBean;
import com.butter.bean.ClientWS;
import com.butter.bean.EmpresaBean;
import com.butter.bean.EquipoDao;
import com.butter.bean.ErrorResponse;
import com.butter.bean.Main;
import com.butter.bean.Utils;
import com.butter.bean.WSException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.core.app.NeoService;
import com.core.app.protocols.GilbarcoControllerE500;
import com.core.app.protocols.MepsanController;
import com.neo.app.bean.AConstant;
import com.neo.app.bean.Autorizacion;
import com.neo.app.bean.Cara;
import com.neo.app.bean.Manguera;
import com.neo.app.bean.MedioPago;
import com.neo.app.bean.Precio;
import com.neo.app.bean.ProductoBean;
import com.neo.app.bean.Recibo;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.Totalizador;
import com.neo.app.bean.Venta;
import com.neo.app.notificacion.Notificacion;
import com.core.app.server.filter.AuthorizationFilter;
import com.core.database.DAOException;
import com.core.database.impl.SurtidorDao;
import com.core.print.services.PrinterFacade;
import com.google.gson.JsonSyntaxException;
import com.neo.app.bean.BaseControllerProtocols;
import static com.neo.app.bean.BaseControllerProtocols.ESTA_PROCESANDO;
import static com.neo.app.bean.BaseControllerProtocols.TIENE_PETICION;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerComandoWS extends Thread {

    AuthorizationFilter filters = new AuthorizationFilter();
    Gson gson = new Gson();
    static SimpleDateFormat sdf = new SimpleDateFormat(Butter.FORMAT_FULL_DATE_ISO);
    SimpleDateFormat sdfSQL = new SimpleDateFormat(Butter.FORMAT_DATETIME_SQL);
    SimpleDateFormat sdf2 = new SimpleDateFormat(Butter.FORMAT_PROCESS);

    @Override
    public void run() {
        try {
            init();
        } catch (Exception ex) {
            NeoService.setLog("PUERTO DE " + NeoService.PORT_SERVER_WS_API + " ocupado...");
            System.exit(0);
            Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init() throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(NeoService.PORT_SERVER_WS_API), 0);

        server.createContext("/api/ping", new HttpHandlerPing());
        server.createContext("/api/multicambioprecio", new HttpHandlerMultiChangePrice());
        server.createContext("/api/totalizadoresSurtidor", new HttpHandlerTotalizadoresSurtidor());
        server.createContext("/api/precio", new HttpHandlerPrecio());
        server.createContext("/api/detener", new HttpHandlerDetener());
        server.createContext("/api/cambioprecio", new HttpHandlerChangePrice());

        server.createContext("/api/registroTotalizadores", new HttpHandlerRegistroTotalizadores());

        server.createContext("/api/autorizacion", new HttpHandlerAuthorize());
        server.createContext("/api/estado", new HttpHandlerStatus());

        server.createContext("/api/preAutorizacion", new HttpHandlerPreAuthorize());

        server.createContext("/api/aperturaTurno", new HttpHandlerAperturaTurno());
        server.createContext("/api/cierreTurno", new HttpHandlerCierreTurno());

        //server.createContext("/api/terminate", new HttpHandlerTerminate());
        //server.createContext("/api/initiate", new HttpHandlerInitiate());
        server.createContext("/api", new HttpHandlerApi());
        server.setExecutor(null);
        server.start();
    }

    private class HttpHandlerAperturaTurno implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                String method = t.getRequestMethod();
                if (method.equalsIgnoreCase(Butter.POST)) {
                    ErrorResponse error = filters.doFilterInternal(t);
                    if (error == null) {

                        Headers outHeaders = t.getResponseHeaders();
                        outHeaders.set("Context-Type", "application/json");

                        InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                        BufferedReader br = new BufferedReader(isr);

                        int b;
                        StringBuilder buf = new StringBuilder();
                        while ((b = br.read()) != -1) {
                            buf.append((char) b);
                        }
                        NeoService.setLog(t.getRequestURI().toString());
                        NeoService.setLog(buf.toString());

                        JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                        NeoService.setLog("PROCESANDO JSON");
                        NeoService.setLog(request.toString());
                        if (request.get("surtidor") != null) {

                            NeoService.setLog("PRIMERA VALIDACION");
                            int surtidor = request.get("surtidor").getAsInt();

                            long grupoJornada = 0;
                            if (request.get("turno") != null) {
                                grupoJornada = request.get("turno").getAsLong();
                            }

                            long personaId = 0;
                            if (request.get("persona") != null) {
                                personaId = request.get("persona").getAsLong();
                            }

                            boolean existe = false;
                            if (surtidor != 0) {
                                if (NeoService.surtidores.containsKey(surtidor)) {
                                    existe = true;
                                }
                            } else {
                                existe = true;
                            }

                            if (existe) {
                                for (Map.Entry<Integer, Surtidor> sentry : NeoService.surtidores.entrySet()) {
                                    Surtidor svalue = sentry.getValue();
                                    if (surtidor == 0 || svalue.getId() == surtidor) {

                                        SurtidorDao sdao = new SurtidorDao();
                                        int factorPrecio;
                                        try {
                                            factorPrecio = sdao.getFactorPrecio(svalue.getId());
                                        } catch (DAOException ex) {
                                            factorPrecio = 1;
                                        }

                                        int factorInventario;
                                        try {
                                            factorInventario = sdao.getFactorInventario(svalue.getId());
                                        } catch (DAOException ex) {
                                            factorInventario = 1;
                                        }

                                        int factorVolumen;
                                        try {
                                            factorVolumen = sdao.getFactorVolumen(svalue.getId());
                                        } catch (DAOException ex) {
                                            factorVolumen = 1;
                                        }

                                        int factorImporte;
                                        try {
                                            factorImporte = sdao.getFactorImporte(svalue.getId());
                                        } catch (DAOException ex) {
                                            factorImporte = 1;
                                        }

                                        JsonObject jsona = new JsonObject();

                                        boolean ocupado = false;

                                        for (Map.Entry<Integer, Cara> entry1 : NeoService.surtidores.get(svalue.getId()).getCaras().entrySet()) {
                                            Cara cara = entry1.getValue();
                                            NeoService.setLog("VALIDANDO ESTADO DE LA CARA " + cara.getNumero() + " ESTADO: " + cara.getPublicEstadoDescripcion() + NeoService.ANSI_RESET);
                                            if (cara.getPublicEstadoId() != NeoService.SURTIDORES_PUBLIC_ESTADO_ID_ESPERA) {
                                                ocupado = true;
                                                break;
                                            }
                                        }
                                        NeoService.setLog("SE PROCEDE A CONSEGUIR LOS TOTALIZADORES DEL SURTIDOR " + svalue.getId() + NeoService.ANSI_RESET);

                                        if (!ocupado) {

                                            Surtidor xsurtidor = NeoService.surtidores.get(svalue.getId());
                                            JsonArray jsonarray = new JsonArray();
                                            boolean salto = false;
                                            SurtidorDao dao = new SurtidorDao();
                                            
                                            TIENE_PETICION.set(true);
                                            while (ESTA_PROCESANDO.get()) {
                                            }

                                            for (Map.Entry<Integer, Cara> entry1 : NeoService.surtidores.get(svalue.getId()).getCaras().entrySet()) {

                                                Cara cara = entry1.getValue();
                                                NeoService.setLog(NeoService.ANSI_GREEN + "CONSULTANDO CARA = " + cara.getNumero() + NeoService.ANSI_RESET);

                                                Totalizador[] totales = NeoService.surtidores.get(svalue.getId()).control.getTotalizadoresByCara(cara);
                                                
                                                for (Map.Entry<Integer, Manguera> entry : cara.getMangueras().entrySet()) {
                                                    Manguera value = entry.getValue();
                                                    Totalizador totalSistema = sdao.getTotalizadores(xsurtidor.getUniqueId(), value.getId());
                                                    NeoService.setLog("TOTALIZADORES VALIDADOR SURT " + NeoService.ANSI_CYAN + value.getRegistroSurtidorVolumen() + NeoService.ANSI_RESET + " VS SISTE " + NeoService.ANSI_CYAN + totalSistema.getAcumuladoVolumen() + NeoService.ANSI_RESET);
                                                    if (value.getRegistroSurtidorVolumen() != totalSistema.getAcumuladoVolumen()) {
                                                        NeoService.setLog(NeoService.ANSI_RED + " EXISTE SALTO DE LECTURA POR WS " + cara.getNumero() + NeoService.ANSI_RESET);
                                                        value.setTieneTotalizadores(true);
                                                        value.setTieneSaltoLectura(true);
                                                        sdao.saveSaltoLectura(value);
                                                        salto = true;
                                                        break;
                                                    }
                                                }

                                                if (!salto) {

                                                    for (Totalizador totale : totales) {
                                                        JsonObject json = new JsonObject();
                                                        json.addProperty("isla", NeoService.surtidores.get(svalue.getId()).getIslaId());
                                                        json.addProperty("surtidor", svalue.getId());
                                                        json.addProperty("cara", totale.getCara());
                                                        json.addProperty("manguera", totale.getManguera());
                                                        json.addProperty("grado", totale.getGrado());

                                                        try {
                                                            double precio = Utils.calculeCantidad(totale.getPrecio(), factorPrecio);
                                                            double precio2 = Utils.calculeCantidad(totale.getPrecio2(), factorPrecio);
                                                            json.addProperty("precio", precio);
                                                            json.addProperty("precio2", precio2);
                                                        } catch (Exception e) {
                                                            NeoService.setLog("ERROR EN EL SERVICIO CONSULTA DE TOTALIZADORES, EN PRECIOS");
                                                        }

                                                        try {
                                                            double cantidadVolumen = Utils.calculeCantidadInversaDouble(totale.getAcumuladoVolumen(), factorInventario);
                                                            json.addProperty("acumuladoVolumenReal", totale.getAcumuladoVolumen());
                                                            json.addProperty("acumuladoVolumen", cantidadVolumen);
                                                            json.addProperty("acumuladoVenta", totale.getAcumuladoVenta());
                                                        } catch (Exception a) {
                                                            NeoService.setLog("ERROR EN EL SERVICIO CONSULTA DE TOTALIZADORES, EN ACUMULADOS");
                                                        }

                                                        Manguera manguera = NeoService.surtidores.get(svalue.getId()).getCaras().get(totale.getCara()).getMangueras().get(totale.getManguera());

                                                        long productoId = manguera.getProductoId();

                                                        try {

                                                            ProductoBean p;
                                                            p = dao.getProductoConFamiliaById(productoId);
                                                            json.addProperty("productoIdentificador", productoId);
                                                            json.addProperty("productoDescripcion", p.getDescripcion());
                                                            json.addProperty("familiaIdentificador", p.getCategoriaId());
                                                            json.addProperty("familiaDescripcion", p.getCategoriaDesc());

                                                            json.addProperty("factor_inventario", factorInventario);
                                                            json.addProperty("factor_volumen_parcial", factorVolumen);
                                                            json.addProperty("factor_importe_parcial", factorImporte);
                                                            json.addProperty("factor_precio", factorPrecio);

                                                        } catch (DAOException ex) {
                                                            Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                                                        }

                                                        if (grupoJornada > 0) {
                                                            try {
                                                                int tempSurtidor = surtidor;
                                                                if (surtidor == 0) {
                                                                    tempSurtidor = svalue.getId();
                                                                }
                                                                dao.registrarTotalizadores(tempSurtidor, cara.getNumero(), grupoJornada, manguera);
                                                            } catch (DAOException ex) {
                                                                Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                                                            }
                                                        }
                                                        jsonarray.add(json);
                                                    }

                                                    jsona.add("data", jsonarray);
                                                } else {
                                                    JsonObject jerror = new JsonObject();
                                                    jerror.addProperty("codigoError", ErrorResponse.ERROR_40120_ID);
                                                    jerror.addProperty("mensajeError", ErrorResponse.ERROR_40120_DESC);
                                                    jerror.addProperty("tipoError", "Funcional");
                                                    jerror.addProperty("fechaProceso", sdf.format(new Date()));
                                                    Utils.responseError(t, ErrorResponse.SC_UNAUTHORIZED, jerror);
                                                    break;
                                                }

                                            }
                                            if (!salto) {

                                                if (personaId > 0 && grupoJornada > 0) {
                                                    try {
                                                        float saldo = 0;
                                                        if (request.get("saldo") != null) {
                                                            saldo = request.get("saldo").getAsFloat();
                                                        }
                                                        dao.registrarInicioJornada(grupoJornada, personaId, saldo, jsona.get("data").getAsJsonArray());
                                                    } catch (DAOException ex) {
                                                        Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }

                                                try {
                                                    dao.desbloqueoMangueras();
                                                } catch (DAOException ex) {
                                                    Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                                                }

                                                NeoService.PERSONA_AUTORIZA_ID.set(0);
                                                
                                                NeoService.setLog(NeoService.ANSI_PURPLE + jsona.toString() + NeoService.ANSI_RESET);
                                                Headers respHeaders = t.getResponseHeaders();
                                                respHeaders.add("content-type", "application/json");
                                                t.sendResponseHeaders(200, jsona.toString().getBytes().length);

                                                try (OutputStream os = t.getResponseBody()) {
                                                    os.write(jsona.toString().getBytes());
                                                    os.flush();
                                                }
                                            }
                                        } else {

                                            JsonObject jerror = new JsonObject();
                                            jerror.addProperty("codigoError", ErrorResponse.ERROR_40601_ID);
                                            jerror.addProperty("mensajeError", ErrorResponse.ERROR_40601_DESC);
                                            jerror.addProperty("tipoError", "Funcional");
                                            jerror.addProperty("fechaProceso", sdf.format(new Date()));
                                            Utils.responseError(t, ErrorResponse.SC_NOT_ACCEPTABLE, jerror);
                                        }

                                    }

                                }
                            } else {
                                JsonObject jerror = new JsonObject();
                                jerror.addProperty("codigoError", ErrorResponse.ERROR_40124_ID);
                                jerror.addProperty("mensajeError", ErrorResponse.ERROR_40124_DESC);
                                jerror.addProperty("tipoError", "Funcional");
                                jerror.addProperty("fechaProceso", sdf.format(new Date()));
                                Utils.responseError(t, ErrorResponse.SC_NOT_ACCEPTABLE, jerror);
                            }

                        } else {
                            Utils.responseError(t, error);
                        }
                    } else {

                        JsonObject jerror = new JsonObject();
                        jerror.addProperty("codigoError", ErrorResponse.ERROR_40500_ID);
                        jerror.addProperty("mensajeError", ErrorResponse.ERROR_40500_DESC);
                        jerror.addProperty("tipoError", "Funcional");
                        jerror.addProperty("fechaProceso", new Date().toString());
                        Utils.responseError(t, ErrorResponse.SC_METHOD_NOT_ALLOWED, jerror);
                    }

                }
            } catch (Exception ex) {
                Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                TIENE_PETICION.set(false);
            }
        }
    }

    public static class TotalizadoresCierre {

        static SurtidorDao sdao;

        static JsonObject Procesar(long grupoJornada, int surtidor) {
            SurtidorDao dao = new SurtidorDao();
            sdao = dao;
            JsonObject json = new JsonObject();
            JsonArray jsonarrayInicial = new JsonArray();
            JsonArray jsonarrayFinal = new JsonArray();
            try {
                dao.bloqueoMangueras();
            } catch (DAOException ex) {
                Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (grupoJornada == 0) {
                grupoJornada = sdao.getUltimoGrupoJornada();
                NeoService.setLog("CONSULTANDO ULTIMA GRUPO_JORNADA = " + grupoJornada);
            } else {
                NeoService.setLog("GENERANDO CIERRE CON GRUPO_JORNADA = " + grupoJornada);
            }

            for (Map.Entry<Integer, Surtidor> sentry : NeoService.surtidores.entrySet()) {
                Surtidor svalue = sentry.getValue();
                if (surtidor == 0 || svalue.getId() == surtidor) {
                    boolean ocupado = VerificarEstadoSurtidor(svalue.getId());
                    if (ocupado) {
                        json.addProperty("codigoError", ErrorResponse.ERROR_40601_ID);
                        json.addProperty("mensajeError", ErrorResponse.ERROR_40601_DESC);
                        json.addProperty("tipoError", "Funcional");
                        json.addProperty("fechaProceso", sdf.format(new Date()));
                        json.addProperty("status", ErrorResponse.SC_NOT_ACCEPTABLE);
                        break;
                    }
                    NeoService.setLog(NeoService.ANSI_PURPLE + "PROCEDE A BUSCAR LOS TOTALIZADORES DEL SURTIDOR " + svalue.getId() + NeoService.ANSI_RESET);
                    try {
                        buscartotalizadoresSurtidor(grupoJornada, svalue, jsonarrayInicial, jsonarrayFinal);
                    } catch (DAOException ex) {
                        json.addProperty("codigoError", ErrorResponse.ERROR_40124_DESC);
                        json.addProperty("mensajeError", ErrorResponse.ERROR_40124_DESC);
                        json.addProperty("tipoError", "Funcional");
                        json.addProperty("fechaProceso", sdf.format(new Date()));
                        json.addProperty("status", ErrorResponse.SC_NOT_ACCEPTABLE);
                        break;
                    }
                }
            }

            if (jsonarrayInicial.size() > 0) {
                json.addProperty("status", ErrorResponse.SC_OK);
                json.add("totalizadoresIniciales", jsonarrayInicial);
                json.addProperty("turnos_id", grupoJornada);
                json.add("totalizadoresFinales", jsonarrayFinal);

                if (grupoJornada != 0) {
                    try {
                        dao.cerrarJornada(grupoJornada, jsonarrayFinal);
                    } catch (DAOException ex) {
                        Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        EquipoDao edao = new EquipoDao();
                        edao.guardarTransmisionTexto(Main.credencial, json.toString(), Butter.SECURE_END_POINT_JORNADAS, Butter.POST, Butter.TX_TIPO_JORNADA_CIERRE, String.valueOf(grupoJornada));
                    } catch (DAOException ex) {
                        Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return json;
            
        }

        static boolean VerificarEstadoSurtidor(int surtidor) {
            boolean ocupado = false;

            for (Map.Entry<Integer, Cara> entry1 : NeoService.surtidores.get(surtidor).getCaras().entrySet()) {
                Cara carasa = entry1.getValue();
                NeoService.setLog("VALIDANDO ESTADO DE LA CARA " + carasa.getNumero() + " ESTADO: " + carasa.getPublicEstadoDescripcion() + NeoService.ANSI_RESET);
                if (carasa.getPublicEstadoId() != NeoService.SURTIDORES_PUBLIC_ESTADO_ID_ESPERA) {
                    ocupado = true;
                    break;
                }
            }
            return ocupado;
        }

        static void buscartotalizadoresSurtidor(long grupoJornada, Surtidor surtidor, JsonArray jsonarrayInicial, JsonArray jsonarrayFinal) throws DAOException {
            int factorPrecio;
            try {
                factorPrecio = sdao.getFactorPrecio(surtidor.getId());
            } catch (DAOException ex) {
                factorPrecio = 1;
            }

            int factorInventario;
            try {
                factorInventario = sdao.getFactorInventario(surtidor.getId());
            } catch (DAOException ex) {
                factorInventario = 100;
            }

            int factorVolumen;
            try {
                factorVolumen = sdao.getFactorVolumen(surtidor.getId());
            } catch (DAOException ex) {
                factorVolumen = 1;
            }

            int factorImporte;
            try {
                factorImporte = sdao.getFactorImporte(surtidor.getId());
            } catch (DAOException ex) {
                factorImporte = 1;
            }

            //TOTALIZADORES
            for (Map.Entry<Integer, Cara> entry1 : NeoService.surtidores.get(surtidor.getId()).getCaras().entrySet()) {

                Cara cara = entry1.getValue();
                NeoService.setLog(NeoService.ANSI_GREEN + "CONSULTANDO CARA = " + cara.getNumero() + NeoService.ANSI_RESET);

                for (Map.Entry<Integer, Manguera> entry : cara.getMangueras().entrySet()) {

                    Manguera manguera = entry.getValue();
                    NeoService.setLog(NeoService.ANSI_YELLOW + "CONSULTANDO MANGUERA = " + manguera.getId() + NeoService.ANSI_RESET);
                    Totalizador[] totalizador = sdao.getTotalizadoresCompletoVentas(grupoJornada, manguera.getId());
                    
                    NeoService.setLog(NeoService.ANSI_YELLOW + "TOTALIZADORES = " + totalizador.length + NeoService.ANSI_RESET);
                    //[0] es inicial
                    //[1] es final

                    for (int i = 0; i < totalizador.length; i++) {
                        Totalizador totales = totalizador[i];
                        JsonObject json = new JsonObject();

                        json.addProperty("isla", NeoService.surtidores.get(surtidor.getId()).getIslaId());
                        json.addProperty("surtidor", surtidor.getId());
                        json.addProperty("cara", totales.getCara());
                        json.addProperty("manguera", totales.getManguera());
                        json.addProperty("grado", totales.getGrado());

                        try {
                            double precio = Utils.calculeCantidad(totales.getPrecio(), factorPrecio);
                            double precio2 = Utils.calculeCantidad(totales.getPrecio2(), factorPrecio);
                            json.addProperty("precio", precio);
                            json.addProperty("precio2", precio2);
                        } catch (Exception e) {
                            NeoService.setLog("ERROR EN EL SERVICIO CONSULTA DE TOTALIZADORES, EN PRECIOS");
                        }

                        try {
                            double cantidadVolumen = Utils.calculeCantidadInversaDouble(totales.getAcumuladoVolumen(), factorInventario);
                            json.addProperty("acumuladoVolumenReal", totales.getAcumuladoVolumen());
                            json.addProperty("acumuladoVolumen", cantidadVolumen);
                            json.addProperty("acumuladoVenta", totales.getAcumuladoVenta());
                        } catch (Exception a) {
                            NeoService.setLog("ERROR EN EL SERVICIO CONSULTA DE TOTALIZADORES, EN ACUMULADOS");
                        }

                        long productoId = manguera.getProductoId();

                        try {

                            ProductoBean p;
                            p = sdao.getProductoConFamiliaById(productoId);
                            json.addProperty("productoIdentificador", productoId);
                            json.addProperty("productoDescripcion", p.getDescripcion());
                            json.addProperty("familiaIdentificador", p.getCategoriaId());
                            json.addProperty("familiaDescripcion", p.getCategoriaDesc());

                            json.addProperty("factor_inventario", factorInventario);
                            json.addProperty("factor_volumen_parcial", factorVolumen);
                            json.addProperty("factor_importe_parcial", factorImporte);
                            json.addProperty("factor_precio", factorPrecio);

                        } catch (DAOException ex) {
                            Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if (grupoJornada != 0) {
                            try {
                                sdao.registrarTotalizadoresFinales(grupoJornada, manguera);
                            } catch (DAOException ex) {
                                Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        if (i == 0) {
                            jsonarrayInicial.add(json);
                        } else {
                            jsonarrayFinal.add(json);
                        }
                    }
                }
            }
        }
    }

    private class HttpHandlerCierreTurno implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {

            try {

                String method = t.getRequestMethod();
                if (method.equalsIgnoreCase(Butter.POST)) {
                    ErrorResponse error = filters.doFilterInternal(t);
                    if (error == null) {

                        Headers outHeaders = t.getResponseHeaders();
                        outHeaders.set("Context-Type", "application/json");

                        InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                        BufferedReader br = new BufferedReader(isr);

                        int b;
                        StringBuilder buf = new StringBuilder();
                        while ((b = br.read()) != -1) {
                            buf.append((char) b);
                        }
                        NeoService.setLog(t.getRequestURI().toString());
                        NeoService.setLog(buf.toString());

                        JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                        NeoService.setLog(request.toString());
                        if (request.get("surtidor") != null) {
                            SurtidorDao dao = new SurtidorDao();

                            NeoService.setLog("PRIMERA VALIDACION");
                            int surtidor = request.get("surtidor").getAsInt();

                            long grupoJornada = request.get("turno").getAsLong();
                            if (grupoJornada == 0) {
                                grupoJornada = dao.getGrupoJornada();
                            }

                            boolean existe = false;
                            if (surtidor != 0) {
                                if (NeoService.surtidores.containsKey(surtidor)) {
                                    existe = true;
                                }
                            } else {
                                existe = true;
                            }

                            if (existe) {
                                NeoService.setLog(NeoService.ANSI_YELLOW + "ENTRA A PROCESAR TOTALIZADORES" + NeoService.ANSI_RESET);
                                JsonObject json = TotalizadoresCierre.Procesar(grupoJornada, surtidor);
                                Utils.responseStatus(t, json.get("status").getAsInt(), json);
                            } else {
                                JsonObject jerror = new JsonObject();
                                jerror.addProperty("codigoError", ErrorResponse.ERROR_40124_ID);
                                jerror.addProperty("mensajeError", ErrorResponse.ERROR_40124_DESC);
                                jerror.addProperty("tipoError", "Funcional");
                                jerror.addProperty("fechaProceso", sdf.format(new Date()));
                                Utils.responseError(t, ErrorResponse.SC_NOT_ACCEPTABLE, jerror);
                            }
                        } else {
                            Utils.responseError(t, error);
                        }
                    } else {

                        JsonObject jerror = new JsonObject();
                        jerror.addProperty("codigoError", ErrorResponse.ERROR_40500_ID);
                        jerror.addProperty("mensajeError", ErrorResponse.ERROR_40500_DESC);
                        jerror.addProperty("tipoError", "Funcional");
                        jerror.addProperty("fechaProceso", new Date().toString());
                        Utils.responseError(t, ErrorResponse.SC_METHOD_NOT_ALLOWED, jerror);
                    }

                }
            } catch (JsonSyntaxException | IOException ex) {
                Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                JsonObject jerror = new JsonObject();
                jerror.addProperty("codigoError", ErrorResponse.SC_INTERNAL_SERVER_ERROR);
                jerror.addProperty("mensajeError", ex.getMessage());
                jerror.addProperty("tipoError", "Funcional");
                jerror.addProperty("fechaProceso", sdf.format(new Date()));
                Utils.responseError(t, ErrorResponse.SC_NOT_ACCEPTABLE, jerror);
            }
        }

    }

    private class HttpHandlerRegistroTotalizadores implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {

                Headers headers = t.getRequestHeaders();

                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);

                int b;
                StringBuilder buf = new StringBuilder();
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }
                //NeoService.setLog(buf);

                JsonObject json = new JsonObject();
                json.addProperty("version_codigo", NeoService.VERSION_CODE);
                json.addProperty("version_nombre", NeoService.VERSION_NAME);
                json.addProperty("empresa_id", 1);
                json.addProperty("empresa_code", 1);
                json.addProperty("error", NeoService.SURTIDOR_ERROR);
                json.addProperty("fechaProceso", sdf.format(new Date()));

                String response = json.toString();

                // String response = "This is the response";
                Headers respHeaders = t.getResponseHeaders();
                respHeaders.add("content-type", "application/json");
                t.sendResponseHeaders(200, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                    os.flush();
                }

            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }

    }

    private class HttpHandlerPing implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.GET)) {

                Headers headers = t.getRequestHeaders();

                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);

                int b;
                StringBuilder buf = new StringBuilder();
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }
                //NeoService.setLog(buf);

                JsonObject json = new JsonObject();
                json.addProperty("version_codigo", NeoService.VERSION_CODE);
                json.addProperty("version_nombre", NeoService.VERSION_NAME);
                json.addProperty("empresa_id", 1);
                json.addProperty("empresa_code", 1);
                json.addProperty("error", NeoService.SURTIDOR_ERROR);
                json.addProperty("fechaProceso", sdf.format(new Date()));

                String response = json.toString();

                // String response = "This is the response";
                Headers respHeaders = t.getResponseHeaders();
                respHeaders.add("content-type", "application/json");
                t.sendResponseHeaders(200, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                    os.flush();
                }

            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }

    }

    private class HttpHandlerAuthorize implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();

                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }

                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);
                    //String peticiontext = buf.toString();
                    //String peticiontext = NeoService.soc.procesaTrama(buf.toString());

                    String parametro = null;
                    try {

                        parametro = "numeroCara";
                        String snumeroCara = request.get(parametro).getAsString();
                        Utils.validarInt(t, snumeroCara, parametro, NeoService.MINIMO_CARA, NeoService.MAXIMO_CARA);
                        int numeroCara = Integer.parseInt(snumeroCara);

                        SurtidorDao sdao = new SurtidorDao();
                        int surtidor = sdao.getSurtidorPorNumeroCara(numeroCara);

                        parametro = "identificadorProceso";
                        String autorizacion = request.get(parametro).getAsString();
                        Utils.validarUuid(t, autorizacion, parametro);

                        parametro = "identificadorFamiliaProducto";
                        Utils.validarInt(t, request.get(parametro).getAsString(), parametro);

                        parametro = "identificadorFormaPago";
                        Utils.validarString(t, request.get(parametro).getAsString(), parametro);

                        parametro = "nombreCondutor";
                        Utils.validarString(t, request.get(parametro).getAsString(), parametro);

                        parametro = "documentoIdentificacionConductor";
                        Utils.validarString(t, request.get(parametro).getAsString(), parametro);

                        parametro = "nombreCliente";
                        Utils.validarString(t, request.get(parametro).getAsString(), parametro);

                        parametro = "documentoIdentificacionCliente";
                        Utils.validarString(t, request.get(parametro).getAsString(), parametro);

                        parametro = "placaVehiculo";
                        Utils.validarString(t, request.get(parametro).getAsString(), parametro);

                        parametro = "precioUnidad";
                        Utils.validarInt(t, request.get(parametro).getAsString(), parametro);
                        int precioUnidad = request.get(parametro).getAsInt();

                        parametro = "montoMaximo";
                        Utils.validarDouble(t, request.get(parametro).getAsString(), parametro);

                        parametro = "cantidadMaxima";
                        Utils.validarDouble(t, request.get(parametro).getAsString(), parametro);

                        int existe = sdao.getExisteVentaPorToken(autorizacion);
                        if (existe == 0) {

                            parametro = "identificadorFamiliaProducto";
                            long familiaId = request.get(parametro).getAsLong();

                            Manguera manguera = sdao.getGradoByProductFamily(numeroCara, familiaId);
                            if (manguera != null) {

                                JsonObject response = new JsonObject();

                                try {
                                    EquipoDao dao = new EquipoDao();
                                    EmpresaBean empresa = dao.findEmpresa(Main.credencial);
                                    response.addProperty("codigoEstacion", empresa.getCodigo());

                                } catch (DAOException e) {
                                }

                                JsonArray array = new JsonArray();
                                JsonObject jmanguera = new JsonObject();
                                jmanguera.addProperty("surtidor", manguera.getSurtidor());
                                jmanguera.addProperty("cara", manguera.getCara());
                                jmanguera.addProperty("manguera", manguera.getId());
                                jmanguera.addProperty("mangueraSurtidor", manguera.getGrado());
                                jmanguera.addProperty("identificadorFamiliaProducto", manguera.getProductoFamiliaId());
                                jmanguera.addProperty("familiaProducto", manguera.getProductoFamiliaDescripcion());
                                jmanguera.addProperty("precioUnidad", precioUnidad);
                                array.add(jmanguera);

                                response.add("manguera", array);

                                parametro = "numeroCara";
                                response.addProperty(parametro, request.get(parametro).getAsInt());

                                parametro = "identificadorProceso";
                                response.addProperty(parametro, request.get(parametro).getAsString());

                                long PICAFUEL_PROVEEDOR = 1;
                                boolean PREVENTA = true;
                                String ESTADO = "A";

                                boolean exito = sdao.registrarAutorizacion(request, PICAFUEL_PROVEEDOR, PREVENTA, ESTADO, surtidor, numeroCara, manguera.getGrado());

                                NeoService.sharedPreference.put(NeoService.PREFERENCE_AUTORIZACION + "_C" + numeroCara + "G" + manguera.getGrado(), autorizacion);

                                JsonObject json = new JsonObject();
                                json.addProperty("tipo", 3);
                                json.addProperty("subtipo", 12);
                                json.addProperty("mensaje", "VENTA AUTORIZADA DESDE LA APP");
                                NotificacionSocket.publishrr(1, json);

                                response.addProperty("identificadorProcesoLazo", sdf2.format(new Date()));
                                response.addProperty("fechaProceso", sdf.format(new Date()));

                                String resp = response.toString();
                                Headers respHeaders = t.getResponseHeaders();
                                respHeaders.add("content-type", "application/json");
                                t.sendResponseHeaders(200, resp.length());

                                try (OutputStream os = t.getResponseBody()) {
                                    os.write(resp.getBytes());
                                    os.flush();
                                }

                            } else {

                                error = new ErrorResponse();
                                error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                                error.setTipo("negocio");
                                error.setCodigo("40004");
                                error.setMensaje("producto no asociado en esta cara");
                                error.setFechaProceso(new Date().toString());
                                Utils.responseError(t, error);

                            }

                        } else {

                            error = new ErrorResponse();
                            error.setTipo("negocio");
                            error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                            error.setCodigo(ErrorResponse.ERROR_40002_ID);
                            error.setMensaje(ErrorResponse.ERROR_40002_DESC);
                            error.setFechaProceso(new Date().toString());
                            Utils.responseError(t, error);

                        }

                    } catch (NullPointerException e) {

                        error = new ErrorResponse();
                        error.setTipo(ErrorResponse.ERROR_NEGOCIO);
                        error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                        error.setCodigo(ErrorResponse.ERROR_40001_ID);
                        error.setMensaje(ErrorResponse.ERROR_40001_DESC_PARA + parametro);
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (DAOException ex) {

                        error = new ErrorResponse();
                        error.setTipo(ErrorResponse.ERROR_NEGOCIO);
                        error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                        error.setCodigo(ErrorResponse.ERROR_40003_ID);
                        error.setMensaje(ErrorResponse.ERROR_40003_DESC);
                        error.setException(ex.getMessage());
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (IOException e) {

                        error = new ErrorResponse();
                        error.setTipo(ErrorResponse.ERROR_NEGOCIO);
                        error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                        error.setCodigo(ErrorResponse.ERROR_40000_ID);
                        error.setMensaje(ErrorResponse.ERROR_40000_DESC);
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (Exception e) {

                        error = new ErrorResponse();
                        error.setTipo(ErrorResponse.ERROR_NEGOCIO);
                        error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                        error.setCodigo(ErrorResponse.ERROR_40000_ID);
                        error.setMensaje(ErrorResponse.ERROR_40000_DESC);
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    }
                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerPreAuthorize implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();

                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }

                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                    EquipoDao eado = new EquipoDao();
                    String url = "http://apis.neobox.com.co:8010/lazo.surtidor/autorizacionTag";

                    ClientWS client = new ClientWS();
                    try {
                        JsonObject response = client.executeToPicafuel(
                                Butter.DISABLE_HTTPS,
                                "ENVIANDO VENTA AL WS PICAFUEL",
                                Main.credencial,
                                url,
                                Butter.POST,
                                request,
                                true
                        );

                        if (response != null && response.get("success").getAsBoolean()) {
                            NeoService.setLog(response.toString());

                            if (response.get("success").getAsBoolean()) {

                                String parametro = "identificadorProcesoCombustible";
                                String auto = response.get(parametro).getAsString();
                                Utils.validarUuid(t, auto, parametro);

                                SurtidorDao sdao = new SurtidorDao();
                                JsonObject xjson = new JsonObject();
                                xjson.addProperty("identificadorProceso", auto);
                                xjson.addProperty("placaVehiculo", response.get("placaVehiculo").getAsString());
                                xjson.addProperty("documentoIdentificacionCliente", response.get("documentoIdentificacionCliente").getAsString());
                                xjson.addProperty("documentoIdentificacionConductor", response.get("documentoIdentificacionConductor").getAsString());

                                xjson.addProperty("montoMaximo", response.get("montoMaximo").getAsString());
                                xjson.addProperty("cantidadMaxima", response.get("cantidadMaxima").getAsString());
                                xjson.addProperty("porcentajeDescuentoCliente", response.get("porcentajeDescuentoCliente").getAsString());

                                xjson.addProperty("nombreCliente", response.get("nombreCliente").getAsString());
                                xjson.addProperty("identificadorFormaPago", response.get("identificadorFormaPago").getAsString());
                                boolean exito = sdao.registrarAutorizacion(xjson, request.get("numeroCara").getAsInt(), request.get("numeroGrado").getAsInt(), request.get("medio").getAsString(), request.get("medio").getAsString(), request.get("medio").getAsString());
                                NeoService.setLog("REGISTRADA CON EXITO");

                                String resp = response.toString();
                                Headers respHeaders = t.getResponseHeaders();
                                respHeaders.add("content-type", "application/json");
                                t.sendResponseHeaders(200, resp.length());
                                //NO ME SIRVE ESTA RESPUESTA . NO TIENE CODIGO , MENSAJE
                                try (OutputStream os = t.getResponseBody()) {
                                    os.write(resp.getBytes());
                                    os.flush();
                                }

                            } else {
                                //SIN RESPONSE.....IF IS 404 401 
                                NeoService.setLog("ERROR ENVIANDO AUTORIZACION A PICAFUEL");
                            }

                        }

                    } catch (WSException ex) {
                        //SIN REPONSE
                        Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerPrecio implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    for (Map.Entry<String, List<String>> entry : t.getRequestHeaders().entrySet()) {
                        String key = entry.getKey();
                        List<String> value = entry.getValue();
                        NeoService.setLog(">> " + key + ":" + value.get(0));
                    }

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();
                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }
                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                    String parametro = null;
                    try {
                        JsonObject response = new JsonObject();
                        try {
                            EquipoDao dao = new EquipoDao();
                            EmpresaBean empresa = dao.findEmpresa(Main.credencial);
                            response.addProperty("codigoEstacion", empresa.getCodigo());

                        } catch (DAOException e) {
                        }

                        parametro = "identificadorProceso";
                        String identificadorProceso = request.get(parametro).getAsString();
                        Utils.validarUuid(t, identificadorProceso, parametro);

                        response.addProperty("identificadorProceso", identificadorProceso);

                        parametro = "numeroCara";
                        String snumeroCara = request.get(parametro).getAsString();
                        Utils.validarInt(t, snumeroCara, parametro, NeoService.MINIMO_CARA, NeoService.MAXIMO_CARA);
                        int numeroCara = Integer.parseInt(snumeroCara);

                        parametro = "identificadorFamiliaProducto";
                        long familiaId = request.get(parametro).getAsLong();

                        SurtidorDao sdao = new SurtidorDao();
                        Manguera manguera = sdao.getGradoByProductFamily(numeroCara, familiaId);

                        if (manguera != null) {

                            response.addProperty("surtidor", manguera.getSurtidor());
                            response.addProperty("cara", manguera.getCara());
                            response.addProperty("grado", manguera.getGrado());
                            response.addProperty("manguera", manguera.getId());

                            response.addProperty("identificadorProducto", manguera.getProductoPublicId());
                            response.addProperty("nombreProducto", manguera.getProductoDescripcion());
                            response.addProperty("identificadorFamiliaProducto", manguera.getProductoFamiliaId());
                            response.addProperty("familiaProducto", manguera.getProductoFamiliaDescripcion());

                            response.addProperty("precioUnidad", manguera.getProductoPrecio());

                            response.addProperty("identificadorProcesoLazo", sdf2.format(new Date()));
                            response.addProperty("fechaProceso", sdf.format(new Date()));

                            String resp = response.toString();
                            Headers respHeaders = t.getResponseHeaders();
                            respHeaders.add("content-type", "application/json");
                            t.sendResponseHeaders(200, resp.length());

                            try (OutputStream os = t.getResponseBody()) {
                                os.write(resp.getBytes());
                                os.flush();
                            }

                        } else {

                            error = new ErrorResponse();
                            error.setTipo("negocio");
                            error.setCodigo("40004");
                            error.setMensaje("producto no asociado en esta cara");
                            error.setFechaProceso(new Date().toString());
                            Utils.responseError(t, error);

                        }

                    } catch (NullPointerException e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setMensaje("parameter not found '" + parametro + "'");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (DAOException ex) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setMensaje("familia enviada no soportada. las familias soportada son: 'corriente', 'diesel', 'extra', 'gas'");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (Exception e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setMensaje("peticion errada");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    }
                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerTotalizadoresSurtidor implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();
                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }
                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                    if (request.get("surtidor") != null && request.get("cara") != null) {

                        int surtidor = request.get("surtidor").getAsInt();

                        int cara = 0;
                        cara = request.get("cara").getAsInt();

                        Cara ocara = new Cara();
                        ocara.setNumero(cara);

                        boolean extended = false;
                        if (request.get("extended") != null) {
                            extended = request.get("extended").getAsBoolean();
                        }

                        SurtidorDao sdao = new SurtidorDao();
                        int factorPrecio;
                        try {
                            factorPrecio = sdao.getFactorPrecio(surtidor);
                        } catch (DAOException ex) {
                            factorPrecio = 1;
                        }

                        int factorInventario;
                        try {
                            factorInventario = sdao.getFactorInventario(surtidor);
                        } catch (DAOException ex) {
                            factorInventario = 1;
                        }

                        int factorVolumen;
                        try {
                            factorVolumen = sdao.getFactorVolumen(surtidor);
                        } catch (DAOException ex) {
                            factorVolumen = 1;
                        }

                        int factorImporte;
                        try {
                            factorImporte = sdao.getFactorImporte(surtidor);
                        } catch (DAOException ex) {
                            factorImporte = 1;
                        }

                        JsonArray jsonArray = new JsonArray();

                        if (cara != 0) {

                            Totalizador[] totales = null;
                            switch (NeoService.TOTALIZADOR_ORIGEN) {
                                case NeoService.TOTALIZADOR_ORIGEN_BD:
                                        try {
                                    totales = sdao.getTotalizadoresByCara(ocara);
                                } catch (DAOException e) {
                                    error = new ErrorResponse();
                                    error.setTipo("negocio");
                                    error.setStatusCode(ErrorResponse.SC_NOT_ACCEPTABLE);
                                    error.setCodigo(ErrorResponse.ERROR_40601_ID);
                                    error.setMensaje(ErrorResponse.ERROR_40601_DESC);
                                    error.setFechaProceso(new Date().toString());
                                    Utils.responseError(t, error);
                                }
                                break;
                                default:
                                    totales = NeoService.surtidores.get(surtidor).control.getTotalizadoresByCara(ocara);
                                    break;
                            }
                            for (Totalizador total : totales) {
                                JsonObject json = new JsonObject();
                                json.addProperty("isla", NeoService.surtidores.get(surtidor).getIslaId());
                                json.addProperty("surtidor", surtidor);
                                json.addProperty("cara", total.getCara());
                                json.addProperty("manguera", total.getManguera());
                                json.addProperty("grado", total.getGrado());
                                double precio = Utils.calculeCantidad(total.getPrecio(), factorPrecio);
                                double precio2 = Utils.calculeCantidad(total.getPrecio2(), factorPrecio);
                                json.addProperty("precio", precio);
                                json.addProperty("precio2", precio2);
                                double cantidadVolumen = Utils.calculeCantidadInversaDouble(total.getAcumuladoVolumen(), factorInventario);
                                json.addProperty("acumuladoVolumenReal", total.getAcumuladoVolumen());
                                json.addProperty("acumuladoVolumen", cantidadVolumen);
                                json.addProperty("acumuladoVenta", total.getAcumuladoVenta());
                                if (extended) {
                                    Manguera manguera = NeoService.surtidores.get(surtidor).getCaras().get(cara).getMangueras().get(total.getManguera());
                                    long productoId = manguera.getProductoId();
                                    String productoDesc = manguera.getProductoDescripcion();
                                    long familiaId = manguera.getProductoFamiliaId();
                                    String familiaDesc = manguera.getProductoFamiliaDescripcion();
                                    json.addProperty("productoIdentificador", productoId);
                                    json.addProperty("productoDescripcion", productoDesc);
                                    json.addProperty("familiaIdentificador", familiaId);
                                    json.addProperty("familiaDescripcion", familiaDesc);

                                    json.addProperty("factor_inventario", factorInventario);
                                    json.addProperty("factor_volumen_parcial", factorVolumen);
                                    json.addProperty("factor_importe_parcial", factorImporte);
                                    json.addProperty("factor_precio", factorPrecio);

                                }
                                jsonArray.add(json);
                            }
                        } else {
                            TreeMap<Integer, Cara> caras = NeoService.surtidores.get(surtidor).getCaras();
                            for (Map.Entry<Integer, Cara> entry : caras.entrySet()) {
                                Integer key = entry.getKey();
                                Cara value = entry.getValue();

                                Totalizador[] totales = null;
                                switch (NeoService.TOTALIZADOR_ORIGEN) {
                                    case NeoService.TOTALIZADOR_ORIGEN_BD:
                                        try {
                                        totales = sdao.getTotalizadoresByCara(value);
                                    } catch (DAOException e) {
                                        error = new ErrorResponse();
                                        error.setTipo("negocio");
                                        error.setStatusCode(ErrorResponse.SC_NOT_ACCEPTABLE);
                                        error.setCodigo(ErrorResponse.ERROR_40601_ID);
                                        error.setMensaje(ErrorResponse.ERROR_40601_DESC);
                                        error.setFechaProceso(new Date().toString());
                                        Utils.responseError(t, error);
                                    }
                                    break;
                                    default:
                                        totales = NeoService.surtidores.get(surtidor).control.getTotalizadoresByCara(value);
                                        break;
                                }
                                for (Totalizador totale : totales) {
                                    JsonObject json = new JsonObject();
                                    json.addProperty("isla", NeoService.surtidores.get(surtidor).getIslaId());
                                    json.addProperty("surtidor", surtidor);
                                    json.addProperty("cara", totale.getCara());
                                    json.addProperty("manguera", totale.getManguera());
                                    json.addProperty("grado", totale.getGrado());
                                    double precio = Utils.calculeCantidad(totale.getPrecio(), factorPrecio);
                                    double precio2 = Utils.calculeCantidad(totale.getPrecio2(), factorPrecio);
                                    json.addProperty("precio", precio);
                                    json.addProperty("precio2", precio2);
                                    double cantidadVolumen = Utils.calculeCantidadInversaDouble(totale.getAcumuladoVolumen(), factorInventario);
                                    json.addProperty("acumuladoVolumenReal", totale.getAcumuladoVolumen());
                                    json.addProperty("acumuladoVolumen", cantidadVolumen);
                                    json.addProperty("acumuladoVenta", totale.getAcumuladoVenta());
                                    if (extended) {
                                        Manguera manguera = NeoService.surtidores.get(surtidor).getCaras().get(totale.getCara()).getMangueras().get(totale.getManguera());

                                        long productoId = manguera.getProductoId();

                                        try {
                                            SurtidorDao dao = new SurtidorDao();
                                            ProductoBean p;
                                            p = dao.getProductoConFamiliaById(productoId);
                                            json.addProperty("productoIdentificador", productoId);
                                            json.addProperty("productoDescripcion", p.getDescripcion());
                                            json.addProperty("familiaIdentificador", p.getCategoriaId());
                                            json.addProperty("familiaDescripcion", p.getCategoriaDesc());

                                            json.addProperty("factor_inventario", factorInventario);
                                            json.addProperty("factor_volumen_parcial", factorVolumen);
                                            json.addProperty("factor_importe_parcial", factorImporte);
                                            json.addProperty("factor_precio", factorPrecio);

                                        } catch (DAOException ex) {
                                            Logger.getLogger(ServerComandoWS.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    }
                                    jsonArray.add(json);
                                }
                            }
                        }

                        Headers respHeaders = t.getResponseHeaders();
                        respHeaders.add("content-type", "application/json");
                        t.sendResponseHeaders(200, jsonArray.toString().getBytes().length);

                        try (OutputStream os = t.getResponseBody()) {
                            os.write(jsonArray.toString().getBytes());
                            os.flush();
                        }

                    } else {
                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setStatusCode(ErrorResponse.SC_BAD_REQUEST);
                        error.setCodigo(ErrorResponse.ERROR_40001_ID);
                        if (request.get("surtidor") == null) {
                            error.setMensaje(ErrorResponse.ERROR_40001_DESC_PARA + "'surtidor'");
                        } else {
                            error.setMensaje(ErrorResponse.ERROR_40001_DESC_PARA + "'cara'");
                        }
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);
                    }

                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerChangePrice implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();
                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }
                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);
                    //String peticiontext = buf.toString();
                    //String peticiontext = NeoService.soc.procesaTrama(buf.toString());

                    String parametro = null;
                    try {

                        JsonObject response = new JsonObject();
                        try {
                            EquipoDao dao = new EquipoDao();
                            EmpresaBean empresa = dao.findEmpresa(Main.credencial);
                            response.addProperty("codigoEstacion", empresa.getCodigo());

                        } catch (DAOException e) {
                        }

                        parametro = "identificadorProceso";
                        response.addProperty("identificadorProceso", request.get(parametro).getAsString());

                        parametro = "surtidor";
                        int ssurtidor = request.get(parametro).getAsInt();

                        parametro = "precioUnidad";
                        long xprecio = request.get(parametro).getAsLong();

                        SurtidorDao sdao = new SurtidorDao();
                        int factorPrecio;
                        try {
                            factorPrecio = sdao.getFactorPrecio(ssurtidor);
                        } catch (DAOException ex) {
                            factorPrecio = 1;
                        }

                        long precio = Utils.calculeCantidadInversa(xprecio, factorPrecio);

                        Surtidor surtidor = NeoService.surtidores.get(ssurtidor);


                        /*CANTIDAD DIGITO DEBE SER UNA CONSTANTE DE 6*/
                        //parametro = "cantidadDigitos";
                        //int scantdigitos = request.get(parametro).getAsInt();
                        parametro = "listaPrecio";
                        int listaPrecio = request.get(parametro).getAsInt();

                        parametro = "grado";
                        int sgrado = request.get(parametro).getAsInt();

                        parametro = "cara";
                        int scara = request.get(parametro).getAsInt();

                        surtidor.control.TIENE_PETICION.set(true);
                        while (surtidor.control.ESTA_PROCESANDO.get()) {
                        }

                        boolean estadoDiferenteEspera = false;

                        for (Map.Entry<Integer, Cara> entry1 : surtidor.getCaras().entrySet()) {
                            Integer key1 = entry1.getKey();
                            Cara cara = entry1.getValue();
                            if (cara.getEstado() != MepsanController.SURTIDORES_ESTADO_ESPERA) {
                                estadoDiferenteEspera = true;
                                break;
                            }
                        }

                        Precio resp = new Precio();
                        resp.setId(-1);
                        if (!estadoDiferenteEspera) {
                            resp = NeoService.surtidores.get(ssurtidor).control.actualizaPrecios(precio, listaPrecio, sgrado, scara, true);

                            JsonObject jsoncp = new JsonObject();
                            switch ((int) resp.getId()) {
                                case 0:
                                    jsoncp.addProperty(AConstant.STRING_SUCCESS, true);
                                    jsoncp.addProperty(AConstant.STRING_MENSSAGE, AConstant.COMANDO_APLICADO + " Nuevo precio aplicado: " + xprecio);
                                    jsoncp.addProperty("precio", resp.getPrecio());
                                    break;
                                case 1:
                                    jsoncp.addProperty(AConstant.STRING_SUCCESS, false);
                                    jsoncp.addProperty(AConstant.STRING_MENSSAGE, "El Valor en Mayor que los digitos");
                                    break;
                                case 2:
                                    jsoncp.addProperty(AConstant.STRING_SUCCESS, false);
                                    jsoncp.addProperty(AConstant.STRING_MENSSAGE, "Cantidad de digitos no soportados");
                                    break;
                                case 3:
                                    jsoncp.addProperty(AConstant.STRING_SUCCESS, false);
                                    jsoncp.addProperty(AConstant.STRING_MENSSAGE, "Surtidor no esta preparado");
                                    break;
                                default:
                                    jsoncp.addProperty(AConstant.STRING_SUCCESS, false);
                                    jsoncp.addProperty(AConstant.STRING_MENSSAGE, "Existe una inconsistencia");
                                    break;
                            }

                            String respuesta = jsoncp.toString();

                            Headers respHeaders = t.getResponseHeaders();
                            respHeaders.add("content-type", "application/json");
                            t.sendResponseHeaders(200, respuesta.getBytes().length);

                            try (OutputStream os = t.getResponseBody()) {
                                os.write(respuesta.getBytes());
                                os.flush();
                            }

                        } else {
                            error = new ErrorResponse();
                            error.setTipo("negocio");
                            error.setCodigo("40019");
                            error.setMensaje("Surtidor ocupado, no es posible realizar el proceso");
                            error.setFechaProceso(new Date().toString());
                            Utils.responseError(t, error);
                        }
                    } catch (NullPointerException e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40001");
                        error.setMensaje("parameter not found '" + parametro + "'");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (Exception e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40000");
                        error.setMensaje("peticion errada");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    }
                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerDetener implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();
                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }
                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                    String parametro = null;
                    try {

                        JsonObject response = new JsonObject();

                        parametro = "surtidor";
                        int ssurtidor = request.get(parametro).getAsInt();

                        parametro = "cara";
                        int scara = request.get(parametro).getAsInt();

                        NeoService.surtidores.get(ssurtidor).control.detenerSurtidor(ssurtidor, scara);

                        JsonObject jsoncp = new JsonObject();
                        jsoncp.addProperty("fechaProceso", sdf.format(new Date()));

                        String respuesta = jsoncp.toString();

                        Headers respHeaders = t.getResponseHeaders();
                        respHeaders.add("content-type", "application/json");
                        t.sendResponseHeaders(200, respuesta.getBytes().length);

                        try (OutputStream os = t.getResponseBody()) {
                            os.write(respuesta.getBytes());
                            os.flush();
                        }

                    } catch (NullPointerException e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40001");
                        error.setMensaje("parameter not found '" + parametro + "'");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    } catch (Exception e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40000");
                        error.setMensaje("peticion errada");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);

                    }
                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerMultiChangePrice implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {

                ErrorResponse error = filters.doFilterInternal(t);
                if (error == null) {

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();
                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }
                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);
                    String parametro = null;
                    try {

                        parametro = "surtidor";
                        int surtidorId = request.get(parametro).getAsInt();

                        parametro = "listaPrecio";
                        int listaPrecio = request.get(parametro).getAsInt();

                        SurtidorDao sdao = new SurtidorDao();
                        EquipoDao dao = new EquipoDao();
                        int CANTIDAD_DIGITOS;
                        try {
                            CANTIDAD_DIGITOS = dao.getParametroInt("cantidad_digitos_s" + surtidorId);
                            if (CANTIDAD_DIGITOS == 0) {
                                NeoService.setLog("SE APLICA LA CANTIDAD DE DIGITOS DEFAULT 4 [cantidad_digitos_s]");
                                CANTIDAD_DIGITOS = 4;
                            } else {

                            }
                        } catch (Exception a) {
                            NeoService.setLog("SE APLICA LA CANTIDAD DE DIGITOS EXCEPTION 4 [cantidad_digitos_s]");
                            CANTIDAD_DIGITOS = 4;
                        }

                        NeoService.setLog("CANTIDAD DE DIGITOS " + CANTIDAD_DIGITOS);
                        boolean estadoDiferenteEspera = false;
                        if (NeoService.surtidores.containsKey(surtidorId)) {

                            Surtidor surtidor = NeoService.surtidores.get(surtidorId);

                            NeoService.setLog("ESPERE QUE EL CORE SE DESOCUPE...");
                            BaseControllerProtocols.TIENE_PETICION.set(true);
                            while (BaseControllerProtocols.ESTA_PROCESANDO.get()) {
                            }

                            NeoService.setLog("BUSCO FACTOR DE PRECIO");
                            int factorPrecio;
                            try {
                                factorPrecio = sdao.getFactorPrecio(surtidorId);
                            } catch (DAOException ex) {
                                factorPrecio = 1;
                            }

                            //Validación para unificación de precios
                            boolean inconsistencia = false;
                            parametro = "data";
                            JsonArray array = request.getAsJsonArray(parametro);
                            TreeMap<Long, ProductoBean> validacionProductoPrecio = new TreeMap<>();
                            parametro = "request";
                            for (JsonElement data : array) {
                                int scara = data.getAsJsonObject().get("cara").getAsInt();
                                if (scara == 2) {
                                    NeoService.setLog("");
                                }
                                for (JsonElement jprecios : data.getAsJsonObject().get("precios").getAsJsonArray()) {
                                    int smanguera = jprecios.getAsJsonObject().get("manguera").getAsInt();
                                    try {

                                        ProductoBean producto = sdao.getProductoIdByCaraAndManguera(surtidorId, scara, smanguera);
                                        if (producto == null) {
                                            throw new Exception("Información de la cara no valida");
                                        }

                                        long sprecio = jprecios.getAsJsonObject().get("precioUnidad").getAsLong();
                                        producto.setPrecio(sprecio);

                                        if (!validacionProductoPrecio.containsKey(producto.getId())) {
                                            validacionProductoPrecio.put(producto.getId(), producto);
                                        } else {
                                            if (validacionProductoPrecio.get(producto.getId()).getPrecio() != sprecio) {
                                                error = new ErrorResponse();
                                                error.setTipo("negocio");
                                                error.setCodigo("40020");
                                                error.setMensaje("EL precio del producto "
                                                        + validacionProductoPrecio.get(producto.getId()).getDescripcion()
                                                        + ", debe ser "
                                                        + ((long) validacionProductoPrecio.get(producto.getId()).getPrecio())
                                                        + " y no " + sprecio
                                                );
                                                error.setFechaProceso(new Date().toString());
                                                Utils.responseError(t, error);
                                                inconsistencia = true;
                                                surtidor.control.TIENE_PETICION.set(false);
                                                break;
                                            }
                                        }
                                    } catch (DAOException a) {
                                        error = new ErrorResponse();
                                        error.setTipo("negocio");
                                        error.setCodigo("40020");
                                        error.setMensaje("Error al ejecutar una sentencia en la base datos");
                                        error.setFechaProceso(new Date().toString());
                                        Utils.responseError(t, error);
                                    }
                                }
                            }
                            NeoService.setLog("EL RESULTADO DE INCONSITENCIA ES? " + inconsistencia);
                            if (!inconsistencia) {
                                for (Map.Entry<Integer, Cara> entry1 : surtidor.getCaras().entrySet()) {
                                    Cara cara = entry1.getValue();
                                    if (cara.getPublicEstadoId() != NeoService.SURTIDORES_PUBLIC_ESTADO_ID_ESPERA) {
                                        NeoService.setLog("ESTADO SURTIDOR  = " + cara.getEstado());
                                        NeoService.setLog("ESTADO PUBLICO   = " + cara.getPublicEstadoId());
                                        NeoService.setLog("ESTADO ESPERA_PUB= " + NeoService.SURTIDORES_PUBLIC_ESTADO_ID_ESPERA);
                                        estadoDiferenteEspera = true;
                                        break;
                                    }
                                }
                                NeoService.setLog("EL SURTIDOR ES DIFERENTE A ESPERA? " + estadoDiferenteEspera);
                                if (!estadoDiferenteEspera) {
                                    String caras = "";
                                    surtidor.control.TIENE_PETICION.set(true);
                                    for (JsonElement data : array) {
                                        int scara = data.getAsJsonObject().get("cara").getAsInt();
                                        for (JsonElement jprecios : data.getAsJsonObject().get("precios").getAsJsonArray()) {
                                            int smanguera = jprecios.getAsJsonObject().get("manguera").getAsInt();
                                            try {
                                                int grado = sdao.getGradoByCaraAndManguera(surtidorId, scara, smanguera);
                                                long precioOriginal = jprecios.getAsJsonObject().get("precioUnidad").getAsInt();
                                                long precio = Utils.calculeCantidadInversa(precioOriginal, factorPrecio);

                                                NeoService.setLog("CAMBIANDO DE PRECIO A [" + precioOriginal + "] CON FACTOR ES [" + precio + "] " + factorPrecio);
                                                NeoService.surtidores.get(surtidorId).control.actualizaMultipreciosPrecios(precio, listaPrecio, grado, scara, false, precioOriginal, CANTIDAD_DIGITOS);
                                                NeoService.setLog("CAMBIO DE PRECIOS APLICADO");
                                            } catch (DAOException a) {
                                                error = new ErrorResponse();
                                                error.setTipo("negocio");
                                                error.setCodigo("40020");
                                                error.setMensaje("Error en la base datos interna");
                                                error.setFechaProceso(new Date().toString());
                                                Utils.responseError(t, error);
                                            }
                                        }
                                    }
                                    surtidor.control.TIENE_PETICION.set(false);

                                    JsonObject response = new JsonObject();
                                    response.addProperty("fechaProceso", sdf.format(new Date()));

                                    Headers respHeaders = t.getResponseHeaders();
                                    respHeaders.add("content-type", "application/json");
                                    t.sendResponseHeaders(200, response.toString().getBytes().length);

                                    try (OutputStream os = t.getResponseBody()) {
                                        os.write(response.toString().getBytes());
                                        os.flush();
                                    }

                                } else {
                                    error = new ErrorResponse();
                                    error.setTipo("negocio");
                                    error.setCodigo("40019");
                                    error.setMensaje("Surtidor ocupado, no es posible realizar el proceso");
                                    error.setFechaProceso(new Date().toString());
                                    Utils.responseError(t, error);
                                    surtidor.control.TIENE_PETICION.set(false);
                                }
                            }
                        }
                    } catch (NullPointerException e) {
                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40001");
                        error.setMensaje("parameter not found '" + parametro + "'");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);
                    } catch (Exception e) {

                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40000");
                        error.setMensaje("peticion errada");
                        error.setFechaProceso(new Date().toString());
                        if (parametro != null && parametro.equals("request")) {
                            error.setMensaje(e.getMessage());
                        }
                        Utils.responseError(t, error);

                    }
                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerStatus implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {
                ErrorResponse error = null; //filters.doFilterInternal(t);
                if (error == null) {

                    //TODO: Partimos del hecho que solo hay un surtidor, queda pendiente consultar varios surtidores
                    int estadoId = NeoService.SURTIDORES_PUBLIC_ESTADO_ID_ESPERA;
                    String estadoDescripcion = NeoService.SURTIDORES_PUBLIC_ESTADO_DS_ESPERA;

                    Headers outHeaders = t.getResponseHeaders();
                    outHeaders.set("Context-Type", "application/json");

                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    int b;
                    StringBuilder buf = new StringBuilder();
                    while ((b = br.read()) != -1) {
                        buf.append((char) b);
                    }
                    NeoService.setLog(t.getRequestURI().toString());
                    NeoService.setLog(buf.toString());

                    JsonObject request = gson.fromJson(buf.toString(), JsonObject.class);

                    JsonObject object = new JsonObject();
                    try {
                        EquipoDao dao = new EquipoDao();
                        EmpresaBean empresa = dao.findEmpresa(Main.credencial);
                        object.addProperty("codigoEstacion", empresa.getCodigo());

                    } catch (DAOException e) {
                    }

                    String parametro = "numeroCara";
                    String snumeroCara = request.get(parametro).getAsString();
                    Utils.validarInt(t, snumeroCara, parametro, NeoService.MINIMO_CARA, NeoService.MAXIMO_CARA);
                    int numeroCara = Integer.parseInt(snumeroCara);

                    SurtidorDao sdao = new SurtidorDao();
                    int surtidor = sdao.getSurtidorPorNumeroCara(numeroCara);

                    parametro = "identificadorProceso";
                    String REQUEST_TOKEN = request.get(parametro).getAsString();
                    object.addProperty(parametro, request.get(parametro).getAsString());
                    Utils.validarUuid(t, REQUEST_TOKEN, parametro);

                    try {

                        int factorPrecio;
                        try {
                            factorPrecio = sdao.getFactorPrecio(surtidor);
                        } catch (DAOException ex) {
                            factorPrecio = 1;
                        }

                        int factorVolumen;
                        try {
                            factorVolumen = sdao.getFactorVolumen(surtidor);
                        } catch (DAOException ex) {
                            factorVolumen = 1;
                        }

                        int factorImporte;
                        try {
                            factorImporte = sdao.getFactorImporte(surtidor);
                        } catch (DAOException ex) {
                            factorImporte = 1;
                        }

                        final int VENTA_SIN_INICIAR = 1;
                        final int VENTA_EN_CURSO = 2;
                        final int VENTA_FINALIZADA = 3;

                        Venta venta = sdao.getVentaPorToken(REQUEST_TOKEN);
                        if (venta != null) {

                            long precio = Utils.calculeCantidad(venta.getActualPrecio(), factorPrecio);
                            double cantidad = Utils.calculeCantidadDouble(venta.getActualVolumen(), factorVolumen);
                            long total = Utils.calculeCantidad(venta.getActualImporte(), factorImporte);

                            switch ((int) venta.getId()) {
                                case VENTA_SIN_INICIAR: {

                                    if (!venta.isVentaFinalizo()) {
                                        CatalogoBean estado = sdao.getEstadoPublicoCaraGrado(venta.getCara(), venta.getGrado());
                                        object.addProperty("estado", estado.getValor());
                                    } else {
                                        object.addProperty("estado", NeoService.SURTIDORES_PUBLIC_ESTADO_DS_FINALIZADA_FEOT);
                                    }
                                    JsonObject jmang = new JsonObject();
                                    jmang.addProperty("surtidor", surtidor);
                                    jmang.addProperty("caraSurtidor", venta.getCara());
                                    jmang.addProperty("manguera", venta.getManguera());
                                    jmang.addProperty("mangueraSurtidor", venta.getGrado());
                                    jmang.addProperty("identificadorFamiliaProducto", venta.getFamiliaProductoId());
                                    jmang.addProperty("familiaProducto", venta.getFamiliaProductoDescripcion());
                                    jmang.addProperty("precioUnidad", precio);

                                    JsonArray array = new JsonArray();
                                    array.add(jmang);
                                    object.remove("manguera");
                                    object.add("manguera", array);
                                    object.addProperty("cantidadVenta", cantidad);
                                    object.addProperty("precioUnidad", precio);
                                    object.addProperty("valorVenta", total);
                                    if (venta.getAutorizacionToken() != null) {

                                        Autorizacion aut = venta.getAutorizacionToken();
                                        object.addProperty("documentoIdentificacionCliente", aut.getDocumentoIdentificacionCliente());
                                        object.addProperty("documentoIdentificacionConductor", aut.getDocumentoIdentificacionConductor());
                                        object.addProperty("nombreCliente", aut.getNombreCliente());
                                        object.addProperty("nombreConductor", aut.getNombreConductor());

                                        object.addProperty("placaVehiculo", aut.getPlacaVehiculo());

                                        object.addProperty("porcentajeDescuentoCliente", aut.getPorcentajeDescuentoCliente());
                                        object.addProperty("montoMaximo", aut.getMontoMaximo());
                                        object.addProperty("cantidadMaxima", aut.getCantidadMaxima());

                                    }
                                    break;
                                }
                                case VENTA_EN_CURSO: {
                                    CatalogoBean estado = sdao.getEstadoPublicoCaraGrado(venta.getCara(), venta.getGrado());
                                    object.addProperty("estado", estado.getValor());

                                    Autorizacion aut = sdao.getAutorizacionPorToken(REQUEST_TOKEN);
                                    JsonObject jmang = new JsonObject();
                                    jmang.addProperty("surtidor", venta.getSurtidorId());
                                    jmang.addProperty("caraSurtidor", venta.getCara());
                                    jmang.addProperty("manguera", venta.getManguera());
                                    jmang.addProperty("mangueraSurtidor", venta.getGrado());
                                    jmang.addProperty("identificadorFamiliaProducto", venta.getFamiliaProductoId());
                                    jmang.addProperty("familiaProducto", venta.getFamiliaProductoDescripcion());
                                    jmang.addProperty("precioUnidad", precio);
                                    JsonArray array = new JsonArray();
                                    array.add(jmang);
                                    object.add("manguera", array);

                                    object.addProperty("cantidadVenta", cantidad);
                                    object.addProperty("precioUnidad", precio);
                                    object.addProperty("valorVenta", total);

                                    object.addProperty("documentoIdentificacionCliente", aut.getDocumentoIdentificacionCliente());
                                    object.addProperty("documentoIdentificacionConductor", aut.getDocumentoIdentificacionConductor());
                                    object.addProperty("nombreCliente", aut.getNombreCliente());
                                    object.addProperty("nombreConductor", aut.getNombreConductor());

                                    object.addProperty("placaVehiculo", aut.getPlacaVehiculo());
                                    object.addProperty("porcentajeDescuentoCliente", aut.getPorcentajeDescuentoCliente());
                                    object.addProperty("montoMaximo", aut.getMontoMaximo());
                                    object.addProperty("cantidadMaxima", aut.getCantidadMaxima());
                                    break;
                                }
                                case VENTA_FINALIZADA:
                                default: {

                                    JsonObject jmang = new JsonObject();
                                    jmang.addProperty("surtidor", venta.getSurtidorId());
                                    jmang.addProperty("caraSurtidor", venta.getCara());
                                    jmang.addProperty("manguera", venta.getManguera());
                                    jmang.addProperty("mangueraSurtidor", venta.getGrado());
                                    jmang.addProperty("identificadorFamiliaProducto", venta.getFamiliaProductoId());
                                    jmang.addProperty("familiaProducto", venta.getFamiliaProductoDescripcion());
                                    jmang.addProperty("precioUnidad", precio);
                                    JsonArray array = new JsonArray();
                                    array.add(jmang);

                                    object.addProperty("estado", NeoService.SURTIDORES_PUBLIC_ESTADO_DS_FINALIZADA_FEOT);
                                    object.add("manguera", array);

                                    object.addProperty("cantidadVenta", cantidad);
                                    object.addProperty("precioUnidad", precio);
                                    object.addProperty("valorVenta", total);

                                    if (venta.getAutorizacionToken() != null) {
                                        Autorizacion aut = venta.getAutorizacionToken();

                                        object.addProperty("documentoIdentificacionCliente", aut.getDocumentoIdentificacionCliente());
                                        object.addProperty("documentoIdentificacionConductor", aut.getDocumentoIdentificacionConductor());
                                        object.addProperty("nombreCliente", aut.getNombreCliente());
                                        object.addProperty("nombreConductor", aut.getNombreConductor());

                                        object.addProperty("placaVehiculo", aut.getPlacaVehiculo());
                                        object.addProperty("porcentajeDescuentoCliente", aut.getPorcentajeDescuentoCliente());
                                        object.addProperty("montoMaximo", aut.getMontoMaximo());
                                        object.addProperty("cantidadMaxima", aut.getCantidadMaxima());
                                    }
                                    break;
                                }
                            }

                            if (object.get("manguera") != null && object.get("manguera").getAsJsonArray().size() > 0) {

                                String response = object.toString();
                                Headers respHeaders = t.getResponseHeaders();
                                respHeaders.add("content-type", "application/json");
                                t.sendResponseHeaders(200, response.length());
                                try (OutputStream os = t.getResponseBody()) {
                                    os.write(response.getBytes());
                                    os.flush();
                                    os.close();
                                }

                            } else {
                                error = new ErrorResponse();
                                error.setTipo("negocio");
                                error.setCodigo("40005");
                                error.setMensaje("familia producto no relacionada en la cara");
                                error.setFechaProceso(new Date().toString());
                                Utils.responseError(t, error);
                            }
                        } else {
                            error = new ErrorResponse();
                            error.setTipo("negocio");
                            error.setCodigo("40006");
                            error.setMensaje("identificador de proceso no registrado");
                            error.setFechaProceso(new Date().toString());
                            Utils.responseError(t, error);
                        }

                    } catch (DAOException ex) {
                        error = new ErrorResponse();
                        error.setTipo("negocio");
                        error.setCodigo("40006");
                        error.setMensaje("identificador de proceso no valido");
                        error.setFechaProceso(new Date().toString());
                        Utils.responseError(t, error);
                    }
                } else {
                    Utils.responseError(t, error);
                }
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }
    }

    private class HttpHandlerApi implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            if (method.equalsIgnoreCase(Butter.POST)) {

                Headers outHeaders = t.getResponseHeaders();
                outHeaders.set("Context-Type", "application/json");

                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);

                int b;
                StringBuilder buf = new StringBuilder();
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }
                NeoService.setLog(buf.toString());

                //String peticiontext = buf.toString();
                String peticiontext = procesaTrama(buf.toString());

                // String response = "This is the response";
                Headers respHeaders = t.getResponseHeaders();
                respHeaders.add("content-type", "application/json");
                t.sendResponseHeaders(200, peticiontext.getBytes().length);
                OutputStream os = t.getResponseBody();
                os.write(peticiontext.getBytes());
                os.flush();
                os.close();
            } else {
                ErrorResponse error = new ErrorResponse();
                error.setTipo("negocio");
                error.setStatusCode(ErrorResponse.SC_METHOD_NOT_ALLOWED);
                error.setCodigo(ErrorResponse.ERROR_40500_ID);
                error.setMensaje(ErrorResponse.ERROR_40500_DESC);
                error.setFechaProceso(new Date().toString());
                Utils.responseError(t, error);
            }
        }

        public String procesaTrama(String trama) {
            String respuesta = new String();
            Notificacion notification = gson.fromJson(trama, Notificacion.class);
            if (notification.getTipo() != 0) {
                switch (notification.getTipo()) {
                    case Notificacion.ERROR:
                        break;
                    case Notificacion.IMPRIMIR:

                        PrinterFacade fcade = new PrinterFacade();
                        fcade.conFormatoIcono(notification.getIcono(), notification.getMensaje());

                        JsonObject json = new JsonObject();
                        json.addProperty("success", true);
                        json.addProperty("mensaje", "DOCUMENTO IMPRESO CORRECTAMENTE");
                        respuesta = json.toString();

                        break;
                    case Notificacion.COMANDO:
                        respuesta = subcomando(notification);
                        break;
                    default:
                        NeoService.setLog("Notificacion no implementada");
                        break;
                }
            }
            return respuesta;
        }

        public String subcomando(Notificacion notificacion) {
            String respuesta = "";
            SurtidorDao dao = new SurtidorDao();
            switch (notificacion.getSubtipo()) {
                case Notificacion.SUB_COMANDO_ARREGLA_SALTO_LECTURA:
                    NeoService.setLog("SUB_COMANDO_ARREGLA_SALTO_LECTURA");
                    Manguera m = gson.fromJson(notificacion.getPaquete().toString(), Manguera.class);
                    NeoService.sutidao.corrigeSaltoLectura(m);
                    int surtidor = dao.getSurtidorByConfiguracionID(m.getConfiguracionId());

                    JsonObject jsonxx = new JsonObject();
                    if (surtidor > -1) {
                        NeoService.surtidores.get(surtidor).control.setAjusteSaltoLectura(m.getConfiguracionId(), false);
                        jsonxx.addProperty("success", true);
                        jsonxx.addProperty("mensaje", "SALTO DE LECTURA CORREGIDO");
                    } else {
                        jsonxx.addProperty("success", false);
                        jsonxx.addProperty("mensaje", "SURTIDOR NO ENCONTRADO");

                    }
                    respuesta = jsonxx.toString();
                    break;
                case Notificacion.SUB_COMANDO_INICIAR_JORNADA:
                    NeoService.setLog("//METODO SUB_COMANDO_INICIAR_JORNADA DEPRECADO");
                    break;
                case Notificacion.SUB_COMANDO_FINALIZAR_JORNADA:
                    NeoService.setLog("//METODO SUB_COMANDO_FINALIZAR_JORNADA DEPRECADO");
                    break;
                case Notificacion.SUB_COMANDO_SOLICITAR_TOTALIZADORES:
                    NeoService.setLog("SUB_COMANDO_SOLICITAR_TOTALIZADORES");
                    Cara cara = gson.fromJson(notificacion.getPaquete().toString(), Cara.class);
                    Totalizador[] totales = NeoService.surtidores.get(1).control.getTotalizadoresByCara(cara);

                    JsonArray jsonArray = new JsonArray();
                    for (Totalizador totale : totales) {
                        JsonObject json = new JsonObject();
                        json.addProperty("cara", totale.getCara());
                        json.addProperty("manguera", totale.getManguera());
                        json.addProperty("grado", totale.getGrado());

                        json.addProperty("acumuladoVolumen", Long.toString(totale.getAcumuladoVolumen()));
                        json.addProperty("acumuladoVenta", Long.toString(totale.getAcumuladoVenta()));
                        jsonArray.add(json);
                    }
                    respuesta = jsonArray.toString();
                    break;
                case Notificacion.SUB_COMANDO_ULTIMA_VENTA:
                    NeoService.setLog("SUB_COMANDO_ULTIMA_VENTA");
                    Boolean detallado = notificacion.isDetallado();
                    Manguera mangueraUltimaVenta = gson.fromJson(notificacion.getPaquete().toString(), Manguera.class);
                    try {
                        Recibo recibo = dao.getUltimaVenta(mangueraUltimaVenta.getConfiguracionId());
                        if (recibo != null) {
                            PrinterFacade facade = new PrinterFacade();
                            facade.printRecibo(recibo);
                            dao.documentoImpreso(recibo.getNumero());

                            if (!detallado) {
//                                Response jresult = new Response();
//                                jresult.setSuccess(true);
//                                jresult.setMensaje("DOCUMENTO IMPRESO CORRECTAMENTE");

                                JsonObject json = new JsonObject();
                                json.addProperty("success", true);
                                json.addProperty("mensaje", "DOCUMENTO IMPRESO CORRECTAMENTE");
                                respuesta = json.toString();

                            } else {
                                respuesta = recibo.toString();
                            }
                        }
                    } catch (DAOException ex) {
                        Logger.getLogger(NotificacionSocket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Notificacion.SUB_COMANDO_LISTA_ULTIMA_VENTA:
                    NeoService.setLog("SUB_COMANDO_LISTA_ULTIMA_VENTA");
                    try {
                        List<Recibo> recibos = dao.getUltimasVenta(notificacion.getCriterios());
                        respuesta = recibos.toString();
                    } catch (DAOException ex) {
                        Logger.getLogger(NotificacionSocket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Notificacion.SUB_COMANDO_RECIBO_VENTA:
                    NeoService.setLog("SUB_COMANDO_RECIBO_VENTA");
                    Recibo rec = gson.fromJson(notificacion.getPaquete().toString(), Recibo.class);
                    try {
                        Recibo recibo = dao.getReciboVenta(rec.getNumero());
                        if (rec.getPlaca() != null) {
                            recibo.setPlaca(rec.getPlaca());
                            dao.actualizaReciboPlaca(recibo);
                        }

                        if (recibo != null) {
                            PrinterFacade facade = new PrinterFacade();
                            facade.printRecibo(recibo);
                            dao.documentoImpreso(recibo.getNumero());

                            JsonObject json = new JsonObject();
                            json.addProperty("success", true);
                            json.addProperty("mensaje", "DOCUMENTO IMPRESO CORRECTAMENTE");
                            respuesta = json.toString();

                        }
                    } catch (DAOException ex) {
                        Logger.getLogger(NotificacionSocket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Notificacion.SUB_COMANDO_MEDIOS_PAGOS:
                    NeoService.setLog("SUB_COMANDO_MEDIOS_PAGOS");
                    List<MedioPago> pagos = dao.getMediosPagos();
                    respuesta = pagos.toString();
                    break;
                case Notificacion.SUB_COMANDO_ACTUALIZA_RECIBO_VENTA:
                    NeoService.setLog("SUB_COMANDO_RECIBO_VENTA");
                    Recibo rec1 = gson.fromJson(notificacion.getPaquete().toString(), Recibo.class);
                    try {
                        dao.actualizaReciboMedio(rec1);
                        JsonObject json = new JsonObject();
                        json.addProperty(AConstant.STRING_SUCCESS, true);
                        respuesta = json.toString();
                    } catch (Exception ex) {
                        Logger.getLogger(NotificacionSocket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Notificacion.SUB_COMANDO_SIMULA_RFID:
                    JsonObject person = gson.fromJson(notificacion.getPaquete().toString(), JsonObject.class);
                    NeoService.PERSONA_AUTORIZA_ID.set(person.get("personaId").getAsLong());
                    NeoService.REGISTRO_PERSONA.put(person.get("cara").getAsInt(), person.get("personaId").getAsLong());
                    NeoService.AUTORIZACION.put(person.get("cara").getAsInt(), new Date());
                    JsonObject json = new JsonObject();
                    json.addProperty(AConstant.STRING_SUCCESS, true);
                    respuesta = json.toString();
                    break;
                default:
                    break;
            }
            return respuesta;
        }

    }

    public void imprimirUltimoRecibo(long configurcionId) {
        SurtidorDao dao = new SurtidorDao();
        Manguera mangueraUltimaVenta = new Manguera();
        mangueraUltimaVenta.setConfiguracionId(configurcionId);
        try {
            Recibo recibo = dao.getUltimaVenta(mangueraUltimaVenta.getConfiguracionId());
            if (recibo != null) {
                PrinterFacade facade = new PrinterFacade();
                facade.printRecibo(recibo);
                dao.documentoImpreso(recibo.getNumero());
            }
        } catch (DAOException ex) {
            Logger.getLogger(NotificacionSocket.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

}
