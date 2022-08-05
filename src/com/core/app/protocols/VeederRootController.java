/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.protocols;

import com.butter.bean.TanqueBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author usuario
 */
public class VeederRootController {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm");
    int CONSTANTE_FIND_VALOR = 30;

    public ArrayList<TanqueBean> decodeTrama(ArrayList<Byte> trama) {

        ArrayList<TanqueBean> tanques = new ArrayList<>();

        int ano1 = (Integer.parseInt(String.format("%02x", trama.get(0)))) - CONSTANTE_FIND_VALOR;
        int ano2 = (Integer.parseInt(String.format("%02x", trama.get(1)))) - CONSTANTE_FIND_VALOR;
        String ano = 20 + "" + ano1 + "" + ano2;

        int mes1 = (Integer.parseInt(String.format("%02x", trama.get(2)))) - CONSTANTE_FIND_VALOR;
        int mes2 = (Integer.parseInt(String.format("%02x", trama.get(3)))) - CONSTANTE_FIND_VALOR;
        String mes = mes1 + "" + mes2;

        int dia1 = (Integer.parseInt(String.format("%02x", trama.get(4)))) - CONSTANTE_FIND_VALOR;
        int dia2 = (Integer.parseInt(String.format("%02x", trama.get(5)))) - CONSTANTE_FIND_VALOR;
        String dia = dia1 + "" + dia2;

        int hora1 = (Integer.parseInt(String.format("%02x", trama.get(6)))) - CONSTANTE_FIND_VALOR;
        int hora2 = (Integer.parseInt(String.format("%02x", trama.get(7)))) - CONSTANTE_FIND_VALOR;
        String hora = hora1 + "" + hora2;

        int min1 = (Integer.parseInt(String.format("%02x", trama.get(8)))) - CONSTANTE_FIND_VALOR;
        int min2 = (Integer.parseInt(String.format("%02x", trama.get(9)))) - CONSTANTE_FIND_VALOR;
        String min = min1 + "" + min2;

        Date fecha = null;
        try {
            fecha = sdf.parse(ano + "" + mes + "" + dia + " " + hora + "" + min);
        } catch (ParseException ex) {
            Logger.getLogger(VeederRootController.class.getName()).log(Level.SEVERE, null, ex);
        }

        int indice = 9;
        indice++;

        boolean finalTrama = false;
        while (!finalTrama) {
            TanqueBean tanque = new TanqueBean();
            int num1 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int num2 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int NUMERO_TANQUE = Integer.parseInt(num1 + "" + num2);
            tanque.setNumero(NUMERO_TANQUE);
            tanque.setFecha(fecha);
            int productoCode = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;

            int status1 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int status2 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int status3 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int status4 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int status = status4;
            tanque.setStatus(status);

            int dato1 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int dato2 = (Integer.parseInt(String.format("%02x", trama.get(indice)))) - CONSTANTE_FIND_VALOR;
            indice++;
            int numeroDigitoDato = Integer.parseInt(dato1 + "" + dato2);

            String data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataVolumen = data;

            data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataTCVolumen = data;

            data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataMerma = data;

            data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataALtura = data;

            data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataAgua = data;

            data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataTemperatura = data;

            data = "";
            for (int i = 0; i <= numeroDigitoDato; i++) {
                int subdata = (Integer.parseInt(String.format("%02x", trama.get(indice))));
                indice++;
                data += subdata + " ";
            }
            String dataVolumenAgua = data;

            /*
            NeoService.setLog("dataVolumen -> " + calcularValor(dataVolumen));
            NeoService.setLog("dataTCVolumen -> " + calcularValor(dataTCVolumen));
            NeoService.setLog("dataMerma -> " + calcularValor(dataMerma));
            NeoService.setLog("dataALtura -> " + calcularValor(dataALtura));
            NeoService.setLog("dataAgua -> " + calcularValor(dataAgua));
            NeoService.setLog("dataTemperatura -> " + calcularValor(dataTemperatura));
            NeoService.setLog("dataVolumenAgua -> " + calcularValor(dataVolumenAgua));
             */
            tanque.setVolumen(calcularValor(dataVolumen));
            tanque.setVolumenTC(calcularValor(dataTCVolumen));
            tanque.setMerma(calcularValor(dataMerma));
            tanque.setAltura(calcularValor(dataALtura));
            tanque.setAgua(calcularValor(dataAgua));
            tanque.setTemperatura(calcularValor(dataTemperatura));
            tanque.setVolumenAgua(calcularValor(dataVolumenAgua));
            tanques.add(tanque);

            int iFinal1 = (Integer.parseInt(String.format("%02x", trama.get(indice))));
            int iFinal2 = (Integer.parseInt(String.format("%02x", trama.get(indice + 1))));

            if (iFinal1 == 26 && iFinal2 == 26) {
                finalTrama = true;
            }
        }

        return tanques;
    }

    private double calcularValor(String dataSource) {

        String[] data = dataSource.split(" ");
        String result = "";
        int i = 0;
        for (String c : data) {
            if (i % 2 == 0) {
                result += ((char) Integer.parseInt(c, 16));
            } else {
                result += ((char) Integer.parseInt(c, 16)) + " ";
            }
            i++;
        }
        String[] subdata = result.split(" ");
        String nuevoDataBinaruia = "";
        for (String caracter : subdata) {
            String binaryString = String.format("%8s", Integer.toBinaryString(Integer.parseInt(caracter, 16))).replaceAll(" ", "0");
            nuevoDataBinaruia += binaryString + " ";
        }
        double CONSTANTE_EXPONENTE = 127;
        int CONSTANTE_MANTISA = 8388608;
        int signo = nuevoDataBinaruia.startsWith("1") ? -1 : 1;
        String e = nuevoDataBinaruia.substring(1, 10);
        String m = nuevoDataBinaruia.substring(10);
        int valorE = Integer.parseInt(e.replace(" ", ""), 2);
        int valorM = Integer.parseInt(m.replace(" ", ""), 2);
        int exponencial = (int) (Math.pow(2, (valorE - CONSTANTE_EXPONENTE)));
        double mantisa = 1 + ((double) valorM / (double) CONSTANTE_MANTISA);

        double decimal = round(signo * (exponencial * mantisa), 2);

        /**
         * NeoService.setLog(nuevoDataBinaruia); NeoService.setLog("signo " +
         * signo); NeoService.setLog("e " + e + " -> " + e.replace(" ", "") + "
         * = " + valorE); NeoService.setLog("m " + valorM);
         * NeoService.setLog("expo " + exponencial);
         * NeoService.setLog("mantisa " + mantisa); NeoService.setLog("decimal
         * " + decimal);
         *
         */
        return decimal;

    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
