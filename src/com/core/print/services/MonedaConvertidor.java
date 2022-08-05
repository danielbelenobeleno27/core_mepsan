package com.core.print.services;

import com.core.app.NeoService;
import com.google.gson.JsonObject;

public class MonedaConvertidor {

    private String getUnidades(int num) {
        String result = "";
        switch (num) {
            case 1:
                result = "UN";
                break;
            case 2:
                result = "DOS";
                break;
            case 3:
                result = "TRES";
                break;
            case 4:
                result = "CUATRO";
                break;
            case 5:
                result = "CINCO";
                break;
            case 6:
                result = "SEIS";
                break;
            case 7:
                result = "SIETE";
                break;
            case 8:
                result = "OCHO";
                break;
            case 9:
                result = "NUEVE";
                break;
            default:
                break;
        }
        return result;
    }

    private String getDecenas(double num) {

        int decena = (int) (Math.floor(num / 10));
        int unidad = (int) (num - (decena * 10));
        String resultado = "";
        switch (decena) {
            case 1:
                switch (unidad) {
                    case 0:
                        resultado = "DIEZ";
                        break;
                    case 1:
                        resultado = "ONCE";
                        break;
                    case 2:
                        resultado = "DOCE";
                        break;
                    case 3:
                        resultado = "TRECE";
                        break;
                    case 4:
                        resultado = "CATORCE";
                        break;
                    case 5:
                        resultado = "QUINCE";
                        break;
                    default:
                        resultado = "DIECI" + getUnidades(unidad);
                        break;
                }
                break;
            case 2:
                NeoService.setLog("getUnidades ->" + unidad);
                switch (decena) {
                    case 0:
                        resultado = "VEINTE";
                        break;
                    default:
                        resultado = "VEINTI" + getUnidades(unidad);
                        break;
                }
                break;
            case 3:
                resultado = getDecenas("TREINTA", unidad);
                break;
            case 4:
                resultado = getDecenas("CUARENTA", unidad);
                break;
            case 5:
                resultado = getDecenas("CINCUENTA", unidad);
                break;
            case 6:
                resultado = getDecenas("SESENTA", unidad);
                break;
            case 7:
                resultado = getDecenas("SETENTA", unidad);
                break;
            case 8:
                resultado = getDecenas("OCHENTA", unidad);
                break;
            case 9:
                resultado = getDecenas("NOVENTA", unidad);
                break;
            case 0:
                resultado = getUnidades(unidad);
                break;
            default:
                break;
        }
        return resultado;
    }

    private String getCentenas(double num) {

        int centenas = (int) (Math.floor(num / 100));
        int decenas = (int) (num - (centenas * 100));
        String resultado = "";
        switch (centenas) {
            case 1:
                if (decenas > 0) {
                    resultado = "CIENTO " + getDecenas(decenas);
                } else {
                    resultado = "CIEN";
                }
                break;
            case 2:
                resultado = "DOCIENTOS " + getDecenas(decenas);
                break;
            case 3:
                resultado = "TRECIENTOS " + getDecenas(decenas);
                break;
            case 4:
                resultado = "CUATROCIENTOS " + getDecenas(decenas);
                break;
            case 5:
                resultado = "QUINIENTOS " + getDecenas(decenas);
                break;
            case 6:
                resultado = "SEISCIENTOS " + getDecenas(decenas);
                break;
            case 7:
                resultado = "SETECIENTOS " + getDecenas(decenas);
                break;
            case 8:
                resultado = "OCHOCIENTOS " + getDecenas(decenas);
                break;
            case 9:
                resultado = "NOVECIENTOS " + getDecenas(decenas);
                break;
            default:
                resultado = getDecenas(decenas);
                break;
        }
        return resultado;
    }

    private String getDecenas(String resultado, int num) {
        if (num > 0) {
            resultado = resultado + " Y " + getUnidades(num);
        }
        return resultado;
    }

    private String session(int num, int divisor, String singular, String plurar) {

        int cientos = (int) (Math.floor(num / divisor));
        int resto = num - (cientos * divisor);
        String letras = "";

        if (cientos > 0) {
            if (cientos > 1) {
                letras = getCentenas(cientos) + " " + plurar;
            } else {
                letras = getCentenas(cientos) + " " + singular;
            }
        }
        if (resto > 0) {
            letras += "";
        }

        return letras;
    }

    private String getMiles(int num) {
        int divisor = 1000;
        int cientos = (int) (Math.floor(num / divisor));
        int resto = (int) (num - (cientos * divisor));
        
        
        String miles = session(num, divisor, "UN MIL", "MIL");
        
        
        String centenas = getCentenas(resto);
        if (miles.equals("")) {
            return centenas;
        } else {
            return miles + " " + centenas;
        }
    }

    private String getMillones(int num) {
        int divisor = 1000000;
        int cientos = (int) (Math.floor(num / divisor));
        int resto = (int) (num - (cientos * divisor));

        String millones = session(num, divisor, "UN MILLON DE", "MILLONES DE");
        String miles = getMiles(resto);
        if (millones.equals("")) {
            return miles.trim();
        } else {
            return millones.trim() + " " + miles;
        }
    }

    public String numeroALetras(double num) {

        JsonObject currency = new JsonObject();
        currency.addProperty("plural", "PESOS");
        currency.addProperty("singular", "PESO");
        currency.addProperty("centPlural", "CENTAVOS");
        currency.addProperty("centSingular", "CENTAVOS");

        JsonObject data = new JsonObject();
        data.addProperty("numero", num);
        data.addProperty("enteros", Math.floor(num));
        data.addProperty("centavos", (Math.round(num * 100)) - (Math.floor(num) * 100));
        data.addProperty("letrasCentavos", "");
        data.addProperty("letrasMonedaPlural", currency.get("plural").getAsString());
        data.addProperty("letrasMonedaSingular", currency.get("singular").getAsString());
        data.addProperty("letrasMonedaCentavoPlural", currency.get("centPlural").getAsString());
        data.addProperty("letrasMonedaCentavoSingular", currency.get("centSingular").getAsString());

        String result = "";
        if (data.get("centavos").getAsInt() > 0) {
            data.addProperty("letrasCentavos", "CON " + getfuncMillones(data));
        }

        switch (data.get("enteros").getAsInt()) {
            case 0:
                result = "CERO " + data.get("letrasMonedaPlural").getAsString() + " " + data.get("letrasCentavos").getAsString();
                break;
            case 1:
                result = getMillones(data.get("enteros").getAsInt()) + " " + data.get("letrasMonedaSingular").getAsString() + " " + data.get("letrasCentavos").getAsString();
                break;
            default:
                result = getMillones(data.get("enteros").getAsInt()) + " " + data.get("letrasMonedaPlural").getAsString() + " " + data.get("letrasCentavos").getAsString();
                break;
        }
        return result;

    }

    public String getfuncMillones(JsonObject data) {
        if (data.get("centavos").getAsInt() == 1) {
            return getMillones(data.get("centavos").getAsInt()) + " " + data.get("letrasMonedaCentavoSingular").getAsString();
        } else {
            return getMillones(data.get("centavos").getAsInt()) + " " + data.get("letrasMonedaCentavoPlural").getAsString();
        }
    }

    public static void main(String[] args) {
        MonedaConvertidor conver = new MonedaConvertidor();
        NeoService.setLog(conver.numeroALetras(22050)); 
    }

}
