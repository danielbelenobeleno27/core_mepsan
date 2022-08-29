/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.print.services;

import com.core.app.NeoService;
import com.core.database.DAOException;
import com.core.database.impl.SurtidorDao;
import com.escpos.EscPos;
import com.escpos.EscPosConst;
import com.escpos.image.BitonalThreshold;
import com.escpos.image.CoffeeImageImpl;
import com.escpos.image.EscPosImage;
import com.escpos.image.RasterBitImageWrapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

/**
 *
 * @author ASUS-PC
 */
public class PrinterService {

    public int TIEMPO_ESPERA_IMPRESORA = 30000;
    public static final byte[] JUEGO_CARACTERERS_LATINO = {0x1B, 0x74, 0x02}; // Partial cut paper

    public final byte[] CTL_LF = {0x0a}; // Print and line feed
    public static final byte[] CAN_HT = {0x1b, 0x44, 0x00}; // Cancel  Horizontal Tab
    public static final byte[] TAB_H = {0x09}; // Horizontal Tab
    public static final byte[] LINE_SPACE_24 = {0x1b, 0x33, 24}; // Set the line spacing at 24
    public static final byte[] LINE_SPACE_30 = {0x1b, 0x33, 30}; // Set the line spacing at 30
    //Image
    public static final byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33};
    // Printer hardware
    public static final byte[] HW_INIT = {0x1b, 0x40}; // Clear data in buffer and reset modes
    // Cash Drawer
    public static final byte[] CD_KICK_2 = {0x1b, 0x70, 0x00}; // Sends a pulse to pin 2 []
    public static final byte[] CD_KICK_5 = {0x1b, 0x70, 0x01}; // Sends a pulse to pin 5 []
    // Paper
    public static final byte[] PAPER_FULL_CUT = {0x1d, 0x56, 0x00}; // Full cut paper
    public static final byte[] PAPER_PART_CUT = {0x1d, 0x56, 0x01}; // Partial cut paper
    // Text format
    public static final byte[] TXT_NORMAL = {0x1b, 0x21, 0x00}; // Normal text
    public static final byte[] TXT_2HEIGHT = {0x1b, 0x21, 0x10}; // Double height text
    public static final byte[] TXT_2WIDTH = {0x1b, 0x21, 0x20}; // Double width text
    public static final byte[] TXT_4SQUARE = {0x1b, 0x21, 0x30}; // Quad area text
    public static final byte[] TXT_UNDERL_OFF = {0x1b, 0x2d, 0x00}; // Underline font OFF
    public static final byte[] TXT_UNDERL_ON = {0x1b, 0x2d, 0x01}; // Underline font 1-dot ON
    public static final byte[] TXT_UNDERL2_ON = {0x1b, 0x2d, 0x02}; // Underline font 2-dot ON
    public static final byte[] TXT_BOLD_OFF = {0x1b, 0x45, 0x00}; // Bold font OFF
    public static final byte[] TXT_BOLD_ON = {0x1b, 0x45, 0x01}; // Bold font ON
    public static final byte[] TXT_FONT_A = {0x1b, 0x4d, 0x00}; // Font type A
    public static final byte[] TXT_FONT_B = {0x1b, 0x4d, 0x01}; // Font type B
    public static final byte[] TXT_FONT_C = {0x1b, 0x4d, 0x02}; // Font type C
    public static final byte[] TXT_FONT_D = {0x1b, 0x4d, 0x03}; // Font type D
    public static final byte[] TXT_FONT_E = {0x1b, 0x4d, 0x04}; // Font type E
    public static final byte[] TXT_ALIGN_LT = {0x1b, 0x61, 0x00}; // Left justification
    public static final byte[] TXT_ALIGN_CT = {0x1b, 0x61, 0x01}; // Centering
    public static final byte[] TXT_ALIGN_RT = {0x1b, 0x61, 0x02}; // Right justification
    public static final byte[] LEFT_MARGIN = {0x1b, 0x6c, 0x08}; // Left Margin
    
    private final SurtidorDao sdao = new SurtidorDao();

    protected void printText(String test) throws UnknownHostException, IOException, PrintException {
        NeoService.IP_IMPRESORA = sdao.getParametroString("impresora");
        NeoService.setLog("[INFO (main)] IP_IMPRESORA -> " + NeoService.IP_IMPRESORA);
        NeoService.PORT_IMPRESORA = sdao.getParametroInt("impresora_puerto");
        NeoService.setLog("[INFO (main)] PORT_IMPRESORA -> " + NeoService.PORT_IMPRESORA);
        try {

            if (NeoService.IP_IMPRESORA.toLowerCase().contains("usb:")) {
                byte[] INIT = new byte[]{0x1B, 0x40};
                byte[] CUT = new byte[]{0x1B, 0x69};
                byte[] PRSTAND = new byte[]{0x0C};
                byte[] CANCEL = new byte[]{0x18};
                String impresora = NeoService.IP_IMPRESORA.split(":")[1];
                NeoService.setLog("IMPRIMIENDO EN " + impresora);
                PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                int selectedService = 0;
                int index = 0;
                for (PrintService service : services) {
                    if (service.getName().toUpperCase().contains(impresora.toUpperCase())) {
                        selectedService = index;
                        break;
                    }
                    index++;
                }
                PrintService printService = services[selectedService];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(baos);
                try {
                    out.write(INIT);
                    out.write(test.getBytes("Cp850"));
                    out.write("\r\n\r\n\r\n\r\n\r\n\r\n\r\n".getBytes());
                    out.write(CUT);
                    out.write(PRSTAND);
                    out.write(CANCEL);
                } catch (IOException ex) {
                    Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
                }
                byte[] bytes = baos.toByteArray();

                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
                DocPrintJob docPrintJob = printService.createPrintJob();
                Doc doc = new SimpleDoc(bytes, flavor, null);
                try {
                    docPrintJob.print(doc, null);
                } catch (PrintException e) {
                    e.printStackTrace();
                }

            } else {
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(NeoService.IP_IMPRESORA, NeoService.PORT_IMPRESORA), 10000);
                DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());

                try {

                    SurtidorDao dao = new SurtidorDao();
                    EscPos escpos = new EscPos(osw);
                    RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
                    imageWrapper.setJustification(EscPosConst.Justification.Center);
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dao.getParametros("logo"))));
                    EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(img), new BitonalThreshold());
                    escpos.write(imageWrapper, escposImage);

                } catch (Exception e) {
                    NeoService.setLog(e.getMessage());
                } catch (DAOException ex) {
                    Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
                }
                osw.write(PrinterService.JUEGO_CARACTERERS_LATINO);
                osw.write(test.getBytes("Cp850"));
                printSpace(osw);
                byte[] cutP = new byte[]{0x1b, 'i', 1};
                osw.write(cutP);
                osw.flush();
                osw.close();
                NeoService.setLog(test);
            }

        } catch (Exception e) {
            NeoService.setLog("Error al conectar a la impresora " + NeoService.IP_IMPRESORA + ":" + NeoService.PORT_IMPRESORA);
        }
    }

    protected void printBytes(ArrayList<byte[]> array) throws UnknownHostException, IOException, PrintException {
        NeoService.IP_IMPRESORA = sdao.getParametroString("impresora");
        NeoService.setLog("[INFO (main)] IP_IMPRESORA -> " + NeoService.IP_IMPRESORA);
        NeoService.PORT_IMPRESORA = sdao.getParametroInt("impresora_puerto");
        NeoService.setLog("[INFO (main)] PORT_IMPRESORA -> " + NeoService.PORT_IMPRESORA);

        if (NeoService.IP_IMPRESORA.toLowerCase().contains("usb:")) {

            byte[] INIT = new byte[]{0x1B, 0x40};
            byte[] CUT = new byte[]{0x1B, 0x69};
            byte[] PRSTAND = new byte[]{0x0C};
            byte[] CANCEL = new byte[]{0x18};
            String impresora = NeoService.IP_IMPRESORA.split(":")[1];
            NeoService.setLog("IMPRIMIENDO EN " + impresora);
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            int selectedService = 0;
            int index = 0;
            for (PrintService service : services) {
                if (service.getName().toUpperCase().contains(impresora.toUpperCase())) {
                    selectedService = index;
                    break;
                }
                index++;
            }
            PrintService printService = services[selectedService];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            try {

                SurtidorDao dao = new SurtidorDao();
                EscPos escpos = new EscPos(baos);
                RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
                imageWrapper.setJustification(EscPosConst.Justification.Center);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dao.getParametros("logo"))));
                EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(img), new BitonalThreshold());
                escpos.write(imageWrapper, escposImage);

            } catch (Exception e) {
                NeoService.setLog(e.getMessage());
            } catch (DAOException ex) {
                Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                out.write(INIT);
                out.write(PrinterService.JUEGO_CARACTERERS_LATINO);
                for (byte[] bs : array) {
                    out.write(bs);
                }
                out.write("\r\n\r\n\r\n\r\n\r\n\r\n\r\n".getBytes());
                out.write(CUT);
                out.write(PRSTAND);
                out.write(CANCEL);
            } catch (IOException ex) {
                Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
            }
            byte[] bytes = baos.toByteArray();

            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            DocPrintJob docPrintJob = printService.createPrintJob();
            Doc doc = new SimpleDoc(bytes, flavor, null);
            try {
                docPrintJob.print(doc, null);
            } catch (PrintException e) {
                e.printStackTrace();
            }

        } else {

            try {
                NeoService.setLog("______________________________________________");
                NeoService.setLog("");
                NeoService.setLog("");
                NeoService.setLog("");
                for (byte[] bs : array) {
                    String sp = new String(bs);
                    NeoService.setLog(sp);
                }

                NeoService.setLog("");
                NeoService.setLog("");
                NeoService.setLog("");
                NeoService.setLog("______________________________________________");

                byte[] cutP = new byte[]{0x1b, 'i', 1};

                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(NeoService.IP_IMPRESORA, NeoService.PORT_IMPRESORA), TIEMPO_ESPERA_IMPRESORA);
                DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());

                try {

                    SurtidorDao dao = new SurtidorDao();
                    EscPos escpos = new EscPos(osw);
                    RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
                    imageWrapper.setJustification(EscPosConst.Justification.Center);
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dao.getParametros("logo"))));
                    EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(img), new BitonalThreshold());
                    escpos.write(imageWrapper, escposImage);

                } catch (Exception e) {
                    NeoService.setLog(e.getMessage());
                } catch (DAOException ex) {
                    Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
                }

                for (byte[] bs : array) {
                    osw.write(bs);
                }
                osw.write(PrinterService.JUEGO_CARACTERERS_LATINO);
                printSpace(osw);

                osw.write(cutP);
                osw.flush();
                osw.close();

            } catch (Exception e) {
                NeoService.setLog("Error al conectar a la impresora " + NeoService.IP_IMPRESORA + ":" + NeoService.PORT_IMPRESORA);

            }
        }
    }

    protected void printSpace(DataOutputStream osw) {
        for (int i = 0; i < 8; i++) {
            try {
                osw.write("\r\n".getBytes());
            } catch (IOException ex) {
                Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void printTextConLogo(String test) throws UnknownHostException, IOException, PrintException {
        NeoService.IP_IMPRESORA = sdao.getParametroString("impresora");
        NeoService.setLog("[INFO (main)] IP_IMPRESORA -> " + NeoService.IP_IMPRESORA);
        NeoService.PORT_IMPRESORA = sdao.getParametroInt("impresora_puerto");
        NeoService.setLog("[INFO (main)] PORT_IMPRESORA -> " + NeoService.PORT_IMPRESORA);

        try {
            SurtidorDao dao = new SurtidorDao();

            try {
                NeoService.IP_IMPRESORA = dao.getParametros("impresora");
            } catch (DAOException ex) {
                Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
            }

            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(NeoService.IP_IMPRESORA, NeoService.PORT_IMPRESORA), TIEMPO_ESPERA_IMPRESORA);
            DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());

            try {

                EscPos escpos = new EscPos(osw);
                RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
                imageWrapper.setJustification(EscPosConst.Justification.Center);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dao.getParametros("logo"))));
                EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(img), new BitonalThreshold());
                escpos.write(imageWrapper, escposImage);

            } catch (Exception e) {
                NeoService.setLog(e.getMessage());
            } catch (DAOException ex) {
                Logger.getLogger(PrinterService.class.getName()).log(Level.SEVERE, null, ex);
            }
            osw.write(PrinterService.JUEGO_CARACTERERS_LATINO);
            osw.write(test.getBytes("Cp850"));
            printSpace(osw);
            byte[] cutP = new byte[]{0x1b, 'i', 1};
            osw.write(cutP);
            osw.flush();
            osw.close();
            NeoService.setLog(test);
        } catch (Exception e) {
            NeoService.setLog("Error al conectar a la impresora " + NeoService.IP_IMPRESORA + ":" + NeoService.PORT_IMPRESORA);
        }
    }
}
