/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.database.impl;

import com.core.app.NeoService;
import com.neo.app.bean.Persona;
import com.neo.app.bean.Surtidor;
import com.core.database.DAOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author ASUS-PC
 */
public class PersonaDao {

    public Persona getPersonaByIdentificacion(String identificacion, String pin) throws DAOException {
        Persona p = null;
        try {
            String sql = "SELECT id, nombre FROM personas WHERE identificacion=? and clave=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, identificacion);
            ps.setString(2, pin);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                p = new Persona();
                p.setId(re.getLong("id"));
                p.setIdentificacion(identificacion);
                p.setNombre(re.getString("nombre"));
            }
        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }
        return p;
    }

    public Persona getPersonaByIdentificacion(String identificacion) throws DAOException {
        Persona p = null;
        try {
            String sql = "SELECT id, nombre FROM personas WHERE identificacion=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, identificacion);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                p = new Persona();
                p.setId(re.getLong("id"));
                p.setIdentificacion(identificacion);
                p.setNombre(re.getString("nombre"));
            }
        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }
        return p;
    }

    public boolean tieneVentaEnCurso(Persona persona) throws DAOException {
        boolean respuesta = false;
        try {

            long cantidad = -1;
            String sql = "SELECT 1, cantidad FROM ventas_curso WHERE operario_id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, persona.getId());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                respuesta = true;
                cantidad = re.getLong("cantidad");
            }

            if (cantidad == 0) {
                sql = "DELETE FROM ventas_curso WHERE operario_id=? ";
                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, persona.getId());
                ps.executeUpdate();

                respuesta = false;

                sql = "SELECT 1, cantidad FROM ventas_curso WHERE operario_id=?";
                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, persona.getId());
                re = ps.executeQuery();
                while (re.next()) {
                    respuesta = true;
                }

            }

        } catch (SQLException s) {
            throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
        }
        return respuesta;
    }

    public Surtidor getSurtidorByJornada(Persona persona) throws DAOException {
        Surtidor surtidor = null;
        try {
            String sql = "select s.id uniqueid, s.surtidor from \n"
                    + "jornadas j\n"
                    + "inner join surtidores s on s.id=j.surtidores_id\n"
                    + "where personas_id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, persona.getId());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                surtidor = new Surtidor();
                surtidor.setUniqueId(re.getLong("uniqueid"));
                surtidor.setId(re.getInt("surtidor"));
            }
        } catch (SQLException s) {
            throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
        }
        return surtidor;
    }

}
