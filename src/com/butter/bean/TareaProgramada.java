/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.util.Date;

/**
 *
 * @author novusteam
 */
public class TareaProgramada {
    
    long id;
    int tipo;
    Date hora;
    int producto;
    int cara;
    int manguera; 
    int grado;
    int precio;
    int precioOriginal;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public int getCara() {
        return cara;
    }

    public void setCara(int cara) {
        this.cara = cara;
    }

    public int getManguera() {
        return manguera;
    }

    public void setManguera(int manguera) {
        this.manguera = manguera;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getGrado() {
        return grado;
    }

    public void setGrado(int grado) {
        this.grado = grado;
    }

    public int getPrecioOriginal() {
        return precioOriginal;
    }

    public void setPrecioOriginal(int precioOriginal) {
        this.precioOriginal = precioOriginal;
    }

    public int getProducto() {
        return producto;
    }

    public void setProducto(int producto) {
        this.producto = producto;
    }
    
    
    
}
