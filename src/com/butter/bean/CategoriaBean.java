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
public class CategoriaBean {

    long gp_id;
    long id;
    long empresas_id;
    long grupos_id;
    String grupo;
    long gruposTiposId;
    String url_foto;
    String estado;

    ArrayList<ProductoBean> productos;
    float totales;

    public CategoriaBean() {
    }

    public CategoriaBean(ArrayList<ProductoBean> productos) {
        this.productos = productos;
    }

    public long getGp_id() {
        return gp_id;
    }

    public void setGp_id(long gp_id) {
        this.gp_id = gp_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getUrl_foto() {
        return url_foto;
    }

    public void setUrl_foto(String url_foto) {
        this.url_foto = url_foto;
    }

    public long getEmpresas_id() {
        return empresas_id;
    }

    public void setEmpresas_id(long empresas_id) {
        this.empresas_id = empresas_id;
    }

    public long getGrupos_id() {
        return grupos_id;
    }

    public void setGrupos_id(long grupos_id) {
        this.grupos_id = grupos_id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getGruposTiposId() {
        return gruposTiposId;
    }

    public void setGruposTiposId(long gruposTiposId) {
        this.gruposTiposId = gruposTiposId;
    }

    public ArrayList<ProductoBean> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<ProductoBean> productos) {
        this.productos = productos;
    }

    public float getTotales() {
        return totales;
    }

    public void setTotales(float totales) {
        this.totales = totales;
    }
    
    

}
