/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author usuario
 */
public class ReporteJornadaBean {

    long id;
    long jornadaId;
    PersonaBean promotor;
    Date inicio;
    Date fin;
    int impresos;
    int reimpresos;
    int numeroVentas;
    
    ArrayList<CatalogoBean> resumen;

    public PersonaBean getPromotor() {
        return promotor;
    }

    public void setPromotor(PersonaBean promotor) {
        this.promotor = promotor;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFin() {
        return fin;
    }

    public void setFin(Date fin) {
        this.fin = fin;
    }

    public ArrayList<CatalogoBean> getResumen() {
        return resumen;
    }

    public void setResumen(ArrayList<CatalogoBean> resumen) {
        this.resumen = resumen;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJornadaId() {
        return jornadaId;
    }

    public void setJornadaId(long jornadaId) {
        this.jornadaId = jornadaId;
    }

    public int getImpresos() {
        return impresos;
    }

    public void setImpresos(int impresos) {
        this.impresos = impresos;
    }

    public int getReimpresos() {
        return reimpresos;
    }

    public void setReimpresos(int reimpresos) {
        this.reimpresos = reimpresos;
    }

    public int getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(int numeroVentas) {
        this.numeroVentas = numeroVentas;
    }
    
}
