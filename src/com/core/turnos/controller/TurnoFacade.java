/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.turnos.controller;

import com.core.turnos.bean.TurnosResponse;

/**
 *
 * @author ASUS-PC
 */
public class TurnoFacade {

    TurnoBusiness business = new TurnoBusiness();

    public TurnosResponse iniciarJornada(String identificacion, String pin, int surtidor) {
        TurnosResponse respuesta = business.iniciarJornada(identificacion, pin, surtidor);
        return respuesta;
    }

    public TurnosResponse finalizarJornada(String identificacion) {
        TurnosResponse respuesta = business.finalizarJornada(identificacion);
        return respuesta;
    }

    public TurnosResponse consultaJornada(String identificacion) {
        TurnosResponse jornada = business.consultaJornada(identificacion);
        return jornada;
    }
}
