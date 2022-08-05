package com.butter.bean;

import com.google.gson.JsonObject;
import com.core.database.DAOException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ASUS-PC
 */
public class RegistroEquipo extends javax.swing.JFrame {

    private CredencialBean credencial;


    /**
     * Creates new form Principal
     *
     * @param credencial
     */
    public RegistroEquipo(CredencialBean credencial) {
        initComponents();
        this.credencial = credencial;
        jEquipo.setText(credencial.getSerial());
        jAlmacenamiento.setText(credencial.getAlmacenamiento());
        jMac.setText(credencial.getMac());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jEquipo = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jAlmacenamiento = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jMac = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(null);

        jLabel2.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jLabel2.setText("Serial Equipo ");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(20, 10, 250, 30);

        jEquipo.setFont(new java.awt.Font("Roboto", 0, 36)); // NOI18N
        jEquipo.setText("000000000000");
        getContentPane().add(jEquipo);
        jEquipo.setBounds(20, 40, 860, 40);

        jLabel3.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jLabel3.setText("Serial Almacenamiento");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(20, 80, 250, 30);

        jAlmacenamiento.setFont(new java.awt.Font("Roboto", 0, 36)); // NOI18N
        jAlmacenamiento.setText("0000000000000000000");
        getContentPane().add(jAlmacenamiento);
        jAlmacenamiento.setBounds(20, 110, 860, 40);

        jLabel4.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jLabel4.setText("Mac Principal");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(20, 150, 250, 30);

        jMac.setFont(new java.awt.Font("Roboto", 0, 36)); // NOI18N
        jMac.setText("000000000000");
        getContentPane().add(jMac);
        jMac.setBounds(20, 180, 860, 40);

        jButton1.setText("VALIDAR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(20, 250, 190, 60);

        jLabel1.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        getContentPane().add(jLabel1);
        jLabel1.setBounds(20, 320, 900, 40);

        jTextField1.setEditable(false);
        jTextField1.setFont(new java.awt.Font("Roboto", 0, 24)); // NOI18N
        getContentPane().add(jTextField1);
        jTextField1.setBounds(20, 380, 670, 50);

        jButton2.setText("COPY");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(700, 380, 78, 50);

        jLabel5.setText("Version 1.0.24");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(20, 570, 690, 16);

        setSize(new java.awt.Dimension(1024, 600));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        validar(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        copy();
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jAlmacenamiento;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jEquipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jMac;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    public void validar(boolean inBackground) {

        SimpleDateFormat sdf = new SimpleDateFormat(Butter.FORMAT_TIME_AM);
        try {
            EquipoDao dao = new EquipoDao();
            ClientWS cliente = new ClientWS();

            if (!credencial.isRegistroPrevio()) {

                JsonObject result = cliente.execute(
                        Butter.ENABLE_HTTPS,
                        "REGISTRO DE EQUIPO",
                        Butter.SECURE_END_POINT_EQUIPO,
                        Butter.POST,
                        credencial.toJson(), 
                        Butter.DISABLE_DEBUG
                        );
                CredencialBean credencialResponse = Main.GSON.fromJson(result, CredencialBean.class);

                credencial.setToken(credencialResponse.getToken());
                credencial.setPassword(credencialResponse.getPassword());
                credencial.setReferencia(credencialResponse.get_Id());
                credencial.setAutorizado(credencialResponse.isAutorizado());
                dao.create(credencial);
                if (credencialResponse.isAutorizado()) {

                    JsonObject json = new JsonObject();
                    json.addProperty("empresas_id", credencialResponse.getEmpresas_id());

                    String url = Butter.SECURE_END_POINT_EMPRESA;
                    JsonObject jempresa = cliente.execute(
                            Butter.ENABLE_HTTPS,
                            "BUSCANDO LOS DATOS DE LA EMPRESA",
                            credencial,
                            url,
                            Butter.POST,
                            json, Butter.ENABLE_DEBUG);
                    crearEmpresa(jempresa);
                    
                    credencial.setEmpresas_id(credencialResponse.getEmpresas_id());
                    dao.update(credencial, 1);
                    credencial.setEquipos_id(credencialResponse.getEquipos_id());
                    dao.update(credencial, 1);
                }

                credencial.setRegistroPrevio(true);

                if (credencial.isAutorizado()) {
                    jLabel1.setText("EQUIPO AUTORIZADO");
                    JOptionPane.showMessageDialog(this, "EL EQUIPO SE ENCUENTRA AUTORIZADO!");
                    dispose();
                } else {
                    jLabel1.setText("EQUIPO REGISTRADO.... ESPERANDO AUTORIZACIÓN (" + sdf.format(new Date()) + ")");
                    jTextField1.setText(credencialResponse.getReferencia());
                }

            } else {

                JsonObject result = cliente.execute(
                        Butter.ENABLE_HTTPS,
                        "CONSULTA DE ESTADO",
                        credencial,
                        Butter.SECURE_END_POINT_EQUIPO + "/" + credencial.getReferencia(),
                        Butter.GET,
                        new JsonObject(), 
                        Butter.DISABLE_DEBUG
                        );
                CredencialBean credencialResponse = Main.GSON.fromJson(result, CredencialBean.class);

                if (credencialResponse != null && credencialResponse.getToken() != null) {
                    credencial.setToken(credencialResponse.getToken());
                    credencial.setPassword(credencialResponse.getPassword());
                    credencial.setAutorizado(credencialResponse.isAutorizado());

                    if (credencial.getEmpresas_id() == null || credencial.getEmpresas_id() == 0) {
                        //TODO: Agregar los datos de la empresa;

                        JsonObject json = new JsonObject();
                        json.addProperty("empresas_id", credencialResponse.getEmpresas_id());

                        String url = Butter.SECURE_END_POINT_EMPRESA;
                        JsonObject jempresa = cliente.execute(
                                Butter.ENABLE_HTTPS,
                                "BUSCANDO LOS DATOS DE LA EMPRESA",
                                credencial,
                                url,
                                Butter.POST,
                                json, 
                                Butter.DISABLE_DEBUG);

                        long id = 0;
                        if (credencial.getEquipos_id() != null) {
                            id = credencial.getEquipos_id();
                        }
                        credencial.setEmpresas_id(credencialResponse.getEmpresas_id());
                        credencial.setEquipos_id(credencialResponse.getEquipos_id());

                        crearEmpresa(jempresa);
                        dao.update(credencial, id);

                    } else {
                        dao.update(credencial, 0);
                    }

                }
                credencial.setRegistroPrevio(true);

                if (credencial.isAutorizado()) {
                    if (!inBackground) {
                        jLabel1.setText("EQUIPO AUTORIZADO");
                        JOptionPane.showMessageDialog(this, "EL EQUIPO SE ENCUENTRA AUTORIZADO!");
                        dispose();
                    }
                } else {
                    jLabel1.setText("EQUIPO REGISTRADO.... ESPERANDO AUTORIZACIÓN (" + sdf.format(new Date()) + ")");
                }

            }

        } catch (WSException ex) {
            Logger.getLogger(RegistroEquipo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DAOException ex) {
            Logger.getLogger(RegistroEquipo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crearEmpresa(JsonObject jobject) throws DAOException {

        JsonObject jemp = jobject.get("data").getAsJsonArray().get(0).getAsJsonObject();

        EmpresaBean empresa = new EmpresaBean();
        empresa.setId(jemp.get("id").getAsLong());
        empresa.setRazonSocial(jemp.get("razon_social").getAsString());
        empresa.setNit(jemp.get("nit").getAsString());
        empresa.setLocalizacion(jemp.get("localizacion").getAsString());

        empresa.setEmpresasId(jemp.get("empresas_id").getAsLong());
        empresa.setCiudadId(jemp.get("c_id").getAsLong());
        empresa.setCiudadDescripcion(jemp.get("c_descripcion").getAsString());
        empresa.setCiudadZonaHoraria(jemp.get("zona_horaria").getAsString());
        empresa.setCiudadIndicador(jemp.get("indicadores").getAsInt());

        empresa.setProvinciaId(jemp.get("pr_id").getAsLong());
        empresa.setProvinciaDescripcion(jemp.get("pr_descripcion").getAsString());

        empresa.setPaisId(jemp.get("pa_id").getAsLong());
        empresa.setPaisDescripcion(jemp.get("pa_descripcion").getAsString());
        empresa.setPaisMoneda(jemp.get("moneda").getAsString());
        empresa.setPaisIndicador(jemp.get("indicador").getAsInt());
        empresa.setPaisNomenclatura(jemp.get("nomenclatura").getAsString());

        empresa.setUrlFotos(jemp.get("url_foto").getAsString());

        EquipoDao dao = new EquipoDao();
        dao.createEmpresas(empresa);

    }

    private void copy() {

        StringSelection stringSelection = new StringSelection(jTextField1.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

    }
}
