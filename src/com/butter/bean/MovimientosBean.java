/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import java.util.Date;
import java.util.TreeMap;

/**
 *
 * @author novus
 */
public class MovimientosBean {

    private long id;
    private long empresasId;
    private long bodegaId;
    private int operacionId;
    private Date fecha;

    private long personaId;
    private String personaNit;
    private String personaNombre;

    private long terceroId;
    private String terceroNit;
    private String terceroNombre;

    private float costoTotal;
    private float ventaTotal;
    private float impuestoTotal;
    private float descuentoTotal;
    private long origenId;
    private String impreso;
    private long createUser;
    private Date createDate;
    private long updateUser;
    private Date updateDate;
    private long remotoId;
    private int sincronizado;
    private ConsecutivoBean consecutivo;
    private boolean success;

    private String movmientoEstado;

    private TreeMap<Long, MovimientosDetallesBean> detalles;

    public MovimientosBean() {
        detalles = new TreeMap<>();
    }

    public long getEmpresasId() {
        return empresasId;
    }

    public void setEmpresasId(long empresasId) {
        this.empresasId = empresasId;
    }

    public int getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(int operacionId) {
        this.operacionId = operacionId;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public ConsecutivoBean getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(ConsecutivoBean consecutivo) {
        this.consecutivo = consecutivo;
    }

    public long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(long personaId) {
        this.personaId = personaId;
    }

    public String getPersonaNit() {
        return personaNit;
    }

    public void setPersonaNit(String personaNit) {
        this.personaNit = personaNit;
    }

    public String getPersonaNombre() {
        return personaNombre;
    }

    public void setPersonaNombre(String personaNombre) {
        this.personaNombre = personaNombre;
    }

    public long getTerceroId() {
        return terceroId;
    }

    public void setTerceroId(long terceroId) {
        this.terceroId = terceroId;
    }

    public String getTerceroNit() {
        return terceroNit;
    }

    public void setTerceroNit(String terceroNit) {
        this.terceroNit = terceroNit;
    }

    public String getTerceroNombre() {
        return terceroNombre;
    }

    public void setTerceroNombre(String terceroNombre) {
        this.terceroNombre = terceroNombre;
    }

    public float getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(float costoTotal) {
        this.costoTotal = costoTotal;
    }

    public float getVentaTotal() {
        return ventaTotal;
    }

    public void setVentaTotal(float ventaTotal) {
        this.ventaTotal = ventaTotal;
    }

    public float getImpuestoTotal() {
        return impuestoTotal;
    }

    public void setImpuestoTotal(float impuestoTotal) {
        this.impuestoTotal = impuestoTotal;
    }

    public float getDescuentoTotal() {
        return descuentoTotal;
    }

    public void setDescuentoTotal(float descuentoTotal) {
        this.descuentoTotal = descuentoTotal;
    }

    public long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(long origenId) {
        this.origenId = origenId;
    }

    public String getImpreso() {
        return impreso;
    }

    public void setImpreso(String impreso) {
        this.impreso = impreso;
    }

    public long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(long createUser) {
        this.createUser = createUser;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public long getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(long updateUser) {
        this.updateUser = updateUser;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public long getRemotoId() {
        return remotoId;
    }

    public void setRemotoId(long remotoId) {
        this.remotoId = remotoId;
    }

    public int getSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(int sincronizado) {
        this.sincronizado = sincronizado;
    }

    public TreeMap<Long, MovimientosDetallesBean> getDetalles() {
        return detalles;
    }

    public void setDetalles(TreeMap<Long, MovimientosDetallesBean> detalles) {
        this.detalles = detalles;
    }

    public long getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(long bodegaId) {
        this.bodegaId = bodegaId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMovmientoEstado() {
        return movmientoEstado;
    }

    public void setMovmientoEstado(String movmientoEstado) {
        this.movmientoEstado = movmientoEstado;
    }

}
