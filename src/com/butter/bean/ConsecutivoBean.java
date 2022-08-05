/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.util.Date;

/**
 *
 * @author ASUS-PC
 */
public class ConsecutivoBean {

    long id;
    int tipo_documento;
    String prefijo;
    Date fecha_inicio;
    Date fecha_fin;
    long consecutivo_inicial;
    long consecutivo_actual;
    long consecutivo_final;
    String estado;
    String resolucion;
    String observaciones;
    long equipo_id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTipo_documento() {
        return tipo_documento;
    }

    public void setTipo_documento(int tipo_documento) {
        this.tipo_documento = tipo_documento;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public Date getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(Date fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public Date getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(Date fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public long getConsecutivo_inicial() {
        return consecutivo_inicial;
    }

    public void setConsecutivo_inicial(long consecutivo_inicial) {
        this.consecutivo_inicial = consecutivo_inicial;
    }

    public long getConsecutivo_actual() {
        return consecutivo_actual;
    }

    public void setConsecutivo_actual(long consecutivo_actual) {
        this.consecutivo_actual = consecutivo_actual;
    }

    public long getConsecutivo_final() {
        return consecutivo_final;
    }

    public void setConsecutivo_final(long consecutivo_final) {
        this.consecutivo_final = consecutivo_final;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public long getEquipo_id() {
        return equipo_id;
    }

    public void setEquipo_id(long equipo_id) {
        this.equipo_id = equipo_id;
    }

}
