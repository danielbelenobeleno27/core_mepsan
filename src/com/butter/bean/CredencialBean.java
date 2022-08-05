package com.butter.bean;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CredencialBean {

    private long id;
    private String _id;
    private String serial;
    private String mac;
    private String almacenamiento;
    private int versionApp;
    private int versionDataBase;

    private String token;
    private String password;
    private String referencia;

    private Long tipoEquipoId;
    private String estadoId;
    private String estadoDescripcion;

    private Long equipos_id;
    private Long empresas_id;

    private boolean autorizado;
    private boolean registroPrevio;

    private Long bodegaId;

    private EmpresaBean empresa;

    public JsonObject toJson() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        JsonElement element = gson.fromJson(json, JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        return jsonObj;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String get_Id() {
        return _id;
    }

    public void set_Id(String _id) {
        this._id = _id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getAlmacenamiento() {
        return almacenamiento;
    }

    public void setAlmacenamiento(String almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    public int getVersionApp() {
        return versionApp;
    }

    public void setVersionApp(int versionApp) {
        this.versionApp = versionApp;
    }

    public int getVersionDataBase() {
        return versionDataBase;
    }

    public void setVersionDataBase(int versionDataBase) {
        this.versionDataBase = versionDataBase;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Long getTipoEquipoId() {
        return tipoEquipoId;
    }

    public void setTipoEquipoId(Long tipoEquipoId) {
        this.tipoEquipoId = tipoEquipoId;
    }

    public String getEstadoId() {
        return estadoId;
    }

    public void setEstadoId(String estadoId) {
        this.estadoId = estadoId;
    }

    public String getEstadoDescripcion() {
        return estadoDescripcion;
    }

    public void setEstadoDescripcion(String estadoDescripcion) {
        this.estadoDescripcion = estadoDescripcion;
    }

    public Long getEquipos_id() {
        return equipos_id;
    }

    public void setEquipos_id(Long equipos_id) {
        this.equipos_id = equipos_id;
    }

    public Long getEmpresas_id() {
        return empresas_id;
    }

    public void setEmpresas_id(Long empresas_id) {
        this.empresas_id = empresas_id;
    }

    public boolean isAutorizado() {
        return autorizado;
    }

    public void setAutorizado(boolean autorizado) {
        this.autorizado = autorizado;
    }

    public boolean isRegistroPrevio() {
        return registroPrevio;
    }

    public void setRegistroPrevio(boolean registroPrevio) {
        this.registroPrevio = registroPrevio;
    }

    public Long getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(Long bodegaId) {
        this.bodegaId = bodegaId;
    }

    public EmpresaBean getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaBean empresa) {
        this.empresa = empresa;
    }

}
