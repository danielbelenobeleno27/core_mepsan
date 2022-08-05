package com.butter.bean;

import com.core.app.NeoService;
import com.neo.app.bean.MediosPagosBean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.core.database.DAOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author novus
 */
public class SetupAsync extends Thread {

    SetupView vpanel;
    JDialog dialog;
    JLabel dialogLabel;
    boolean success;
    String mensaje;
    JsonObject request;
    JsonObject response;
    Object params;

    int CONTACTO_TIPO_CORREO = 1;
    int CONTACTO_TIPO_TELEFONO = 2;
    int CONTACTO_TIPO_DIRECCION = 3;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getParams() {
        return params;
    }

    public SetupAsync(SetupView vpanel) {
        this.vpanel = vpanel;
    }

    public SetupAsync(JDialog dialog, JLabel dialogLabel) {
        this.dialog = dialog;
        this.dialogLabel = dialogLabel;
    }

    public JsonObject getRequest() {
        return request;
    }

    public void setRequest(JsonObject request) {
        this.request = request;
    }

    public JsonObject getResponse() {
        return response;
    }

    public void setResponse(JsonObject response) {
        this.response = response;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public void run() {

        if (dialog == null) {
            int puntos = 0;

            String texto = "Sincronizando";
            for (int i = 0; i < puntos; i++) {
                texto += ".";
            }
            vpanel.jLabel3.setText(texto);

            descargaDatos();
            descargaCategorias();
            descargarProductos();
            descargaBodegas();
            descargaPersonal();
            descargaMediosPagos();
        } else {
            descargaFuncion();
        }
    }

    public void descargaDatos() {

        JsonObject json = new JsonObject();
        json.addProperty("empresas_id", Main.credencial.getEmpresas_id());

        String url = Butter.SECURE_END_POINT_EMPRESA;
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DATOS BASICOS DE LA EMPRESA",
                Main.credencial,
                url,
                Butter.POST,
                json,
                Butter.ENABLE_DEBUG
        );
        long startTime = System.nanoTime();
        async.start();
        try {
            async.join();
            JsonObject response = async.getResponse();

            JsonArray array = response.getAsJsonArray("data");
            int size = array.size();
            if (vpanel != null) {
                vpanel.jdatos.setText("<html><font color=yellow>DATOS BASICOS DESCARGANDO...</font></html>");
            }

            SetupDao pdao = new SetupDao();
            int insert = 0;

            for (int i = 0; i < size; i++) {
                if (vpanel != null) {
                    vpanel.jdatos.setText("<html><font color=yellow>DATOS BASICOS PROCESANDO...</font></html>");
                }
                try {
                    JsonObject jemp = response.get("data").getAsJsonArray().get(0).getAsJsonObject();

                    EmpresaBean empresa = new EmpresaBean();
                    empresa.setId(jemp.get("id").getAsLong());
                    empresa.setRazonSocial(jemp.get("razon_social").getAsString());
                    empresa.setDominioId(jemp.get("dominio_id").getAsLong());
                    empresa.setNit(jemp.get("nit").getAsString());
                    empresa.setLocalizacion(jemp.get("localizacion").getAsString());

                    empresa.setEmpresasId(jemp.get("empresas_id").getAsLong());
                    empresa.setCiudadId(jemp.get("c_id").getAsLong());
                    empresa.setCiudadDescripcion(jemp.get("c_descripcion").getAsString());
                    empresa.setCiudadZonaHoraria(jemp.get("zona_horaria").getAsString());
                    empresa.setCiudadIndicador(jemp.get("indicadores").getAsInt());

                    empresa.setProvinciaId(jemp.get("pr_id").getAsLong());
                    empresa.setProvinciaDescripcion(jemp.get("pr_descripcion").getAsString());

                    empresa.setPaisId(jemp.get("pa_id").getAsLong());
                    empresa.setPaisDescripcion(jemp.get("pa_descripcion").getAsString());
                    empresa.setPaisMoneda(jemp.get("moneda").getAsString());
                    empresa.setPaisIndicador(jemp.get("indicador").getAsInt());
                    empresa.setPaisNomenclatura(jemp.get("nomenclatura").getAsString());

                    empresa.setUrlFotos(jemp.get("url_foto").getAsString());

                    if (jemp.get("contactos") != null && !jemp.get("contactos").isJsonNull()) {
                        JsonArray catg = jemp.getAsJsonArray("contactos").getAsJsonArray();
                        empresa.setContacto(new ArrayList<>());
                        for (JsonElement elemt : catg) {
                            ContactoBean contacto = new ContactoBean();
                            contacto.setId(elemt.getAsJsonObject().get("id").getAsLong());
                            contacto.setTipo(elemt.getAsJsonObject().get("tipo").getAsInt());
                            contacto.setEtiqueta(elemt.getAsJsonObject().get("etiqueta").getAsString());
                            contacto.setContacto(elemt.getAsJsonObject().get("contacto").getAsString());
                            contacto.setEstado(elemt.getAsJsonObject().get("estado").getAsString());
                            contacto.setPrincipal(elemt.getAsJsonObject().get("principal").getAsString().contains("S"));
                            empresa.getContacto().add(contacto);

                            if (contacto.getTipo() == CONTACTO_TIPO_CORREO && empresa.getCorreo()==null) {
                                empresa.setCorreo(contacto.getContacto());
                            }
                            
                            if (contacto.getTipo() == CONTACTO_TIPO_DIRECCION && empresa.getDireccion()==null) {
                                empresa.setDireccion(contacto.getContacto());
                            }
                            
                            if (contacto.getTipo() == CONTACTO_TIPO_TELEFONO && empresa.getTelefonos()==null) {
                                empresa.setTelefonos(contacto.getContacto());
                            }
                        }
                    }

                    if (jemp.get("descriptores") != null && !jemp.get("descriptores").isJsonNull()) {
                        empresa.setDescriptorId(jemp.get("descriptores").getAsJsonObject().get("id").getAsLong());
                        empresa.setDescriptorHeader(jemp.get("descriptores").getAsJsonObject().get("header").getAsString());
                        empresa.setDescriptorFooter(jemp.get("descriptores").getAsJsonObject().get("footer").getAsString());
                    }
                    EquipoDao dao = new EquipoDao();
                    dao.createEmpresas(empresa);
                    Main.credencial.setEmpresa(empresa);

                } catch (DAOException ex) {
                    NeoService.setLog(ex.getMessage());
                } catch (Exception ex) {
                    NeoService.setLog(ex.getMessage());
                }
                insert++;
            }
            if (vpanel != null) {
                vpanel.jdatos.setText("<html><font color='#00FF00'>DATOS BASICOS OK</font></html>");
            }
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            NeoService.setLog("Execution time in milliseconds : " + timeElapsed / 1000000);

        } catch (Exception a) {
            NeoService.setLog(a.getMessage());
        }

    }

    private void descargarProductos() {
        vpanel.jproductos.setText("<html><font color=yellow>PRODUCTOS</font></html>");
        JsonObject json = new JsonObject();

        String url = Butter.SECURE_END_POINT_PRODUCTOS_POS_ACUERDOS + "/" + Main.credencial.getEmpresas_id();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "CONSULTA DE ESTADO",
                Main.credencial,
                url,
                Butter.GET,
                json,
                Butter.ENABLE_DEBUG
        );
        long startTime = System.nanoTime();
        async.start();
        try {
            async.join();
            JsonObject response = async.getResponse();

            JsonArray array = response.getAsJsonArray("Productos");
            int size = array.size();
            vpanel.jproductos.setText("<html><font color=yellow>PRODUCTOS DESCARGADOS 0/" + size + "</font></html>");

            SetupDao pdao = new SetupDao();
            int insert = 0;

            try {
                pdao.borraTodosImpuestos();
            } catch (DAOException a) {
            } catch (Exception a) {
            }

            for (int i = 0; i < size; i++) {
                vpanel.jproductos.setText("<html><font color=yellow>PRODUCTOS DESCARGADOS " + insert + "/" + size + "</font></html>");
                try {
                    JsonObject jprd = array.get(i).getAsJsonObject();
                    ProductoBean p = ProductoBean.fromJson(jprd, Main.credencial.getEmpresas_id());

                    pdao.createBloqueado(insert, p, Main.credencial);

                    JsonElement arrayIngre = jprd.get("ingredientes");
                    if (arrayIngre != null && !arrayIngre.isJsonNull()) {
                        p.setIngredientes(new ArrayList<>());
                        for (JsonElement elemento : arrayIngre.getAsJsonArray()) {
                            JsonObject jsons = elemento.getAsJsonObject();
                            ProductoBean ingrediente = new ProductoBean();
                            ingrediente.setId(jsons.get("id").getAsLong());
                            ingrediente.setProducto_compuesto_id(jsons.get("pc_id").getAsLong());
                            ingrediente.setProducto_compuesto_cantidad(jsons.get("cantidad").getAsLong());

                            p.getIngredientes().add(ingrediente);
                        }
                    }

                    MovimientosDao mdao = new MovimientosDao();
                    mdao.limpiarIngredientes(p.getId());
                    for (ProductoBean ingrediente : p.getIngredientes()) {
                        ProductoBean temp = mdao.findProductByIdActive(ingrediente.getId());
                        if (temp == null) {
                            temp = descargaProductoWS(ingrediente.getId() + "");
                        }
                        if (temp != null) {
                            temp.setProducto_compuesto_id(ingrediente.getProducto_compuesto_id());
                            temp.setProducto_compuesto_cantidad(ingrediente.getProducto_compuesto_cantidad());
                            mdao.integrarProdcto(p, temp);
                        }

                    }
                    pdao.actualiazaEstado(p);
                    insert++;
                } catch (DAOException ex) {
                    NeoService.setLog(ex.getMessage());
                } catch (Exception ex) {
                    NeoService.setLog(ex.getMessage());
                }
            }

            vpanel.jproductos.setText("<html><font color='#00FF00'>PRODUCTOS FINALIZADO " + insert + "/" + size + "</font></html>");

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            NeoService.setLog("Execution time in milliseconds : " + timeElapsed / 1000000);

        } catch (Exception a) {

        }
    }

    /**
     *
     * @param id
     * @return
     */
    public ProductoBean descargaProductoWSV2(String id) {
        ProductoBean bean = null;
        String url = Butter.SECURE_END_POINT_PRODUCTOS_DETALLES_FULL;
        JsonObject json = new JsonObject();
        json.addProperty("empresas_id", Main.credencial.getEmpresas_id());
        json.addProperty("bodegas_id", Main.credencial.getBodegaId());
        json.addProperty("producto_id", id);

        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "CONSULTA DE DETALLE DEL PRODUCTO FULL V2",
                Main.credencial,
                url,
                Butter.POST,
                json,
                Butter.ENABLE_DEBUG
        );
        async.start();

        try {
            async.join();
        } catch (Exception e) {

        }
        try {

            JsonArray array = async.getResponse().get("data").getAsJsonArray();

            for (JsonElement jsonElement : array) {
                JsonObject response = jsonElement.getAsJsonObject();//.getAsJsonObject("producto");
                JsonObject producto = jsonElement.getAsJsonObject().getAsJsonObject("producto");

                if (!response.getAsJsonObject().get("identificadores").isJsonNull()) {
                    if (jsonElement.getAsJsonObject().getAsJsonArray("identificadores") != null) {
                        JsonArray identificadores = jsonElement.getAsJsonObject().getAsJsonArray("identificadores");
                        producto.add("identificadores", identificadores);
                    }
                }

                if (!response.getAsJsonObject().get("productos_impuestos").isJsonNull()) {
                    if (response.getAsJsonObject().getAsJsonArray("productos_impuestos") != null) {
                        JsonArray identificadores = response.getAsJsonObject().getAsJsonArray("productos_impuestos");
                        producto.add("productos_impuestos", identificadores);
                    }
                }

                bean = ProductoBean.fromJson(producto, Main.credencial.getEmpresas_id());
                SetupDao dao = new SetupDao();

                dao.borraTodosIdentificadores(bean.getId());
                dao.createBloqueado(1, bean, Main.credencial);

                if (response.get("ingredientes") != null && !response.get("ingredientes").isJsonNull()) {
                    bean.setIngredientes(new ArrayList<>());
                    for (JsonElement elemento : response.get("ingredientes").getAsJsonArray()) {

                        JsonObject jsoning = elemento.getAsJsonObject().get("productos_compuestos").getAsJsonObject();
                        ProductoBean ingrediente = new ProductoBean();
                        ingrediente.setProducto_compuesto_id(jsoning.get("id").getAsLong()); //RELACION ID
                        ingrediente.setId(jsoning.get("ingredientes_id").getAsLong());
                        ingrediente.setProducto_compuesto_cantidad(jsoning.get("cantidad").getAsLong());
                        bean.getIngredientes().add(ingrediente);

                        JsonObject jsonIngrediente = elemento.getAsJsonObject();
                        JsonObject ingredienteCompleto = elemento.getAsJsonObject().getAsJsonObject("producto");

                        if (!jsonIngrediente.getAsJsonObject().get("identificadores").isJsonNull()) {
                            if (jsonIngrediente.getAsJsonObject().getAsJsonArray("identificadores") != null) {
                                JsonArray identificadores = jsonIngrediente.getAsJsonObject().getAsJsonArray("identificadores");
                                ingredienteCompleto.add("identificadores", identificadores);
                            }
                        }

                        if (!jsonIngrediente.getAsJsonObject().get("productos_impuestos").isJsonNull()) {
                            if (jsonIngrediente.getAsJsonObject().getAsJsonArray("productos_impuestos") != null) {
                                JsonArray identificadores = jsonIngrediente.getAsJsonObject().getAsJsonArray("productos_impuestos");
                                ingredienteCompleto.add("productos_impuestos", identificadores);
                            }
                        }

                        ProductoBean ingredienteComp = ProductoBean.fromJson(ingredienteCompleto, Main.credencial.getEmpresas_id());
                        dao.borraTodosIdentificadores(ingredienteComp.getId());
                        dao.createBloqueado(1, ingredienteComp, Main.credencial);
                        dao.actualiazaEstado(ingredienteComp);

                    }

                    MovimientosDao mdao = new MovimientosDao();
                    mdao.limpiarIngredientes(bean.getId());

                    if (!bean.getIngredientes().isEmpty()) {
                        for (ProductoBean ingrediente : bean.getIngredientes()) {
                            mdao.integrarProdcto(bean, ingrediente);
                        }
                    }

                    dao.actualiazaEstado(bean);
                } else {
                    dao.actualiazaEstado(bean);
                }
            }

        } catch (DAOException e) {

        }
        return bean;
    }

    public void descargarInvetarioBodega(String id) {
        String url = Butter.SECURE_END_POINT_BODEGAS_PRODUCTOS + "/" + Main.credencial.getBodegaId() + "/" + id;
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "CONSULTA DE PRODUCTOS EMPRESAS",
                Main.credencial,
                url,
                Butter.GET,
                new JsonObject(),
                Butter.ENABLE_DEBUG
        );
        async.start();

        try {
            async.join();
            JsonObject response = async.getResponse().get("data").getAsJsonObject();

            boolean success = async.getResponse().get("success").getAsBoolean();

            ProductoBean prd = new ProductoBean();
            if (success) {
                prd.setBodega_producto_id(response.get("id").getAsLong());
                prd.setId(response.get("productos_id").getAsLong());
                prd.setSaldo(response.get("saldo").getAsFloat());
                prd.setCantidadMaxima(response.get("cantidad_maxima").getAsInt());
                prd.setCantidadMaxima(response.get("cantidad_minima").getAsInt());
                prd.setProducto_compuesto_costo(response.get("costo").getAsFloat());
            } else {
                prd.setId(Long.parseLong(id));
            }

            if (prd.getBodega_producto_id() != 0) {
                SetupDao dao = new SetupDao();
                dao.updateInventario(prd, Main.credencial);
            }

        } catch (DAOException e) {
            NeoService.setLog("ERROR AL GUARDAR INVENTARIOS DEL PRODUCTO");
        } catch (InterruptedException ex) {
            Logger.getLogger(MqttNotify.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Descarga un producto desde webservice, lo crea bloqueado en caso de que
     * no tenga ingredientes, lo desbloquea.
     *
     * @param id
     * @return
     */
    public ProductoBean descargaProductoWS(String id) {
        ProductoBean bean = null;
        String url = Butter.SECURE_END_POINT_PRODUCTOS_DETALLES + "/" + id + "/" + Main.credencial.getEmpresas_id();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "CONSULTA DE DETALLE DEL PRODUCTO",
                Main.credencial,
                url,
                Butter.GET,
                new JsonObject(),
                Butter.ENABLE_DEBUG
        );
        async.start();

        try {
            async.join();
        } catch (Exception e) {

        }
        try {
            JsonObject response = async.getResponse().get("data").getAsJsonObject().getAsJsonObject("producto");

            if (!async.getResponse().get("data").getAsJsonObject().get("identificadores").isJsonNull()) {
                if (async.getResponse().get("data").getAsJsonObject().getAsJsonArray("identificadores") != null) {
                    JsonArray identificadores = async.getResponse().get("data").getAsJsonObject().getAsJsonArray("identificadores");
                    response.add("identificadores", identificadores);
                }
            }

            if (!async.getResponse().get("data").getAsJsonObject().get("proveedores_productos").isJsonNull()) {
                if (async.getResponse().get("data").getAsJsonObject().getAsJsonArray("proveedores_productos") != null) {
                    JsonArray identificadores = async.getResponse().get("data").getAsJsonObject().getAsJsonArray("proveedores_productos");
                    response.add("proveedores_productos", identificadores);
                }
            }

            if (!async.getResponse().get("data").getAsJsonObject().get("productos_impuestos").isJsonNull()) {
                if (async.getResponse().get("data").getAsJsonObject().getAsJsonArray("productos_impuestos") != null) {
                    JsonArray identificadores = async.getResponse().get("data").getAsJsonObject().getAsJsonArray("productos_impuestos");
                    response.add("productos_impuestos", identificadores);
                }
            }

            bean = ProductoBean.fromJson(response, Main.credencial.getEmpresas_id());
            SetupDao dao = new SetupDao();
            dao.createBloqueado(1, bean, Main.credencial);

            JsonObject responseIng = async.getResponse().get("data").getAsJsonObject();

            if (responseIng.get("ingredientes") != null && !responseIng.get("ingredientes").isJsonNull()) {
                bean.setIngredientes(new ArrayList<>());
                for (JsonElement elemento : responseIng.get("ingredientes").getAsJsonArray()) {
                    JsonObject json = elemento.getAsJsonObject();
                    ProductoBean ingrediente = new ProductoBean();
                    ingrediente.setId(json.get("id").getAsLong());
                    ingrediente.setProducto_compuesto_id(json.get("pc_id").getAsLong());
                    ingrediente.setProducto_compuesto_cantidad(json.get("cantidad").getAsLong());

                    bean.getIngredientes().add(ingrediente);
                }
            } else {
                dao.actualiazaEstado(bean);
            }
        } catch (DAOException e) {

        }
        return bean;
    }

    private void descargaBodegas() {
        vpanel.jbodegas.setText("<html><font color=yellow>DESCARGANDO BODEGA...</font></html>");
        JsonObject json = new JsonObject();

        String url = Butter.SECURE_END_POINT_BODEGA + "/equipo/" + Main.credencial.getId();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA DE BODEGA",
                Main.credencial,
                url,
                Butter.GET,
                json,
                Butter.ENABLE_DEBUG
        );
        long startTime = System.nanoTime();
        async.start();
        try {
            vpanel.jbodegas.setText("<html><font color=yellow>BODEGA</font></html>");
            async.join();

            JsonObject response = async.getResponse();

            if (response == null) {
                vpanel.jbodegas.setText("<html><font color=red>SIN INFORMACIÓN DE LA BODEGA</font></html>");
                vpanel.jTextArea1.setText("Sin configuracion asignada a: \r\n" + url);
                throw new NullPointerException("Sin configuración de la bodega");
            }

            JsonObject object = response.get("bodega").getAsJsonObject();

            BodegaBean bodega = new BodegaBean();
            bodega.setId(object.get("id").getAsLong());
            bodega.setEmpresaId(object.get("empresas_id").getAsLong());
            bodega.setDescripcion(object.get("descripcion").getAsString());
            bodega.setCodigo(object.get("codigo").getAsString());
            bodega.setEstado(object.get("estado").getAsString());
            bodega.setDimension(object.get("dimension").getAsString());
            bodega.setUbicacion(object.get("ubicacion").getAsString());

            JsonArray products = null;
            if (object.get("productos") != null && !object.get("productos").isJsonNull()) {
                products = object.get("productos").getAsJsonArray();
                bodega.setProductos(new ArrayList<>());
            } else {
                products = new JsonArray();
            }

            if (response.get("consecutivo") != null && !response.get("consecutivo").isJsonNull()) {
                JsonArray catg = response.getAsJsonArray("consecutivo").getAsJsonArray();
                bodega.setConsecutivos(new ArrayList<>());

                for (JsonElement elemt : catg) {
                    ConsecutivoBean cons = new ConsecutivoBean();
                    cons.setId(elemt.getAsJsonObject().get("id").getAsLong());
                    cons.setTipo_documento(elemt.getAsJsonObject().get("tipo_documento").getAsInt());
                    cons.setPrefijo(elemt.getAsJsonObject().get("prefijo").getAsString());
                    cons.setFecha_inicio(Utils.stringToDate(elemt.getAsJsonObject().get("fecha_inicio").getAsString()));

                    if (!elemt.getAsJsonObject().get("fecha_fin").isJsonNull()) {
                        cons.setFecha_fin(Utils.stringToDate(elemt.getAsJsonObject().get("fecha_fin").getAsString()));
                    }

                    cons.setConsecutivo_inicial(elemt.getAsJsonObject().get("consecutivo_inicial").getAsLong());

                    if (!elemt.getAsJsonObject().get("consecutivo_final").isJsonNull()) {
                        cons.setConsecutivo_final(elemt.getAsJsonObject().get("consecutivo_final").getAsLong());
                    }

                    if (!elemt.getAsJsonObject().get("consecutivo_actual").isJsonNull()) {
                        cons.setConsecutivo_actual(elemt.getAsJsonObject().get("consecutivo_actual").getAsLong());
                    }

                    cons.setEstado(elemt.getAsJsonObject().get("estado").getAsString());

                    if (!elemt.getAsJsonObject().get("resolucion").isJsonNull()) {
                        cons.setResolucion(elemt.getAsJsonObject().get("resolucion").getAsString());
                    }

                    if (!elemt.getAsJsonObject().get("observaciones").isJsonNull()) {
                        cons.setObservaciones(elemt.getAsJsonObject().get("observaciones").getAsString());
                    }

                    if (!elemt.getAsJsonObject().get("equipos_id").isJsonNull()) {
                        cons.setEquipo_id(elemt.getAsJsonObject().get("equipos_id").getAsLong());
                    }
                    bodega.getConsecutivos().add(cons);
                }
            }

            SetupDao pdao = new SetupDao();

            int cant = products.size();
            int i = 0;
            vpanel.jbodegas.setText("<html><font color='#00FF00'>BODEGA C:" + bodega.getCodigo() + " CANT. " + cant + "</font></html>");

            Main.credencial.setBodegaId(bodega.getId());
            pdao.createBodega(bodega, Main.credencial);
            if (bodega.getConsecutivos() != null) {
                try {
                    pdao.createConsecutivos(bodega.getConsecutivos(), Main.credencial);
                    vpanel.jconsecutivos.setText("<html><font color='#00FF00'>CONSECUTIVOS</font></html>");
                } catch (DAOException r) {
                    vpanel.jconsecutivos.setText("<html><font color=yellow>CONSECUTIVOS NO PROCESADOS</font></html>");
                }

            } else {
                vpanel.jconsecutivos.setText("<html><font color=yellow>SIN CONSECUTIVOS</font></html>");
            }

            vpanel.Jkardex.setText("<html><font color=yellow>KARDEX PROCESANDO</font></html>");

            for (JsonElement product : products) {
                i++;
                try {
                    ProductoBean pp = new ProductoBean();
                    pp.setBodega_producto_id(product.getAsJsonObject().get("id").getAsLong());
                    pp.setId(product.getAsJsonObject().get("productos_id").getAsLong());
                    pp.setSaldo(product.getAsJsonObject().get("saldo").getAsLong());
                    pp.setCantidadMinima(product.getAsJsonObject().get("cantidad_minima").getAsInt());
                    pp.setCantidadMaxima(product.getAsJsonObject().get("cantidad_maxima").getAsInt());
                    if (!product.getAsJsonObject().get("tiempo_reorden").isJsonNull()) {
                        pp.setTiempoReorden(product.getAsJsonObject().get("tiempo_reorden").getAsInt());
                    } else {
                        pp.setTiempoReorden(0);
                    }
                    if (!product.getAsJsonObject().get("costo").isJsonNull()) {
                        pp.setProducto_compuesto_costo(product.getAsJsonObject().get("costo").getAsFloat());
                    } else {
                        pp.setProducto_compuesto_costo(0);
                    }
                    bodega.getProductos().add(pp);
                } catch (Exception a) {
                    NeoService.setLog(product.toString());
                    vpanel.jTextArea1.setText(product.toString());
                }
            }

            i = 0;
            for (ProductoBean producto : bodega.getProductos()) {
                try {
                    pdao.procerInventarioXX(i, bodega, producto);
                    i++;
                    vpanel.Jkardex.setText("<html><font color='#00FF00'>KARDEX DE PRODUCTOS " + i + "/" + cant + "</font></html>");
                } catch (DAOException e) {
                    NeoService.setLog("Error almacenando el inventario");
                }
            }

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            NeoService.setLog("Execution time in milliseconds : " + timeElapsed / 1000000);

        } catch (Exception a) {
            NeoService.setLog("ErrorGeneral general al sincronizar bodegas");
            NeoService.setLog(a.getMessage());
            NeoService.setLog("----");
        } catch (DAOException ex) {
            vpanel.jbodegas.setText("<html><font color=red>DESCARGANDO BODEGA... E2</font></html>");
            NeoService.setLog("ErrorDAO general al sincronizar bodegas");
        }
    }

    public void descargaPersonal() {
        JsonObject json = new JsonObject();

        String url = Butter.SECURE_END_POINT_PERSONAL + "/empresa/" + Main.credencial.getEmpresas_id();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "CONSULTA DE ESTADO",
                Main.credencial,
                url,
                Butter.GET,
                json,
                Butter.ENABLE_DEBUG
        );

        long startTime = System.nanoTime();
        async.start();
        try {
            async.join();
            JsonObject response = async.getResponse();

            JsonArray array = response.getAsJsonArray("data");
            int size = array.size();
            if (vpanel != null) {
                vpanel.jpersonal.setText("<html><font color=yellow>PERSONAL DESCARGADOS 0/" + size + "</font></html>");
            }

            SetupDao pdao = new SetupDao();
            int insert = 0;

            if (size > 0) {
                try {
                    pdao.limpiaEmpleados();
                } catch (DAOException a) {
                }
            }

            for (int i = 0; i < size; i++) {
                if (vpanel != null) {
                    vpanel.jpersonal.setText("<html><font color=yellow>PERSONAL DESCARGADOS " + insert + "/" + size + "</font></html>");
                }
                try {
                    JsonObject jper = array.get(i).getAsJsonObject();

                    long id = jper.get("id").getAsLong();
                    if (id == 91) {
                        NeoService.setLog("");
                    }
                    PersonaBean personal = new PersonaBean();
                    personal.setId(jper.get("id").getAsLong());

                    personal.setTipoIdentificacionId(jper.get("tipo_identificacion").getAsJsonObject().get("id").getAsLong());
                    personal.setTipoIdentificacionDesc(jper.get("tipo_identificacion").getAsJsonObject().get("descripcion").getAsString());
                    personal.setIdentificacion(jper.get("identificacion").getAsString());
                    personal.setNombre(jper.get("nombres").getAsString());
                    personal.setApellidos(jper.get("apellidos").getAsString());
                    personal.setEstado(jper.get("estado").getAsString());
                    if (!jper.get("pin").isJsonNull()) {
                        personal.setPin(jper.get("pin").getAsInt());
                        personal.setClave(jper.get("clave").getAsString());
                    }

                    if (personal.getId() == 91) {
                        NeoService.setLog("");
                    }

                    if (!jper.get("modulos").isJsonNull()) {
                        JsonArray arrat = jper.get("modulos").getAsJsonArray();
                        personal.setModulos(new ArrayList<>());
                        for (JsonElement obj : arrat) {
                            ModulosBean mod = new ModulosBean();
                            mod.setId(obj.getAsJsonObject().get("id").getAsInt());
                            mod.setDescripcion(obj.getAsJsonObject().get("descripcion").getAsString());
                            personal.getModulos().add(mod);
                        }
                    }
                    if (!jper.get("identificadores").isJsonNull()) {
                        JsonArray arrat = jper.get("identificadores").getAsJsonArray();
                        personal.setIdentificadores(new ArrayList<>());
                        for (JsonElement obj : arrat) {
                            IdentificadoresBean ident = new IdentificadoresBean();
                            ident.setId(obj.getAsJsonObject().get("id").getAsInt());
                            ident.setIdentificador(obj.getAsJsonObject().get("identificador").getAsString());
                            ident.setEstado(obj.getAsJsonObject().get("estado").getAsString());
                            personal.getIdentificadores().add(ident);

                            if (ident.getEstado().equals("A")) {
                                personal.setTag(obj.getAsJsonObject().get("identificador").getAsString());
                            } else {
                                personal.setTag("FFFFFFFFFF");
                            }
                        }
                    }

                    personal.setEmpresaId(jper.get("empresa").getAsJsonObject().get("id").getAsLong());
                    personal.setEmpresaRazonSocial(jper.get("empresa").getAsJsonObject().get("razon_social").getAsString());

                    personal.setCiudadId(jper.get("ciudad").getAsJsonObject().get("id").getAsLong());
                    personal.setCiudadDesc(jper.get("ciudad").getAsJsonObject().get("descripcion").getAsString());

                    pdao.procesarEmpleado(personal, Main.credencial);
                    insert++;
                } catch (DAOException ex) {
                    NeoService.setLog(ex.getMessage());
                } catch (Exception ex) {
                    NeoService.setLog(ex.getMessage());
                }
            }
            if (vpanel != null) {
                vpanel.jpersonal.setText("<html><font color='#00FF00'>PERSONAL FINALIZADO " + insert + "/" + size + "</font></html>");
            }

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            NeoService.setLog("Execution time in milliseconds : " + timeElapsed / 1000000);

        } catch (Exception a) {

        }
    }

    private void descargaCategorias() {

        JsonObject json = new JsonObject();
        String url = Butter.SECURE_END_POINT_CATEGORIAS + "/" + Main.credencial.getEmpresas_id();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "CONSULTA DE CATEGORIAS",
                Main.credencial,
                url,
                Butter.GET,
                json,
                Butter.ENABLE_DEBUG
        );
        long startTime = System.nanoTime();
        async.start();
        try {
            async.join();
            JsonObject response = async.getResponse();

            JsonArray array = response.getAsJsonArray("data");
            int size = array.size();
            vpanel.jcategorias.setText("<html><font color=yellow>CATEGORIA DESCARGADOS 0/" + size + "</font></html>");

            SetupDao pdao = new SetupDao();
            int insert = 0;

            for (int i = 0; i < size; i++) {
                vpanel.jcategorias.setText("<html><font color=yellow>CATEGORIA DESCARGADOS " + insert + "/" + size + "</font></html>");
                try {
                    JsonObject jper = array.get(i).getAsJsonObject();
                    CategoriaBean cat = new CategoriaBean();
                    cat.setId(jper.get("id").getAsLong());
                    cat.setGrupo(jper.get("grupo").getAsString());
                    cat.setEstado(jper.get("estado").getAsString());
                    cat.setGruposTiposId(jper.get("grupos_tipos_id").getAsLong());
                    cat.setEmpresas_id(jper.get("empresas_id").getAsLong());

                    if (!jper.get("grupos_id").isJsonNull()) {
                        cat.setGrupos_id(jper.get("grupos_id").getAsLong());
                    } else {
                        cat.setGrupos_id(0);
                    }
                    cat.setUrl_foto(jper.get("url_foto").getAsString());

                    pdao.procesarCategorias(insert, cat, Main.credencial);
                    insert++;
                } catch (DAOException ex) {
                    NeoService.setLog(ex.getMessage());
                } catch (Exception ex) {
                    NeoService.setLog(ex.getMessage());
                }
            }
            vpanel.jcategorias.setText("<html><font color='#00FF00'>CATEGORIA FINALIZADO " + insert + "/" + size + "</font></html>");

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            NeoService.setLog("Execution time in milliseconds : " + timeElapsed / 1000000);

        } catch (Exception a) {
            NeoService.setLog(a.getMessage());
        }

    }

    private void descargaMediosPagos() {

        JsonObject json = new JsonObject();

        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA DE MEDISOS DE PAGOS",
                Main.credencial,
                Butter.SECURE_END_POINT_MEDIOS_PAGOS + "/" + Main.credencial.getEmpresas_id(),
                Butter.GET,
                json,
                Butter.ENABLE_DEBUG
        );
        long startTime = System.nanoTime();
        async.start();
        try {
            async.join();
            JsonObject response = async.getResponse();

            JsonArray array = response.getAsJsonArray("data");
            int size = array.size();
            vpanel.jmedios.setText("<html><font color=yellow>MEDIOS P. DESCARGADOS 0/" + size + "</font></html>");

            SetupDao pdao = new SetupDao();
            int insert = 0;

            for (int i = 0; i < size; i++) {
                vpanel.jmedios.setText("<html><font color=yellow>MEDIOS P. DESCARGADOS " + insert + "/" + size + "</font></html>");
                try {
                    JsonObject jper = array.get(i).getAsJsonObject();
                    MediosPagosBean med = new MediosPagosBean();
                    med.setId(jper.get("id").getAsLong());
                    med.setDescripcion(jper.get("descripcion").getAsString().toUpperCase());
                    med.setEstado(jper.get("estado").getAsString());
                    med.setCredito(jper.get("credito").getAsString().equals("S"));
                    med.setCambio(jper.get("cambio").getAsString().equals("S"));
                    med.setComprobante(jper.get("comprobante").getAsString().equals("S"));

                    if (!jper.get("minimo_valor").isJsonNull()) {
                        med.setMinimo_valor(jper.get("minimo_valor").getAsFloat());
                    } else {
                        med.setMinimo_valor(0);
                    }

                    if (!jper.get("maximo_cambio").isJsonNull()) {
                        med.setMaximo_cambio(jper.get("maximo_cambio").getAsFloat());
                    } else {
                        med.setMaximo_cambio(0);
                    }

                    pdao.procesarMediosPago(med, Main.credencial);

                } catch (DAOException ex) {
                    NeoService.setLog(ex.getMessage());
                } catch (Exception ex) {
                    NeoService.setLog(ex.getMessage());
                }
                insert++;
            }
            vpanel.jmedios.setText("<html><font color='#00FF00'>MEDIOS DE PAGO FINALIZADO " + insert + "/" + size + "</font></html>");

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            NeoService.setLog("Execution time in milliseconds : " + timeElapsed / 1000000);

        } catch (Exception a) {
            NeoService.setLog(a.getMessage());
        }

    }

    private void descargaFuncion() {
        String name = this.dialog.getName();
        switch (name) {
            case Butter.MODAL_CONSECUTIVOS:
                getConsecutivos();
                break;
            case Butter.MODAL_REGISTRO_TAG:
                registroTag();
                break;
            case Butter.MODAL_BODEGAS_LISTA:
                obtenerBodegas();
                break;
            case Butter.MODAL_BODEGAS_PRODUCTOS:
                obtenerBodegasProductos();
                break;
            case Butter.MODAL_TRANSLADOS:
                hacerTraslados();
                break;
            default:
                break;
        }

    }

    private void registroTag() {

        String url = Butter.SECURE_END_POINT_EMPLEADOS_IDENTIFICADORES;
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA DE BODEGA",
                Main.credencial,
                url,
                Butter.POST,
                request,
                Butter.ENABLE_DEBUG
        );

        dialogLabel.setText("INICIANDO DESCARGA...");
        async.start();
        dialogLabel.setText("DESCARGANDO...");
        try {
            async.join();
            response = async.getResponse();
            if (response.get("success").getAsBoolean()) {
                success = true;
                mensaje = response.get("success").getAsString();
            } else {
                mensaje = null;
                success = false;
            }

        } catch (Exception a) {
            dialogLabel.setText("ERROR DE DESCARGA");
        }

    }

    public void getConsecutivos() {

        JsonObject json = new JsonObject();
        String url = Butter.SECURE_END_POINT_BODEGA + "/equipo/" + Main.credencial.getId();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA DE BODEGA",
                Main.credencial,
                url,
                Butter.GET,
                json,
                Butter.ENABLE_DEBUG
        );
        if (dialogLabel != null) {
            dialogLabel.setText("INICIANDO DESCARGA...");
        }
        async.start();
        if (dialogLabel != null) {
            dialogLabel.setText("DESCARGANDO...");
        }
        try {
            async.join();
            JsonObject response = async.getResponse();

            if (response.get("consecutivo") != null && !response.get("consecutivo").isJsonNull()) {
                JsonArray catg = response.getAsJsonArray("consecutivo").getAsJsonArray();

                ArrayList<ConsecutivoBean> consecutivos = new ArrayList<>();

                for (JsonElement elemt : catg) {
                    ConsecutivoBean cons = new ConsecutivoBean();
                    cons.setId(elemt.getAsJsonObject().get("id").getAsLong());
                    cons.setTipo_documento(elemt.getAsJsonObject().get("tipo_documento").getAsInt());
                    cons.setPrefijo(elemt.getAsJsonObject().get("prefijo").getAsString());

                    cons.setFecha_inicio(Utils.stringToDate(elemt.getAsJsonObject().get("fecha_inicio").getAsString()));

                    if (!elemt.getAsJsonObject().get("fecha_fin").isJsonNull()) {
                        cons.setFecha_fin(Utils.stringToDate(elemt.getAsJsonObject().get("fecha_fin").getAsString()));
                    }

                    cons.setConsecutivo_inicial(elemt.getAsJsonObject().get("consecutivo_inicial").getAsLong());

                    if (!elemt.getAsJsonObject().get("consecutivo_final").isJsonNull()) {
                        cons.setConsecutivo_final(elemt.getAsJsonObject().get("consecutivo_final").getAsLong());
                    }

                    if (!elemt.getAsJsonObject().get("consecutivo_actual").isJsonNull()) {
                        cons.setConsecutivo_actual(elemt.getAsJsonObject().get("consecutivo_actual").getAsLong());
                    }

                    cons.setEstado(elemt.getAsJsonObject().get("estado").getAsString());

                    if (!elemt.getAsJsonObject().get("resolucion").isJsonNull()) {
                        cons.setResolucion(elemt.getAsJsonObject().get("resolucion").getAsString());
                    }

                    if (!elemt.getAsJsonObject().get("observaciones").isJsonNull()) {
                        cons.setObservaciones(elemt.getAsJsonObject().get("observaciones").getAsString());
                    }

                    if (!elemt.getAsJsonObject().get("equipos_id").isJsonNull()) {
                        cons.setEquipo_id(elemt.getAsJsonObject().get("equipos_id").getAsLong());
                    }

                    consecutivos.add(cons);
                }

                if (!consecutivos.isEmpty()) {
                    try {
                        SetupDao pdao = new SetupDao();
                        pdao.createConsecutivos(consecutivos, Main.credencial);
                        ConsecutivoBean conse = pdao.getLastConsecutivoByList(consecutivos);
                        if (dialogLabel != null) {
                            if (conse != null) {
                                dialogLabel.setText("DESCARGADA CORRECTAMENTE");
                            } else {
                                dialogLabel.setText("SIN RESOLUCION DISPONIBLE");
                            }
                        }
                    } catch (DAOException a) {
                    }
                }
            }
            NeoService.setLog("DESCARGA OK DE LOS CONSECUTIVOS");

        } catch (Exception a) {
            if (dialogLabel != null) {
                dialogLabel.setText("ERROR DE DESCARGA");
            }
            NeoService.setLog("ERROR EN LA DESCARGA DE LOS CONSECUTIVOS");
        }

    }

    private void obtenerBodegas() {

        boolean isArray = true;
        String url = Butter.SECURE_END_POINT_BODEGA + "/empresa/" + Main.credencial.getEmpresas_id();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA DE BODEGAS",
                Main.credencial,
                url,
                Butter.GET,
                null,
                Butter.ENABLE_DEBUG,
                isArray
        );

        dialogLabel.setText("INICIANDO DESCARGA...");
        async.start();
        dialogLabel.setText("DESCARGANDO...");
        try {
            async.join();
            response = async.getResponse();
            if (response.get("success").getAsBoolean()) {
                success = true;
                mensaje = response.get("success").getAsString();
            } else {
                mensaje = null;
                success = false;
            }

        } catch (Exception a) {
            dialogLabel.setText("ERROR DE DESCARGA");
        }

    }

    private void obtenerBodegasProductos() {

        boolean isArray = true;
        BodegaBean param = (BodegaBean) params;
        String url = Butter.SECURE_END_POINT_BODEGA + "/productos/" + param.getId();
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA DE PRODUCTOS DE BODEGAS",
                Main.credencial,
                url,
                Butter.GET,
                null,
                Butter.ENABLE_DEBUG,
                isArray
        );

        dialogLabel.setText("INICIANDO DESCARGA...");
        async.start();
        dialogLabel.setText("DESCARGANDO...");
        try {
            async.join();
            response = async.getResponse();
            if (response.get("success").getAsBoolean()) {
                success = true;
                mensaje = response.get("success").getAsString();
            } else {
                mensaje = null;
                success = false;
            }

        } catch (Exception a) {
            dialogLabel.setText("ERROR DE DESCARGA");
        }

    }

    private void hacerTraslados() {

        boolean isArray = false;
        JsonObject param = (JsonObject) params;
        String url = Butter.SECURE_END_POINT_MOVIMENTO;
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "TRASLADOS DE BODEGAS",
                Main.credencial,
                url,
                Butter.POST,
                param,
                Butter.ENABLE_DEBUG,
                isArray
        );

        dialogLabel.setText("INICIANDO DESCARGA...");
        async.start();
        dialogLabel.setText("DESCARGANDO...");
        try {
            async.join();
            response = async.getResponse();
            if (response.get("success").getAsBoolean()) {
                success = true;
                mensaje = response.get("success").getAsString();
            } else {
                mensaje = null;
                success = false;
            }

        } catch (Exception a) {
            dialogLabel.setText("ERROR DE DESCARGA");
        }

    }

}
