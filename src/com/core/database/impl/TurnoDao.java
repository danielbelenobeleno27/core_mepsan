/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.database.impl;

import com.core.app.NeoService;
import com.neo.app.bean.Cara;
import com.core.turnos.bean.TurnosResponse;
import com.neo.app.bean.Manguera;
import com.neo.app.bean.Persona;
import com.neo.app.bean.Surtidor;
import com.core.database.DAOException;
import com.core.database.Postgrest;
import com.core.turnos.bean.SurtidorInventario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS-PC
 */
public class TurnoDao {

    public TurnosResponse iniciarJornada(Persona p, Surtidor surtidor) throws DAOException {
        TurnosResponse respuesta = new TurnosResponse();
        try {
            NeoService.setLog("Conectando desde " + this.getClass().getName());
            //Si la empresa no exige surtidor
            if (p != null) {
                long jornadaId = 0;
                long personaLogueada = 0;
                NeoService.obtenerConexion().setAutoCommit(false);
                Date fechaInicio = null;

                String sql = "select id, personas_id, fecha_inicio from jornadas where surtidores_id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, surtidor.getUniqueId());
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    jornadaId = re.getLong(1);
                    personaLogueada = re.getLong(2);
                    fechaInicio = re.getTimestamp("fecha_inicio");
                }

                if (jornadaId == 0) {

                    sql = "INSERT INTO jornadas(\n"
                            + "            id, personas_id, fecha_inicio, fecha_fin, sincronizado, surtidores_id)\n"
                            + "    VALUES (nextval('jornadas_id'), ?, now(), null, 0, ?) RETURNING currval('jornadas_id'), now()";
                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, p.getId());
                    ps.setLong(2, surtidor.getUniqueId());
                    re = ps.executeQuery();
                    while (re.next()) {
                        jornadaId = re.getLong(1);
                        fechaInicio = re.getTimestamp(2);
                    }

                    if (surtidor.getCaras().isEmpty()) {
                        for (Map.Entry<Integer, Manguera> entry : surtidor.getMangueras().entrySet()) {
                            Integer key = entry.getKey();
                            Manguera manguera = entry.getValue();

                            sql = "INSERT INTO jornadas_inventarios(\n"
                                    + "            id, jornadas_id, surtidores_detalles_id, acumulado_venta_inicial, \n"
                                    + "            acumulado_cantidad_inicial, acumulado_venta_final, acumulado_cantidad_final)\n"
                                    + "    VALUES (NEXTVAL('jornadas_inventarios_ID'), ?, ?, ?, \n"
                                    + "            ?, ?, ?);";
                            ps = NeoService.obtenerConexion().prepareStatement(sql);
                            ps.setLong(1, jornadaId);
                            ps.setLong(2, manguera.getConfiguracionId());
                            ps.setLong(3, manguera.getRegistroSurtidorVentas());
                            ps.setLong(4, manguera.getRegistroSurtidorVolumen());
                            ps.setLong(5, manguera.getRegistroSurtidorVentas());
                            ps.setLong(6, manguera.getRegistroSurtidorVolumen());
                            ps.executeUpdate();

                        }
                    } else {
                        for (Map.Entry<Integer, Cara> entry : surtidor.getCaras().entrySet()) {
                            Integer key = entry.getKey();
                            Cara cara = entry.getValue();
                            for (Map.Entry<Integer, Manguera> kmanguera : cara.getMangueras().entrySet()) {
                                Integer key1 = kmanguera.getKey();
                                Manguera manguera = kmanguera.getValue();

                                sql = "INSERT INTO jornadas_inventarios(\n"
                                        + "            id, jornadas_id, surtidores_detalles_id, acumulado_venta_inicial, \n"
                                        + "            acumulado_cantidad_inicial, acumulado_venta_final, acumulado_cantidad_final)\n"
                                        + "    VALUES (NEXTVAL('jornadas_inventarios_ID'), ?, ?, ?, \n"
                                        + "            ?, ?, ?);";
                                ps = NeoService.obtenerConexion().prepareStatement(sql);
                                ps.setLong(1, jornadaId);
                                ps.setLong(2, manguera.getConfiguracionId());
                                ps.setLong(3, manguera.getRegistroSurtidorVentas());
                                ps.setLong(4, manguera.getRegistroSurtidorVolumen());
                                ps.setLong(5, manguera.getRegistroSurtidorVentas());
                                ps.setLong(6, manguera.getRegistroSurtidorVolumen());
                                ps.executeUpdate();
                            }
                        }
                    }
                    NeoService.obtenerConexion().commit();

                    respuesta.setSuccess(true);
                    respuesta.setFechaInicio(fechaInicio);
                    respuesta.setMensaje("Bienvenido " + p.getNombre().toUpperCase() + "!");
                } else {
                    if (personaLogueada == p.getId()) {
                        respuesta.setSuccess(false);
                        respuesta.setMensaje(p.getNombre().toUpperCase() + "\r\nYa tiene un inicio de jornada");
                        respuesta.setFechaInicio(fechaInicio);
                    } else {
                        respuesta.setSuccess(false);
                        respuesta.setMensaje(p.getNombre().toUpperCase() + "! Ya existe una jornada iniciada en este surtidor");
                    }
                }

            } else {
                respuesta.setSuccess(false);
                respuesta.setMensaje("Usuario no existe");
            }

        } catch (SQLException s) {
            try {
                NeoService.obtenerConexion().rollback();
            } catch (SQLException e) {
                NeoService.setLog(e.getMessage());
            }
            switch (s.getSQLState()) {
                case "1062":
                    throw new DAOException("Ya se esta usando ese codigo");
                case "23505":
                    respuesta.setSuccess(true);
                    respuesta.setMensaje("Ya existe un inicio de jornada");
                    break;
                default:
                    throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }

        return respuesta;
    }

    public TurnosResponse finalizarJornada(Persona p, Surtidor surtidor) throws DAOException {
        TurnosResponse respuesta = new TurnosResponse();
        try {
            NeoService.setLog("Conectando desde " + this.getClass().getName());
            //Si la empresa no exige surtidor
            if (p != null) {
                boolean session = false;
                long jornadaId = 0;
                //se optiene el id de la jornada
                String sql = "SELECT s.id, s.surtidor, j.fecha_inicio, j.id j_id FROM jornadas j\n"
                        + "INNER JOIN surtidores s on s.id=j.surtidores_id\n"
                        + "WHERE personas_id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, p.getId());
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    session = true;
                    respuesta.setSurtidor(surtidor);
                    respuesta.setFecha(re.getTimestamp("fecha_inicio"));
                    jornadaId = re.getLong("j_id");
                }
                if (session) {
                    NeoService.obtenerConexion().setAutoCommit(false);

                    sql = "UPDATE jornadas "
                            + " SET fecha_fin=NOW(), sincronizado=0 \n"
                            + " WHERE personas_id=?";
                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, p.getId());
                    ps.executeUpdate();

                    sql = "INSERT INTO jornadas_hist "
                            + " SELECT NEXTVAL('jornadas_hist_id'), J.* FROM jornadas J WHERE personas_id=?";
                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, p.getId());
                    ps.executeUpdate();

                    sql = "select id, acumulado_venta, acumulado_cantidad from surtidores_detalles";
                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    re = ps.executeQuery();
                    while (re.next()) {

                        String sql2 = "update jornadas_inventarios set acumulado_venta_final=?, acumulado_cantidad_final=?\n"
                                + "where jornadas_id=? and surtidores_detalles_id=? ";
                        PreparedStatement ps2 = NeoService.obtenerConexion().prepareStatement(sql2);
                        ps2.setLong(1, re.getLong("acumulado_venta"));
                        ps2.setLong(2, re.getLong("acumulado_cantidad"));
                        ps2.setLong(3, jornadaId);
                        ps2.setLong(4, re.getLong("id"));
                        NeoService.setLog("..................");
                        NeoService.setLog(ps2.toString());
                        NeoService.setLog("..................");
                        ps2.executeUpdate();

                    }

                    sql = "DELETE FROM jornadas WHERE personas_id=?";
                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, p.getId());
                    ps.executeUpdate();

                    NeoService.setLog("jornada = " + jornadaId);

                    NeoService.obtenerConexion().commit();

                    respuesta.setSuccess(true);
                    respuesta.setMensaje(p.getNombre().toUpperCase() + ", \r\nHemos finalizado la jornada!");
                } else {
                    respuesta.setSuccess(false);
                    respuesta.setMensaje(p.getNombre().toUpperCase() + ", \r\nNo tienes una jornada iniciada!");
                }
            } else {
                respuesta.setSuccess(false);
                respuesta.setMensaje("Usuario no existe");
            }

        } catch (SQLException s) {
            try {
                NeoService.obtenerConexion().rollback();
            } catch (SQLException ex) {
                NeoService.setLog(ex.getMessage());
            }
            Logger.getLogger(TurnoDao.class.getName()).log(Level.SEVERE, null, s);
            switch (s.getSQLState()) {
                case "1062":
                    throw new DAOException("Ya se esta usando ese codigo");
                case "23505":
                    respuesta.setSuccess(true);
                    respuesta.setMensaje("Ya existe un inicio de jornada");
                    break;
                default:
                    throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        } 
        return respuesta;
    }

    public TurnosResponse consultaJornada(Persona p) throws DAOException {
        TurnosResponse respuesta = new TurnosResponse();
        try {
                NeoService.setLog("Conectando desde " + this.getClass().getName());
            //Si la empresa no exige surtidor
            if (p != null) {
                boolean session = false;
                String sql = "SELECT  s.surtidor, s.factor_inventario, s.factor_volumen_parcial, s.factor_importe_parcial, j.id, j.fecha_inicio,\n"
                        + "SUM(vd.total) acumulado_ventas,  SUM(vd.cantidad_precisa) acumulado_volumen, COUNT(vd.cantidad) cantidad\n"
                        + "FROM jornadas j \n"
                        + "INNER JOIN surtidores s on s.id=j.surtidores_id\n"
                        + "LEFT JOIN ventas v on v.jornada_id=j.id\n"
                        + "LEFT JOIN ventas_detalles vd on v.id=vd.ventas_id\n"
                        + "WHERE j.personas_id=?\n"
                        + "GROUP BY 1,2,3,4,5\n";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, p.getId());
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    session = true;
                    respuesta.setId(re.getLong("id"));
                    respuesta.setAcumuadoVentas(re.getFloat("acumulado_ventas"));
                    respuesta.setAcumuadoVolumen(re.getFloat("acumulado_volumen"));
                    respuesta.setFechaInicio(re.getTimestamp("fecha_inicio"));
                    respuesta.setCantidad(re.getInt("cantidad"));
                    respuesta.setFactorInventario(re.getInt("factor_inventario"));
                    respuesta.setFactorPrecio(re.getInt("factor_importe_parcial"));
                    respuesta.setFactorPrecio(re.getInt("factor_importe_parcial"));
                    respuesta.setFactorVolumen(re.getInt("factor_volumen_parcial"));
                }
                if (session) {
                    respuesta.setSuccess(true);
                    respuesta.setMensaje(p.getNombre().toUpperCase() + ", \r\nHemos finalizado la jornada!");
                } else {
                    respuesta.setSuccess(false);
                    respuesta.setMensaje(p.getNombre().toUpperCase() + ", \r\nNo tienes una jornada iniciada!");
                }
            } else {
                respuesta.setSuccess(false);
                respuesta.setMensaje("Usuario no existe");
            }

        } catch (SQLException s) {
            try {
                if (!NeoService.obtenerConexion().getAutoCommit()) {
                    NeoService.obtenerConexion().rollback();
                }
                switch (s.getSQLState()) {
                    case "1062":
                        throw new DAOException("Ya se esta usando ese codigo");
                    case "23505":
                        respuesta.setSuccess(true);
                        respuesta.setMensaje("Ya existe un inicio de jornada");
                        break;
                    default:
                        throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
                }
            } catch (SQLException ex) {
                Logger.getLogger(TurnoDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        NeoService.setLog(respuesta.getMensaje());
        return respuesta;
    }

    public TurnosResponse consultaAcumulado(long jornada, Surtidor surtidor) throws DAOException {
        TurnosResponse respuesta = new TurnosResponse();
        try {
                NeoService.setLog("Conectando desde " + this.getClass().getName());
            //Si la empresa no exige surtidor
            if (jornada != 0) {

                String sql = "SELECT \n"
                        + "SD.ID, SURTIDOR, CARA, MANGUERA, P.DESCRIPCION, P.PRECIO, \n"
                        + "ACUMULADO_VENTA_INICIAL, ACUMULADO_VENTA_FINAL, \n"
                        + "ACUMULADO_CANTIDAD_INICIAL, ACUMULADO_CANTIDAD_FINAL \n"
                        + "FROM JORNADAS_INVENTARIOS JI \n"
                        + "INNER JOIN SURTIDORES_DETALLES SD ON SD.ID=JI.SURTIDORES_DETALLES_ID \n"
                        + "INNER JOIN PRODUCTOS P ON P.ID=SD.PRODUCTOS_ID \n"
                        + "WHERE \n"
                        + "JORNADAS_ID=? ORDER BY 4\n";

                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, jornada);
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    respuesta.setSuccess(true);
                    SurtidorInventario surt = new SurtidorInventario();
                    if (respuesta.getInventario() == null) {
                        respuesta.setInventario(new ArrayList<SurtidorInventario>());
                    }
                    surt.setSurtidor(re.getInt("SURTIDOR"));
                    surt.setCara(re.getInt("CARA"));
                    surt.setManguera(re.getInt("MANGUERA"));
                    surt.setAcumuadoVentasInicial(re.getLong("ACUMULADO_VENTA_INICIAL"));
                    surt.setAcumuadoVolumenInicial(re.getLong("ACUMULADO_CANTIDAD_INICIAL"));

                    surt.setProductoDescripcion(re.getString("DESCRIPCION"));
                    surt.setProductoPrecio(re.getLong("PRECIO"));

                    surt.setAcumuadoVentasFinal(re.getLong("ACUMULADO_VENTA_FINAL"));
                    surt.setAcumuadoVolumenFinal(re.getLong("ACUMULADO_CANTIDAD_FINAL"));

                    
                    respuesta.getInventario().add(surt);
                }

            } else {
                respuesta.setSuccess(false);
                respuesta.setMensaje("ID DE JORNADA NO IDENTIFICADO");
            }

        } catch (SQLException s) {
            respuesta.setSuccess(false);
            respuesta.setError(s.getMessage());
            respuesta.setMensaje("Ya existe un inicio de jornada");
        }
        NeoService.setLog(respuesta.getMensaje());
        return respuesta;
    }

}
