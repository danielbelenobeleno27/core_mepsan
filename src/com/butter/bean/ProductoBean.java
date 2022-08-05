/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;

/**
 *
 * @author novus
 */
public class ProductoBean implements Cloneable {

   
    long id;
    long empresaId;
    String plu;
    String descripcion;
    float precio;
    boolean puede_vender;
    boolean puede_comprar;
    boolean ingrediente;
    boolean favorito;
    int tipo;

    String url_foto;
    String usa_balanza;
    String estado;
    String unidades_medida;
    long unidades_medida_id;
    float unidades_medida_valor;
    String unidades_contenido;
    long unidades_contenido_id;
    long contador;
    float unidades_contenido_valor;

    String dispensado;
    String promocion;
    String vigencia_inicio;
    String vigencia_fin;
    ArrayList<PreciosBean> precios;
    ArrayList<IdentificadoresBean> identificadores;
    ArrayList<CategoriaBean> categorias;
    ArrayList<ImpuestosBean> impuestos;
    ArrayList<ProductoBean> ingredientes;

    float saldo;
    float cantidad;
    float cantidadMinima;
    float cantidadMaxima;
    int tiempoReorden;

    float precio_maximo;
    float precio_minimo;
    String precio_tipo_limite;
    boolean precio_flexible;

    long bodega_producto_id;

    long producto_compuesto_id;
    float producto_compuesto_cantidad;
    float producto_compuesto_costo;
    boolean compuesto;

    String categoriaDesc;
    long categoriaId;
    String grupos;
    
    public static final String BLOQUEADO = "B";

    public static ProductoBean fromJson(JsonObject obj, long empresaId) {
        ProductoBean producto = new ProductoBean();
        producto.setId(obj.get("id").getAsLong());
        producto.setEmpresaId(empresaId);
        producto.setPlu(obj.get("plu").getAsString());
        producto.setDescripcion(obj.get("descripcion").getAsString());
        producto.setPrecio(obj.get("precio").getAsFloat());
        producto.setPuede_vender(obj.get("puede_vender").getAsString().equals("S"));
        producto.setPuede_comprar(obj.get("puede_comprar").getAsString().equals("S"));
        producto.setIngrediente(obj.get("ingrediente").getAsString().equals("S"));
        producto.setTipo(obj.get("tipo").getAsInt());
        producto.setCompuesto(obj.get("tipo").getAsInt() == 2);

        producto.setUsa_balanza(obj.get("usa_balanza").getAsString());
        producto.setEstado(obj.get("estado").getAsString());

        producto.setUnidades_medida(obj.get("unidades_medida").getAsString());
        producto.setUnidades_medida_id(obj.get("unidades_medida_id").getAsLong());
        producto.setUnidades_medida_valor(obj.get("unidades_medida_valor").getAsFloat());

        producto.setUnidades_contenido(obj.get("unidades_contenido").getAsString());
        producto.setUnidades_contenido_id(obj.get("unidades_contenido_id").getAsLong());
        producto.setUnidades_contenido_valor(obj.get("unidades_contenido_valor").getAsFloat());

        if (!obj.get("cantidad_maxima").isJsonNull()) {
            producto.setCantidadMaxima(obj.get("cantidad_maxima").getAsFloat());
        }

        if (!obj.get("cantidad_minima").isJsonNull()) {
            producto.setCantidadMinima(obj.get("cantidad_minima").getAsFloat());
        }
        if (!obj.get("tiempo_reorden").isJsonNull()) {
            producto.setTiempoReorden(obj.get("tiempo_reorden").getAsInt());
        }

        if (!obj.get("precio_maximo").isJsonNull()) {
            producto.setPrecio_maximo(obj.get("precio_maximo").getAsFloat());
        }
        if (!obj.get("precio_minimo").isJsonNull()) {
            producto.setPrecio_minimo(obj.get("precio_minimo").getAsFloat());
        }
        if (!obj.get("precio_tipo_limite").isJsonNull()) {
            producto.setPrecio_tipo_limite(obj.get("precio_tipo_limite").getAsString());
        }
        if (!obj.get("precio_flexible").isJsonNull()) {
            producto.setPrecio_flexible(obj.get("precio_flexible").getAsString().equals("S"));
        }
        if (!obj.get("seguimiento").isJsonNull()) {
            producto.setDispensado(obj.get("seguimiento").getAsString());
        }
        if (!obj.get("promocion").isJsonNull()) {
            producto.setPromocion(obj.get("promocion").getAsString());
        }
        if (!obj.get("vigencia_inicio").isJsonNull()) {
            producto.setVigencia_inicio(obj.get("vigencia_inicio").getAsString());
        }
        if (!obj.get("vigencia_fin").isJsonNull()) {
            producto.setVigencia_fin(obj.get("vigencia_fin").getAsString());
        }

        if (obj.get("categorias") != null && !obj.get("categorias").isJsonNull()) {
            JsonArray catg = obj.getAsJsonArray("categorias").getAsJsonArray();
            producto.setCategorias(new ArrayList<>());

            for (JsonElement elemt : catg) {
                CategoriaBean cat = new CategoriaBean();
                cat.setGp_id(elemt.getAsJsonObject().get("gp_id").getAsLong());
                cat.setGrupo(elemt.getAsJsonObject().get("grupo").getAsString());
                cat.setUrl_foto(elemt.getAsJsonObject().get("url_foto").getAsString());
                producto.getCategorias().add(cat);
            }

        }

        if (obj.get("identificadores") != null && !obj.get("identificadores").isJsonNull()) {
            producto.setIdentificadores(new ArrayList<>());
            for (JsonElement elemento : obj.get("identificadores").getAsJsonArray()) {
                IdentificadoresBean cb = new IdentificadoresBean();
                cb.setId(elemento.getAsJsonObject().get("id").getAsLong());
                cb.setIdentificador(elemento.getAsJsonObject().get("identificador").getAsString());
                producto.getIdentificadores().add(cb);
            }
        }

        if (obj.get("precios") != null && !obj.get("precios").isJsonNull()) {
            producto.setPrecios(new ArrayList<>());
            for (JsonElement elemento : obj.get("precios").getAsJsonArray()) {
                PreciosBean pc = new PreciosBean();
                pc.setId(elemento.getAsJsonObject().get("id").getAsLong());
                pc.setPrecio(elemento.getAsJsonObject().get("precio").getAsLong());
                producto.getPrecios().add(pc);
            }
        }

        if (obj.get("url_foto") != null && !obj.get("url_foto").isJsonNull()) {
            producto.setUrl_foto(obj.get("url_foto").getAsString());
           }

        if (obj.get("favorito") != null && !obj.get("favorito").isJsonNull()) {
            producto.setFavorito(obj.get("favorito").getAsString().contains("S"));
        }

        if (obj.get("productos_impuestos") != null && !obj.get("productos_impuestos").isJsonNull()) {
            producto.setImpuestos(new ArrayList<>());
            for (JsonElement elemento : obj.get("productos_impuestos").getAsJsonArray()) {
                JsonObject json = elemento.getAsJsonObject();
                ImpuestosBean imp = ImpuestosBean.fromJson(json);
                producto.getImpuestos().add(imp);
            }
        }

        return producto;
    }

    public ArrayList<IdentificadoresBean> getIdentificadores() {
        return identificadores;
    }

    public void setIdentificadores(ArrayList<IdentificadoresBean> identificadores) {
        this.identificadores = identificadores;
    }

    public ArrayList<PreciosBean> getPrecios() {
        return precios;
    }

    public void setPrecios(ArrayList<PreciosBean> precios) {
        this.precios = precios;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public boolean isPuede_vender() {
        return puede_vender;
    }

    public void setPuede_vender(boolean puede_vender) {
        this.puede_vender = puede_vender;
    }

    public boolean isPuede_comprar() {
        return puede_comprar;
    }

    public void setPuede_comprar(boolean puede_comprar) {
        this.puede_comprar = puede_comprar;
    }

    public boolean isIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(boolean ingrediente) {
        this.ingrediente = ingrediente;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getUrl_foto() {
        return url_foto;
    }

    public void setUrl_foto(String url_foto) {
        this.url_foto = url_foto;
    }

    public String getUsa_balanza() {
        return usa_balanza;
    }

    public void setUsa_balanza(String usa_balanza) {
        this.usa_balanza = usa_balanza;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUnidades_medida() {
        return unidades_medida;
    }

    public void setUnidades_medida(String unidades_medida) {
        this.unidades_medida = unidades_medida;
    }

    public float getUnidades_medida_valor() {
        return unidades_medida_valor;
    }

    public void setUnidades_medida_valor(float unidades_medida_valor) {
        this.unidades_medida_valor = unidades_medida_valor;
    }

    public String getUnidades_contenido() {
        return unidades_contenido;
    }

    public void setUnidades_contenido(String unidades_contenido) {
        this.unidades_contenido = unidades_contenido;
    }

    public float getUnidades_contenido_valor() {
        return unidades_contenido_valor;
    }

    public void setUnidades_contenido_valor(float unidades_contenido_valor) {
        this.unidades_contenido_valor = unidades_contenido_valor;
    }

    public long getUnidades_medida_id() {
        return unidades_medida_id;
    }

    public void setUnidades_medida_id(long unidades_medida_id) {
        this.unidades_medida_id = unidades_medida_id;
    }

    public long getUnidades_contenido_id() {
        return unidades_contenido_id;
    }

    public void setUnidades_contenido_id(long unidades_contenido_id) {
        this.unidades_contenido_id = unidades_contenido_id;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    public String getDispensado() {
        return dispensado;
    }

    public void setDispensado(String dispensado) {
        this.dispensado = dispensado;
    }

    public float getSaldo() {
        return saldo;
    }

    public void setSaldo(float saldo) {
        this.saldo = saldo;
    }

    public float getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(float cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public float getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(float cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public long getBodega_producto_id() {
        return bodega_producto_id;
    }

    public void setBodega_producto_id(long bodega_producto_id) {
        this.bodega_producto_id = bodega_producto_id;
    }

    public int getTiempoReorden() {
        return tiempoReorden;
    }

    public void setTiempoReorden(int tiempoReorden) {
        this.tiempoReorden = tiempoReorden;
    }

    public ArrayList<CategoriaBean> getCategorias() {
        return categorias;
    }

    public void setCategorias(ArrayList<CategoriaBean> categorias) {
        this.categorias = categorias;
    }

    public long getContador() {
        return contador;
    }

    public void setContador(long contador) {
        this.contador = contador;
    }

    public long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(long empresaId) {
        this.empresaId = empresaId;
    }

    public float getPrecio_maximo() {
        return precio_maximo;
    }

    public float getPrecio_minimo() {
        return precio_minimo;
    }

    public void setPrecio_maximo(float precio_maximo) {
        this.precio_maximo = precio_maximo;
    }

    public void setPrecio_minimo(float precio_minimo) {
        this.precio_minimo = precio_minimo;
    }

    public String getPrecio_tipo_limite() {
        return precio_tipo_limite;
    }

    public void setPrecio_tipo_limite(String precio_tipo_limite) {
        this.precio_tipo_limite = precio_tipo_limite;
    }

    public boolean isPrecio_flexible() {
        return precio_flexible;
    }

    public void setPrecio_flexible(boolean precio_flexible) {
        this.precio_flexible = precio_flexible;
    }

    public ArrayList<ImpuestosBean> getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(ArrayList<ImpuestosBean> impuestos) {
        this.impuestos = impuestos;
    }

    public boolean isCompuesto() {
        return compuesto;
    }

    public void setCompuesto(boolean compuesto) {
        this.compuesto = compuesto;
    }

    public ArrayList<ProductoBean> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(ArrayList<ProductoBean> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }

    public long getProducto_compuesto_id() {
        return producto_compuesto_id;
    }

    public void setProducto_compuesto_id(long producto_compuesto_id) {
        this.producto_compuesto_id = producto_compuesto_id;
    }

    public float getProducto_compuesto_cantidad() {
        return producto_compuesto_cantidad;
    }

    public void setProducto_compuesto_cantidad(float producto_compuesto_cantidad) {
        this.producto_compuesto_cantidad = producto_compuesto_cantidad;
    }

    public float getProducto_compuesto_costo() {
        return producto_compuesto_costo;
    }

    public void setProducto_compuesto_costo(float producto_compuesto_costo) {
        this.producto_compuesto_costo = producto_compuesto_costo;
    }

    public String getCategoriaDesc() {
        return categoriaDesc;
    }

    public void setCategoriaDesc(String categoriaDesc) {
        this.categoriaDesc = categoriaDesc;
    }

    public long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getPromocion() {
        return promocion;
    }

    public void setPromocion(String promocion) {
        this.promocion = promocion;
    }

    public String getVigencia_inicio() {
        return vigencia_inicio;
    }

    public void setVigencia_inicio(String vigencia_inicio) {
        this.vigencia_inicio = vigencia_inicio;
    }

    public String getVigencia_fin() {
        return vigencia_fin;
    }

    public void setVigencia_fin(String vigencia_fin) {
        this.vigencia_fin = vigencia_fin;
    }

    public String getGrupos() {
        return grupos;
    }

    public void setGrupos(String grupos) {
        this.grupos = grupos;
    }
    
    

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
