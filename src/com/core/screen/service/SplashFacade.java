/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.screen.service;

/**
 *
 * @author ASUS-PC
 */
public class SplashFacade {

    String ultimoTextoImpreso = "";

    public void show(String texto) {
        if (!ultimoTextoImpreso.equals(texto)) {
            SplashService service = new SplashService();
            service.showPlaca(0, 0, texto);
            ultimoTextoImpreso = texto;
        }
    }

}
