/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.bean.service;

import com.butter.bean.Butter;
import com.butter.bean.ClientWS;
import com.butter.bean.EquipoDao;
import com.butter.bean.Main;
import com.butter.bean.TransmisionBean;
import com.butter.bean.WSException;
import com.core.app.NeoService;
import com.core.database.DAOException;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS-PC
 */
public class ControllerSync extends Thread {

    int PAUSA = 10;
    int PAUSA_JORNADAS = 20;
    int TIEMPO = 0;

    SimpleDateFormat sdf = new SimpleDateFormat(Butter.FORMAT_DATETIME_SQL);

    @Override
    public void run() {

        while (true) {
            try {
                sincronizaJornada();
                TIEMPO++;
            } catch (Exception a) {
                Logger.getLogger(ControllerSync.class.getName()).log(Level.SEVERE, null, a);
            }
            pause(PAUSA);
        }
    }

    public void pause(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException s) {
        }
    }

    private void sincronizaJornada() {
        try {
            ClientWS client = new ClientWS();
            EquipoDao edao = new EquipoDao();
            ArrayList<TransmisionBean> trasmisiones = edao.getTransmisiones();

            for (TransmisionBean trasmisione : trasmisiones) {
                JsonObject response;
                try {
                    client.setTimeOut(30000);
                    response = client.execute(
                            Butter.DISABLE_HTTPS,
                            "ENVIANDO JORNADAS A CONSOLIDADO",
                            Main.credencial,
                            trasmisione.getUrl(),
                            trasmisione.getMethod(),
                            trasmisione.getRequest(),
                            Butter.ENABLE_DEBUG
                    );

                    if (client.getStatus() == 200) {
                        NeoService.setLog("Se ha sincronizado correctamente la venta");
                        trasmisione.setReintentos(trasmisione.getReintentos() + 1);
                        edao.updateTransmision(trasmisione.getId(), response, 1, new Date(), trasmisione.getReintentos());
                    } else {
                        trasmisione.setReintentos(trasmisione.getReintentos() + 1);
                        edao.updateTransmision(trasmisione.getId(), response, 0, new Date(), trasmisione.getReintentos());
                        NeoService.setLog("Error al sincronizar la venta");
                    }

                } catch (WSException ex) {
                    Logger.getLogger(ControllerSync.class.getName()).log(Level.SEVERE, null, ex);
                }
                pause(PAUSA_JORNADAS);
            }

        } catch (DAOException ex) {
            Logger.getLogger(ControllerSync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
