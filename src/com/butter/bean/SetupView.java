/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

/**
 *
 * @author novus
 */
public class SetupView extends javax.swing.JPanel {

    /**
     * Creates new form DescargandoRecursos
     */
    SetupAsync sync;

    public SetupView() {
        initComponents();
        sync = new SetupAsync(this);
        sync.start();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag); //To change body of generated methods, choose Tools | Templates.

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jdatos = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jconsecutivos = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jcategorias = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        Jkardex = new javax.swing.JLabel();
        jpersonal = new javax.swing.JLabel();
        jproductos = new javax.swing.JLabel();
        jbodegas = new javax.swing.JLabel();
        jmedios = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(null);

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));
        jPanel1.setLayout(null);

        jTextArea1.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(10, 10, 520, 60);

        add(jPanel1);
        jPanel1.setBounds(30, 480, 540, 80);

        jdatos.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jdatos.setForeground(new java.awt.Color(255, 255, 255));
        jdatos.setText("DATOS BASICOS");
        add(jdatos);
        jdatos.setBounds(40, 110, 420, 40);

        jButton8.setText("Reintentar");
        jButton8.setEnabled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        add(jButton8);
        jButton8.setBounds(470, 110, 90, 40);

        jButton2.setText("Reintentar");
        jButton2.setEnabled(false);
        add(jButton2);
        jButton2.setBounds(470, 190, 90, 40);

        jButton3.setText("Reintentar");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        add(jButton3);
        jButton3.setBounds(470, 150, 90, 40);

        jButton4.setText("Reintentar");
        jButton4.setEnabled(false);
        add(jButton4);
        jButton4.setBounds(470, 310, 90, 40);

        jButton5.setText("Reintentar");
        jButton5.setEnabled(false);
        add(jButton5);
        jButton5.setBounds(470, 230, 90, 40);

        jButton6.setText("Reintentar");
        jButton6.setEnabled(false);
        add(jButton6);
        jButton6.setBounds(470, 350, 90, 40);

        jButton7.setText("Reintentar");
        jButton7.setEnabled(false);
        add(jButton7);
        jButton7.setBounds(470, 390, 90, 40);

        jconsecutivos.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jconsecutivos.setForeground(new java.awt.Color(255, 255, 255));
        jconsecutivos.setText("CONSECUTIVOS");
        add(jconsecutivos);
        jconsecutivos.setBounds(40, 270, 420, 40);

        jButton9.setText("Reintentar");
        jButton9.setEnabled(false);
        add(jButton9);
        jButton9.setBounds(470, 270, 90, 40);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/butter/view/resources/download.gif"))); // NOI18N
        add(jLabel4);
        jLabel4.setBounds(590, 120, 408, 408);

        jcategorias.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jcategorias.setForeground(new java.awt.Color(255, 255, 255));
        jcategorias.setText("CATEGORIAS");
        add(jcategorias);
        jcategorias.setBounds(40, 150, 420, 40);

        jButton1.setText("CERRAR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1);
        jButton1.setBounds(470, 20, 100, 50);

        Jkardex.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        Jkardex.setForeground(new java.awt.Color(255, 255, 255));
        Jkardex.setText("KARDEX");
        add(Jkardex);
        Jkardex.setBounds(40, 310, 420, 40);

        jpersonal.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jpersonal.setForeground(new java.awt.Color(255, 255, 255));
        jpersonal.setText("PERSONAL");
        add(jpersonal);
        jpersonal.setBounds(40, 350, 420, 40);

        jproductos.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jproductos.setForeground(new java.awt.Color(255, 255, 255));
        jproductos.setText("PRODUCTOS");
        add(jproductos);
        jproductos.setBounds(40, 190, 420, 40);

        jbodegas.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jbodegas.setForeground(new java.awt.Color(255, 255, 255));
        jbodegas.setText("BODEGA");
        add(jbodegas);
        jbodegas.setBounds(40, 230, 420, 40);

        jmedios.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jmedios.setForeground(new java.awt.Color(255, 255, 255));
        jmedios.setText("MEDIOS DE PAGO");
        add(jmedios);
        jmedios.setBounds(40, 390, 420, 40);

        jLabel3.setFont(new java.awt.Font("Juicebox", 1, 54)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("SINCRONIZANDO...");
        add(jLabel3);
        jLabel3.setBounds(30, 30, 520, 60);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/butter/view/resources/bg_back.png"))); // NOI18N
        add(jLabel2);
        jLabel2.setBounds(30, 90, 540, 380);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/butter/view/resources/bg_back.png"))); // NOI18N
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });
        add(jLabel5);
        jLabel5.setBounds(0, 0, 1024, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel Jkardex;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTextArea jTextArea1;
    public javax.swing.JLabel jbodegas;
    public javax.swing.JLabel jcategorias;
    public javax.swing.JLabel jconsecutivos;
    public javax.swing.JLabel jdatos;
    public javax.swing.JLabel jmedios;
    public javax.swing.JLabel jpersonal;
    public javax.swing.JLabel jproductos;
    // End of variables declaration//GEN-END:variables

}
