/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean; 
import com.core.app.NeoService;
import java.util.Date;

/**
 *
 * @author novus
 */
public class MovimientosDetallesBean extends ProductoBean {

    private long movimientoId;
    private long bodegasId;
    private long productoId;

    private int tipo_operacion;
    private Date fecha;
    private long descuentoId;
    private float descuentoProducto;
    private long remotoId;
    private int sincronizado;

    private int item;
    private String codigoBarra;
    private float subtotal;

    boolean nuevo;

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException ex) {
            NeoService.setLog(" no se puede duplicar");
        }
        return obj;
    }
    
    public long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(long movimientoId) {
        this.movimientoId = movimientoId;
    }

    public long getBodegasId() {
        return bodegasId;
    }

    public void setBodegasId(long bodegasId) {
        this.bodegasId = bodegasId;
    }

    public long getProductoId() {
        return productoId;
    }

    public void setProductoId(long productoId) {
        this.productoId = productoId;
    }

    public int getTipo_operacion() {
        return tipo_operacion;
    }

    public void setTipo_operacion(int tipo_operacion) {
        this.tipo_operacion = tipo_operacion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public long getDescuentoId() {
        return descuentoId;
    }

    public void setDescuentoId(long descuentoId) {
        this.descuentoId = descuentoId;
    }

    public float getDescuentoProducto() {
        return descuentoProducto;
    }

    public void setDescuentoProducto(float descuentoProducto) {
        this.descuentoProducto = descuentoProducto;
    }

    public long getRemotoId() {
        return remotoId;
    }

    public void setRemotoId(long remotoId) {
        this.remotoId = remotoId;
    }

    public int getSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(int sincronizado) {
        this.sincronizado = sincronizado;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public boolean isNuevo() {
        return nuevo;
    }

    public void setNuevo(boolean nuevo) {
        this.nuevo = nuevo;
    }

}
