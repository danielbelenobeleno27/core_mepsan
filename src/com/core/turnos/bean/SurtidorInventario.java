/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.turnos.bean;

/**
 *
 * @author ASUS-PC
 */
public class SurtidorInventario {

    long surtidoresDetallesId;
    int surtidor;
    int manguera;
    int cara;

    long productoId;
    String productoDescripcion;
    float productoPrecio;
    long acumuadoVentasInicial;
    long acumuadoVolumenInicial;
    long acumuadoVentasFinal;
    long acumuadoVolumenFinal;

    public int getSurtidor() {
        return surtidor;
    }

    public void setSurtidor(int surtidor) {
        this.surtidor = surtidor;
    }

    public int getManguera() {
        return manguera;
    }

    public void setManguera(int manguera) {
        this.manguera = manguera;
    }

    public int getCara() {
        return cara;
    }

    public void setCara(int cara) {
        this.cara = cara;
    }

    public long getAcumuadoVentasInicial() {
        return acumuadoVentasInicial;
    }

    public void setAcumuadoVentasInicial(long acumuadoVentasInicial) {
        this.acumuadoVentasInicial = acumuadoVentasInicial;
    }

    public long getAcumuadoVolumenInicial() {
        return acumuadoVolumenInicial;
    }

    public void setAcumuadoVolumenInicial(long acumuadoVolumenInicial) {
        this.acumuadoVolumenInicial = acumuadoVolumenInicial;
    }

    public long getAcumuadoVentasFinal() {
        return acumuadoVentasFinal;
    }

    public void setAcumuadoVentasFinal(long acumuadoVentasFinal) {
        this.acumuadoVentasFinal = acumuadoVentasFinal;
    }

    public long getAcumuadoVolumenFinal() {
        return acumuadoVolumenFinal;
    }

    public void setAcumuadoVolumenFinal(long acumuadoVolumenFinal) {
        this.acumuadoVolumenFinal = acumuadoVolumenFinal;
    }

    public long getProductoId() {
        return productoId;
    }

    public void setProductoId(long productoId) {
        this.productoId = productoId;
    }

    public long getSurtidoresDetallesId() {
        return surtidoresDetallesId;
    }

    public void setSurtidoresDetallesId(long surtidoresDetallesId) {
        this.surtidoresDetallesId = surtidoresDetallesId;
    }

    public String getProductoDescripcion() {
        return productoDescripcion;
    }

    public void setProductoDescripcion(String productoDescripcion) {
        this.productoDescripcion = productoDescripcion;
    }

    public float getProductoPrecio() {
        return productoPrecio;
    }

    public void setProductoPrecio(float productoPrecio) {
        this.productoPrecio = productoPrecio;
    }

}
