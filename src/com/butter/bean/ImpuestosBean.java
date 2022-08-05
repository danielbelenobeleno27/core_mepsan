/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import com.google.gson.JsonObject;

/**
 *
 * @author ASUS-PC
 */
public class ImpuestosBean {

    long id;
    long impuestos_id;
    boolean iva_incluido;
    float valor;
    String descripcion;
    String porcentaje_valor;
    float calculado;
    float totales;

    public static ImpuestosBean fromJson(JsonObject json) {
        ImpuestosBean imp = new ImpuestosBean();
        imp.setDescripcion(json.get("descripcion").getAsString());
        imp.setId(json.get("id").getAsLong());
        imp.setImpuestos_id(json.get("impuestos_id").getAsLong());
        if (json.get("iva_incluido") != null && !json.get("iva_incluido").isJsonNull()) {
            imp.setIva_incluido(json.get("iva_incluido").getAsString().equals("S"));
        } else {
            imp.setIva_incluido(false);
        }
        imp.setValor(json.get("valor").getAsFloat());
        imp.setPorcentaje_valor(json.get("porcentaje_valor").getAsString());
        return imp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getImpuestos_id() {
        return impuestos_id;
    }

    public void setImpuestos_id(long impuestos_id) {
        this.impuestos_id = impuestos_id;
    }

    public boolean isIva_incluido() {
        return iva_incluido;
    }

    public void setIva_incluido(boolean iva_incluido) {
        this.iva_incluido = iva_incluido;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPorcentaje_valor() {
        return porcentaje_valor;
    }

    public void setPorcentaje_valor(String porcentaje_valor) {
        this.porcentaje_valor = porcentaje_valor;
    }

    public float getCalculado() {
        return calculado;
    }

    public void setCalculado(float calculado) {
        this.calculado = calculado;
    }

    public float getTotales() {
        return totales;
    }

    public void setTotales(float totales) {
        this.totales = totales;
    }
    
    

}
