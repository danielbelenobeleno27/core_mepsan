/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.util.ArrayList;

/**
 *
 * @author novus
 */
public class PersonaBean {
    
    long id;
    long empresaId;
    String empresaRazonSocial;
    String nombre;
    String apellidos;
    String identificacion;
    String genero;
    String tag;
    
    long tipoIdentificacionId;
    String tipoIdentificacionDesc;
    
    String estado;
    String sangre;
    long ciudadId;
    String ciudadDesc;
    String clave;
    int pin;
    String correo;
    
    ArrayList<IdentificadoresBean> identificadores;
    ArrayList<ModulosBean> modulos;
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setIdentificadores(ArrayList<IdentificadoresBean> identificadores) {
        this.identificadores = identificadores;
    }

    public ArrayList<IdentificadoresBean> getIdentificadores() {
        return identificadores;
    }

    public long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(long empresaId) {
        this.empresaId = empresaId;
    }

    

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getSangre() {
        return sangre;
    }

    public void setSangre(String sangre) {
        this.sangre = sangre;
    }

    public long getCiudadId() {
        return ciudadId;
    }

    public void setCiudadId(long ciudadId) {
        this.ciudadId = ciudadId;
    }

    public long getTipoIdentificacionId() {
        return tipoIdentificacionId;
    }

    public void setTipoIdentificacionId(long tipoIdentificacionId) {
        this.tipoIdentificacionId = tipoIdentificacionId;
    }

    public String getTipoIdentificacionDesc() {
        return tipoIdentificacionDesc;
    }

    public void setTipoIdentificacionDesc(String tipoIdentificacionDesc) {
        this.tipoIdentificacionDesc = tipoIdentificacionDesc;
    }

    public String getCiudadDesc() {
        return ciudadDesc;
    }

    public void setCiudadDesc(String ciudadDesc) {
        this.ciudadDesc = ciudadDesc;
    }

    public String getEmpresaRazonSocial() {
        return empresaRazonSocial;
    }

    public void setEmpresaRazonSocial(String empresaRazonSocial) {
        this.empresaRazonSocial = empresaRazonSocial;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public int getPin() {
        return pin;
    }
    
    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public ArrayList<ModulosBean> getModulos() {
        return modulos;
    }

    public void setModulos(ArrayList<ModulosBean> modulos) {
        this.modulos = modulos;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
    
    
}
