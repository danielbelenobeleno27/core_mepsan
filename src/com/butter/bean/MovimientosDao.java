/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import com.core.app.NeoService;
import com.core.database.DAOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import org.postgresql.util.PSQLException;

public class MovimientosDao {

    public MovimientosDetallesBean findByBarCode(String code) throws DAOException {
        MovimientosDetallesBean bean = null;
        try {
            String sql = "Select p.id, p.plu, p.descripcion, p.precio, p.tipo, COALESCE(saldo, 0) saldo, bp.costo\n"
                    + "from productos p \n"
                    + "left join bodegas_productos bp on bp.productos_id=p.id\n"
                    + "where p.id=(select entidad_id from identificadores i where i.identificador=? and origen=4) and P.ESTADO='A' and P.puede_vender='S' \n"
                    + "";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, code);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean = new MovimientosDetallesBean();
                bean.setId(re.getLong("ID"));
                bean.setProductoId(re.getLong("ID"));
                bean.setPlu(re.getString("PLU"));
                bean.setDescripcion(re.getString("DESCRIPCION"));
                bean.setPrecio(re.getFloat("PRECIO"));
                bean.setCantidad(re.getInt("SALDO"));
                bean.setSaldo(re.getInt("SALDO"));
                bean.setProducto_compuesto_costo(re.getFloat("COSTO"));
                bean.setCompuesto(re.getInt("TIPO") == 2);
                bean.setImpuestos(findById(re.getLong("ID")));
                bean.setIngredientes(findIngredientesById(re.getLong("ID")));
                if (!bean.getIngredientes().isEmpty()) {
                    for (ProductoBean ingrediente : bean.getIngredientes()) {
                        bean.setProducto_compuesto_costo(bean.getProducto_compuesto_costo() + (ingrediente.getProducto_compuesto_costo() * ingrediente.getProducto_compuesto_cantidad()));
                    }
                }
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return bean;
    }

    public MovimientosDetallesBean findByPlu(String plu) throws DAOException {
        MovimientosDetallesBean bean = null;
        try {
            String sql = "Select p.id, p.plu, p.descripcion, p.precio, p.tipo, COALESCE(saldo, 0) saldo, bp.costo\n"
                    + "from productos p \n"
                    + "left join bodegas_productos bp on bp.productos_id=p.id\n"
                    + "where p.plu=? and P.ESTADO='A' and P.puede_vender='S' \n"
                    + "";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, plu);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean = new MovimientosDetallesBean();
                bean.setId(re.getLong("ID"));
                bean.setProductoId(re.getLong("ID"));
                bean.setPlu(re.getString("PLU"));
                bean.setDescripcion(re.getString("DESCRIPCION"));
                bean.setPrecio(re.getFloat("PRECIO"));
                bean.setCantidad(re.getInt("SALDO"));
                bean.setSaldo(re.getInt("SALDO"));
                bean.setProducto_compuesto_costo(re.getFloat("COSTO"));
                bean.setCompuesto(re.getInt("TIPO") == 2);
                bean.setImpuestos(findById(re.getLong("ID")));
                bean.setIngredientes(findIngredientesById(re.getLong("ID")));
                if (!bean.getIngredientes().isEmpty()) {
                    for (ProductoBean ingrediente : bean.getIngredientes()) {
                        bean.setProducto_compuesto_costo(bean.getProducto_compuesto_costo() + (ingrediente.getProducto_compuesto_costo() * ingrediente.getProducto_compuesto_cantidad()));
                    }
                }
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return bean;
    }

    public ArrayList<MovimientosDetallesBean> findByCategoria(CategoriaBean cat) throws DAOException {
        ArrayList<MovimientosDetallesBean> lista = new ArrayList<>();
        try {
            String sql = "Select p.id, p.plu, p.descripcion, p.precio, p.tipo, COALESCE(saldo, 0) saldo, bp.costo\n"
                    + "from productos p \n"
                    + "left join bodegas_productos bp on bp.productos_id=p.id\n"
                    + "inner join grupos_entidad ge on p.id=ge.entidad_id\n"
                    + "where grupo_id=? and P.ESTADO='A' and P.puede_vender='S'";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, cat.getId());
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                MovimientosDetallesBean bean = new MovimientosDetallesBean();
                bean.setId(re.getLong("ID"));
                bean.setProductoId(re.getLong("ID"));
                bean.setPlu(re.getString("PLU"));
                bean.setDescripcion(re.getString("DESCRIPCION"));
                bean.setPrecio(re.getFloat("PRECIO"));
                bean.setCantidad(re.getInt("SALDO"));
                bean.setSaldo(re.getInt("SALDO"));
                //bean.setCostos(re.getFloat("COSTO"));
                //bean.setCostoProducto(re.getFloat("COSTO"));
                bean.setProducto_compuesto_costo(re.getFloat("COSTO"));
                bean.setCompuesto(re.getInt("TIPO") == 2);
                bean.setImpuestos(findById(re.getLong("ID")));
                bean.setIngredientes(findIngredientesById(re.getLong("ID")));

                if (!bean.getIngredientes().isEmpty()) {
                    bean.getIngredientes().forEach((ingrediente) -> {
                        bean.setProducto_compuesto_costo(bean.getProducto_compuesto_costo() + (ingrediente.getProducto_compuesto_costo() * ingrediente.getProducto_compuesto_cantidad()));
                    });
                }

                lista.add(bean);
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return lista;
    }

    public ArrayList<ProductoBean> findIngredientesById(long id) throws DAOException {
        ArrayList<ProductoBean> lista = new ArrayList<>();
        try {
            String sql = "SELECT PC.*, COSTO, SALDO, PI.DESCRIPCION\n"
                    + "FROM PRODUCTOS P\n"
                    + "INNER JOIN PRODUCTOS_COMPUESTOS PC ON PC.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN BODEGAS_PRODUCTOS BP ON PC.INGREDIENTES_ID=BP.PRODUCTOS_ID\n"
                    + "INNER JOIN PRODUCTOS PI ON PI.ID=PC.INGREDIENTES_ID\n"
                    + "WHERE P.ID=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                ProductoBean bean = new ProductoBean();
                bean.setId(re.getLong("INGREDIENTES_ID"));
                bean.setDescripcion(re.getString("DESCRIPCION"));
                bean.setCantidad(re.getInt("SALDO"));
                bean.setSaldo(re.getInt("SALDO"));
                bean.setProducto_compuesto_cantidad(re.getFloat("cantidad"));
                bean.setProducto_compuesto_costo(re.getFloat("costo"));
                lista.add(bean);
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return lista;
    }

    public ProductoBean findProductByIdActive(long id) throws DAOException {
        ProductoBean bean = null;
        try {
            String sql = "Select p.id, p.plu, p.descripcion, p.precio, p.tipo, COALESCE(saldo, 0) saldo\n"
                    + "from productos p \n"
                    + "left join bodegas_productos bp on bp.productos_id=p.id\n"
                    + "where p.id=? and p.estado='A'\n"
                    + "";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean = new ProductoBean();
                bean.setId(re.getLong("ID"));
                bean.setPlu(re.getString("PLU"));
                bean.setDescripcion(re.getString("DESCRIPCION"));
                bean.setPrecio(re.getFloat("PRECIO"));
                bean.setCantidad(re.getInt("SALDO"));
                bean.setSaldo(re.getInt("SALDO"));

            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return bean;
    }

    public ProductoBean findProductByOnlyId(long id) throws DAOException {
        ProductoBean bean = null;
        try {
            String sql = "Select p.id, p.plu, p.descripcion, p.precio, p.tipo, COALESCE(saldo, 0) saldo\n"
                    + "from productos p \n"
                    + "left join bodegas_productos bp on bp.productos_id=p.id\n"
                    + "where p.id=?\n"
                    + "";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean = new ProductoBean();
                bean.setId(re.getLong("ID"));
                bean.setPlu(re.getString("PLU"));
                bean.setDescripcion(re.getString("DESCRIPCION"));
                bean.setPrecio(re.getFloat("PRECIO"));
                bean.setCantidad(re.getInt("SALDO"));
                bean.setSaldo(re.getInt("SALDO"));

            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return bean;
    }

    public ArrayList<ImpuestosBean> findById(long id) throws DAOException {
        ArrayList<ImpuestosBean> lista = new ArrayList<>();
        try {
            String sql = "SELECT i.id impuesto_id, i.descripcion, productos_id, iva_incluido, porcentaje_valor, valor\n"
                    + "  FROM public.productos_impuestos pi\n"
                    + "  INNER JOIN impuestos i ON i.id=pi.impuestos_id\n"
                    + "  WHERE pi.productos_id=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                ImpuestosBean bean = new ImpuestosBean();
                bean.setId(re.getLong("impuesto_id"));
                bean.setDescripcion(re.getString("descripcion"));
                bean.setIva_incluido(re.getString("iva_incluido").equals("S"));
                bean.setPorcentaje_valor(re.getString("porcentaje_valor"));
                bean.setValor(re.getFloat("valor"));
                lista.add(bean);
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return lista;
    }

    public MovimientosBean getLast(long empresaId, MovimientosBean movimiConsecutivo) throws DAOException {
        MovimientosBean movimiento = null;

        try {
            String sql = "SELECT id, empresas_id, operacion, fecha, consecutivo, persona_id, persona_nit, \n"
                    + "       persona_nombre, tercero_id, tercero_nit, tercero_nombre, costo_total, \n"
                    + "       venta_total, impuesto_total, descuento_total, origen_id, impreso, \n"
                    + "       create_user, create_date, update_user, update_date, remoto_id, \n"
                    + "       sincronizado, movimiento_estado\n"
                    + "  FROM public.movimientos WHERE empresas_id=? ORDER BY 1 DESC LIMIT 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, empresaId);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                movimiento = movimiConsecutivo;
                movimiento.setId(re.getLong("id"));
                movimiento.setFecha(re.getTimestamp("fecha"));
                movimiento.getConsecutivo().setConsecutivo_actual(re.getLong("consecutivo"));
                movimiento.setPersonaId(re.getLong("persona_id"));
                movimiento.setPersonaNit(re.getString("persona_nit"));
                movimiento.setPersonaNombre(re.getString("persona_nombre"));
                movimiento.setTerceroId(re.getLong("tercero_id"));
                movimiento.setTerceroNit(re.getString("tercero_nit"));
                movimiento.setTerceroNombre(re.getString("tercero_nombre"));
                movimiento.setCostoTotal(re.getFloat("costo_total"));
                movimiento.setVentaTotal(re.getFloat("venta_total"));
                movimiento.setImpuestoTotal(re.getFloat("impuesto_total"));
                movimiento.setDescuentoTotal(re.getFloat("descuento_total"));
                movimiento.setOrigenId(re.getLong("origen_id"));
                movimiento.setImpreso(re.getString("impreso"));
                movimiento.setCreateUser(re.getLong("create_user"));
                movimiento.setCreateDate(re.getTimestamp("create_date"));
                movimiento.setUpdateUser(re.getLong("update_user"));
                movimiento.setUpdateDate(re.getTimestamp("update_date"));
                movimiento.setRemotoId(re.getLong("remoto_id"));
                movimiento.setSincronizado(re.getInt("sincronizado"));
                movimiento.setEmpresasId(empresaId);
            }

            if (movimiento != null) {

                sql = "SELECT md.id, movimientos_id, bodegas_id, productos_id, cantidad, costo_producto, plu, descripcion, \n"
                        + "       tipo_operacion, fecha, md.precio, descuento_id, descuento_producto, \n"
                        + "       remoto_id, sincronizado, subtotal, sub_detalle_id\n"
                        + "  FROM movimientos_detalles md\n"
                        + "  INNER JOIN productos p ON p.id=md.productos_id\n"
                        + " WHERE movimientos_id=?";

                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, movimiento.getId());
                re = ps.executeQuery();

                while (re.next()) {
                    if (movimiento.getDetalles() == null) {
                        movimiento.setDetalles(new TreeMap<>());
                    }
                    MovimientosDetallesBean detalle = new MovimientosDetallesBean();
                    detalle.setId(re.getLong("id"));
                    detalle.setMovimientoId(movimiento.getId());
                    detalle.setBodegasId(re.getLong("bodegas_id"));
                    detalle.setProductoId(re.getLong("productos_id"));
                    detalle.setCantidad(re.getFloat("cantidad"));
                    detalle.setPlu(re.getString("plu"));
                    detalle.setDescripcion(re.getString("descripcion"));
                    detalle.setProducto_compuesto_costo(re.getFloat("costo_producto"));
                    detalle.setTipo_operacion(re.getInt("tipo_operacion"));
                    detalle.setFecha(re.getTimestamp("fecha"));
                    detalle.setPrecio(re.getFloat("precio"));
                    detalle.setDescuentoId(re.getLong("descuento_id"));
                    detalle.setDescuentoProducto(re.getLong("descuento_producto"));
                    detalle.setRemotoId(re.getLong("remoto_id"));
                    detalle.setSincronizado(re.getInt("sincronizado"));
                    detalle.setSaldo(re.getInt("subtotal"));
                    detalle.setSubtotal(re.getInt("subtotal"));

                    movimiento.getDetalles().put(detalle.getId(), detalle);

                }

                for (Map.Entry<Long, MovimientosDetallesBean> entry : movimiento.getDetalles().entrySet()) {
                    Long key = entry.getKey();
                    MovimientosDetallesBean detalle = entry.getValue();

                    //CARGUE DE IMPUESTOS
                    sql = "select mi.*, i.descripcion from movimientos_impuestos mi\n"
                            + "inner join impuestos i on i.id=mi.impuestos_id\n"
                            + "where mi.movimientos_detalles_id = ?";

                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, detalle.getId());
                    re = ps.executeQuery();

                    while (re.next()) {
                        if (detalle.getImpuestos() == null) {
                            detalle.setImpuestos(new ArrayList<>());
                        }
                        ImpuestosBean impuesto = new ImpuestosBean();
                        impuesto.setId(re.getLong("impuestos_id"));
                        impuesto.setDescripcion(re.getString("descripcion"));
                        impuesto.setCalculado(re.getFloat("impuesto_valor"));
                        detalle.getImpuestos().add(impuesto);
                    }
                }
            }
        } catch (PSQLException s) {
            throw new DAOException("11." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("12." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("13." + s.getMessage());
        }
        return movimiento;
    }

    public MovimientosBean getById(long movimientoId, MovimientosBean movimiConsecutivo) throws DAOException {
        MovimientosBean movimiento = null;

        try {
            String sql = "SELECT id, empresas_id, operacion, fecha, consecutivo, persona_id, persona_nit, \n"
                    + "       persona_nombre, tercero_id, tercero_nit, tercero_nombre, costo_total, \n"
                    + "       venta_total, impuesto_total, descuento_total, origen_id, impreso, \n"
                    + "       create_user, create_date, update_user, update_date, remoto_id, \n"
                    + "       sincronizado, movimiento_estado\n"
                    + "  FROM public.movimientos WHERE id=? ORDER BY 1 DESC LIMIT 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, movimientoId);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                movimiento = movimiConsecutivo;
                movimiento.setId(re.getLong("id"));
                movimiento.setFecha(re.getTimestamp("fecha"));
                movimiento.getConsecutivo().setConsecutivo_actual(re.getLong("consecutivo"));
                movimiento.setPersonaId(re.getLong("persona_id"));
                movimiento.setPersonaNit(re.getString("persona_nit"));
                movimiento.setPersonaNombre(re.getString("persona_nombre"));
                movimiento.setTerceroId(re.getLong("tercero_id"));
                movimiento.setTerceroNit(re.getString("tercero_nit"));
                movimiento.setTerceroNombre(re.getString("tercero_nombre"));
                movimiento.setCostoTotal(re.getFloat("costo_total"));
                movimiento.setVentaTotal(re.getFloat("venta_total"));
                movimiento.setImpuestoTotal(re.getFloat("impuesto_total"));
                movimiento.setDescuentoTotal(re.getFloat("descuento_total"));
                movimiento.setOrigenId(re.getLong("origen_id"));
                movimiento.setImpreso(re.getString("impreso"));
                movimiento.setCreateUser(re.getLong("create_user"));
                movimiento.setCreateDate(re.getTimestamp("create_date"));
                movimiento.setUpdateUser(re.getLong("update_user"));
                movimiento.setUpdateDate(re.getTimestamp("update_date"));
                movimiento.setRemotoId(re.getLong("remoto_id"));
                movimiento.setSincronizado(re.getInt("sincronizado"));
                movimiento.setEmpresasId(re.getLong("empresas_id"));
            }

            if (movimiento != null) {

                sql = "SELECT md.id, movimientos_id, bodegas_id, productos_id, cantidad, costo_producto, plu, descripcion, \n"
                        + "       tipo_operacion, fecha, md.precio, descuento_id, descuento_producto, \n"
                        + "       remoto_id, sincronizado, subtotal, sub_detalle_id\n"
                        + "  FROM movimientos_detalles md\n"
                        + "  INNER JOIN productos p ON p.id=md.productos_id\n"
                        + " WHERE movimientos_id=?";

                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, movimiento.getId());
                re = ps.executeQuery();

                while (re.next()) {
                    if (movimiento.getDetalles() == null) {
                        movimiento.setDetalles(new TreeMap<>());
                    }
                    MovimientosDetallesBean detalle = new MovimientosDetallesBean();
                    detalle.setId(re.getLong("id"));
                    detalle.setMovimientoId(movimiento.getId());
                    detalle.setBodegasId(re.getLong("bodegas_id"));
                    detalle.setProductoId(re.getLong("productos_id"));
                    detalle.setCantidad(re.getFloat("cantidad"));
                    detalle.setPlu(re.getString("plu"));
                    detalle.setDescripcion(re.getString("descripcion"));
                    detalle.setProducto_compuesto_costo(re.getFloat("costo_producto"));
                    detalle.setTipo_operacion(re.getInt("tipo_operacion"));
                    detalle.setFecha(re.getTimestamp("fecha"));
                    detalle.setPrecio(re.getFloat("precio"));
                    detalle.setDescuentoId(re.getLong("descuento_id"));
                    detalle.setDescuentoProducto(re.getLong("descuento_producto"));
                    detalle.setRemotoId(re.getLong("remoto_id"));
                    detalle.setSincronizado(re.getInt("sincronizado"));
                    detalle.setSaldo(re.getInt("subtotal"));
                    detalle.setSubtotal(re.getInt("subtotal"));

                    movimiento.getDetalles().put(detalle.getId(), detalle);

                }

                for (Map.Entry<Long, MovimientosDetallesBean> entry : movimiento.getDetalles().entrySet()) {
                    Long key = entry.getKey();
                    MovimientosDetallesBean detalle = entry.getValue();

                    //CARGUE DE IMPUESTOS
                    sql = "select mi.*, i.descripcion from movimientos_impuestos mi\n"
                            + "inner join impuestos i on i.id=mi.impuestos_id\n"
                            + "where mi.movimientos_detalles_id = ?";

                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, detalle.getId());
                    re = ps.executeQuery();

                    while (re.next()) {
                        if (detalle.getImpuestos() == null) {
                            detalle.setImpuestos(new ArrayList<>());
                        }
                        ImpuestosBean impuesto = new ImpuestosBean();
                        impuesto.setId(re.getLong("impuestos_id"));
                        impuesto.setDescripcion(re.getString("descripcion"));
                        impuesto.setCalculado(re.getFloat("impuesto_valor"));
                        detalle.getImpuestos().add(impuesto);
                    }
                }
            }
        } catch (PSQLException s) {
            throw new DAOException("11." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("12." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("13." + s.getMessage());
        }
        return movimiento;
    }

    public MovimientosBean create(MovimientosBean movimiento, CredencialBean cr) throws DAOException {

        try {
            String sql = "INSERT INTO movimientos(\n"
                    + "            id, empresas_id, operacion, fecha, consecutivo, persona_id, persona_nit, \n"
                    + "            persona_nombre, tercero_id, tercero_nit, tercero_nombre, costo_total, \n"
                    + "            venta_total, impuesto_total, descuento_total, origen_id, impreso, \n"
                    + "            create_user, create_date, update_user, update_date, remoto_id, \n"
                    + "            sincronizado, movimiento_estado)\n"
                    + "    VALUES (nextval('movimientos_id'), ?, ?, ?, ?, ?, ?, \n"
                    + "            ?, ?, ?, ?, ?, \n"
                    + "            ?, ?, ?, ?, ?, \n"
                    + "            ?, ?, NULL, NULL, CURRVAL('movimientos_id'), \n"
                    + "            0, 0) RETURNING ID";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, movimiento.getEmpresasId());
            ps.setLong(2, movimiento.getOperacionId());
            ps.setTimestamp(3, new Timestamp(movimiento.getFecha().getTime()));

            if (movimiento.getConsecutivo() == null) {
                ps.setLong(4, 0);
            } else {
                ps.setLong(4, movimiento.getConsecutivo().getConsecutivo_actual());
            }

            ps.setLong(5, movimiento.getPersonaId());
            ps.setString(6, movimiento.getPersonaNit());
            ps.setString(7, movimiento.getPersonaNombre());

            if (movimiento.getTerceroId() != 0) {
                ps.setLong(8, movimiento.getTerceroId());
                ps.setString(9, movimiento.getTerceroNit());
                ps.setString(10, movimiento.getTerceroNombre());
            } else {
                ps.setNull(8, Types.NULL);
                ps.setNull(9, Types.NULL);
                ps.setNull(10, Types.NULL);
            }

            ps.setFloat(11, movimiento.getCostoTotal());
            ps.setFloat(12, movimiento.getVentaTotal());
            ps.setFloat(13, movimiento.getImpuestoTotal());
            ps.setFloat(14, movimiento.getDescuentoTotal());
            ps.setLong(15, Main.credencial.getId());
            ps.setString(16, "N");
            ps.setLong(17, movimiento.getCreateUser());
            ps.setTimestamp(18, new Timestamp(movimiento.getCreateDate().getTime()));

            ResultSet re = ps.executeQuery();

            while (re.next()) {
                movimiento.setId(re.getLong("id"));
                movimiento.setRemotoId(re.getLong("id"));
            }

            if (movimiento.getId() != 0) {

                for (Map.Entry<Long, MovimientosDetallesBean> entry : movimiento.getDetalles().entrySet()) {
                    Long key = entry.getKey();
                    MovimientosDetallesBean value = entry.getValue();

                    sql = "INSERT INTO movimientos_detalles(\n"
                            + "            movimientos_id, bodegas_id, productos_id, cantidad, costo_producto, \n"
                            + "            tipo_operacion, fecha, precio, descuento_id, descuento_producto, \n"
                            + "            remoto_id, sincronizado, subtotal)\n"
                            + "    VALUES (?, ?, ?, ?, ?, \n"
                            + "            ?, ?, ?, ?, ?, \n"
                            + "            ?, 0, ?) RETURNING ID";

                    ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setLong(1, movimiento.getId());
                    ps.setLong(2, movimiento.getBodegaId());
                    ps.setLong(3, value.getProductoId());
                    ps.setFloat(4, value.getCantidad());
                    ps.setFloat(5, value.getProducto_compuesto_costo());
                    ps.setFloat(6, value.getTipo_operacion());
                    ps.setTimestamp(7, new Timestamp(movimiento.getCreateDate().getTime()));
                    ps.setFloat(8, value.getPrecio());
                    ps.setLong(9, value.getDescuentoId());
                    ps.setFloat(10, value.getDescuentoProducto());
                    ps.setFloat(11, cr.getId());
                    ps.setFloat(12, value.getSubtotal());

                    re = ps.executeQuery();
                    while (re.next()) {
                        value.setId(re.getLong("id"));
                        value.setRemotoId(re.getLong("id"));
                    }
                    if (value.isCompuesto()) {
                        for (ProductoBean ingrediente : value.getIngredientes()) {
                            sql = "UPDATE BODEGAS_PRODUCTOS SET SALDO=SALDO-? WHERE bodegas_id=? and productos_id=?";
                            ps = NeoService.obtenerConexion().prepareStatement(sql);
                            ps.setFloat(1, (ingrediente.getProducto_compuesto_cantidad() * value.getCantidad()));
                            ps.setLong(2, movimiento.getBodegaId());
                            ps.setLong(3, ingrediente.getId());
                            ps.executeUpdate();
                        }
                    } else {
                        sql = "UPDATE BODEGAS_PRODUCTOS SET SALDO=SALDO-? WHERE bodegas_id=? and productos_id=?";
                        ps = NeoService.obtenerConexion().prepareStatement(sql);
                        ps.setFloat(1, value.getCantidad());
                        ps.setLong(2, movimiento.getBodegaId());
                        ps.setLong(3, value.getProductoId());
                        ps.executeUpdate();
                    }
                    if (value.getImpuestos() != null) {
                        for (ImpuestosBean impuesto : value.getImpuestos()) {
                            sql = "INSERT INTO movimientos_impuestos(\n"
                                    + "            id, impuestos_id, movimientos_detalles_id, impuesto_valor)\n"
                                    + "    VALUES (nextval('movimientos_impuestos_id'), ?, ?, ?);";
                            ps = NeoService.obtenerConexion().prepareStatement(sql);
                            ps.setLong(1, impuesto.getId());
                            ps.setLong(2, value.getId());
                            ps.setFloat(3, impuesto.getCalculado());
                            ps.executeUpdate();
                        }
                    }
                }
                movimiento.setSuccess(true);
                if (movimiento.getConsecutivo() != null) {
                    sql = "UPDATE CONSECUTIVOS SET CONSECUTIVO_ACTUAL=CONSECUTIVO_ACTUAL + 1 WHERE ID = ?";
                    ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, movimiento.getConsecutivo().getId());
                    ps.executeUpdate();
                } else {
                    ConsecutivoBean cons = new ConsecutivoBean();
                    cons.setConsecutivo_actual(1);
                    cons.setConsecutivo_inicial(1);
                    cons.setConsecutivo_inicial(99999999);
                    movimiento.setConsecutivo(cons);
                }
            }
        } catch (PSQLException s) {
            throw new DAOException("11." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("12." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("13." + s.getMessage());
        }
        return movimiento;
    }

    public void integrarProdcto(ProductoBean bean, ProductoBean ingrediente) throws DAOException {

        try {
            String sql = "INSERT INTO public.productos_compuestos(\n"
                    + "            id, productos_id, ingredientes_id, cantidad)\n"
                    + "    VALUES (?, ?, ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, ingrediente.getProducto_compuesto_id());
            ps.setLong(2, bean.getId());
            ps.setLong(3, ingrediente.getId());
            ps.setFloat(4, ingrediente.getProducto_compuesto_cantidad());
            ps.executeUpdate();
        } catch (PSQLException s) {
            if (s.getMessage().contains("productos_compuestos_pk")) {
                try {
                    String sql = "UPDATE public.productos_compuestos\n"
                            + "   SET productos_id=?, ingredientes_id=?, cantidad=?\n"
                            + " WHERE id=?";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setLong(1, bean.getId());
                    ps.setLong(2, ingrediente.getId());
                    ps.setFloat(3, ingrediente.getProducto_compuesto_cantidad());
                    ps.setFloat(4, ingrediente.getProducto_compuesto_id());
                    ps.executeUpdate();

                } catch (SQLException a) {
                    NeoService.setLog(s.getMessage());
                }
            } else {
                throw new DAOException(s.getMessage());
            }
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
    }

    public TreeMap<Long, CategoriaBean> getInventario() throws DAOException {

        TreeMap<Long, CategoriaBean> lista = new TreeMap<>();
        try {
            String sql = "SELECT G.ID GRUPO_ID, G.GRUPO GRUPO_DESC, P.ID, P.PLU, P.DESCRIPCION, SALDO\n"
                    + "FROM PRODUCTOS P\n"
                    + "LEFT JOIN GRUPOS_ENTIDAD GD ON GD.ENTIDAD_ID=P.ID\n"
                    + "LEFT JOIN GRUPOS G ON GD.GRUPO_ID=G.ID \n"
                    + "LEFT JOIN BODEGAS_PRODUCTOS BP ON BP.PRODUCTOS_ID=P.ID\n"
                    + "WHERE GRUPOS_TIPOS_ID=1 OR GRUPOS_TIPOS_ID IS NULL ORDER BY PLU ";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();

            while (re.next()) {

                long categoria = re.getLong("GRUPO_ID");

                if (lista.get(categoria) == null) {
                    ArrayList<ProductoBean> productos = new ArrayList<>();
                    CategoriaBean cat = new CategoriaBean(productos);
                    cat.setId(categoria);
                    if (re.getString("GRUPO_DESC") != null) {
                        cat.setGrupo(re.getString("GRUPO_DESC"));
                    } else {
                        cat.setGrupo(Butter.SIN_GRUPO);
                    }
                    lista.put(categoria, cat);
                }
                ProductoBean bean = new ProductoBean();
                bean.setId(re.getLong("id"));
                bean.setPlu(re.getString("plu"));
                bean.setDescripcion(re.getString("descripcion"));
                bean.setSaldo(re.getFloat("saldo"));
                lista.get(categoria).setTotales(lista.get(categoria).getTotales() + bean.getSaldo());
                lista.get(categoria).getProductos().add(bean);
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return lista;

    }

    public LinkedHashSet<ProductoBean> getSaldoBodeda() throws DAOException {

        LinkedHashSet<ProductoBean> lista = new LinkedHashSet<>();
        try {
            String sql = "select array_to_string(ARRAY( \n"
                    + "	SELECT  G.GRUPO GRUPO_DESC\n"
                    + "	FROM PRODUCTOS P\n"
                    + "	LEFT JOIN GRUPOS_ENTIDAD GD ON GD.ENTIDAD_ID=P.ID\n"
                    + "	LEFT JOIN GRUPOS G ON GD.GRUPO_ID=G.ID \n"
                    + "	WHERE P.ID=px.id\n"
                    + "), ', ') grupos, px.*, saldo FROM productos PX \n"
                    + "LEFT JOIN BODEGAS_PRODUCTOS BP ON BP.PRODUCTOS_ID=PX.ID\n"
                    + "order by NULLIF(PX.plu, '0')::int";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();

            while (re.next()) {

                ProductoBean bean = new ProductoBean();
                bean.setId(re.getLong("id"));
                bean.setPlu(re.getString("plu"));
                bean.setDescripcion(re.getString("descripcion"));
                bean.setSaldo(re.getFloat("saldo"));
                bean.setGrupos(re.getString("grupos"));
                lista.add(bean);
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return lista;

    }

    public ReporteJornadaBean getLastCierreByPersona(PersonaBean promotor) throws DAOException {
        ReporteJornadaBean bean = null;
        try {
            String sql = "select * from jornadas_hist where personas_id=? order by id desc limit 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, promotor.getId());
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean = new ReporteJornadaBean();
                bean.setId(re.getLong("id"));
                bean.setJornadaId(re.getLong("jornada_id"));
                bean.setInicio(re.getTimestamp("fecha_inicio"));
                bean.setFin(re.getTimestamp("fecha_fin"));
            }

            if (bean != null) {
                sql = "select * from movimientos \n"
                        + "where \n"
                        + "persona_id=? and fecha>=? and fecha<=? ";
                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, promotor.getId());
                ps.setTimestamp(2, new Timestamp(bean.getInicio().getTime()));
                ps.setTimestamp(3, new Timestamp(bean.getFin().getTime()));
                re = ps.executeQuery();

                while (re.next()) {
                    if (re.getString("impreso").equals("N")) {
                        bean.setImpresos(bean.getImpresos() + 1);
                    } else {
                        bean.setReimpresos(bean.getReimpresos() + 1);
                    }
                    bean.setNumeroVentas(bean.getNumeroVentas() + 1);
                }

            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return bean;
    }

    public void limpiarIngredientes(long productoId) throws DAOException {
        try {
            String sql = "DELETE FROM productos_compuestos WHERE productos_id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, productoId);
            ps.executeUpdate();

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
    }

    public ArrayList<MovimientosBean> findMovimientos() throws DAOException {

        ArrayList<MovimientosBean> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM MOVIMIENTOS";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                MovimientosBean movimiento = new MovimientosBean();
                movimiento.setId(re.getLong("id"));
                movimiento.setFecha(re.getTimestamp("fecha"));
                movimiento.setConsecutivo(new ConsecutivoBean());
                movimiento.getConsecutivo().setConsecutivo_actual(re.getLong("consecutivo"));
                movimiento.setPersonaId(re.getLong("persona_id"));
                movimiento.setPersonaNit(re.getString("persona_nit"));
                movimiento.setPersonaNombre(re.getString("persona_nombre"));
                movimiento.setTerceroId(re.getLong("tercero_id"));
                movimiento.setTerceroNit(re.getString("tercero_nit"));
                movimiento.setTerceroNombre(re.getString("tercero_nombre"));
                movimiento.setCostoTotal(re.getFloat("costo_total"));
                movimiento.setVentaTotal(re.getFloat("venta_total"));
                movimiento.setImpuestoTotal(re.getFloat("impuesto_total"));
                movimiento.setDescuentoTotal(re.getFloat("descuento_total"));
                movimiento.setOrigenId(re.getLong("origen_id"));
                movimiento.setImpreso(re.getString("impreso"));
                movimiento.setCreateUser(re.getLong("create_user"));
                movimiento.setCreateDate(re.getTimestamp("create_date"));
                movimiento.setUpdateUser(re.getLong("update_user"));
                movimiento.setUpdateDate(re.getTimestamp("update_date"));
                movimiento.setRemotoId(re.getLong("remoto_id"));
                movimiento.setSincronizado(re.getInt("sincronizado"));

                lista.add(movimiento);
            }

        } catch (PSQLException s) {
            throw new DAOException(s.getMessage());
        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return lista;
    }
}
