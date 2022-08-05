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
public class BodegaBean {

    long id;
    String descripcion;
    String estado;
    long empresaId;
    String codigo;
    String dimension;
    String ubicacion;
    int numeroStand;
    ArrayList<ProductoBean> productos;
    ArrayList<ConsecutivoBean> consecutivos;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(long empresaId) {
        this.empresaId = empresaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getNumeroStand() {
        return numeroStand;
    }

    public void setNumeroStand(int numeroStand) {
        this.numeroStand = numeroStand;
    }

    public ArrayList<ProductoBean> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<ProductoBean> productos) {
        this.productos = productos;
    }

    public ArrayList<ConsecutivoBean> getConsecutivos() {
        return consecutivos;
    }

    public void setConsecutivos(ArrayList<ConsecutivoBean> consecutivos) {
        this.consecutivos = consecutivos;
    }

}
