/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import com.core.app.NeoService;
import com.neo.app.bean.MediosPagosBean;
import com.google.gson.JsonObject;
import com.core.database.DAOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import org.postgresql.util.PSQLException;

public class SetupDao {

    public void createBloqueado(int index, ProductoBean p, CredencialBean cr) throws DAOException {

        if (index == 1) {
            try {
                String sql = "INSERT INTO public.grupos_tipos(\n"
                        + "            id, descripcion, estado, entidad)\n"
                        + "    VALUES (?, ?, ?, ?);";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                ps.setLong(1, 1);
                ps.setString(2, "PRODUCTOS");
                ps.setString(3, "A");
                ps.setInt(4, 1); //1 = PRODUCTOS
                ps.executeUpdate();

            } catch (SQLException e) {
                if (!e.getMessage().contains("grupos_tipos_pk")) {
                    NeoService.setLog(e.getMessage());
                }
            } catch (Exception e) {
                NeoService.setLog(e.getMessage());
            }

            try {
                //1;"NORMAL";"A"
                //2;"COMPUESTO";"A"
                //3;"PRODUCIDO";"A"
                //4;"NO INVENTARIO";"A"

                String sql = "INSERT INTO public.productos_tipos(\n"
                        + "            id, descripcion, estado)\n"
                        + "    VALUES (?, ?, ?);";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                ps.setLong(1, 1);
                ps.setString(2, "NORMAL");
                ps.setString(3, "A");
                ps.executeUpdate();

                ps.setLong(1, 2);
                ps.setString(2, "COMPUESTO");
                ps.setString(3, "A");
                ps.executeUpdate();

                ps.setLong(1, 3);
                ps.setString(2, "PRODUCIDO");
                ps.setString(3, "A");
                ps.executeUpdate();

                ps.setLong(1, 4);
                ps.setString(2, "NO INVENTARIO");
                ps.setString(3, "A");
                ps.executeUpdate();

                ps.setLong(1, 5);
                ps.setString(2, "COMBUSTIBLE");
                ps.setString(3, "A");
                ps.executeUpdate();

            } catch (SQLException e) {
                if (!e.getMessage().contains("grupos_tipos_pk")) {
                    NeoService.setLog(e.getMessage());
                }
            } catch (Exception e) {
                NeoService.setLog(e.getMessage());
            }

        }

        if (p.getCategorias() != null) {
            p.getCategorias().forEach((categoria) -> {
                try {
                    String sql = "INSERT INTO public.grupos(\n"
                            + "            id, grupo, estado, grupos_tipos_id, empresas_id, grupos_id, url_foto)\n"
                            + "    VALUES (?, ?, ?, ?, ?, NULL, ?);";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setLong(1, categoria.getGp_id());
                    ps.setString(2, categoria.getGrupo());
                    ps.setString(3, "A");
                    ps.setInt(4, 1); //1 = PRODUCTOS
                    ps.setLong(5, cr.getEmpresas_id());
                    ps.setString(6, categoria.getUrl_foto());
                    ps.executeUpdate();

                } catch (SQLException e) {
                    if (!e.getMessage().contains("grupos_pk")) {
                        NeoService.setLog(e.getMessage());
                    }
                } catch (Exception e) {
                    NeoService.setLog(e.getMessage());
                }
            });
        }

        try {
            String sql = "INSERT INTO unidades(\n"
                    + "            id, descripcion, valor, empresas_id, estado)\n"
                    + "    VALUES (?, ?, ?, ?, 'A');";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, p.getUnidades_medida_id());
            ps.setString(2, p.getUnidades_contenido());
            ps.setFloat(3, p.getUnidades_medida_valor());
            ps.setFloat(4, cr.getEmpresas_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        }

        try {

            String sql = "INSERT INTO unidades(\n"
                    + "            id, descripcion, valor, empresas_id, estado)\n"
                    + "    VALUES (?, ?, ?, ?, 'A');";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, p.getUnidades_contenido_id());
            ps.setString(2, p.getUnidades_contenido());
            ps.setFloat(3, p.getUnidades_contenido_valor());
            ps.setFloat(4, cr.getEmpresas_id());
            ps.executeUpdate();

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        }

        try {
            String sql = "INSERT INTO identificadores_origenes(\n"
                    + "            id, descripcion, estado)\n"
                    + "    VALUES (?, ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, 4);
            ps.setString(2, "PRODUCTOS");
            ps.setString(3, "A");
            ps.executeUpdate();

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        }

        try {

            String sql = "INSERT INTO productos(\n"
                    + "            id, descripcion, estado, productos_tipos_id, empresas_id)\n"
                    + "    VALUES (?, ?, ?, ?, ?);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, p.getId());
            ps.setString(2, p.getDescripcion().toUpperCase());
            ps.setString(3, p.getEstado());
            ps.setInt(4, p.getTipo());
            ps.setLong(5, p.getEmpresaId());
            ps.executeUpdate();
            NeoService.setLog("[SETUPDAO UPD] ID:" + p.getId() + " DESC: " + p.getDescripcion() + " BLOQUEADO");

        } catch (PSQLException e) {

            if (e.getMessage().contains("productos_pk")) {

                try {
                    String sqlup = "UPDATE public.productos\n"
                            + "   SET id=?, descripcion=?, estado=?, productos_tipos_id=?, empresas_id=?\n"
                            + " WHERE id=?";

                    PreparedStatement ps2 = NeoService.obtenerConexion().prepareStatement(sqlup);
                    ps2.setLong(1, p.getId());
                    ps2.setString(2, p.getDescripcion().toUpperCase());
                    ps2.setString(3, p.getEstado());
                    ps2.setInt(4, p.getTipo());
                    ps2.setLong(5, p.getEmpresaId());
                    ps2.setLong(6, p.getId());
                    ps2.executeUpdate();

                    ps2.executeUpdate();
                    NeoService.setLog("[SETUPDAO UPD] ID:" + p.getId() + " DESC: " + p.getDescripcion() + " BLOQUEADO");
                } catch (SQLException ed) {
                    throw new DAOException("111." + ed.getMessage());
                }
            }

        } catch (SQLException e) {
            throw new DAOException("12." + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("13." + e.getMessage());
        }

        try {
            //Falta agregar el tipo de identificadores
            if (p.getIdentificadores() != null) {
                p.getIdentificadores().forEach((identi) -> {
                    try {

                        String sql = "INSERT INTO identificadores(\n"
                                + "            id, identificador, origen, placa_vin, fecha_instalacion, fecha_revision, \n"
                                + "            fecha_vencimiento, estado, create_user, create_date, update_user, \n"
                                + "            update_date, empresas_id, entidad_id)\n"
                                + "    VALUES (?, ?, 4, null, now(), null, \n"
                                + "            NULL, 'A', 1, NOW(), NULL, \n"
                                + "            NULL, ?, ?);";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, identi.getId());
                        ps.setString(2, identi.getIdentificador());
                        ps.setLong(3, cr.getEmpresas_id());
                        ps.setLong(4, p.getId());
                        ps.executeQuery();

                    } catch (SQLException e) {
                        NeoService.setLog("Error sql insertando los identificadores");
                    }
                });
            }
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }

        try {
            if (p.getCategorias() != null) {
                p.getCategorias().forEach((categoria) -> {
                    try {
                        String sql = "SELECT 1 FROM grupos_entidad where grupo_id=? and entidad_id=?";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setLong(1, categoria.getGp_id());
                        ps.setLong(2, p.getId());
                        ResultSet re = ps.executeQuery();
                        if (!re.next()) {
                            sql = "INSERT INTO grupos_entidad(\n"
                                    + "            id, grupo_id, entidad_id)\n"
                                    + "    VALUES (nextval('grupos_entidad_id'), ?, ?);";
                            ps = NeoService.obtenerConexion().prepareStatement(sql);

                            ps.setLong(1, categoria.getGp_id());
                            ps.setLong(2, p.getId());
                            ps.executeUpdate();
                        }

                    } catch (SQLException e) {
                        if (!e.getMessage().contains("grupos_unicos")) {
                            NeoService.setLog(e.getMessage());
                        }
                    } catch (Exception e) {
                        NeoService.setLog(e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
        }

        if (p.getPrecios() != null) {
            try {

                for (PreciosBean precio : p.getPrecios()) {
                    try {
                        String sql = "INSERT INTO public.grupos_entidad(\n"
                                + "            id, grupo_id, entidad_id)\n"
                                + "    VALUES (?, ?, ?);";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, precio.getId());
                        ps.setLong(2, p.getId());
                        ps.setFloat(3, precio.getPrecio());
                        ps.executeUpdate();

                    } catch (PSQLException e) {

                        String sql = "UPDATE public.listas_precios\n"
                                + "   SET productos_id=?, precio=?\n"
                                + " WHERE id=?;";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, precio.getId());
                        ps.setFloat(2, precio.getPrecio());
                        ps.setLong(3, p.getId());
                        ps.executeUpdate();

                    } catch (SQLException e) {
                        NeoService.setLog(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                NeoService.setLog(e.getMessage());
            }
        }

        if (p.getImpuestos() != null) {
            try {

                String sql = "DELETE FROM public.productos_impuestos WHERE productos_id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, p.getId());
                ps.executeUpdate();

                for (ImpuestosBean impuesto : p.getImpuestos()) {
                    try {
                        sql = "INSERT INTO public.impuestos(\n"
                                + "            id, descripcion, porcentaje_valor, valor, fecha_inicio, \n"
                                + "            fecha_fin, estado, empresas_id)\n"
                                + "    VALUES (?, ?, ?, ?, null, \n"
                                + "            null, 'A', ?);";

                        ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setLong(1, impuesto.getImpuestos_id());
                        ps.setString(2, impuesto.getDescripcion().toUpperCase());
                        ps.setString(3, impuesto.getPorcentaje_valor());
                        ps.setFloat(4, impuesto.getValor());
                        ps.setLong(5, cr.getEmpresas_id());
                        ps.executeUpdate();

                    } catch (PSQLException e) {

                        if (e.getMessage().contains("impuestos_pk")) {
                            sql = "UPDATE public.impuestos\n"
                                    + "   SET descripcion=?, porcentaje_valor=?, valor=?, fecha_inicio=null, \n"
                                    + "       fecha_fin=null, estado='A', empresas_id=?\n"
                                    + " WHERE id=?";
                            ps = NeoService.obtenerConexion().prepareStatement(sql);
                            ps.setString(1, impuesto.getDescripcion().toUpperCase());
                            ps.setString(2, impuesto.getPorcentaje_valor());
                            ps.setFloat(3, impuesto.getValor());
                            ps.setLong(4, cr.getEmpresas_id());
                            ps.setLong(5, impuesto.getImpuestos_id());
                            ps.executeUpdate();
                        } else {
                            NeoService.setLog(e.getMessage());
                        }
                    } catch (SQLException e) {
                        NeoService.setLog(e.getMessage());
                    }

                    try {
                        sql = "INSERT INTO public.productos_impuestos(\n"
                                + "            id, productos_id, impuestos_id, iva_incluido)\n"
                                + "    VALUES (?, ?, ?, ?);";
                        ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setLong(1, impuesto.getId());
                        ps.setLong(2, p.getId());
                        ps.setLong(3, impuesto.getImpuestos_id());
                        ps.setString(4, impuesto.isIva_incluido() ? "S" : "N");
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        NeoService.setLog("Error al ejecutar la relacion productos_impuestos");
                        NeoService.setLog(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                NeoService.setLog(e.getMessage());
            }
        }

    }

    public void update(ProductoBean p, CredencialBean cr) throws DAOException {
        try {
            String sqlup = "UPDATE public.productos\n"
                    + "   SET precio=?, estado=?, seguimiento=?\n"
                    + " WHERE id=?";

            PreparedStatement ps2 = NeoService.obtenerConexion().prepareStatement(sqlup);

            ps2.setFloat(1, p.getPrecio());
            ps2.setString(2, p.getEstado());
            if (p.getDispensado() != null) {
                ps2.setString(3, p.getDispensado());
            } else {
                ps2.setNull(3, Types.NULL);
            }
            ps2.setLong(4, p.getId());

            ps2.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("12." + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("13." + e.getMessage());
        }

        try {
            //Falta agregar el tipo de identificadores
            if (p.getIdentificadores() != null) {
                p.getIdentificadores().forEach((identi) -> {
                    try {

                        String sql = "INSERT INTO identificadores(\n"
                                + "            id, identificador, origen, placa_vin, fecha_instalacion, fecha_revision, \n"
                                + "            fecha_vencimiento, estado, create_user, create_date, update_user, \n"
                                + "            update_date, empresas_id, entidad_id)\n"
                                + "    VALUES (?, ?, 4, null, now(), null, \n"
                                + "            NULL, 'A', 1, NOW(), NULL, \n"
                                + "            NULL, ?, ?);";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, identi.getId());
                        ps.setString(2, identi.getIdentificador());
                        ps.setLong(3, cr.getEmpresas_id());
                        ps.setLong(4, p.getId());
                        ps.executeQuery();

                    } catch (SQLException e) {
                        NeoService.setLog("Error sql insertando los identificadores");
                    }
                });
            }
        } catch (Exception e) {
        }

        try {
            p.getCategorias().forEach((categoria) -> {
                try {
                    String sql = "INSERT INTO grupos_entidad(\n"
                            + "            id, grupo_id, entidad_id)\n"
                            + "    VALUES (nextval('grupos_entidad_id'), ?, ?);";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setLong(1, categoria.getGp_id());
                    ps.setLong(2, p.getId());
                    ps.executeUpdate();

                } catch (SQLException e) {
                    if (!e.getMessage().contains("grupos_unicos")) {
                        NeoService.setLog(e.getMessage());
                    }
                } catch (Exception e) {
                    NeoService.setLog(e.getMessage());
                }
            });
        } catch (Exception e) {
        }

        if (p.getPrecios() != null) {
            try {

                for (PreciosBean precio : p.getPrecios()) {
                    try {
                        String sql = "INSERT INTO public.grupos_entidad(\n"
                                + "            id, grupo_id, entidad_id)\n"
                                + "    VALUES (?, ?, ?);";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, precio.getId());
                        ps.setLong(2, p.getId());
                        ps.setFloat(3, precio.getPrecio());
                        ps.executeUpdate();

                    } catch (PSQLException e) {

                        String sql = "UPDATE public.listas_precios\n"
                                + "   SET productos_id=?, precio=?\n"
                                + " WHERE id=?;";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, precio.getId());
                        ps.setFloat(2, precio.getPrecio());
                        ps.setLong(3, p.getId());
                        ps.executeUpdate();

                    } catch (SQLException e) {
                        NeoService.setLog(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                NeoService.setLog(e.getMessage());
            }
        }

        if (p.getImpuestos() != null) {
            try {

                String sql = "DELETE FROM public.productos_impuestos WHERE productos_id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, p.getId());
                ps.executeUpdate();

                for (ImpuestosBean impuesto : p.getImpuestos()) {
                    try {
                        sql = "INSERT INTO public.impuestos(\n"
                                + "            id, descripcion, porcentaje_valor, valor, fecha_inicio, \n"
                                + "            fecha_fin, estado, empresas_id)\n"
                                + "    VALUES (?, ?, ?, ?, null, \n"
                                + "            null, 'A', ?);";

                        ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setLong(1, impuesto.getImpuestos_id());
                        ps.setString(2, impuesto.getDescripcion().toUpperCase());
                        ps.setString(3, impuesto.getPorcentaje_valor());
                        ps.setFloat(4, impuesto.getValor());
                        ps.setLong(5, cr.getEmpresas_id());
                        ps.executeUpdate();

                    } catch (PSQLException e) {

                        if (e.getMessage().contains("impuestos_pk")) {
                            sql = "UPDATE public.impuestos\n"
                                    + "   SET descripcion=?, porcentaje_valor=?, valor=?, fecha_inicio=null, \n"
                                    + "       fecha_fin=null, estado='A', empresas_id=?\n"
                                    + " WHERE id=?";
                            ps = NeoService.obtenerConexion().prepareStatement(sql);
                            ps.setString(1, impuesto.getDescripcion().toUpperCase());
                            ps.setString(2, impuesto.getPorcentaje_valor());
                            ps.setFloat(3, impuesto.getValor());
                            ps.setLong(4, cr.getEmpresas_id());
                            ps.setLong(5, impuesto.getImpuestos_id());
                            ps.executeUpdate();
                        } else {
                            NeoService.setLog(e.getMessage());
                        }
                    } catch (SQLException e) {
                        NeoService.setLog(e.getMessage());
                    }

                    try {
                        sql = "INSERT INTO public.productos_impuestos(\n"
                                + "            id, productos_id, impuestos_id, iva_incluido)\n"
                                + "    VALUES (?, ?, ?, ?);";
                        ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setLong(1, impuesto.getId());
                        ps.setLong(2, p.getId());
                        ps.setLong(3, impuesto.getImpuestos_id());
                        ps.setString(4, impuesto.isIva_incluido() ? "S" : "N");
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        NeoService.setLog("Error al ejecutar la relacion productos_impuestos");
                        NeoService.setLog(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                NeoService.setLog(e.getMessage());
            }
        }

    }

    public void createBodega(BodegaBean bodega, CredencialBean cr) throws DAOException {
        try {
            String sql = "INSERT INTO bodegas(\n"
                    + "            id, descripcion, estado, empresas_id, codigo, dimension, ubicacion, \n"
                    + "            numeros_stand)\n"
                    + "    VALUES (?, ?, ?, ?, ?, ?, ?, \n"
                    + "            ?);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, bodega.getId());
            ps.setString(2, bodega.getDescripcion());
            ps.setString(3, bodega.getEstado());
            ps.setLong(4, bodega.getEmpresaId());
            ps.setString(5, bodega.getCodigo());
            ps.setString(6, bodega.getDimension());
            ps.setString(7, bodega.getUbicacion());
            ps.setInt(8, bodega.getNumeroStand());
            ps.executeUpdate();

        } catch (PSQLException e) {

            if (e.getMessage().contains("bodegas_pk")) {

                try {
                    String sql = "UPDATE bodegas\n"
                            + "   SET descripcion=?, estado=?, empresas_id=?, codigo=?, dimension=?, \n"
                            + "       ubicacion=?, numeros_stand=?\n"
                            + " WHERE id=?";

                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setString(1, bodega.getDescripcion());
                    ps.setString(2, bodega.getEstado());
                    ps.setLong(3, bodega.getEmpresaId());
                    ps.setString(4, bodega.getCodigo());
                    ps.setString(5, bodega.getDimension());
                    ps.setString(6, bodega.getUbicacion());
                    ps.setInt(7, bodega.getNumeroStand());
                    ps.setLong(8, bodega.getId());
                    ps.executeUpdate();

                } catch (SQLException ed) {
                    throw new DAOException("111." + ed.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new DAOException("12." + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("13." + e.getMessage());
        }
    }

    public void createConsecutivos(ArrayList<ConsecutivoBean> consecutivos, CredencialBean cr) throws DAOException {
        if (!consecutivos.isEmpty()) {

            try {
                String sql = "UPDATE consecutivos SET estado='I'";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DAOException("13." + e.getMessage());
            }

            try {
                for (ConsecutivoBean consecutivo : consecutivos) {

                    try {
                        String sql = "INSERT INTO consecutivos(\n"
                                + "            id, empresas_id, tipo_documento, prefijo, fecha_inicio, fecha_fin, \n"
                                + "            consecutivo_inicial, consecutivo_final, consecutivo_actual,  estado, \n"
                                + "            resolucion, observaciones, equipos_id)\n"
                                + "    VALUES (?, ?, ?, ?, ?, ?, \n"
                                + "            ?, ?, ?, ?, \n"
                                + "            ?, ?, ?);";

                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setLong(1, consecutivo.getId());
                        ps.setLong(2, cr.getEmpresas_id());
                        ps.setLong(3, consecutivo.getTipo_documento());
                        ps.setString(4, consecutivo.getPrefijo());

                        if (consecutivo.getFecha_inicio() != null) {
                            ps.setDate(5, new Date(consecutivo.getFecha_inicio().getTime()));
                        } else {
                            ps.setNull(5, Types.NULL);
                        }

                        if (consecutivo.getFecha_fin() != null) {
                            ps.setDate(6, new Date(consecutivo.getFecha_fin().getTime()));
                        } else {
                            ps.setNull(6, Types.NULL);
                        }

                        ps.setLong(7, consecutivo.getConsecutivo_inicial());

                        if (consecutivo.getConsecutivo_final() != 0) {
                            ps.setLong(8, consecutivo.getConsecutivo_final());
                        } else {
                            ps.setNull(8, Types.NULL);
                        }

                        if (consecutivo.getConsecutivo_actual() != 0) {
                            ps.setLong(9, consecutivo.getConsecutivo_actual());
                        } else {
                            ps.setNull(9, Types.NULL);
                        }
                        ps.setString(10, consecutivo.getEstado());
                        ps.setString(11, consecutivo.getResolucion());
                        ps.setString(12, consecutivo.getObservaciones());

                        if (consecutivo.getEquipo_id() != 0) {
                            ps.setLong(13, consecutivo.getEquipo_id());
                        } else {
                            ps.setNull(13, Types.NULL);
                        }

                        ps.executeUpdate();

                    } catch (PSQLException e) {
                        if (e.getMessage().contains("consecutivos_pk")) {
                            try {

                                String sql = "Select consecutivo_actual from consecutivos where id=? ";
                                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                                ps.setLong(1, consecutivo.getId());

                                ResultSet res = ps.executeQuery();

                                while (res.next()) {
                                    consecutivo.setConsecutivo_actual(res.getLong("consecutivo_actual"));
                                }

                                sql = "UPDATE public.consecutivos\n"
                                        + "   SET id=?, empresas_id=?, tipo_documento=?, prefijo=?, fecha_inicio=?, \n"
                                        + "       fecha_fin=?, consecutivo_inicial=?, consecutivo_actual=?, consecutivo_final=?, \n"
                                        + "       estado=?, resolucion=?, observaciones=?, equipos_id=?\n"
                                        + " WHERE id=?;";

                                ps = NeoService.obtenerConexion().prepareStatement(sql);
                                ps.setLong(1, consecutivo.getId());
                                ps.setLong(2, cr.getEmpresas_id());
                                ps.setLong(3, consecutivo.getTipo_documento());
                                ps.setString(4, consecutivo.getPrefijo());

                                if (consecutivo.getFecha_inicio() != null) {
                                    ps.setDate(5, new Date(consecutivo.getFecha_inicio().getTime()));
                                } else {
                                    ps.setNull(5, Types.NULL);
                                }

                                if (consecutivo.getFecha_fin() != null) {
                                    ps.setDate(6, new Date(consecutivo.getFecha_fin().getTime()));
                                } else {
                                    ps.setNull(6, Types.NULL);
                                }

                                ps.setLong(7, consecutivo.getConsecutivo_inicial());

                                if (consecutivo.getConsecutivo_actual() != 0) {
                                    ps.setLong(8, consecutivo.getConsecutivo_actual());
                                } else {
                                    ps.setNull(8, Types.NULL);
                                }

                                if (consecutivo.getConsecutivo_final() != 0) {
                                    ps.setLong(9, consecutivo.getConsecutivo_final());
                                } else {
                                    ps.setNull(9, Types.NULL);
                                }

                                ps.setString(10, consecutivo.getEstado());
                                ps.setString(11, consecutivo.getResolucion());
                                ps.setString(12, consecutivo.getObservaciones());

                                if (consecutivo.getEquipo_id() != 0) {
                                    ps.setLong(13, consecutivo.getEquipo_id());
                                } else {
                                    ps.setNull(13, Types.NULL);
                                }

                                ps.setLong(14, consecutivo.getId());
                                ps.executeUpdate();

                            } catch (SQLException ed) {
                                throw new DAOException("CONSECUTIVO ERROR 111." + ed.getMessage());
                            }
                        }
                    } catch (SQLException e) {
                        throw new DAOException("CONSECUTIVO ERROR 12." + e.getMessage());
                    } catch (Exception e) {
                        throw new DAOException("CONSECUTIVO ERROR 13." + e.getMessage());
                    }
                }

                try {
                    String sql = "DELETE FROM consecutivos WHERE estado='I'";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new DAOException("CONSECUTIVO ERROR 18." + e.getMessage());
                }

            } catch (Exception e) {
                throw new DAOException("CONSECUTIVO ERROR 16." + e.getMessage());
            }
        }
    }

    public void procerInventarioXX(int i, BodegaBean bodega, ProductoBean producto) throws DAOException {

        if (i == 0) {
            try {
                String sql = "TRUNCATE bodegas_productos CASCADE";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.executeUpdate();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            String sql = "INSERT INTO bodegas_productos(\n"
                    + "            id, productos_id, bodegas_id, saldo, cantidad_minima, cantidad_maxima, \n"
                    + "            empresas_id, tiempo_reorden, costo)\n"
                    + "    VALUES (?, ?, ?, ?, ?, ?, \n"
                    + "            ?, ?, ?);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, producto.getBodega_producto_id());
            ps.setLong(2, producto.getId());
            ps.setLong(3, bodega.getId());
            ps.setFloat(4, producto.getSaldo());
            ps.setFloat(5, producto.getCantidadMinima());
            ps.setFloat(6, producto.getCantidadMaxima());
            ps.setLong(7, bodega.getEmpresaId());
            ps.setInt(8, producto.getTiempoReorden());
            ps.setFloat(9, producto.getProducto_compuesto_costo());
            ps.executeUpdate();

        } catch (PSQLException e) {

            if (e.getMessage().contains("bodegas_productos_pk")) {

                try {
                    String sql = "UPDATE bodegas_productos\n"
                            + "   SET productos_id=?, bodegas_id=?, saldo=?, cantidad_minima=?, \n"
                            + "       cantidad_maxima=?, empresas_id=?, tiempo_reorden=?, costo=?\n"
                            + " WHERE id=?";

                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setLong(1, producto.getId());
                    ps.setLong(2, bodega.getId());
                    ps.setFloat(3, producto.getSaldo());
                    ps.setFloat(4, producto.getCantidadMinima());
                    ps.setFloat(5, producto.getCantidadMaxima());
                    ps.setLong(6, bodega.getEmpresaId());
                    ps.setInt(7, producto.getTiempoReorden());
                    ps.setFloat(9, producto.getProducto_compuesto_costo());
                    ps.setLong(8, producto.getBodega_producto_id());

                    ps.executeUpdate();

                } catch (SQLException ed) {
                    throw new DAOException("bodega_producto 111." + ed.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new DAOException("bodega_producto 21." + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("bodega_producto 31." + e.getMessage());
        }
    }

    public void procesarEmpleado(PersonaBean pers, CredencialBean credencial) throws DAOException {

        try {
            String sql = "INSERT INTO public.tipos_identificaciones(\n"
                    + "            id, descripcion, estado)\n"
                    + "    VALUES (?, ?, 'A');";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, pers.getTipoIdentificacionId());
            ps.setString(2, pers.getTipoIdentificacionDesc());

            ps.executeUpdate();

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        }

        try {
            if (pers.getModulos() != null) {
                for (ModulosBean modulo : pers.getModulos()) {
                    String sql = "INSERT INTO permisos_post(\n"
                            + "            id, personas_id, modulos_id, modulos_descripcion)\n"
                            + "    VALUES (nextval('permisos_post_id'), ?, ?, ?);";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, pers.getId());
                    ps.setLong(2, modulo.getId());
                    ps.setString(3, modulo.getDescripcion());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }

        if (pers.getId() == 91) {
            NeoService.setLog("");
        }

        try {
            String sql = "INSERT INTO public.personas(\n"
                    + "            id, usuario, clave, identificacion, tipos_identificacion_id, \n"
                    + "            nombre, estado, correo, direccion, fecha_nacimiento, perfiles_id, \n"
                    + "            telefono, celular, create_user, create_date, update_user, update_date, \n"
                    + "            ciudades_id, sangre, genero, empresas_id, sucursales_id, sincronizado, \n"
                    + "            tag)\n"
                    + "    VALUES (?, ?, ?, ?, ?, \n"
                    + "            ?, ?, null, '', null, 1, \n"
                    + "            '', '', null, null, null, null, \n"
                    + "            null, null, null, ?, null, 1, \n"
                    + "            ?);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, pers.getId());
            ps.setString(2, pers.getIdentificacion());
            ps.setInt(3, pers.getPin());
            ps.setString(4, pers.getIdentificacion());
            ps.setLong(5, pers.getTipoIdentificacionId());
            ps.setString(6, pers.getNombre() + " " + pers.getApellidos());
            ps.setString(7, pers.getEstado());
            ps.setLong(8, credencial.getEmpresas_id());
            ps.setString(9, pers.getTag());

            ps.executeUpdate();

        } catch (PSQLException e) {

            if (e.getMessage().contains("personas_pk")) {
                try {
                    String sql = "UPDATE public.personas\n"
                            + "   SET id=?, usuario=?, clave=?, identificacion=?, tipos_identificacion_id=?, \n"
                            + "       nombre=?, estado=?, correo=null, direccion='', fecha_nacimiento=null, \n"
                            + "       perfiles_id=1, telefono='', celular='', create_user=null, create_date=null, \n"
                            + "       update_user=null, update_date=null, ciudades_id=null, sangre=null, genero=null, \n"
                            + "       empresas_id=?, sucursales_id=null, sincronizado=1, tag=?\n"
                            + " WHERE ID=?;";

                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, pers.getId());
                    ps.setString(2, pers.getIdentificacion());
                    ps.setInt(3, pers.getPin());
                    ps.setString(4, pers.getIdentificacion());
                    ps.setLong(5, pers.getTipoIdentificacionId());
                    ps.setString(6, pers.getNombre() + " " + pers.getApellidos());
                    ps.setString(7, pers.getEstado());
                    ps.setLong(8, credencial.getEmpresas_id());
                    ps.setString(9, pers.getTag());
                    ps.setLong(10, pers.getId());

                    ps.executeUpdate();

                } catch (SQLException ed) {
                    throw new DAOException("bodega_producto 175." + ed.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new DAOException("bodega_producto 21." + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("bodega_producto 31." + e.getMessage());
        } finally {

        }

        try {
            //Falta agregar el tipo de identificadores
            if (pers.getIdentificadores() != null) {
                pers.getIdentificadores().forEach((identi) -> {
                    try {

                        String sql = "INSERT INTO identificadores(\n"
                                + "            id, identificador, origen, placa_vin, fecha_instalacion, fecha_revision, \n"
                                + "            fecha_vencimiento, estado, create_user, create_date, update_user, \n"
                                + "            update_date, empresas_id, entidad_id)\n"
                                + "    VALUES (?, ?, 5, null, now(), null, \n"
                                + "            NULL, ?, 1, NOW(), NULL, \n"
                                + "            NULL, ?, ?);";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, identi.getId());
                        ps.setString(2, identi.getIdentificador());
                        ps.setString(3, identi.getEstado());
                        ps.setLong(4, credencial.getEmpresas_id());
                        ps.setLong(5, pers.getId());
                        ps.executeQuery();
                    } catch (SQLException e) {
                        NeoService.setLog("Error sql insertando los identificadores");
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    public void procesarCategorias(int index, CategoriaBean categoria, CredencialBean cr) throws DAOException {

        if (index == 0) {
            try {

                String sql = "INSERT INTO public.grupos_tipos(\n"
                        + "            id, descripcion, estado, entidad)\n"
                        + "    VALUES (?, ?, ?, ?);";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                ps.setLong(1, 1);
                ps.setString(2, "PRODUCTOS");
                ps.setString(3, "A");
                ps.setInt(4, 1); //1 = PRODUCTOS
                ps.executeUpdate();

            } catch (SQLException e) {
                if (!e.getMessage().contains("grupos_tipos_pk")) {
                    NeoService.setLog(e.getMessage());
                }
            } catch (Exception e) {
                NeoService.setLog(e.getMessage());
            }

            try {
                String sql = "DELETE FROM GRUPOS_ENTIDAD";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!e.getMessage().contains("grupos_tipos_pk")) {
                    NeoService.setLog(e.getMessage());
                }
            } catch (Exception e) {
                NeoService.setLog(e.getMessage());
            }
        }

        try {

            String sql = "INSERT INTO public.grupos(\n"
                    + "            id, grupo, estado, grupos_tipos_id, empresas_id, grupos_id, url_foto)\n"
                    + "    VALUES (?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, categoria.getId());
            ps.setString(2, categoria.getGrupo());
            ps.setString(3, categoria.getEstado());
            ps.setLong(4, categoria.getGruposTiposId());
            ps.setLong(5, cr.getEmpresas_id());
            ps.setNull(6, Types.NULL);
            ps.setString(7, categoria.getUrl_foto());
            ps.executeUpdate();

        } catch (SQLException e) {
            if (!e.getMessage().contains("grupos_pk")) {
                NeoService.setLog(e.getMessage());
            }
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public void procesarMediosPago(MediosPagosBean medio, CredencialBean cr) throws DAOException {
        try {

            String sql = "INSERT INTO public.medios_pagos(\n"
                    + "            id, descripcion, empresas_id, credito, estado, cambio, minimo_valor, \n"
                    + "            maximo_cambio, comprobante)\n"
                    + "    VALUES (?, ?, ?, ?, ?, ?, ?, \n"
                    + "            ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, medio.getId());
            ps.setString(2, medio.getDescripcion());
            ps.setLong(3, cr.getEmpresas_id());
            ps.setString(4, medio.isCredito() ? "S" : "N");
            ps.setString(5, medio.getEstado());
            ps.setString(6, medio.isCambio() ? "S" : "N");
            ps.setFloat(7, medio.getMinimo_valor());
            ps.setFloat(8, medio.getMaximo_cambio());
            ps.setString(9, medio.isComprobante() ? "S" : "N");
            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("medios_pagos_pk")) {
                try {
                    String sql = "UPDATE public.medios_pagos\n"
                            + "   SET descripcion=?, empresas_id=?, credito=?, estado=?, cambio=?, \n"
                            + "       minimo_valor=?, maximo_cambio=?, comprobante=?\n"
                            + " WHERE id=? ";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setString(1, medio.getDescripcion());
                    ps.setLong(2, cr.getEmpresas_id());
                    ps.setString(3, medio.isCredito() ? "S" : "N");
                    ps.setString(4, medio.getEstado());
                    ps.setString(5, medio.isCambio() ? "S" : "N");
                    ps.setFloat(6, medio.getMinimo_valor());
                    ps.setFloat(7, medio.getMaximo_cambio());
                    ps.setString(8, medio.isComprobante() ? "S" : "N");
                    ps.setLong(9, medio.getId());
                    ps.executeUpdate();
                } catch (SQLException e2) {
                    NeoService.setLog(e2.getMessage());
                }
            }
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public void limpiaEmpleados() throws DAOException {

        try {
            String sql = "INSERT INTO identificadores_origenes(\n"
                    + "            id, descripcion, estado)\n"
                    + "    VALUES (?, ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, 5);
            ps.setString(2, "PERSONAS");
            ps.setString(3, "A");
            ps.executeUpdate();

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        }

        try {
            String sql = "UPDATE PERSONAS SET ESTADO='I'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.executeUpdate();

            sql = "UPDATE PERSONAS SET ESTADO='I'";
            ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.executeUpdate();

            sql = "DELETE FROM IDENTIFICADORES WHERE ORIGEN=5";
            ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.executeUpdate();

            sql = "DELETE FROM PERMISOS";
            ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.executeUpdate();

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public ConsecutivoBean ultimoConsecutivo(CredencialBean credencial) throws DAOException {
        ConsecutivoBean conseActual = null;
        try {
            String sql = "SELECT id, empresas_id, tipo_documento, prefijo, fecha_inicio, fecha_fin, \n"
                    + "       consecutivo_inicial, consecutivo_actual, consecutivo_final, estado, \n"
                    + "       resolucion, observaciones, equipos_id\n"
                    + "  FROM public.consecutivos WHERE estado in ('A', 'U')";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();

            ArrayList<ConsecutivoBean> consecutivos = new ArrayList<>();
            while (re.next()) {
                ConsecutivoBean cs = new ConsecutivoBean();
                cs.setId(re.getLong("id"));
                cs.setConsecutivo_inicial(re.getLong("consecutivo_inicial"));
                cs.setConsecutivo_actual(re.getLong("consecutivo_actual"));
                cs.setConsecutivo_final(re.getLong("consecutivo_final"));
                cs.setEstado(re.getString("estado"));
                cs.setResolucion(re.getString("resolucion"));
                cs.setObservaciones(re.getString("observaciones"));
                cs.setPrefijo(re.getString("prefijo"));
                consecutivos.add(cs);
            }

            conseActual = getLastConsecutivoByList(consecutivos);

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }

        if (conseActual != null && conseActual.getConsecutivo_actual() == 0) {
            conseActual.setConsecutivo_actual(conseActual.getConsecutivo_inicial());
        }
        return conseActual;
    }

    public void borraTodosImpuestos() throws DAOException {
        try {

            String sql = "DELETE FROM PRODUCTOS_IMPUESTOS WHERE ";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.executeUpdate();

            sql = "DELETE FROM IMPUESTOS";
            ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.executeUpdate();

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public void updateInventario(ProductoBean producto, CredencialBean credencial) throws DAOException {

        try {
            String sql = "INSERT INTO bodegas_productos(\n"
                    + "            id, productos_id, bodegas_id, saldo, cantidad_minima, cantidad_maxima, \n"
                    + "            empresas_id, tiempo_reorden, costo)\n"
                    + "    VALUES (?, ?, ?, ?, ?, ?, \n"
                    + "            ?, ?, ?);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, producto.getBodega_producto_id());
            ps.setLong(2, producto.getId());
            ps.setLong(3, credencial.getBodegaId());
            ps.setFloat(4, producto.getSaldo());
            ps.setFloat(5, producto.getCantidadMinima());
            ps.setFloat(6, producto.getCantidadMaxima());
            ps.setLong(7, credencial.getEmpresas_id());
            ps.setInt(8, producto.getTiempoReorden());
            ps.setFloat(9, producto.getProducto_compuesto_costo());
            ps.executeUpdate();

        } catch (PSQLException e) {

            if (e.getMessage().contains("bodegas_productos_pk")) {

                try {
                    String sql = "UPDATE bodegas_productos\n"
                            + "   SET productos_id=?, bodegas_id=?, saldo=?, cantidad_minima=?, \n"
                            + "       cantidad_maxima=?, empresas_id=?, tiempo_reorden=?, costo=?\n"
                            + " WHERE id=?";

                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setLong(1, producto.getId());
                    ps.setLong(2, credencial.getBodegaId());
                    ps.setFloat(3, producto.getSaldo());
                    ps.setFloat(4, producto.getCantidadMinima());
                    ps.setFloat(5, producto.getCantidadMaxima());
                    ps.setLong(6, credencial.getEmpresas_id());
                    ps.setInt(7, producto.getTiempoReorden());
                    ps.setFloat(8, producto.getProducto_compuesto_costo());
                    ps.setLong(9, producto.getBodega_producto_id());

                    ps.executeUpdate();

                } catch (SQLException ed) {
                    throw new DAOException("bodega_producto 111." + ed.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new DAOException("bodega_producto 21." + e.getMessage());
        } catch (Exception e) {
            throw new DAOException("bodega_producto 31." + e.getMessage());
        }

    }

    public DescriptorBean getDescriptores(long empresasId) throws DAOException {
        DescriptorBean descriptor = null;
        try {
            String sql = "SELECT id, empresas_id, header, footer\n"
                    + "  FROM descriptores WHERE empresas_id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, empresasId);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                descriptor = new DescriptorBean();
                descriptor.setHeader(re.getString("header"));
                descriptor.setFooter(re.getString("footer"));
            }

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
        return descriptor;
    }

    public ConsecutivoBean getLastConsecutivoByList(ArrayList<ConsecutivoBean> consecutivos) {

        consecutivos.sort(Comparator.comparing(ConsecutivoBean::getId));

        ConsecutivoBean conseActual = null;
        if (!consecutivos.isEmpty()) {
            long minimo = 0;
            int i = 0;
            for (ConsecutivoBean cs : consecutivos) {
                if (cs.getConsecutivo_actual() <= cs.getConsecutivo_final()) {
                    conseActual = cs;
                    if (cs.getEstado().equals(Butter.CONSECUTIVO_ESTADO_ACTIVO)) {
                        try {
                            updateEstadoConsecutivo(Butter.CONSECUTIVO_ESTADO_USO, cs.getId());

                            JsonObject json = new JsonObject();
                            json.addProperty("id", cs.getId());
                            json.addProperty("estado", Butter.CONSECUTIVO_ESTADO_USO);
                            json.addProperty("update_user", 1);

                            EquipoDao edao = new EquipoDao();
                            edao.guardarTransmision(Main.credencial, json, Butter.SECURE_END_POINT_CONSECUTIVOS, Butter.PUT);

                        } catch (DAOException e) {
                        }
                    }
                    break;
                }
                if (cs.getConsecutivo_actual() > cs.getConsecutivo_final()) {
                    try {
                        updateEstadoConsecutivo(Butter.CONSECUTIVO_ESTADO_VENCIDO, cs.getId());
                        JsonObject json = new JsonObject();
                        json.addProperty("id", cs.getId());
                        json.addProperty("estado", Butter.CONSECUTIVO_ESTADO_VENCIDO);
                        json.addProperty("update_user", 1);

                        EquipoDao edao = new EquipoDao();
                        edao.guardarTransmision(Main.credencial, json, Butter.SECURE_END_POINT_CONSECUTIVOS, Butter.PUT);

                    } catch (DAOException e) {
                    }
                }
                i++;
            }
        }
        return conseActual;
    }

    public void updateEstadoConsecutivo(String estado, long id) throws DAOException {
        try {
            String sql = "UPDATE CONSECUTIVOS SET ESTADO=? WHERE ID=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, estado);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public boolean existeProductos() throws DAOException {
        boolean existe = false;
        try {
            String sql = "SELECT 1 FROM PRODUCTOS";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                existe = true;
            }

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
        return existe;
    }

    public void actualiazaEstado(ProductoBean p) throws DAOException {
        try {
            String sql = "UPDATE PRODUCTOS SET ESTADO=? WHERE ID=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, p.getEstado());
            ps.setLong(2, p.getId());
            ps.executeUpdate();

            NeoService.setLog("[SETUPDAO UPD] ID:" + p.getId() + " DESC: " + p.getDescripcion() + " DESBLOQUEADO");

        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }

    public void borraTodosIdentificadores(long id) throws DAOException {
        try {
            String sql = "DELETE FROM IDENTIFICADORES WHERE ORIGEN = 4 AND ENTIDAD_ID=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            NeoService.setLog(e.getMessage());
        } catch (Exception e) {
            NeoService.setLog(e.getMessage());
        }
    }
}
