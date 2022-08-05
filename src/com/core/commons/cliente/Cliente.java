/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.commons.cliente;

import com.neo.app.bean.ProtocolsDto;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public abstract class Cliente {

    public byte[] send(ProtocolsDto proto, int wait) throws Exception {
        return null;
    }

    public void reconect() {

    }

    public static void wait(int i) {
        if (i > 0) {
            try {
                Thread.sleep(i);
            } catch (InterruptedException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
