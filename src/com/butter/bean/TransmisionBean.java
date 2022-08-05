/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.util.Date;

/**
 *
 * @author usuario
 */
public class TransmisionBean {

    long id;
    long equipoId;
    String request;
    String response;
    int sincronizado;
    Date fechaGenerado;
    Date fechaTrasmitido;
    Date fechaRecibido;
    String url;
    String method;
    int reintentos;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(long equipoId) {
        this.equipoId = equipoId;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(int sincronizado) {
        this.sincronizado = sincronizado;
    }

    public Date getFechaGenerado() {
        return fechaGenerado;
    }

    public void setFechaGenerado(Date fechaGenerado) {
        this.fechaGenerado = fechaGenerado;
    }

    public Date getFechaTrasmitido() {
        return fechaTrasmitido;
    }

    public void setFechaTrasmitido(Date fechaTrasmitido) {
        this.fechaTrasmitido = fechaTrasmitido;
    }

    public Date getFechaRecibido() {
        return fechaRecibido;
    }

    public void setFechaRecibido(Date fechaRecibido) {
        this.fechaRecibido = fechaRecibido;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getReintentos() {
        return reintentos;
    }

    public void setReintentos(int reintentos) {
        this.reintentos = reintentos;
    }
    
    
}
