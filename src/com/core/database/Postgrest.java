package com.core.database;

import com.core.app.NeoService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Postgrest {

    public String CLASS;
    public String DRIVE;
    public String HOST;
    public String PORT;
    public String DATABASE;
    public String USUARIO;
    public String PASSWORD;

    private Connection conn;

    public Postgrest() {
        CLASS = "org.postgresql.Driver";
        DRIVE = "jdbc:postgresql";
        HOST = NeoService.DATABASE_LOCAL_HOST;
        PORT = NeoService.DATABASE_LOCAL_PORT;
        DATABASE = NeoService.DATABASE_LOCAL_NAME;
        USUARIO = NeoService.DATABASE_LOCAL_USER;
        PASSWORD = NeoService.DATABASE_LOCAL_PASSWORD;
    }

    private void conectar() {
        try {
            Class.forName(CLASS);
            conn = DriverManager.getConnection(DRIVE + "://" + HOST + ":" + PORT + "/" + DATABASE + "?ApplicationName=SERVER_CORE", USUARIO, PASSWORD);
            NeoService.setLog(NeoService.ANSI_YELLOW + "Creando nueva instancia de conexion" + NeoService.ANSI_RESET);
        } catch (ClassNotFoundException | SQLException e) {
            NeoService.setLog(e.getMessage());
            Logger.getLogger(Postgrest.class.getName()).log(Level.SEVERE, null, e);
        }
    }

//    public void desconectar() {
//        try {
//            if (conn != null) {
//                conn.close();
//            }
//        } catch (SQLException e) {
//            NeoService.setLog(e.getMessage());
//            Logger.getLogger(Postgrest.class.getName()).log(Level.SEVERE, null, e);
//        }
//    }

    public Connection getConn() {
        try {
            if (conn == null || conn.isClosed()) {
                conectar();
            }
        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
            Logger.getLogger(Postgrest.class.getName()).log(Level.SEVERE, null, e);
        }
        return conn;
    }
}
