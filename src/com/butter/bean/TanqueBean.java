package com.butter.bean;

import java.util.Date;

/**
 *
 * @author usuario
 */
public class TanqueBean implements Comparable<TanqueBean> {

    Date fecha;
    int numero;
    int status;
    int numeroDatos;

    double volumen;
    double volumenTC;
    double merma; //ullage
    double altura;
    double agua;
    double temperatura;
    double volumenAgua;

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNumeroDatos() {
        return numeroDatos;
    }

    public void setNumeroDatos(int numeroDatos) {
        this.numeroDatos = numeroDatos;
    }

    public double getVolumen() {
        return volumen;
    }

    public void setVolumen(double volumen) {
        this.volumen = volumen;
    }

    public double getVolumenTC() {
        return volumenTC;
    }

    public void setVolumenTC(double volumenTC) {
        this.volumenTC = volumenTC;
    }

    public double getMerma() {
        return merma;
    }

    public void setMerma(double merma) {
        this.merma = merma;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double getAgua() {
        return agua;
    }

    public void setAgua(double agua) {
        this.agua = agua;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public double getVolumenAgua() {
        return volumenAgua;
    }

    public void setVolumenAgua(double volumenAgua) {
        this.volumenAgua = volumenAgua;
    }

    @Override
    public int compareTo(TanqueBean o) {
        if (numero < o.numero) {
            return -1;
        }
        if (numero > o.numero) {
            return 1;
        }
        return 0;
    }

}
