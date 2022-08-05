/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.turnos.bean;

import com.neo.app.bean.Persona;
import com.neo.app.bean.Response;
import com.neo.app.bean.Surtidor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ASUS-PC
 */
public class TurnosResponse extends Response {

    long id;
    Date fechaInicio;
    Date fechaFin;
    float acumuadoVentas;
    float acumuadoVolumen;
    long personaId;
    long surtidorId;
    float saldo;

    int cantidad;
    List<SurtidorInventario> inventario = new ArrayList<>();

    Persona persona;
    Surtidor surtidor;
    Date fecha;
    String error;
    int factorInventario;
    int factorPrecio;
    int factorVolumen;

    public int getFactorInventario() {
        return factorInventario;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Surtidor getSurtidor() {
        return surtidor;
    }

    public void setSurtidor(Surtidor surtidor) {
        this.surtidor = surtidor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<SurtidorInventario> getInventario() {
        return inventario;
    }

    public void setInventario(List<SurtidorInventario> inventario) {
        this.inventario = inventario;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(long personaId) {
        this.personaId = personaId;
    }

    public long getSurtidorId() {
        return surtidorId;
    }

    public void setSurtidorId(long surtidorId) {
        this.surtidorId = surtidorId;
    }

    public float getSaldo() {
        return saldo;
    }

    public void setSaldo(float saldo) {
        this.saldo = saldo;
    }

    public float getAcumuadoVentas() {
        return acumuadoVentas;
    }

    public void setAcumuadoVentas(float acumuadoVentas) {
        this.acumuadoVentas = acumuadoVentas;
    }

    public float getAcumuadoVolumen() {
        return acumuadoVolumen;
    }

    public void setAcumuadoVolumen(float acumuadoVolumen) {
        this.acumuadoVolumen = acumuadoVolumen;
    }

    public int getFactorPrecio() {
        return factorPrecio;
    }

    public void setFactorPrecio(int factorPrecio) {
        this.factorPrecio = factorPrecio;
    }

    public int getFactorVolumen() {
        return factorVolumen;
    }

    public void setFactorVolumen(int factorVolumen) {
        this.factorVolumen = factorVolumen;
    }

    public void setFactorInventario(int factorInventario) {
        this.factorInventario = factorInventario;
    }
    
}
