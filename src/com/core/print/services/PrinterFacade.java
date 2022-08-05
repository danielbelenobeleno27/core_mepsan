/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.print.services;

import com.butter.bean.EmpresaBean;
import com.butter.bean.EquipoDao;
import com.butter.bean.SaltoLecturaBean;
import com.butter.bean.Utils;
import com.core.app.NeoService;
import com.neo.app.bean.AConstant;
import com.neo.app.bean.Recibo;
import com.neo.app.bean.Response;
import com.core.database.DAOException;
import com.core.database.impl.SurtidorDao;
import com.core.turnos.bean.SurtidorInventario;
import com.core.turnos.bean.TurnosResponse;
import com.neo.app.bean.MediosPagosBean;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintException;

/**
 *
 * @author ASUS-PC
 */
public class PrinterFacade {

    String ultimoTextoImpreso = "";

    public void sinFormato(String texto) {
        if (NeoService.TIENE_IMPRESORA) {
            try {
                if (!ultimoTextoImpreso.equals(texto)) {
                    PrinterService service = new PrinterService();
                    service.printText(texto);
                    ultimoTextoImpreso = texto;
                }
            } catch (IOException ex) {
                NeoService.setLog("Ocurrio un error al imprimir");
            } catch (PrintException ex) {
                NeoService.setLog("Ocurrio un error al imprimir");
            }
        }
    }

    public void conFormato(String texto) {
        try {

            if (!ultimoTextoImpreso.equals(texto)) {
                SurtidorDao dao = new SurtidorDao();
                try {
                    NeoService.IP_IMPRESORA = dao.getParametros("impresora");
                } catch (DAOException ex) {
                    Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
                }

                NeoService.setLog("[PRINT ]: " + NeoService.TIENE_IMPRESORA);
                NeoService.setLog(texto);

                String text = "";
                text += "===============================================\r\n";
                text += texto;
                text += "===============================================\r\n";

                if (NeoService.TIENE_IMPRESORA) {
                    PrinterService service = new PrinterService();
                    service.printText(text);
                } else {
                    NeoService.setLog(text);
                }
                ultimoTextoImpreso = texto;
            }
        } catch (IOException ex) {
            NeoService.setLog("Ocurrio un error al imprimir");
        } catch (PrintException ex) {
            NeoService.setLog("Ocurrio un error al imprimir");
        }
    }

    public void conFormatoIcono(int icono, String texto) {
        try {
            if (!ultimoTextoImpreso.equals(texto)) {
                NeoService.setLog("[PRINT ]: " + texto);

                String text = "";
                text += texto;
                if (NeoService.TIENE_IMPRESORA) {
                    PrinterService service = new PrinterService();
                    if (icono > 0) {
                        service.printTextConLogo(text);
                    } else {
                        service.printText(text);
                    }
                } else {
                    NeoService.setLog(text);
                }
                ultimoTextoImpreso = texto;
            }
        } catch (IOException ex) {
            NeoService.setLog("Ocurrio un error al imprimir");
        } catch (PrintException ex) {
            NeoService.setLog("Ocurrio un error al imprimir");
        }
    }

    private String printFullText(int i, String string) {

        String line = "\r\n";
        if (string.length() >= i - 1) {
            while (string.length() >= i - 1) {
                if (string.length() >= i - 1) {
                    line += string.substring(0, i - 1) + "\r\n";
                    string = string.substring(i - 1, string.length());
                } else {
                    line += string + "\r\n";
                }
            }
        } else {
            line += string + "\r\n";
        }
        return line + "\r\n";
    }

    public TurnosResponse printInicioTurnosResponse(TurnosResponse ticket) {

        TurnosResponse op = new TurnosResponse();
        try {
            ArrayList<byte[]> lista = new ArrayList<>();
            lista.add("===============================================\r\n".getBytes());
            lista.add(PrinterService.TXT_ALIGN_CT);
            lista.add(PrinterService.TXT_2HEIGHT);
            lista.add(PrinterService.TXT_FONT_D);
            lista.add(PrinterService.TXT_BOLD_ON);
            lista.add(("INICIO DE JORNADA\r\n").getBytes());

            lista.add(PrinterService.TXT_BOLD_OFF);
            lista.add(PrinterService.TXT_FONT_A);
            lista.add(PrinterService.TXT_NORMAL);
            lista.add("===============================================\r\n".getBytes());

            Date ahora = new Date();//TODOhora de inici de turno de la db
            SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_DATETIME_AM);
            DecimalFormat dff = new DecimalFormat("$ ###,###");
            //String text = "";

            //text += "===============================================\r\n";
            if (ticket.isSuccess()) {
                //text += "INICIO DE JORNADA EXITOSO!\r\n";
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(("INICIO DE JORNADA EXITOSO!\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_ALIGN_LT);
            } else {
                //text += "ERROR AL INICIAR JORNADA!\r\n";
                //text += ticket.getMensaje() + "\r\n";
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(("ERROR AL INICIAR LA JORNADA!\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_ALIGN_LT);
                lista.add((ticket.getMensaje() + "\r\n").getBytes());
            }
            if (ticket.getPersona() != null) {
                //text += "IDENTIF.:     " + ticket.getPersona().getIdentificacion() + "\r\n";
                //text += "NOMBRE:       " + ticket.getPersona().getNombre() + "\r\n";
                lista.add(("IDENTIFICACION:").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add((" " + ticket.getPersona().getIdentificacion() + "\r\n").getBytes());
                lista.add(("NOMBRE:").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((" " + ticket.getPersona().getNombre() + "\r\n").getBytes());

            }
            if (ticket.getSurtidor() != null) {
                //text += "SURTIDOR:     " + ticket.getSurtidor().getId() + "\r\n";
                lista.add(("SURTIDOR:").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add((" " + ticket.getSurtidor().getId() + "\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                if (ticket.getFechaInicio() != null) {
                    //text += "FECHA INICIO: " + sdf.format(ticket.getFechaInicio()) + "\r\n";
                    lista.add(("FECHA DE INICIO: ").getBytes());
                    lista.add((sdf.format(ticket.getFechaInicio()) + "\r\n").getBytes());
                } else {
                    //text += "FECHA: " + sdf.format(ahora) + "\r\n";
                    lista.add(("FECHA: ").getBytes());
                    lista.add(PrinterService.TAB_H);
                    lista.add(PrinterService.TAB_H);
                    lista.add((sdf.format(ahora) + "\r\n").getBytes());
                }
                //text += "FECHA INICIO: " + sdf.format(ticket.getFechaInicio()) + "\r\n";
            }
            //text += "===============================================\r\n";
            lista.add("===============================================\r\n".getBytes());

            for (SurtidorInventario invt : ticket.getInventario()) {
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add("-----------------------------------------------\r\n".getBytes());
                lista.add(("MANG " + String.valueOf(invt.getManguera()) + ": " + invt.getProductoDescripcion() + "\r\n").getBytes());
                lista.add("-----------------------------------------------\r\n".getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);

                int factorInventario = (int) NeoService.sharedPreference.get(NeoService.FACTOR_INVENTARIO + invt.getSurtidor());

                double volIni = invt.getAcumuadoVolumenInicial() / Math.pow(10, factorInventario);
                String descrip = "VOLUMEN INICIAL";
                lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                lista.add((Utils.str_pad(String.valueOf(volIni), 28, " ", "STR_PAD_LEFT")).getBytes());
                lista.add(("\r\n").getBytes());
                /*
                float ventIni = invt.getAcumuadoVentasInicial();
                descrip = "VENTAS INICIAL";
                lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                lista.add((Utils.str_pad(dff.format(ventIni), 28, " ", "STR_PAD_LEFT")).getBytes());
                lista.add(("\r\n").getBytes());
                 */
            }

            //PrinterService service = new PrinterService();
            //service.printText(text);
            /*IMPRESORA AUXILIAR*/
            PrinterService service2 = new PrinterService();
            if (NeoService.TIENE_IMPRESORA) {
                service2.printBytes(lista);
            }
            op.setSuccess(true);
            op.setMensaje("TICKET IMPRESO CORRECTAMENTE");
        } catch (IOException ex) {
            op.setSuccess(false);
            op.setMensaje("ERROR AL IMPRIMIR EL TICKET");
            op.setError(ex.getMessage());
        } catch (PrintException ex) {
            op.setSuccess(true);
            op.setMensaje("ERROR AL IMPRIMIR EL TICKET");
            op.setError(ex.getMessage());
        }
        return op;

    }

    public TurnosResponse printFinTurnosResponse(TurnosResponse ticket) {
        TurnosResponse op = new TurnosResponse();
        try {

            Date ahora = new Date();

            DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
            simbolos.setDecimalSeparator('.');

            SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_DATETIME_AM);
            DecimalFormat dff = new DecimalFormat("$ ###,###");
            DecimalFormat df2 = new DecimalFormat("#.00", simbolos);
            ArrayList<byte[]> lista = new ArrayList<>();

            SurtidorDao ssdao = new SurtidorDao();
            EmpresaBean empresa = ssdao.getDatosEmpresa();

            lista.add(PrinterService.TXT_ALIGN_CT);
            lista.add(PrinterService.TXT_2HEIGHT);
            lista.add(PrinterService.TXT_FONT_D);
            lista.add(PrinterService.TXT_BOLD_ON);
            lista.add((empresa.getRazonSocial() + "\r\n").getBytes());
            lista.add(PrinterService.TXT_BOLD_OFF);
            lista.add(PrinterService.TXT_FONT_A);
            lista.add(PrinterService.TXT_NORMAL);
            lista.add((empresa.getNit() + "\r\n\r\n").getBytes());

            lista.add("===============================================\r\n".getBytes());
            lista.add(PrinterService.TXT_ALIGN_CT);
            lista.add(PrinterService.TXT_2HEIGHT);
            lista.add(PrinterService.TXT_FONT_D);
            lista.add(PrinterService.TXT_BOLD_ON);
            lista.add(("FIN DE JORNADA\r\n").getBytes());
            lista.add(PrinterService.TXT_BOLD_OFF);
            lista.add(PrinterService.TXT_FONT_A);
            lista.add(PrinterService.TXT_NORMAL);
            lista.add("===============================================\r\n".getBytes());
            //String text = "";
            //text += "===============================================\r\n";
            if (ticket.isSuccess()) {
                //text += "FIN DE JORNADA EXITOSO!\r\n";
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(("FIN DE JORNADA EXITOSO!\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_ALIGN_LT);
            } else {
                //text += "ERROR AL FINALIZAR LA JORNADA!\r\n";
                //text += ticket.getMensaje() + "\r\n";
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(("ERROR AL FINALIZAR LA JORNADA!\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_ALIGN_LT);
                lista.add((ticket.getMensaje() + "\r\n").getBytes());
            }
            if (ticket.getPersona() != null) {
                //text += "TURNO No.:   " + ticket.getId() + "\r\n";
                lista.add(("TURNO No.:").getBytes());
                lista.add(PrinterService.TAB_H);
                //lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add((ticket.getId() + "\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                //text += "IDENTIF.:     " + ticket.getPersona().getIdentificacion() + "\r\n";
                lista.add(("IDENTIFICACION:").getBytes());
                lista.add(PrinterService.TAB_H);
                //lista.add(PrinterService.TXT_BOLD_ON);
                lista.add((ticket.getPersona().getIdentificacion() + "\r\n").getBytes());
                //lista.add(PrinterService.TXT_BOLD_OFF);
                //text += "NOMBRE:       " + ticket.getPersona().getNombre() + "\r\n";
                lista.add(("NOMBRE:").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((ticket.getPersona().getNombre() + "\r\n").getBytes());
            }
            if (ticket.getSurtidor() != null) {
                //text += "SURTIDOR:     " + ticket.getSurtidor().getId() + "\r\n";
                lista.add(("SURTIDOR:").getBytes());
                lista.add(PrinterService.TAB_H);
                //lista.add(PrinterService.TAB_H);
                lista.add((ticket.getSurtidor().getId() + "\r\n").getBytes());
                //text += "FECHA INICIO: " + sdf.format(ticket.getFecha()) + "\r\n";
                lista.add(("FECHA INICIO: ").getBytes());
                //lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((sdf.format(ticket.getFecha()) + "\r\n").getBytes());
                //text += "FECHA FIN:    " + sdf.format(ahora) + "\r\n";
                lista.add(("FECHA FIN: ").getBytes());
                //lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((sdf.format(ahora) + "\r\n").getBytes());
                //text += "TRANSCURRIDO: " + Utils.getLapsoTiempo(ticket.getFecha(), ahora) + "\r\n";
                lista.add(("TRANSCURRIDO: ").getBytes());
                lista.add(PrinterService.TAB_H);
                //lista.add(PrinterService.TAB_H);
                lista.add((Utils.getLapsoTiempo(ticket.getFecha(), ahora) + "\r\n").getBytes());
                //text += "\r\n";
                lista.add(("\r\n").getBytes());
                //text += "RESUMEN VENTAS ---- ";
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(("RESUMEN DE VENTAS\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_ALIGN_LT);
                //text += "\r\n";
                //text += "TOTAL CANTIDAD:    " + String.valueOf(ticket.getAcumuadoVolumen() / Math.pow(10, ticket.getFactorInventario())) + " m3" + "\r\n";
                lista.add(("TOTAL CANTIDAD: ").getBytes());
                lista.add(PrinterService.TAB_H);
                //lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TXT_BOLD_ON);

//                text += "TOTAL CANTIDAD:    " + String.valueOf(Utils.round(ticket.getAcumuadoVolumen() / Utils.potencia(100, 1), 2)) + " GL" + "\r\n";
//                text += "TOTAL VENTAS:      " + String.valueOf(dff.format(ticket.getAcumuadoVentas() / 10)) + "\r\n";
                lista.add((String.valueOf(ticket.getAcumuadoVolumen()) + " GL" + "\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                //text += "TOTAL VENTAS:      " + String.valueOf(dff.format(ticket.getAcumuadoVentas())) + "\r\n";
                lista.add(("TOTAL VENTAS: ").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(String.valueOf(dff.format((ticket.getAcumuadoVentas() / Math.pow(10, ticket.getFactorPrecio()))) + "\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                //text += "NUMERO DE VENTAS:  " + String.valueOf(ticket.getCantidad()) + "\r\n";
                lista.add(("NUMERO DE VENTAS: ").getBytes());
                lista.add(PrinterService.TAB_H);
                //lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add((String.valueOf(ticket.getCantidad()) + "\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(("\r\n").getBytes());
                //text += "\r\n";
                //text += "INVENTARIOS ---- ";
                lista.add("===============================================\r\n".getBytes());
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(("INVENTARIOS\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_ALIGN_LT);
                //text += "\r\n";
                int tempS = 0;
                int tempM = 0;

                for (SurtidorInventario invt : ticket.getInventario()) {
                    //text += "S" + String.valueOf(invt.getSurtidor());
                    if (invt.getSurtidor() != tempS) {
                        lista.add(("Surtidor: ").getBytes());
                        lista.add(String.valueOf(invt.getSurtidor() + "\r\n").getBytes());
                        tempS = invt.getSurtidor();
                    }

                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add("-----------------------------------------------\r\n".getBytes());
                    lista.add(("MANG " + String.valueOf(invt.getManguera()) + ": " + invt.getProductoDescripcion() + "\r\n").getBytes());
                    lista.add("-----------------------------------------------\r\n".getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);

                    int factorVolumen = (int) NeoService.sharedPreference.get(NeoService.FACTOR_VOLUMEN + invt.getSurtidor());
                    int factorPrecio = (int) NeoService.sharedPreference.get(NeoService.FACTOR_PRECIO + invt.getSurtidor());
                    int factorInventario = (int) NeoService.sharedPreference.get(NeoService.FACTOR_INVENTARIO + invt.getSurtidor());

                    //text += "M" + String.valueOf(invt.getManguera()) + " ";
                    float ventIni = invt.getAcumuadoVentasInicial();
                    float ventFin = invt.getAcumuadoVentasFinal();
                    float ventDif = invt.getAcumuadoVentasFinal() - invt.getAcumuadoVentasInicial();

                    double volIni = invt.getAcumuadoVolumenInicial() / Math.pow(10, factorInventario);
                    double volFin = invt.getAcumuadoVolumenFinal() / Math.pow(10, factorInventario);
                    double volDif = ((invt.getAcumuadoVolumenFinal() - invt.getAcumuadoVolumenInicial()) / Math.pow(10, factorInventario));

                    String descrip = "VOLUMEN INICIAL";
                    lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                    lista.add((Utils.str_pad(String.valueOf(volIni), 28, " ", "STR_PAD_LEFT")).getBytes());
                    lista.add(("\r\n").getBytes());

                    descrip = "VOLUMEN FINAL";
                    lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                    lista.add((Utils.str_pad(String.valueOf(volFin), 28, " ", "STR_PAD_LEFT")).getBytes());
                    lista.add(("\r\n").getBytes());

                    descrip = "VENTAS INICIAL";
                    lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                    lista.add((Utils.str_pad(dff.format(ventIni), 28, " ", "STR_PAD_LEFT")).getBytes());
                    lista.add(("\r\n").getBytes());

                    descrip = "VENTAS FINAL";
                    lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                    lista.add((Utils.str_pad(dff.format(ventFin), 28, " ", "STR_PAD_LEFT")).getBytes());
                    lista.add(("\r\n").getBytes());

                    lista.add(PrinterService.TAB_H);
                    lista.add(PrinterService.TAB_H);
                    lista.add(PrinterService.TAB_H);
                    lista.add("-----------------------\r\n".getBytes());
                    //lista.add(PrinterService.TXT_ALIGN_LT);
                    /*
                    lista.add("INVENTARIO DIFERENCIA:".getBytes());
                    lista.add(PrinterService.TAB_H);
                    lista.add(PrinterService.TAB_H);
                    lista.add((String.valueOf(volDif) + "\r\n").getBytes());
                    lista.add(PrinterService.TXT_ALIGN_LT);
                     */

                    descrip = "VENTAS TOTAL";
                    lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                    lista.add((Utils.str_pad(dff.format(ventDif), 28, " ", "STR_PAD_LEFT")).getBytes());
                    lista.add(("\r\n").getBytes());

                    ArrayList<SaltoLecturaBean> saltos = ssdao.getSaltoLectura(invt);

                    if (!saltos.isEmpty()) {

                        float nuevoSaldo = 0;
                        lista.add(("SALTOS DE LECTURA ENCONTRADOS\r\n").getBytes());
                        for (SaltoLecturaBean salto : saltos) {
                            descrip = "SALTO: [" + salto.getCantidad() + "]";
                            lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                            lista.add((Utils.str_pad(dff.format(salto.getValor()), 28, " ", "STR_PAD_LEFT")).getBytes());
                            lista.add(("\r\n").getBytes());
                            nuevoSaldo += salto.getValor();
                        }

                        descrip = "SALDO FINAL";
                        lista.add((Utils.str_pad(descrip, 20, " ", "STR_PAD_RIGHT")).getBytes());
                        lista.add((Utils.str_pad(dff.format((ventFin + nuevoSaldo)), 28, " ", "STR_PAD_LEFT")).getBytes());
                        lista.add(("\r\n").getBytes());
                        ssdao.actualizaSaltoLectura();
                    }

                }

            } else {
                //text += "FECHA: " + sdf.format(ahora) + "\r\n";
                lista.add(("FECHA: ").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((sdf.format(ahora) + "\r\n").getBytes());
            }
            //text += "===============================================\r\n";
            lista.add("===============================================\r\n".getBytes());
            //PrinterService service = new PrinterService();
            //service.printText(text);
            PrinterService service2 = new PrinterService();
            if (NeoService.TIENE_IMPRESORA) {
                service2.printBytes(lista);
            }
            op.setSuccess(true);
            op.setMensaje("TICKET IMPRESO CORRECTAMENTE");
        } catch (IOException ex) {
            op.setSuccess(false);
            op.setMensaje("ERROR AL IMPRIMIR EL TICKET");
            op.setError(ex.getMessage());
        } catch (PrintException ex) {
            op.setSuccess(true);
            op.setMensaje("ERROR AL IMPRIMIR EL TICKET");
            op.setError(ex.getMessage());
        }
        return op;
    }

    public Response printJornada(TurnosResponse ticket) {
        Response op = new Response();
        try {

            Date ahora = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_BASIC_DATETIME_AM);
            DecimalFormat dff = new DecimalFormat("$ ###,###");
            DecimalFormat df2 = new DecimalFormat("#.000");
            String text = "";
            text += "===============================================\r\n";
            text += "INFORMACION DEL TURNO. Nro. " + ticket.getId() + "\r\n";
            text += "IDENTIF.:  " + ticket.getPersona().getIdentificacion() + "\r\n";
            text += "NOMBRE:    " + ticket.getPersona().getNombre() + "\r\n";
            text += "\r\n";
            text += "FECHA INICIO:      " + sdf.format(ticket.getFechaInicio()) + "\r\n";
            text += "TOTAL CANTIDAD:    " + String.valueOf(ticket.getAcumuadoVolumen() / Math.pow(10, ticket.getFactorInventario())) + " m3" + "\r\n";
            text += "TOTAL VENTAS:      " + String.valueOf(dff.format(ticket.getAcumuadoVentas())) + "\r\n";
            text += "NUMERO DE VENTAS:  " + String.valueOf(ticket.getCantidad()) + "\r\n";
            text += "\r\n";
            text += "INVENT ---- ";
            text += "\r\n";
            for (SurtidorInventario invt : ticket.getInventario()) {
                text += "S" + String.valueOf(invt.getSurtidor());
                text += "M" + String.valueOf(invt.getManguera()) + " ";
                double vol1 = invt.getAcumuadoVolumenInicial() / Math.pow(10, ticket.getFactorInventario());
                double vol2 = invt.getAcumuadoVolumenFinal() / Math.pow(10, ticket.getFactorInventario());
                text += "VOL I: " + df2.format(vol1) + " m3\t";
                text += "VOL F: " + df2.format(vol2) + " m3\r\n";
            }

            text += "===============================================\r\n";
            PrinterService service = new PrinterService();
            if (NeoService.TIENE_IMPRESORA) {
                service.printText(text);
            }
            op.setSuccess(true);
            op.setMensaje("TICKET IMPRESO CORRECTAMENTE");
        } catch (IOException ex) {
            op.setSuccess(false);
            op.setMensaje("ERROR AL IMPRIMIR EL TICKET");
            op.setError(ex.getMessage());
        } catch (PrintException ex) {
            op.setSuccess(true);
            op.setMensaje("ERROR AL IMPRIMIR EL TICKET");
            op.setError(ex.getMessage());
        }
        return op;
    }

    public void printRecibo(Recibo recibo) {
        if (NeoService.TIENE_IMPRESORA) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_BASIC_DATETIME_AM);
                //DecimalFormat dff = new DecimalFormat("$ 000"); 
                Formatter formatter = new Formatter();

                NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);

                ArrayList<byte[]> lista = new ArrayList<>();

                lista.add("===============================================\r\n".getBytes());
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(PrinterService.TXT_2HEIGHT);
                lista.add(PrinterService.TXT_FONT_D);
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add((recibo.getEmpresa() + "\r\n").getBytes());

                lista.add(PrinterService.TXT_BOLD_OFF);
                lista.add(PrinterService.TXT_FONT_A);
                lista.add(PrinterService.TXT_NORMAL);
                lista.add((recibo.getDireccion() + "\r\n").getBytes());
                lista.add(("TEL: " + recibo.getTelefono() + "\r\n").getBytes());

                EquipoDao edao = new EquipoDao();
                String empresa_dococumento_alias = edao.getParametroString("empresa_dococumento_alias");
                if (empresa_dococumento_alias != null) {
                    lista.add((empresa_dococumento_alias + ": " + recibo.getNit() + "\r\n").getBytes());
                } else {
                    lista.add(("NIT: " + recibo.getNit() + "\r\n").getBytes());
                }
                lista.add(PrinterService.TXT_ALIGN_LT);
                lista.add(("----------------------------------------------\r\n").getBytes());
                lista.add(("NRO DE TICKET: ").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add((recibo.getNumero() + "\r\n").getBytes());
                lista.add(("FECHA:").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((sdf.format(recibo.getFecha()) + "\r\n").getBytes());

                lista.add(("ISLA:").getBytes());
                lista.add(PrinterService.TAB_H);
                lista.add(PrinterService.TAB_H);
                lista.add((recibo.getIsla() + "\r\n").getBytes());

                lista.add(("SURTIDOR: " + recibo.getSurtidor()).getBytes());
                lista.add(PrinterService.TAB_H);

                lista.add(("CARA: ").getBytes());
                lista.add((recibo.getCara()).getBytes());

                lista.add(PrinterService.TAB_H);
                lista.add(("MANGUERA: " + recibo.getManguera() + "\r\n").getBytes());

                lista.add(("OPERARIO:       " + recibo.getOperador() + "   \r\n").getBytes());

                lista.add(("===============================================\r\n").getBytes());
                if (!recibo.getCopia().equals("")) {
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(PrinterService.TXT_ALIGN_CT);
                    lista.add((" " + recibo.getCopia() + "\r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);
                    lista.add(PrinterService.TXT_ALIGN_LT);
                    lista.add(("===============================================\r\n").getBytes());
                }

                SurtidorDao sdao = new SurtidorDao();
                //int factorVolumen = (int) NeoService.sharedPreference.get(NeoService.FACTOR_VOLUMEN + recibo.getSurtidor());
                //int factorPrecio = (int) NeoService.sharedPreference.get(NeoService.FACTOR_PRECIO + recibo.getSurtidor());
                int factorVolumen = 0;
                int factorImporte = 0;
                int factorPrecio = 0;
                try {
                    factorVolumen = sdao.getFactorVolumen(Integer.parseInt(recibo.getSurtidor()));
                    factorImporte = sdao.getFactorImporte(Integer.parseInt(recibo.getSurtidor()));
                    factorPrecio = sdao.getFactorPrecio(Integer.parseInt(recibo.getSurtidor()));
                } catch (DAOException a) {
                }

                double cant = Utils.calculeCantidadDouble(recibo.getCantidad(), factorVolumen);
                double precio = Utils.calculeCantidadDouble(recibo.getPrecio(), factorPrecio);
                double total = Utils.calculeCantidadDouble(recibo.getTotal(), factorImporte);

                lista.add(("PRODUCTO:       ").getBytes());
                lista.add((recibo.getProducto() + "\r\n").getBytes());
                lista.add(("PRECIO:         ").getBytes());
                lista.add((n.format(precio * 1) + "\r\n").getBytes());
                lista.add(("CANTIDAD:       ").getBytes());
                lista.add((cant + " " + NeoService.UNIDAD_MEDIDA + "\r\n").getBytes());

                if (recibo.getRecaudo() > 0) {
                    lista.add(("RECAUDO X " + NeoService.UNIDAD_MEDIDA + "    ").getBytes());
                    lista.add((n.format(recibo.getRecaudo()) + "\r\n").getBytes());
                    double recaudo = (cant * recibo.getRecaudo());
                    lista.add(("TOTAL RECAUDO:  ").getBytes());
                    lista.add((n.format(recaudo) + "\r\n").getBytes());

                    lista.add(("SUBTOTAL:       ").getBytes());
                    lista.add((n.format(total) + "   \r\n").getBytes());

                    total = recaudo + total;
                }

                lista.add(("TOTAL:          ").getBytes());
                lista.add((n.format(total) + "   \r\n").getBytes());

                MonedaConvertidor nm = new MonedaConvertidor();
                String texto = nm.numeroALetras(total);
                lista.add(("(" + texto + ") \r\n\r\n").getBytes());

                if (recibo.getCliente() != null) {
                    lista.add(("CLIENTE:        ").getBytes());
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(((recibo.getCliente().toUpperCase()) + "   \r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);

                    if (recibo.getSerialAutorizacion() != null) {
                        lista.add(("SERIAL:         ").getBytes());
                        lista.add(PrinterService.TXT_BOLD_ON);
                        lista.add(((recibo.getSerialAutorizacion()) + "   \r\n").getBytes());
                        lista.add(PrinterService.TXT_BOLD_OFF);
                    }

                }

                if (recibo.getAtributos() != null && recibo.getAtributos().get("vehiculo_placa") != null) {
                    lista.add(("PLACA:          ").getBytes());
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(((recibo.getAtributos().get("vehiculo_placa").getAsString()) + "   \r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);
                } else {
                    lista.add(("PLACA:       \r\n").getBytes());
                }

                if (recibo.getAtributos() != null && recibo.getAtributos().get("vehiculo_numero") != null) {
                    lista.add(("N.INTERNO:      ").getBytes());
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(((recibo.getAtributos().get("vehiculo_numero").getAsString()) + "   \r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);
                }

                if (recibo.getAtributos() != null && recibo.getAtributos().get("voucher") != null) {
                    lista.add(("COMPROBANTE:    ").getBytes());
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(((recibo.getAtributos().get("voucher").getAsString()) + "   \r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);
                }

                if (recibo.getOdometro() != null) {
                    lista.add(("ODOMETRO:       ").getBytes());
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(((recibo.getOdometro()) + "   \r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);
                }

                if (recibo.getMediosPagos() != null && recibo.getMediosPagos().isEmpty()) {
                    lista.add(("MEDIO DE PAGO:  ").getBytes());
                    lista.add(PrinterService.TXT_BOLD_ON);
                    lista.add(((recibo.getMedioDescripcion() == null ? "EFECTIVO" : recibo.getMedioDescripcion()) + "   \r\n").getBytes());
                    lista.add(PrinterService.TXT_BOLD_OFF);
                } else {
                    int i = 1;
                    for (MediosPagosBean mediosPago : recibo.getMediosPagos()) {
                        if (recibo.getMediosPagos().size() == 1 && mediosPago.getDescripcion().equals("EFECTIVO")) {
                            lista.add(("MEDIO DE PAGO:  ").getBytes());
                            lista.add(PrinterService.TXT_BOLD_ON);
                            lista.add((recibo.getMedioDescripcion() + "\r\n").getBytes());
                            lista.add(PrinterService.TXT_BOLD_OFF);
                        } else {
                            lista.add(("MEDIO DE PAGO " + i + ":   ").getBytes());
                            lista.add(PrinterService.TXT_BOLD_ON);
                            lista.add((mediosPago.getId() + "-" + mediosPago.getDescripcion() + "   \r\n").getBytes());
                            lista.add(PrinterService.TXT_BOLD_OFF);
                            lista.add(("VALOR:             ").getBytes());
                            lista.add(PrinterService.TXT_BOLD_ON);
                            lista.add((n.format(mediosPago.getValor()) + "   \r\n").getBytes());
                            lista.add(PrinterService.TXT_BOLD_OFF);
                            if (mediosPago.getVoucher() != null && !mediosPago.getVoucher().isEmpty()) {
                                lista.add(("VOUCHER:           ").getBytes());
                                lista.add(PrinterService.TXT_BOLD_ON);
                                lista.add((mediosPago.getVoucher() + "   \r\n").getBytes());
                                lista.add(PrinterService.TXT_BOLD_OFF);
                            }
                            lista.add(("\r\n").getBytes());
                            i++;
                        }
                    }
                }
                if (recibo.getRecaudo() > 0) {
                    if (recibo.getAutorizacionAtributos() != null) {
                        String financiera = recibo.getAutorizacionAtributos().get("financiera").getAsString();
                        lista.add(("FINANCIERA:     ").getBytes());
                        lista.add(PrinterService.TXT_BOLD_ON);
                        lista.add((financiera + "   \r\n").getBytes());
                        lista.add(PrinterService.TXT_BOLD_OFF);
                    }
                }

                lista.add(("\r\n\r\n").getBytes());
                lista.add(PrinterService.TXT_ALIGN_CT);
                lista.add(PrinterService.TXT_BOLD_ON);
                lista.add(("GRACIAS POR SU VISITA\r\n").getBytes());
                lista.add(PrinterService.TXT_BOLD_OFF);
                //lista.add(("LAZOEXPRESS POS v." + NeoService.VERSION_NAME + "\r\n").getBytes());
                lista.add(PrinterService.TXT_ALIGN_LT);
                lista.add(("===============================================\r\n").getBytes());

                PrinterService service = new PrinterService();
                service.printBytes(lista);

            } catch (IOException ex) {
                Logger.getLogger(PrinterFacade.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PrintException ex) {
                Logger.getLogger(PrinterFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
