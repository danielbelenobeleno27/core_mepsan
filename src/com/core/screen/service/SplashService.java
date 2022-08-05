/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.screen.service;

import com.core.app.NeoService;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author ASUS-PC
 */
public class SplashService {

    public void showPlaca(int isla, int cara, String test) {
        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(NeoService.IP_PANTALLA, NeoService.PORT_PANTALLA), 500);

            DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());

            byte[] pre = new byte[]{0x3d};
            osw.write(pre);
            if (1 == cara) {
                NeoService.setLog("***************************" + isla + "A" + test);
                osw.write((isla + "A" + test).getBytes());
            } else {
                NeoService.setLog("***************************" + isla + "B" + test);
                osw.write((isla + "B" + test).getBytes());
            }
            byte[] pos = new byte[]{0x2d, 0x0d};
            osw.write(pos);
            osw.flush();
            osw.close();
            NeoService.setLog(test);

        } catch (Exception a) {
        }
    }

    public void showPlaca(int isla, char cara, String test) {
        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(NeoService.IP_PANTALLA, NeoService.PORT_PANTALLA), 500);

            DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());

            byte[] pre = new byte[]{0x3d};
            osw.write(pre);
            if ('1' == cara) {
                NeoService.setLog("***************************" + isla + "A" + test);
                osw.write((isla + "A" + test).getBytes());
            } else {
                NeoService.setLog("***************************" + isla + "B" + test);
                osw.write((isla + "B" + test).getBytes());
            }
            byte[] pos = new byte[]{0x2d, 0x0d};
            osw.write(pos);
            osw.flush();
            osw.close();
            NeoService.setLog(test);

        } catch (Exception a) {
        }
    }

    protected void saveDefaultMessage(String mensaje) {
        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(NeoService.IP_PANTALLA, NeoService.PORT_PANTALLA), 500);

            DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());
            byte[] pre = new byte[]{0x3d, 0x31, 0x30, 0x30, 0x30, 0x36, 0x30, 0x30, 0x30, 0x2b, 0x20, 0x20};
            osw.write(pre);

            if (mensaje.length() >= 200) {
                mensaje = mensaje.substring(0, 200);
            }
            osw.write(mensaje.getBytes());

            byte[] pos = new byte[]{0x20, 0x20, 0x0d};
            osw.write(pos);
            osw.flush();
            osw.close();
            NeoService.setLog(mensaje);
        } catch (Exception a) {
        }
    }

    public static void main(String[] args) throws Exception {
        SplashService spa = new SplashService();
        spa.saveDefaultMessage("EDS SolinGas Combustibles, La Vega, EDS La Floresta...   ***  Corresponsal Bancario Bancolombia    *** Recargas  ***  Factura    ***  Consignaciones  *** SOAT  y mucho mas");

    }

}
