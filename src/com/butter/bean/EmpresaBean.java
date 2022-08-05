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
public class EmpresaBean {

    private long id;
    private long dominioId;
    private long negocioId;
    private String nit;
    private String razonSocial;
    private String localizacion;
    private String direccion;
    private String correo;
    private String telefonos;

    private long empresasId;

    private long ciudadId;
    private String ciudadDescripcion;
    private String ciudadZonaHoraria;
    private int ciudadIndicador;

    private long provinciaId;
    private String provinciaDescripcion;

    private long paisId;
    private String paisDescripcion;
    private String paisMoneda;
    private int paisIndicador;
    private String paisNomenclatura;

    private String urlFotos;

    private ArrayList<ContactoBean> contacto = new ArrayList<>();

    String direccionPrincipal;
    String telefonoPrincipal;

    long descriptorId;
    String descriptorHeader;
    String descriptorFooter;
    String alias;
    String codigo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public long getEmpresasId() {
        return empresasId;
    }

    public void setEmpresasId(long empresasId) {
        this.empresasId = empresasId;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public long getCiudadId() {
        return ciudadId;
    }

    public void setCiudadId(long ciudadId) {
        this.ciudadId = ciudadId;
    }

    public String getCiudadDescripcion() {
        return ciudadDescripcion;
    }

    public void setCiudadDescripcion(String ciudadDescripcion) {
        this.ciudadDescripcion = ciudadDescripcion;
    }

    public long getProvinciaId() {
        return provinciaId;
    }

    public void setProvinciaId(long provinciaId) {
        this.provinciaId = provinciaId;
    }

    public String getProvinciaDescripcion() {
        return provinciaDescripcion;
    }

    public void setProvinciaDescripcion(String provinciaDescripcion) {
        this.provinciaDescripcion = provinciaDescripcion;
    }

    public long getPaisId() {
        return paisId;
    }

    public void setPaisId(long paisId) {
        this.paisId = paisId;
    }

    public String getPaisDescripcion() {
        return paisDescripcion;
    }

    public void setPaisDescripcion(String paisDescripcion) {
        this.paisDescripcion = paisDescripcion;
    }

    public String getUrlFotos() {
        return urlFotos;
    }

    public void setUrlFotos(String urlFotos) {
        this.urlFotos = urlFotos;
    }

    public String getCiudadZonaHoraria() {
        return ciudadZonaHoraria;
    }

    public void setCiudadZonaHoraria(String ciudadZonaHoraria) {
        this.ciudadZonaHoraria = ciudadZonaHoraria;
    }

    public int getCiudadIndicador() {
        return ciudadIndicador;
    }

    public void setCiudadIndicador(int ciudadIndicador) {
        this.ciudadIndicador = ciudadIndicador;
    }

    public String getPaisMoneda() {
        return paisMoneda;
    }

    public void setPaisMoneda(String paisMoneda) {
        this.paisMoneda = paisMoneda;
    }

    public int getPaisIndicador() {
        return paisIndicador;
    }

    public void setPaisIndicador(int paisIndicador) {
        this.paisIndicador = paisIndicador;
    }

    public String getPaisNomenclatura() {
        return paisNomenclatura;
    }

    public void setPaisNomenclatura(String paisNomenclatura) {
        this.paisNomenclatura = paisNomenclatura;
    }

    public ArrayList<ContactoBean> getContacto() {
        return contacto;
    }

    public void setContacto(ArrayList<ContactoBean> contacto) {
        this.contacto = contacto;
    }

    public String getDireccionPrincipal() {
        return direccionPrincipal;
    }

    public void setDireccionPrincipal(String direccionPrincipal) {
        this.direccionPrincipal = direccionPrincipal;
    }

    public String getTelefonoPrincipal() {
        return telefonoPrincipal;
    }

    public void setTelefonoPrincipal(String telefonoPrincipal) {
        this.telefonoPrincipal = telefonoPrincipal;
    }

    public long getDescriptorId() {
        return descriptorId;
    }

    public void setDescriptorId(long descriptorId) {
        this.descriptorId = descriptorId;
    }

    public String getDescriptorHeader() {
        return descriptorHeader;
    }

    public void setDescriptorHeader(String descriptorHeader) {
        this.descriptorHeader = descriptorHeader;
    }

    public String getDescriptorFooter() {
        return descriptorFooter;
    }

    public void setDescriptorFooter(String descriptorFooter) {
        this.descriptorFooter = descriptorFooter;
    }

    public long getDominioId() {
        return dominioId;
    }

    public void setDominioId(long dominioId) {
        this.dominioId = dominioId;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(String telefonos) {
        this.telefonos = telefonos;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public long getNegocioId() {
        return negocioId;
    }

    public void setNegocioId(long negocioId) {
        this.negocioId = negocioId;
    }
    
    

}
