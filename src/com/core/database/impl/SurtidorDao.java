package com.core.database.impl;

import com.butter.bean.CatalogoBean;
import com.butter.bean.EmpresaBean;
import com.butter.bean.Main;
import com.neo.app.bean.MediosPagosBean;
import com.butter.bean.PredeterminadaBean;
import com.butter.bean.SaltoLecturaBean;
import com.butter.bean.TareaProgramada;
import com.butter.bean.Utils;
import com.google.gson.JsonObject;
import com.core.app.NeoService;
import com.core.app.protocols.DispositivoController;
import com.neo.app.bean.ProductoBean;
import com.neo.app.bean.AConstant;
import com.neo.app.bean.Autorizacion;
import com.neo.app.bean.Cara;
import com.neo.app.bean.Criterio;
import com.neo.app.bean.Manguera;
import com.neo.app.bean.MedioPago;
import com.neo.app.bean.Persona;
import com.neo.app.bean.Precio;
import com.neo.app.bean.Recibo;
import com.neo.app.bean.ResponseSicom;
import com.neo.app.bean.Surtidor;
import com.neo.app.bean.Surtidor.SURTIDORES_TIPO;
import com.neo.app.bean.Totalizador;
import com.neo.app.bean.Venta;
import com.neo.app.bean.VentaDetalles;
import com.core.database.DAOException;
import com.core.turnos.bean.SurtidorInventario;
import com.core.turnos.bean.TurnosResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.util.PSQLException;

public class SurtidorDao {

    static final String ESTADO_ANULADA = "X";
    static final String ESTADO_REENVIAR_LIBERACION = "R";
    static final String ESTADO_MANY_REQUEST_LIBERACION = "L";
    SimpleDateFormat sdf = new SimpleDateFormat(AConstant.FORMAT_DATETIME_SQL);

    public EmpresaBean getDatosEmpresaBean(String mac) {
        EmpresaBean empresa = null;
        try {
            String sql = "SELECT \n"
                    + "E.NIT, RAZON_SOCIAL, \n"
                    + "DIRECCION, TELEFONO,\n"
                    + "I.DESCRIPCION ISLA,\n"
                    + "SURTIDOR \n"
                    + "FROM SURTIDORES S\n"
                    + "INNER JOIN ISLAS I ON I.ID=S.ISLAS_ID\n"
                    + "INNER JOIN SUCURSALES E ON E.ID=I.SUCURSALES_ID\n"
                    + "WHERE S.MAC=? ";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, mac);
            ResultSet re = ps.executeQuery();
            if (re.next()) {
                empresa = new EmpresaBean();
                empresa.setRazonSocial(re.getString("RAZON_SOCIAL"));
                empresa.setNit(re.getString("NIT"));
                empresa.setDireccion(re.getString("NIT"));
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return empresa;
    }

    public EmpresaBean getDatosEmpresa() {
        EmpresaBean empresa = null;
        try {

            String sql = "SELECT * FROM EMPRESAS ORDER BY 1 DESC LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                empresa = new EmpresaBean();
                empresa.setRazonSocial(re.getString("RAZON_SOCIAL"));
                empresa.setNit(re.getString("NIT"));
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return empresa;
    }

    public void getDebugEstado(Surtidor surtidor) {
        try {
            String sql = "SELECT debug_tramas, debug_estado FROM surtidores WHERE surtidor=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, surtidor.getId());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                surtidor.setDebugEstado(re.getString("debug_estado") != null ? re.getString("debug_estado").equals("S") : false);
                surtidor.setDebugTrama(re.getString("debug_tramas") != null ? re.getString("debug_tramas").equals("S") : false);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public TreeMap<Integer, Surtidor> getSurtidor(String numero) throws DAOException {
        TreeMap<Integer, Surtidor> surtidores = new TreeMap<>();
        try {
            ResultSet re;
            if (!numero.equals("*")) {
                String sql = "SELECT \n"
                        + "sc.id configuracion_id,\n"
                        + "s.id, s.surtidor, s.islas_id, s.estado, s.surtidores_tipos_id, s.surtidores_protocolos_id, s.lector_ip, s.lector_port, s.tiene_echo, \n"
                        + "s.mac, s.ip, s.port, s.token, s.factor_volumen_parcial, s.factor_importe_parcial, s.factor_precio, s.factor_inventario, sc.estado estado_manguera, \n"
                        + "sc.cara, sc.manguera, sc.grado, sc.productos_id, sc.acumulado_venta, sc.acumulado_cantidad, sc.lector_puerto, s.controlador, p.familias \n"
                        + "FROM SURTIDORES S \n"
                        + "INNER JOIN surtidores_detalles SC ON S.ID=SC.SURTIDORES_ID\n"
                        + "INNER JOIN productos p on p.id=sc.productos_id\n"
                        + "WHERE S.estado='A' and S.surtidor in (" + numero + ") order by sc.id";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                re = ps.executeQuery();
            } else {
                String sql = "SELECT \n"
                        + "sc.id configuracion_id,\n"
                        + "s.id, s.surtidor, s.islas_id, s.estado, s.surtidores_tipos_id, s.surtidores_protocolos_id, s.lector_ip, s.lector_port, s.tiene_echo, \n"
                        + "s.mac, s.ip, s.port, s.token, s.factor_volumen_parcial, s.factor_importe_parcial, s.factor_precio, s.factor_inventario, sc.estado estado_manguera, \n"
                        + "sc.cara, sc.manguera, sc.grado, sc.productos_id, sc.acumulado_venta, sc.acumulado_cantidad, sc.lector_puerto, s.controlador, p.familias\n"
                        + "FROM SURTIDORES S \n"
                        + "INNER JOIN surtidores_detalles SC ON S.ID=SC.SURTIDORES_ID\n"
                        + "INNER JOIN productos p on p.id=sc.productos_id\n"
                        + "WHERE S.estado='A' order by sc.id";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                re = ps.executeQuery();
            }

            while (re.next()) {
                Surtidor surtidor;

                if (surtidores.get(re.getInt("surtidor")) == null) {

                    int factor_volumen = re.getInt("factor_volumen_parcial");
                    int factor_importe = re.getInt("factor_importe_parcial");
                    int factor_precio = re.getInt("factor_precio");
                    int factor_invetario = re.getInt("factor_inventario");

                    NeoService.sharedPreference.put(NeoService.FACTOR_VOLUMEN + re.getInt("surtidor"), factor_volumen);
                    NeoService.sharedPreference.put(NeoService.FACTOR_IMPORTE + re.getInt("surtidor"), factor_importe);
                    NeoService.sharedPreference.put(NeoService.FACTOR_PRECIO + re.getInt("surtidor"), factor_precio);
                    NeoService.sharedPreference.put(NeoService.FACTOR_INVENTARIO + re.getInt("surtidor"), factor_invetario);

                    surtidor = new Surtidor(factor_volumen, factor_precio, factor_importe, factor_invetario);
                    surtidor.setId(re.getInt("surtidor"));
                    surtidor.setUniqueId(re.getInt("id"));
                    surtidor.setLectorIp(re.getString("lector_ip"));
                    surtidor.setLectorPort(re.getInt("lector_port"));
                    surtidor.setIslaId(re.getInt("islas_id"));

                    surtidor.setIp(re.getString("ip"));
                    surtidor.setPort(re.getInt("port"));
                    surtidor.setEcho(re.getString("tiene_echo").equals("S"));
                    surtidor.setControlador(re.getInt("controlador"));

                    surtidor.setTipo(SURTIDORES_TIPO.fromId(re.getInt("surtidores_tipos_id")));
                    NeoService.setLog(" is tipo sur: " + re.getInt("surtidores_tipos_id"));

                } else {
                    surtidor = surtidores.get(re.getInt("surtidor"));
                }

                if (surtidor.getTipo() == Surtidor.SURTIDORES_TIPO.GAS) {

                    Manguera manguera = new Manguera();
                    manguera.setId(re.getInt("manguera"));
                    manguera.setGrado(0);
                    manguera.setProductoId(re.getLong("productos_id"));
                    manguera.setProductoFamiliaId(re.getLong("familias"));
                    manguera.setConfiguracionId(re.getLong("configuracion_id"));
                    manguera.setLectorPort(re.getInt("lector_puerto"));
                    manguera.setEstado(Surtidor.SURTIDORES_ESTADO.fromId(re.getInt("estado_manguera")));
                    surtidor.getMangueras().put(manguera.getId(), manguera);

                } else {
                    NeoService.setLog("El surtidor es de GASOLINA");
                    Cara cara;
                    if (surtidor.getCaras().get(re.getInt("cara")) == null) {
                        cara = new Cara();
                        cara.setNumero(re.getInt("cara"));
                        surtidor.getCaras().put(cara.getNumero(), cara);
                    } else {
                        cara = surtidor.getCaras().get(re.getInt("cara"));
                    }

                    Manguera manguera;
                    if (cara.getMangueras().get(re.getInt("manguera")) == null) {
                        manguera = new Manguera();
                        manguera.setId(re.getInt("manguera"));
                        cara.getMangueras().put(manguera.getId(), manguera);
                    } else {
                        manguera = cara.getMangueras().get(re.getInt("manguera"));
                    }
                    manguera.setLectorPort(re.getInt("lector_puerto"));
                    manguera.setGrado(re.getInt("grado"));
                    manguera.setProductoId(re.getLong("productos_id"));
                    manguera.setProductoFamiliaId(re.getLong("familias"));
                    manguera.setConfiguracionId(re.getLong("configuracion_id"));
                    manguera.setEstado(Surtidor.SURTIDORES_ESTADO.fromId(re.getInt("estado_manguera")));
                }

                surtidores.put(surtidor.getId(), surtidor);
            }

        } catch (SQLException s) {
            throw new DAOException(s.getMessage());
        } catch (Exception s) {
            throw new DAOException(s.getMessage());
        }
        return surtidores;
    }

    public LinkedList<Venta> getVentasSinRegistrar() {
        LinkedList<Venta> lista = new LinkedList<>();
        try {
            String sql = "SELECT v.id, jornada_id, tipo_origen_id, origen_id, v.cara, \n"
                    + "v.manguera, v.grado, operario_id, cliente_id, fecha_inicio, fecha_fin, \n"
                    + "total, impuesto, sincronizado, placa, impresion, sd.surtidor\n"
                    + "FROM ventas v\n"
                    + "INNER JOIN surtidores_detalles sd ON sd.id=v.origen_id\n"
                    + "WHERE sincronizado = 0";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                Venta venta = new Venta();
                venta.setId(re.getLong("id"));
                venta.setJornadaId(re.getLong("jornada_id"));
                venta.setSurtidorId(re.getInt("surtidor"));
                venta.setSurtidorTipoId(Venta.ORIGEN_TIPO.fromId(re.getInt("tipo_origen_id")));
                venta.setOrigenId(re.getInt("origen_id"));
                venta.setCara(re.getInt("cara"));
                venta.setManguera(re.getInt("manguera"));
                venta.setGrado(re.getInt("grado"));
                venta.setOperadorId(re.getLong("operario_id"));
                venta.setClienteId(re.getLong("cliente_id"));
                venta.setFechaInicio(re.getTimestamp("fecha_inicio"));
                venta.setFechaFin(re.getTimestamp("fecha_fin"));
                venta.setActualImporte(re.getLong("total"));
                venta.setImpuesto(re.getLong("impuesto"));
                venta.setPlaca(re.getString("placa"));
                venta.setImpresion(re.getString("impresion"));
                venta.setSincronizado(re.getInt("sincronizado"));
                venta.setDetalles(getDetallesVenta(re.getLong("id")));
                lista.add(venta);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    public LinkedList<Venta> getVentasSinRegistrarWhitToken() {
        LinkedList<Venta> lista = new LinkedList<>();
        try {
            String sql = "SELECT v.id venta_id, jornada_id, tipo_origen_id, origen_id, v.cara, "
                    + " v.manguera, v.grado, operario_id, vd.cantidad_precisa cantidad, cliente_id, "
                    + " fecha_inicio, fecha_fin, "
                    + " v.total, impuesto, v.sincronizado, placa, impresion, sd.surtidor, "
                    + " a.id auto_id, codigo, proveedores_id, preventa, "
                    + " documento_identificacion_cliente, documento_identificacion_conductor, placa_vehiculo, "
                    + " precio_unidad, porcentaje_descuento_cliente, monto_maximo, cantidad_maxima, usado, "
                    + " fecha_servidor, fecha_creacion, fecha_uso, metodo_pago, medio_autorizacion, "
                    + " serial_dispositivo, conductor_nombre, cliente_nombre, vehiculo_odometro, "
                    + " trama, codigo_tercero "
                    + "FROM ventas v "
                    + "INNER JOIN ventas_detalles vd ON vd.ventas_id=v.id "
                    + "INNER JOIN surtidores_detalles sd ON sd.id=v.origen_id "
                    + "INNER JOIN transacciones a ON a.id = v.token_process_id "
                    + "WHERE v.sincronizado = 0 limit 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                Venta venta = new Venta();
                venta.setId(re.getLong("venta_id"));
                venta.setJornadaId(re.getLong("jornada_id"));
                venta.setSurtidorId(re.getInt("surtidor"));
                venta.setSurtidorTipoId(Venta.ORIGEN_TIPO.fromId(re.getInt("tipo_origen_id")));
                venta.setOrigenId(re.getInt("origen_id"));
                venta.setCara(re.getInt("cara"));
                venta.setManguera(re.getInt("manguera"));
                venta.setGrado(re.getInt("grado"));
                venta.setOperadorId(re.getLong("operario_id"));
                venta.setClienteId(re.getLong("cliente_id"));
                venta.setFechaInicio(re.getTimestamp("fecha_inicio"));
                venta.setFechaFin(re.getTimestamp("fecha_fin"));
                venta.setActualImporte(re.getLong("total"));
                venta.setImpuesto(re.getLong("impuesto"));
                venta.setActualVolumen(re.getLong("cantidad"));
                venta.setPlaca(re.getString("placa"));
                venta.setImpresion(re.getString("impresion"));
                venta.setSincronizado(re.getInt("sincronizado"));
                venta.setDetalles(getDetallesVenta(re.getLong("venta_id")));

                Autorizacion aut = new Autorizacion();
                aut.setToken(re.getString("auto_id"));
                aut.setToken(re.getString("codigo"));
                aut.setDocumentoIdentificacionCliente(re.getString("documento_identificacion_cliente"));
                aut.setDocumentoIdentificacionConductor(re.getString("documento_identificacion_conductor"));
                aut.setPlacaVehiculo(re.getString("placa_vehiculo"));
                aut.setPrecioUnidad(re.getDouble("precio_unidad"));
                aut.setPorcentajeDescuentoCliente(re.getDouble("porcentaje_descuento_cliente"));
                aut.setMontoMaximo(re.getDouble("monto_maximo"));
                aut.setCantidadMaxima(re.getDouble("cantidad_maxima"));

                venta.getDetalles().setAutorizacion(aut);

                lista.add(venta);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    public VentaDetalles getDetallesVenta(long id) {
        VentaDetalles detalle = null;
        try {
            String sql = "SELECT \n"
                    + "ventas_id, vd.id, productos_id, cantidad, vd.precio, total, sincronizado, familias\n"
                    + "FROM ventas_detalles vd\n"
                    + "INNER JOIN productos p ON p.id=productos_id\n"
                    + "WHERE sincronizado = 0 and ventas_id=?";

            PreparedStatement ps2 = NeoService.obtenerConexion().prepareStatement(sql);
            ps2.setLong(1, id);
            ResultSet re = ps2.executeQuery();

            while (re.next()) {
                detalle = new VentaDetalles();
                detalle.setId(re.getLong("id"));
                detalle.setProductoId(re.getLong("productos_id"));
                detalle.setCantidad(re.getLong("cantidad"));
                detalle.setPrecio(re.getLong("precio"));
                detalle.setTotal(re.getLong("total"));
                detalle.setFamiliaId(re.getLong("familias"));
                detalle.setSincronizado(re.getInt("sincronizado"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detalle;
    }

    public VentaDetalles getVentaCara(int surtidor, int cara, int existe, String token) {
        VentaDetalles detalle = null;
        try {
            String sql = "select s.id s_id, s.surtidor, sd.cara, sd.manguera, sd.estado, \n"
                    + "sd.productos_id, p.descripcion,\n"
                    + "vc.origen_id configuracion_id, vc.total vc_total, vc.cantidad vc_cantidad, vc.importe vc_precio\n"
                    + "from surtidores s\n"
                    + "inner join surtidores_detalles sd on s.id=sd.surtidores_id\n"
                    + "inner join productos p on p.id= sd.productos_id \n"
                    + "left join ventas_curso vc on vc.origen_id=sd.id \n"
                    + "where s.surtidor=? and sd.cara=? and token_process_id=(select id from transacciones where codigo=? and proveedores_id=1 )";
            //2 EXISTE EN HISTORICO
            if (existe == 2) {
                sql = "select  s.id s_id, s.surtidor, sd.cara, sd.manguera, sd.estado, \n"
                        + "sd.productos_id, p.descripcion,\n"
                        + "vc.origen_id configuracion_id, vc.total vc_total, vd.cantidad_precisa vc_cantidad, vd.precio vc_precio\n"
                        + "from surtidores s\n"
                        + "inner join surtidores_detalles sd on s.id=sd.surtidores_id\n"
                        + "inner join productos p on p.id= sd.productos_id \n"
                        + "left join ventas vc on vc.origen_id=sd.id \n"
                        + "left join ventas_detalles vd on vd.ventas_id=vc.id "
                        + "where s.surtidor=? and sd.cara=? and token_process_id=(select id from transacciones where codigo=? and proveedores_id=1 )";
            }
            PreparedStatement ps2 = NeoService.obtenerConexion().prepareStatement(sql);
            ps2.setLong(1, surtidor);
            ps2.setLong(2, cara);
            ps2.setString(3, token);
            ResultSet re = ps2.executeQuery();
            while (re.next()) {
                try {
                    if (re.getLong("vc_cantidad") != 0) {

                        detalle = new VentaDetalles();
                        detalle.setConfiguracionId(re.getLong("configuracion_id"));
                        detalle.setCantidad(re.getLong("vc_cantidad"));
                        detalle.setPrecio(re.getLong("vc_precio"));
                        detalle.setTotal(re.getLong("vc_total"));
                    }
                } catch (SQLException a) {
                    detalle = new VentaDetalles();
                    detalle.setCantidad(0);
                    detalle.setPrecio(0);
                    detalle.setTotal(0);
                }
            }
            if (detalle != null) {
                sql = "SELECT * FROM AUTORIZACIONES WHERE CODIGO =? ";

                ps2 = NeoService.obtenerConexion().prepareStatement(sql);
                ps2.setString(1, token);
                re = ps2.executeQuery();

                while (re.next()) {
                    Autorizacion aut = new Autorizacion();
                    aut.setDocumentoIdentificacionCliente(re.getString("documento_identificacion_cliente"));
                    aut.setDocumentoIdentificacionConductor(re.getString("documento_identificacion_conductor"));
                    aut.setPlacaVehiculo(re.getString("placa_vehiculo"));
                    aut.setPrecioUnidad(re.getDouble("precio_unidad"));
                    aut.setPorcentajeDescuentoCliente(re.getDouble("porcentaje_descuento_cliente"));
                    aut.setMontoMaximo(re.getDouble("monto_maximo"));
                    aut.setCantidadMaxima(re.getDouble("cantidad_maxima"));
                    detalle.setAutorizacion(aut);
                }
            }
        } catch (SQLException e) {
        }
        return detalle;
    }

    public long setEstadoVenta(Venta venta, Surtidor.SURTIDORES_ESTADO estado) {
        try {
            if (venta.getId() == 0 && estado == Surtidor.SURTIDORES_ESTADO.DEPACHO) {
                String sql = "SELECT id FROM ventas_curso WHERE origen_id = ? and tipo_origen_id = ? and manguera = ?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, venta.getConfiguracionId());
                ps.setInt(2, venta.getSurtidorTipoId().getNumVal());
                ps.setInt(3, venta.getManguera());
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    venta.setId(re.getLong("id"));
                }
            }

            if (venta.getId() == 0 && estado != Surtidor.SURTIDORES_ESTADO.VENTA_FINALIZADA) {
                String sql = "INSERT INTO ventas_curso(\n"
                        + "            id, tipo_origen_id, origen_id, operario_id, cliente_id, surtidor, cara, \n"
                        + "            manguera, grado, productos_id, cantidad, importe, total, fecha_inicio, \n"
                        + "            fecha_fin, acumulado_cantidad, acumulado_importe, sincronizado, jornada_id, placa, token_process_id)\n"
                        + "    VALUES (nextval('ventas_curso_id'), ?, ?, ?, ?, ?, ?, \n"
                        + "            ?, ?, ?, ?, ?, ?, ?, \n"
                        + "            ?, ?, ?, ?, ?, ?, ?) RETURNING currval('ventas_curso_id') ";

                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, venta.getSurtidorTipoId().getNumVal());
                ps.setLong(2, venta.getConfiguracionId());
                ps.setLong(3, venta.getOperadorId());
                ps.setLong(4, venta.getClienteId());
                ps.setLong(5, venta.getSurtidorId());
                ps.setLong(6, venta.getCara());
                ps.setLong(7, venta.getManguera());
                ps.setLong(8, venta.getGrado());
                ps.setLong(9, venta.getProductoId());
                ps.setDouble(10, venta.getActualVolumen());
                ps.setDouble(11, venta.getActualPrecio());
                ps.setDouble(12, venta.getActualImporte());
                ps.setTimestamp(13, new Timestamp(new Date().getTime()));
                ps.setTimestamp(14, new Timestamp(new Date().getTime()));
                ps.setLong(15, venta.getAcumuladoVolumenInicial());
                ps.setLong(16, venta.getAcumuladoImporteInicial());
                ps.setLong(17, 0);
                ps.setLong(18, venta.getJornadaId());
                ps.setString(19, venta.getPlaca());
                if (venta.getAutorizacionToken() != null && esCreditoAutorizacion(venta.getAutorizacionToken().getId())) {
                    ps.setLong(20, venta.getAutorizacionToken().getId());
                } else {
                    ps.setNull(20, Types.NULL);
                }
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    venta.setId(re.getLong(1));
                }
            } else if (estado == Surtidor.SURTIDORES_ESTADO.VENTA_FINALIZADA) {

                if (venta.getId() == 0) {
                    NeoService.setLog("INTENTO DE RECUPERAR LA VENTA.");

                    String sql = "SELECT id FROM ventas_curso WHERE origen_id=? and  tipo_origen_id=? and manguera=?";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, venta.getConfiguracionId());
                    ps.setInt(2, venta.getSurtidorTipoId().getNumVal());
                    ps.setInt(3, venta.getManguera());
                    ResultSet re = ps.executeQuery();
                    while (re.next()) {
                        venta.setId(re.getLong("id"));
                    }
                }

                if (venta.getId() == 0) {
                    NeoService.setLog("VENTA NO RECUPERADA O NO EXISTE, SE CREA REGISTRO");
                    String sql = "INSERT INTO ventas_curso(\n"
                            + "            id, tipo_origen_id, origen_id, operario_id, cliente_id, surtidor, cara, \n"
                            + "            manguera, grado, productos_id, cantidad, importe, total, fecha_inicio, \n"
                            + "            fecha_fin, acumulado_cantidad, acumulado_importe, sincronizado, jornada_id, placa, token_process_id)\n"
                            + "    VALUES (nextval('ventas_curso_id'), ?, ?, ?, ?, ?, ?, \n"
                            + "            ?, ?, ?, ?, ?, ?, now(), \n"
                            + "            now(), ?, ?, ?, ?, ?, ?) RETURNING currval('ventas_curso_id') ";

                    PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, venta.getSurtidorTipoId().getNumVal());
                    ps.setLong(2, venta.getConfiguracionId());
                    ps.setLong(3, venta.getOperadorId());
                    ps.setLong(4, venta.getClienteId());
                    ps.setLong(5, venta.getSurtidorId());
                    ps.setLong(6, venta.getCara());
                    ps.setLong(7, venta.getManguera());
                    ps.setLong(8, venta.getGrado());
                    ps.setLong(9, venta.getProductoId());
                    ps.setDouble(10, venta.getActualVolumen());
                    ps.setDouble(11, venta.getActualPrecio());
                    ps.setDouble(12, venta.getActualImporte());
                    ps.setLong(13, venta.getAcumuladoVolumenInicial());
                    ps.setLong(14, venta.getAcumuladoImporteInicial());
                    ps.setLong(15, 0);
                    ps.setLong(16, venta.getJornadaId());
                    ps.setString(17, venta.getPlaca());
                    if (venta.getAutorizacionToken() != null && esCreditoAutorizacion(venta.getAutorizacionToken().getId())) {
                        ps.setLong(18, venta.getAutorizacionToken().getId());
                    } else {
                        ps.setNull(18, Types.NULL);
                    }

                    ResultSet re = ps.executeQuery();
                    while (re.next()) {
                        venta.setId(re.getLong(1));
                        NeoService.setLog("REGISTRO DE VENTA CREADO " + venta.getId());
                    }

                } else {
                    NeoService.setLog("VENTA RECUPERADA #" + venta.getId());
                }

                NeoService.setLog("FUNCION 1 FINALIZANDO VENTA");
                String sql = "UPDATE ventas_curso SET cantidad=?, fecha_fin=now(), total=?  WHERE id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setDouble(1, venta.getActualVolumen());
                ps.setDouble(2, venta.getActualImporte());
                ps.setLong(3, venta.getId());
                ps.execute();

                NeoService.setLog("FUNCION 2 FINALIZANDO VENTA");
                if (venta.getActualImporte() != 0 || venta.getActualVolumen() != 0) {

                    sql = "SELECT NEXTVAL('ventas_id') recibo";
                    ps = NeoService.obtenerConexion().prepareCall(sql);
                    ResultSet re = ps.executeQuery();
                    while (re.next()) {
                        venta.setReciboId(re.getLong(1));
                    }

                    NeoService.setLog("FUNCION 4 VERIFICANDO LA VENTA GUARDADA");
                    sql = "SELECT * FROM VENTAS WHERE ID = ?";
                    ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, venta.getReciboId());
                    re = ps.executeQuery();
                    boolean existeVenta = false;
                    while (re.next()) {
                        venta.setReciboId(re.getLong(1));
                        existeVenta = true;
                    }

                    NeoService.setLog("FUNCION 4A VALIDANDO LA VENTA");
                    sql = "SELECT ventas_id FROM ventas_detalles WHERE acum_vol_inicial = ? and acum_vol_final = ? and acum_ven_inicial = ? and acum_ven_final = ?";
                    ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, venta.getAcumuladoVolumenInicial());
                    ps.setLong(2, venta.getAcumuladoVolumenFinal());
                    ps.setLong(3, venta.getAcumuladoImporteInicial());
                    ps.setLong(4, venta.getAcumuladoImporteFinal());
                    re = ps.executeQuery();
                    if (re.next()) {
                        venta.setReciboId(re.getLong(1));
                        NeoService.setLog(NeoService.ANSI_RED + Utils.fill("*", Utils.PAGE_SIZE));
                        NeoService.setLog(NeoService.ANSI_YELLOW + "SE INTENTA DUPLICAR UNA VENTA, SE PROCEDE A ELIMINAR" + NeoService.ANSI_RESET);
                        NeoService.setLog(NeoService.ANSI_RED + Utils.fill("*", Utils.PAGE_SIZE));
                        existeVenta = true;
                    }
                    NeoService.setLog("FUNCION 4B VALIDANDO LA VENTA");
                    sql = "select acumulado_cantidad from surtidores_detalles where manguera = ?";
                    ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, venta.getManguera());
                    re = ps.executeQuery();
                    if (re.next()) {
                        if (re.getInt("acumulado_cantidad") == venta.getAcumuladoVolumenFinal()) {
                            existeVenta = true;
                        }
                    }
                    if (!existeVenta) {
                        NeoService.millisecondsPause(200);

                        NeoService.setLog("FUNCION 5A EN ENVIA REGISTRO DE LA VENTA A BD");
                        sql = "INSERT INTO ventas "
                                + "("
                                + "   id, jornada_id, tipo_origen_id, origen_id, surtidor, cara, "
                                + "   manguera, grado, operario_id, cliente_id, fecha_inicio, "
                                + "   fecha_fin, total, impuesto, sincronizado, placa, "
                                + "   impresion, token_process_id, medios_pagos_id, voucher, atributos)\n"
                                + "VALUES("
                                + "       ?,?,?,?,?,?,"
                                + "       ?,?,?,?,now(),"
                                + "       now(),?,0,0,?,"
                                + "       null,?,null,null,?::json"
                                + ")";
                        ps = NeoService.obtenerConexion().prepareCall(sql);

                        ps.setLong(1, venta.getReciboId());
                        ps.setLong(2, venta.getJornadaId());
                        ps.setLong(3, venta.getSurtidorTipoId().getNumVal());
                        ps.setLong(4, venta.getConfiguracionId());
                        ps.setLong(5, venta.getSurtidorId());
                        ps.setLong(6, venta.getCara());
                        ps.setLong(7, venta.getManguera());
                        ps.setLong(8, venta.getGrado());
                        ps.setLong(9, venta.getOperadorId());
                        ps.setLong(10, venta.getClienteId());
                        ps.setLong(11, venta.getActualImporte());
                        ps.setString(12, venta.getPlaca());
                        if (venta.getAutorizacionToken() != null && esCreditoAutorizacion(venta.getAutorizacionToken().getId())) {
                            ps.setLong(13, venta.getAutorizacionToken().getId());
                        } else {
                            ps.setNull(13, Types.NULL);
                        }
                        NeoService.setLog("SE INTENTA AGREGAR LA SIGUIENTE SENTENCIA A LA BD");
                        String atributos = getAtributosVentaEnCurso(venta.getCara(), venta.getManguera(), venta.getGrado());
                        if (atributos != null && atributos.length() > 0 && !atributos.isEmpty()) {
                            ps.setString(14, atributos);
                        } else {
                            ps.setNull(14, Types.NULL);
                        }
                        NeoService.setLog(NeoService.ANSI_YELLOW + ps.toString() + NeoService.ANSI_RESET);
                        ps.executeUpdate();
                        NeoService.setLog("REGISTRO DE LA VENTA A LA BD #" + venta.getReciboId());

                        NeoService.setLog("ACTUALIZANDO CONSECUTIVOS");
                        sql = "select nextval('ventas_curso_id')";
                        ps = NeoService.obtenerConexion().prepareCall(sql);
                        ps.executeQuery();

                        sql = "INSERT INTO ventas_detalles\n"
                                + "(ventas_id, id, productos_id, cantidad, precio, "
                                + " total, sincronizado, cantidad_precisa, acum_vol_inicial, acum_vol_final, "
                                + " acum_ven_inicial, acum_ven_final, cantidad_factor) "
                                + "VALUES("
                                + "?,nextval('ventas_curso_id'),?,?,?, "
                                + "?,0,?,?,?, "
                                + "?,?,null"
                                + ");";

                        ps = NeoService.obtenerConexion().prepareCall(sql);
                        ps.setLong(1, venta.getReciboId());
                        ps.setLong(2, venta.getProductoId());
                        ps.setLong(3, venta.getActualVolumen());
                        ps.setLong(4, venta.getActualPrecio());
                        ps.setLong(5, venta.getActualImporte());
                        ps.setLong(6, venta.getActualVolumenReal());
                        ps.setLong(7, venta.getAcumuladoVolumenInicial());
                        ps.setLong(8, venta.getAcumuladoVolumenFinal());
                        ps.setLong(9, venta.getAcumuladoImporteInicial());
                        ps.setLong(10, venta.getAcumuladoImporteFinal());

                        NeoService.setLog("SE INTENTA AGREGAR LOS DETALLES A LA BD");
                        NeoService.setLog(NeoService.ANSI_YELLOW + ps.toString() + NeoService.ANSI_RESET);
                        ps.executeUpdate();
                    }
                }

                NeoService.setLog("FUNCION 4 FINALIZANDO VENTA");
                sql = "DELETE FROM ventas_curso WHERE id=?";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, venta.getId());
                ps.execute();

                long acumuladoCantidad = 0;
                NeoService.setLog("FUNCION 5 FINALIZANDO VENTA");
                sql = "SELECT acumulado_venta, acumulado_cantidad  FROM surtidores_detalles WHERE id=?";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, venta.getConfiguracionId());
                ResultSet re = ps.executeQuery();
                if (re.next()) {
                    acumuladoCantidad = re.getLong("acumulado_cantidad");
                }

                NeoService.setLog("LA CANTIDAD DE LA VENTA " + venta.getActualVolumen());
                NeoService.setLog("ACUMULADO BASE DE DATOS CANTIDAD " + acumuladoCantidad);

                NeoService.setLog("FUNCION 6 FINALIZANDO VENTA");
                sql = "UPDATE surtidores_detalles SET acumulado_venta = ?, acumulado_cantidad = ? WHERE id=?";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, venta.getAcumuladoImporteFinal());
                ps.setLong(2, venta.getAcumuladoVolumenFinal());
                ps.setLong(3, venta.getConfiguracionId());
                ps.execute();

            } else {
                String sql = "UPDATE ventas_curso SET cantidad=?, fecha_fin=?, total=?  WHERE id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setDouble(1, venta.getVentaCantidad());
                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                ps.setDouble(3, venta.getVentaImporte());
                ps.setLong(4, venta.getId());
                ps.executeUpdate();
            }
        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
            NeoService.setLog(ex.getMessage());
        }

        return venta.getId();
    }

    public void setVentaSyncronizada(Venta venta) {
        try {

            String sql = "UPDATE ventas SET sincronizado=1  WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, venta.getId());
            ps.executeUpdate();

            if (venta.getDetalles() != null) {
                sql = "UPDATE ventas_detalles SET sincronizado=1  WHERE id=?";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, venta.getDetalles().getId());
                ps.executeUpdate();
            }

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Totalizador getTotalizadores(long surtidor, int manguera) {
        Totalizador total = null;
        try {

            String sql = "SELECT acumulado_venta, acumulado_cantidad \n"
                    + "FROM surtidores_detalles SC\n"
                    + "WHERE 1=1\n"
                    + "AND SC.SURTIDORES_ID=?\n"
                    + "AND SC.MANGUERA=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, surtidor);
            ps.setInt(2, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                total = new Totalizador();
                total.setAcumuladoVenta(re.getLong("acumulado_venta"));
                total.setAcumuladoVolumen(re.getLong("acumulado_cantidad"));
            }
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return total;
    }

    public Totalizador getTotalizadoresCaraGrado(int cara, int grado) {
        Totalizador total = null;
        try {

            String sql = "SELECT acumulado_venta, acumulado_cantidad \n"
                    + "FROM surtidores_detalles SC\n"
                    + "WHERE 1=1\n"
                    + "AND SC.CARA=?\n"
                    + "AND SC.GRADO=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ps.setInt(2, grado);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                total = new Totalizador();
                total.setAcumuladoVenta(re.getLong("acumulado_venta"));
                total.setAcumuladoVolumen(re.getLong("acumulado_cantidad"));
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return total;
    }

    public Totalizador[] getMultiTotalizadores(long surtidor, int cara) {
        Totalizador[] totales = null;
        try {

            String sql = "SELECT manguera, acumulado_venta, acumulado_cantidad \n"
                    + "FROM surtidores_detalles SC\n"
                    + "WHERE 1=1\n"
                    + "AND SC.SURTIDORES_ID=?\n"
                    + "AND SC.CARA=?\n"
                    + "ORDER BY MANGUERA";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, surtidor);
            ps.setInt(2, cara);
            ResultSet re = ps.executeQuery();
            int i = 0;
            while (re.next()) {
                i++;
            }
            totales = new Totalizador[i];
            re = ps.executeQuery();
            i = 0;
            while (re.next()) {
                Totalizador total = new Totalizador();
                total.setManguera(re.getInt("manguera"));
                total.setAcumuladoVenta(re.getLong("acumulado_venta"));
                total.setAcumuladoVolumen(re.getLong("acumulado_cantidad"));
                totales[i] = total;
                i++;
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totales;
    }

    public Persona getTurnoSurtidor(long surtidor, int manguera) {
        Persona persona = null;
        try {

            String sql = "SELECT personas_id, j.id jornada_id\n"
                    + "FROM surtidores_detalles SC\n"
                    + "INNER JOIN JORNADAS J ON J.SURTIDORES_ID=SC.SURTIDORES_ID\n"
                    + "WHERE 1=1 \n"
                    + "AND SC.SURTIDORES_ID=?\n";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, surtidor);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                persona = new Persona();
                persona.setId(re.getLong("personas_id"));
                persona.setJornadaId(re.getLong("jornada_id"));
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return persona;
    }

    public boolean getTurnoSurtidor(long surtidor) {
        boolean existeTurno = false;
        try {

            String sql = "SELECT 1 FROM JORNADAS ";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, surtidor);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                existeTurno = true;
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return existeTurno;
    }

    public long getGrupoJornada() {
        long grupoJornada = 0;
        try {
            String sql = "SELECT grupo_jornada FROM JORNADAS LIMIT 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                grupoJornada = re.getLong("grupo_jornada");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return grupoJornada;
    }

    public long existUsuario(String data) {
        long result = -1;
        try {

            String sql = "SELECT ID FROM PERSONAS WHERE TAG=? AND ESTADO='A'";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, data);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                result = re.getLong("id");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return result;
    }

    public ResponseSicom getChip(String ibutton) {
        ResponseSicom sicom = null;
        try {

            String sql = "SELECT * FROM IDROM WHERE IDROM=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, ibutton);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                sicom = new ResponseSicom();
                sicom.setIdrom(ibutton);
                sicom.setPlaca(re.getString("placa_vin"));
                sicom.setFecha_inicio(re.getTimestamp("fecha_revision"));
                sicom.setFecha_fin(re.getTimestamp("fecha_vencimiento"));
                sicom.setEstado(re.getString("estado"));
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return sicom;
    }

    public void saveSaltoLectura(Manguera manguera) {
        try {
            String sql = "UPDATE surtidores_detalles "
                    + "SET salto_lectura='S', acumulado_venta_surt=?, acumulado_cantidad_surt=? "
                    + "WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, manguera.getRegistroSurtidorVentas());
            ps.setLong(2, manguera.getRegistroSurtidorVolumen());
            ps.setLong(3, manguera.getConfiguracionId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public long getPrimeroTurno() {
        long personas_id = 0;
        try {
            String sql = "SELECT personas_id FROM JORNADAS order by id asc LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                personas_id = re.getLong("personas_id");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return personas_id;
    }

    public void corrigeSaltoLectura(Manguera manguera) {
        try {

            long acumuladoVolumenSurtidor = 0;
            long acumuladoVentasSurtidor = 0;

            long acumuladoVolumenSistema = 0;
            long acumuladoVentasSistema = 0;

            long diferenciaVolumen = 0;
            long diferenciaVenta = 0;

            String sql = "SELECT sd.*, p.precio "
                    + "FROM surtidores_detalles sd "
                    + "INNER JOIN productos p ON p.id=sd.productos_id "
                    + "WHERE sd.id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, manguera.getConfiguracionId());
            ResultSet re = ps.executeQuery();
            while (re.next()) {

                manguera.setId(re.getInt("manguera"));
                manguera.setSurtidor(re.getInt("surtidor"));
                manguera.setCara(re.getInt("cara"));
                manguera.setGrado(re.getInt("grado"));
                manguera.setProductoId(re.getInt("productos_id"));
                manguera.setProductoPrecio(re.getInt("precio"));

                acumuladoVolumenSurtidor = re.getLong("acumulado_cantidad_surt");
                acumuladoVentasSurtidor = re.getLong("acumulado_venta_surt");

                acumuladoVolumenSistema = re.getLong("acumulado_cantidad");
                acumuladoVentasSistema = re.getLong("acumulado_venta");

                diferenciaVolumen = acumuladoVolumenSurtidor - acumuladoVolumenSistema;
                diferenciaVenta = acumuladoVentasSurtidor - acumuladoVentasSistema;

            }

            String sql1 = "UPDATE surtidores_detalles "
                    + "SET acumulado_venta = acumulado_venta_surt, acumulado_cantidad = acumulado_cantidad_surt "
                    + "WHERE id=?";
            ps = NeoService.obtenerConexion().prepareCall(sql1);
            ps.setLong(1, manguera.getConfiguracionId());
            ps.executeUpdate();

            String sql2 = "UPDATE surtidores_detalles "
                    + "SET salto_lectura=NULL, acumulado_venta_surt=NULL, acumulado_cantidad_surt=NULL "
                    + "WHERE id=?";
            ps = NeoService.obtenerConexion().prepareCall(sql2);
            ps.setLong(1, manguera.getConfiguracionId());
            ps.executeUpdate();

            long jornadaId = -1;
            sql = "SELECT ID FROM JORNADAS";
            ps = NeoService.obtenerConexion().prepareCall(sql);
            re = ps.executeQuery();
            while (re.next()) {
                jornadaId = re.getLong("id");
            }

            String sql3 = "INSERT INTO saltos_lecturas(\n"
                    + "            valor, cantidad, configuracion_id, surtidor, cara, manguera, \n"
                    + "            grado, producto_id, precio, jornadas_id, resposable, motivo, \n"
                    + "            fecha_correcion, impreso, sistema_acu_volumen, sistema_acu_venta, \n"
                    + "            surtidor_acu_volumen, surtidor_acu_venta)\n"
                    + "    VALUES (?, ?, ?, ?, ?, ?, \n"
                    + "            ?, ?, ?, ?, ?, ?, \n"
                    + "            now(), NULL, ?, ?, \n"
                    + "            ?, ?);";

            ps = NeoService.obtenerConexion().prepareCall(sql3);
            ps.setLong(1, diferenciaVenta);
            ps.setLong(2, diferenciaVolumen);
            ps.setLong(3, manguera.getConfiguracionId());
            ps.setLong(4, manguera.getSurtidor());
            ps.setLong(5, manguera.getCara());
            ps.setLong(6, manguera.getId());
            ps.setLong(7, manguera.getGrado());
            ps.setLong(8, manguera.getProductoId());
            ps.setLong(9, manguera.getProductoPrecio());
            if (jornadaId == -1) {
                ps.setNull(10, Types.NULL);
            } else {
                ps.setLong(10, jornadaId);
            }
            ps.setNull(11, Types.NULL);
            ps.setInt(12, 1);
            ps.setLong(13, acumuladoVolumenSistema);
            ps.setLong(14, acumuladoVentasSistema);
            ps.setLong(15, acumuladoVolumenSurtidor);
            ps.setLong(16, acumuladoVentasSurtidor);

            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public void guardarEstado(Manguera manguera) {
        try {

            String sql = "UPDATE surtidores_detalles "
                    + "SET estado=? "
                    + "WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, manguera.getEstado().getNumVal());
            ps.setLong(2, manguera.getConfiguracionId());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public void guardarEstado(Surtidor surtidor, Cara cara) {
        try {

            String sql = "UPDATE surtidores_detalles "
                    + "SET estado=?, estado_publico=? "
                    + "WHERE surtidor=? and cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, cara.getEstado());
            ps.setLong(2, cara.getPublicEstadoId());
            ps.setLong(3, surtidor.getId());
            ps.setLong(4, cara.getNumero());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public Recibo getUltimaVenta(long configuracionId) throws DAOException {
        Recibo recibo = null;
        try {

            String sql = "SELECT AU.*, OP.NOMBRE OPERADOR, V.ID VENTA_ID,\n"
                    + "E.NIT, E.RAZON_SOCIAL, E.DIRECCION, E.TELEFONO, ISLAS_ID ISLA,\n"
                    + "V.ORIGEN_ID, SD.SURTIDOR SD_SURTIDOR, SD.CARA SD_CARA, SD.MANGUERA, SD.GRADO, \n"
                    + "VD.TOTAL, VD.CANTIDAD_PRECISA, VD.PRECIO,\n"
                    + "V.OPERARIO_ID, V.FECHA_INICIO, V.PLACA, V.IMPRESION, P.DESCRIPCION,\n"
                    + "MP.DESCRIPCION MEDIOS_PAGOS_DESC, MP2.DESCRIPCION MEDIOS_PAGOS_DESC2, V.MEDIOS_PAGOS_ID \n"
                    + "FROM VENTAS V\n"
                    + "INNER JOIN VENTAS_DETALLES VD ON V.ID=VD.VENTAS_ID\n"
                    + "INNER JOIN SURTIDORES_DETALLES SD ON SD.ID=V.ORIGEN_ID\n"
                    + "INNER JOIN PRODUCTOS P ON P.ID=VD.PRODUCTOS_ID\n"
                    + "INNER JOIN SURTIDORES S ON S.ID=SD.SURTIDORES_ID\n"
                    + "INNER JOIN EMPRESAS E ON E.ID=P.EMPRESAS_ID\n"
                    + "INNER JOIN PERSONAS OP ON OP.ID=V.OPERARIO_ID\n"
                    + "LEFT JOIN TRANSACCIONES AU ON AU.ID=V.TOKEN_PROCESS_ID\n"
                    + "LEFT JOIN MEDIOS_PAGOS MP ON MP.ID=V.MEDIOS_PAGOS_ID\n"
                    + "LEFT JOIN MEDIOS_PAGOS MP2 ON MP2.ID=AU.METODO_PAGO\n"
                    + "WHERE ORIGEN_ID=? ORDER BY V.ID DESC LIMIT 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, configuracionId);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                recibo = new Recibo();
                recibo.setNit(re.getString("NIT"));
                recibo.setEmpresa(re.getString("RAZON_SOCIAL"));
                recibo.setDireccion(re.getString("DIRECCION"));
                recibo.setTelefono(re.getString("TELEFONO"));
                recibo.setProducto(re.getString("DESCRIPCION"));
                recibo.setPrecio(re.getLong("PRECIO"));
                recibo.setCantidad(re.getLong("CANTIDAD_PRECISA"));
                recibo.setTotal(re.getLong("TOTAL"));

                if (re.getString("PLACA_VEHICULO") == null) {
                    recibo.setPlaca(re.getString("PLACA"));
                } else {
                    recibo.setPlaca(re.getString("PLACA_VEHICULO"));
                }

                recibo.setNumero(re.getLong("VENTA_ID"));
                recibo.setIsla(re.getString("ISLA"));
                recibo.setSurtidor(re.getString("SD_SURTIDOR"));
                recibo.setCara(re.getString("SD_CARA"));
                recibo.setManguera(re.getString("MANGUERA"));
                recibo.setFecha(re.getTimestamp("FECHA_INICIO"));
                recibo.setOperador(re.getString("OPERADOR"));
                if (re.getString("IMPRESION") == null) {
                    recibo.setCopia("");
                } else {
                    recibo.setCopia("DUPLICADO");
                }
                if (re.getLong("MEDIOS_PAGOS_ID") != 0) {
                    recibo.setMedio(re.getLong("MEDIOS_PAGOS_ID"));
                    recibo.setMedioDescripcion(re.getString("MEDIOS_PAGOS_DESC"));
                } else {
                    recibo.setMedioDescripcion(AConstant.STRING_MEDIO_EFECTIVO);
                }

                if (re.getLong("METODO_PAGO") != 0) {
                    recibo.setMedio(re.getLong("METODO_PAGO"));
                    recibo.setMedioDescripcion(re.getString("MEDIOS_PAGOS_DESC2"));
                }

                if (re.getString("CLIENTE_NOMBRE") != null) {
                    recibo.setCliente(re.getString("CLIENTE_NOMBRE"));
                }

                if (re.getString("CONDUCTOR_NOMBRE") != null) {
                    recibo.setConductor(re.getString("CONDUCTOR_NOMBRE"));
                }

                if (re.getString("SERIAL_DISPOSITIVO") != null) {
                    recibo.setSerialAutorizacion(re.getString("SERIAL_DISPOSITIVO"));
                }

                if (re.getString("VEHICULO_ODOMETRO") != null) {
                    recibo.setOdometro(re.getString("VEHICULO_ODOMETRO"));
                }

            }
        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }
        NeoService.setLog("BUSCAR EN BD " + configuracionId);
        return recibo;
    }

    public Manguera getMangueraByFamilia(String familia) throws DAOException {
        Manguera manguera = null;
        try {

            String sql = "SELECT SD.GRADO, P.ID, P.PUBLICO, P.DESCRIPCION, P.FAMILIAS, PF.CODIGO, P.PRECIO\n"
                    + "FROM SURTIDORES_DETALLES SD \n"
                    + "INNER JOIN PRODUCTOS P ON SD.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN PRODUCTOS_FAMILIAS PF ON PF.ID=P.FAMILIAS\n"
                    + "WHERE PF.CODIGO=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, familia);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                manguera = new Manguera();
                manguera.setGrado(re.getInt("grado"));
                manguera.setProductoId(re.getLong("id"));
                manguera.setProductoPublicId(re.getLong("publico"));
                manguera.setProductoDescripcion(re.getString("descripcion"));
                manguera.setProductoFamiliaId(re.getLong("familias"));
                manguera.setProductoFamiliaDescripcion(re.getString("codigo"));
                manguera.setProductoPrecio(re.getLong("precio"));
            }
        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }
        return manguera;
    }

    public Recibo getReciboVenta(long numero) throws DAOException {
        Recibo recibo = null;
        try {

            String sql = "SELECT AU.*, OP.NOMBRE OPERADOR, V.ID venta_id,\n"
                    + "E.NIT, E.RAZON_SOCIAL, E.DIRECCION, E.TELEFONO, ISLAS_ID ISLA,\n"
                    + "V.ORIGEN_ID, SD.SURTIDOR SD_SURTIDOR, SD.CARA SD_CARA, SD.MANGUERA, SD.GRADO, VD.TOTAL, \n"
                    + "VD.cantidad_precisa CANTIDAD, VD.PRECIO,V.OPERARIO_ID, V.FECHA_INICIO, V.PLACA, V.IMPRESION, P.DESCRIPCION, \n"
                    + "MP.DESCRIPCION MEDIOS_PAGOS_DESC, MP2.DESCRIPCION MEDIOS_PAGOS_DESC2, V.MEDIOS_PAGOS_ID \n"
                    + "FROM VENTAS V\n"
                    + "INNER JOIN VENTAS_DETALLES VD ON V.ID=VD.VENTAS_ID\n"
                    + "INNER JOIN SURTIDORES_DETALLES SD ON SD.ID=V.ORIGEN_ID\n"
                    + "INNER JOIN PRODUCTOS P ON P.ID=VD.PRODUCTOS_ID\n"
                    + "INNER JOIN SURTIDORES S ON S.ID=SD.SURTIDORES_ID\n"
                    + "INNER JOIN EMPRESAS E ON E.ID=P.EMPRESAS_ID\n"
                    + "INNER JOIN PERSONAS OP ON OP.ID=V.OPERARIO_ID\n"
                    + "LEFT JOIN MEDIOS_PAGOS MP ON MP.ID=V.MEDIOS_PAGOS_ID\n"
                    + "LEFT JOIN transacciones AU ON AU.ID=V.TOKEN_PROCESS_ID\n"
                    + "LEFT JOIN MEDIOS_PAGOS MP2 ON MP2.ID=AU.METODO_PAGO\n"
                    + "WHERE V.ID = ? ORDER BY ID DESC";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, numero);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                recibo = new Recibo();
                recibo.setNit(re.getString("NIT"));
                recibo.setEmpresa(re.getString("RAZON_SOCIAL"));
                recibo.setDireccion(re.getString("DIRECCION"));
                recibo.setTelefono(re.getString("TELEFONO"));
                recibo.setProducto(re.getString("DESCRIPCION"));
                recibo.setPrecio(re.getLong("PRECIO"));
                recibo.setCantidad(re.getLong("CANTIDAD"));
                recibo.setTotal(re.getLong("TOTAL"));

                if (re.getString("PLACA_VEHICULO") == null) {
                    recibo.setPlaca(re.getString("PLACA"));
                } else {
                    recibo.setPlaca(re.getString("PLACA_VEHICULO"));
                }

                recibo.setNumero(re.getLong("venta_id"));
                recibo.setIsla(re.getString("ISLA"));
                recibo.setSurtidor(re.getString("SD_SURTIDOR"));
                recibo.setCara(re.getString("SD_CARA"));
                recibo.setManguera(re.getString("MANGUERA"));
                recibo.setFecha(re.getTimestamp("FECHA_INICIO"));
                recibo.setOperador(re.getString("OPERADOR"));
                if (re.getLong("MEDIOS_PAGOS_ID") != 0) {
                    recibo.setMedio(re.getLong("MEDIOS_PAGOS_ID"));
                    recibo.setMedioDescripcion(re.getString("MEDIOS_PAGOS_DESC"));
                } else {
                    recibo.setMedioDescripcion(AConstant.STRING_MEDIO_EFECTIVO);
                }

                if (re.getLong("METODO_PAGO") != 0) {
                    recibo.setMedio(re.getLong("METODO_PAGO"));
                    recibo.setMedioDescripcion(re.getString("MEDIOS_PAGOS_DESC2"));
                }

                if (re.getString("IMPRESION") == null) {
                    recibo.setCopia("");
                } else {
                    recibo.setCopia("DUPLICADO");
                }

                if (re.getString("CLIENTE_NOMBRE") != null) {
                    recibo.setCliente(re.getString("CLIENTE_NOMBRE"));
                }

                if (re.getString("CONDUCTOR_NOMBRE") != null) {
                    recibo.setConductor(re.getString("CONDUCTOR_NOMBRE"));
                }

                if (re.getString("serial_dispositivo") != null) {
                    recibo.setSerialAutorizacion(re.getString("serial_dispositivo"));
                }

                if (re.getString("VEHICULO_ODOMETRO") != null) {
                    recibo.setOdometro(re.getString("VEHICULO_ODOMETRO"));
                }

                ArrayList<MediosPagosBean> mediosPagos = getMediosPagos(numero);
                recibo.setMediosPagos(mediosPagos);
            }
        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }
        return recibo;
    }

    public List<Recibo> getUltimasVenta(Criterio criterios) throws DAOException {
        List<Recibo> recibos = new LinkedList<>();
        try {

            String sql = "SELECT AU.*, OP.NOMBRE OPERADOR, V.ID VENTAS_ID,\n"
                    + "E.NIT, E.RAZON_SOCIAL, E.DIRECCION, E.TELEFONO, ISLAS_ID ISLA,\n"
                    + "V.ORIGEN_ID, SD.SURTIDOR SD_SURTIDOR , SD.CARA SD_CARA, SD.MANGUERA SD_MANGUERA, SD.GRADO SD_GRADO, VD.TOTAL, \n"
                    + "VD.CANTIDAD_PRECISA CANTIDAD, VD.PRECIO,V.OPERARIO_ID, V.FECHA_INICIO, V.PLACA, V.IMPRESION, P.DESCRIPCION,\n"
                    + "MP.DESCRIPCION MEDIOS_PAGOS_DESC, MP2.DESCRIPCION MEDIOS_PAGOS_DESC2, V.MEDIOS_PAGOS_ID \n"
                    + "FROM VENTAS V\n"
                    + "INNER JOIN VENTAS_DETALLES VD ON V.ID=VD.VENTAS_ID\n"
                    + "INNER JOIN SURTIDORES_DETALLES SD ON SD.ID=V.ORIGEN_ID\n"
                    + "INNER JOIN PRODUCTOS P ON P.ID=VD.PRODUCTOS_ID\n"
                    + "INNER JOIN SURTIDORES S ON S.ID=SD.SURTIDORES_ID\n"
                    + "INNER JOIN EMPRESAS E ON E.ID=P.EMPRESAS_ID\n"
                    + "INNER JOIN PERSONAS OP ON OP.ID=V.OPERARIO_ID\n"
                    + "LEFT JOIN MEDIOS_PAGOS MP ON MP.ID=V.MEDIOS_PAGOS_ID\n"
                    + "LEFT JOIN transacciones AU ON AU.ID=V.TOKEN_PROCESS_ID\n"
                    + "LEFT JOIN MEDIOS_PAGOS MP2 ON MP2.ID=AU.METODO_PAGO\n"
                    + "WHERE V.FECHA_INICIO>=?::timestamp without time zone \n"
                    + "AND V.FECHA_INICIO<?::timestamp without time zone \n"
                    + "ORDER BY V.ID DESC";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, criterios.getFechaInicio());
            ps.setString(2, criterios.getFechaFin());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                Recibo recibo = new Recibo();
                recibo.setNit(re.getString("NIT"));
                recibo.setEmpresa(re.getString("RAZON_SOCIAL"));
                recibo.setDireccion(re.getString("DIRECCION"));
                recibo.setTelefono(re.getString("TELEFONO"));
                recibo.setProducto(re.getString("DESCRIPCION"));
                recibo.setPrecio(re.getLong("PRECIO"));
                recibo.setCantidad(re.getLong("CANTIDAD"));
                recibo.setTotal(re.getLong("TOTAL"));

                if (re.getString("PLACA_VEHICULO") == null) {
                    recibo.setPlaca(re.getString("PLACA"));
                } else {
                    recibo.setPlaca(re.getString("PLACA_VEHICULO"));
                }

                recibo.setNumero(re.getLong("VENTAS_ID"));
                recibo.setIsla(re.getString("ISLA"));
                recibo.setSurtidor(re.getString("SD_SURTIDOR"));
                recibo.setCara(re.getString("SD_CARA") == null ? "" : re.getString("SD_CARA"));
                recibo.setManguera(re.getString("SD_MANGUERA"));
                recibo.setFecha(re.getTimestamp("FECHA_INICIO"));
                recibo.setOperador(re.getString("OPERADOR"));

                if (re.getLong("MEDIOS_PAGOS_ID") != 0) {
                    recibo.setMedio(re.getLong("MEDIOS_PAGOS_ID"));
                    recibo.setMedioDescripcion(re.getString("MEDIOS_PAGOS_DESC"));
                } else {
                    recibo.setMedioDescripcion(AConstant.STRING_MEDIO_EFECTIVO);
                }

                if (re.getLong("METODO_PAGO") != 0) {
                    recibo.setMedio(re.getLong("METODO_PAGO"));
                    recibo.setMedioDescripcion(re.getString("MEDIOS_PAGOS_DESC2"));
                }

                if (re.getString("IMPRESION") == null) {
                    recibo.setCopia("");
                } else {
                    recibo.setCopia("DUPLICADO");
                }
                recibos.add(recibo);
            }
        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }

        return recibos;
    }

    public void documentoImpreso(long id) throws DAOException {
        try {

            String sql = "UPDATE VENTAS SET IMPRESION='S' WHERE ID=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException s) {
            if (s.getSQLState().equals("1062")) {
                throw new DAOException("Ya se esta usando ese codigo");
            } else {
                throw new DAOException("ERROR " + s.getSQLState() + ": " + s.getMessage());
            }
        }
    }

    public LinkedList<Precio> getPreciosSinActualizar() {
        LinkedList<Precio> lista = new LinkedList<>();
        try {

            String sql = "SELECT id, producto_id, precio, create_date\n"
                    + "FROM precios_productos WHERE estado = 0";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                Precio precio = new Precio();
                precio.setId(re.getLong("id"));
                precio.setProducto_id(re.getLong("producto_id"));
                precio.setPrecio(re.getLong("precio"));
                precio.setCreateDate(re.getTimestamp("create_date"));
                lista.add(precio);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return lista;

    }

    public ArrayList<MedioPago> getMediosPagos() {
        ArrayList<MedioPago> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM MEDIOS_PAGOS";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                MedioPago medio = new MedioPago();
                medio.setId(re.getLong("id"));
                medio.setDescripcion(re.getString("descripcion"));
                lista.add(medio);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return lista;

    }

    public void setPrecioEstado(Precio precio) {
        try {

            String sql = "UPDATE precios SET estado=1  WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, precio.getId());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public ArrayList<TurnosResponse> getJornadasSinSincronizar() {

        ArrayList<TurnosResponse> jornadas = new ArrayList<>();
        try {

            String sql = "SELECT * FROM JORNADAS WHERE SINCRONIZADO=0";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                TurnosResponse jornada = new TurnosResponse();
                jornada.setId(re.getLong("id"));
                jornada.setFechaInicio(re.getTimestamp("fecha_inicio"));
                jornada.setPersonaId(re.getLong("personas_id"));
                jornada.setSurtidorId(re.getLong("surtidores_id"));
                jornada.setSaldo(re.getFloat("saldo"));
                jornada.setInventario(getInventariosByJornada(re.getLong("id")));
                jornadas.add(jornada);

            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return jornadas;
    }

    public ArrayList<TurnosResponse> getJornadasHistoricasSinSincronizar() {

        ArrayList<TurnosResponse> jornadas = new ArrayList<>();
        try {

            String sql = "SELECT * FROM JORNADAS_HIST WHERE SINCRONIZADO=0";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                TurnosResponse jornada = new TurnosResponse();
                jornada.setId(re.getLong("jornada_id"));
                jornada.setFechaFin(re.getTimestamp("fecha_fin"));
                jornada.setPersonaId(re.getLong("personas_id"));
                jornada.setSurtidorId(re.getLong("surtidores_id"));
                jornada.setSaldo(re.getFloat("saldo"));
                jornada.setInventario(getInventariosByJornada(re.getLong("jornada_id")));
                jornadas.add(jornada);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return jornadas;
    }

    public void actualizaJornada(TurnosResponse jornada) {
        try {

            String sql = "UPDATE jornadas SET sincronizado=1  WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, jornada.getId());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public void actualizaJornadaHist(TurnosResponse jornada) {
        try {

            String sql = "UPDATE jornadas_hist SET sincronizado=1  WHERE jornada_id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, jornada.getId());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    private List<SurtidorInventario> getInventariosByJornada(long aLong) {

        List<SurtidorInventario> inventatios = new ArrayList<>();
        try {

            String sql = "SELECT JI.*, SD.PRODUCTOS_ID\n"
                    + "FROM JORNADAS_INVENTARIOS JI\n"
                    + "INNER JOIN SURTIDORES_DETALLES SD ON SD.ID=JI.SURTIDORES_DETALLES_ID\n"
                    + "WHERE JORNADAS_ID=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, aLong);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                SurtidorInventario surt = new SurtidorInventario();
                surt.setProductoId(re.getLong("productos_id"));
                surt.setAcumuadoVentasInicial(re.getLong("acumulado_venta_inicial"));
                surt.setAcumuadoVolumenInicial(re.getLong("acumulado_cantidad_inicial"));
                surt.setAcumuadoVentasFinal(re.getLong("acumulado_venta_final"));
                surt.setAcumuadoVolumenFinal(re.getLong("acumulado_cantidad_final"));
                surt.setSurtidoresDetallesId(re.getLong("surtidores_detalles_id"));
                inventatios.add(surt);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return inventatios;
    }

    public void actualizarPrecioProucto(long productoId, int listaPrecio, float productoPrecio, boolean a) {

        try {

            if (listaPrecio == 1) {
                String sql = "UPDATE productos SET precio=? WHERE id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setFloat(1, productoPrecio);
                ps.setLong(2, productoId);
                ps.executeUpdate();
            } else {
                String sql = "UPDATE productos SET precio2=?  WHERE id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setFloat(1, productoPrecio);
                ps.setLong(2, productoId);
                ps.executeUpdate();
            }

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public Manguera getGradoByProductFamily(int numeroCara, long familia) throws DAOException {
        Manguera manguera = null;
        try {

            String sql = "SELECT SD.SURTIDOR, SD.CARA, SD.GRADO, SD.MANGUERA, P.PUBLICO, PF.CODIGO, P.DESCRIPCION, P.ID PRODUCTO_ID, P.FAMILIAS, P.PRECIO\n"
                    + "FROM SURTIDORES_DETALLES SD \n"
                    + "INNER JOIN PRODUCTOS P ON SD.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN PRODUCTOS_FAMILIAS PF ON PF.ID=P.FAMILIAS\n"
                    + "WHERE  cara = ? AND PF.id=?";

            //    "-- CARA 1 => SOLO AUTORIZA EXTRA Y DIESEL\n" +
            //    "-- CARA 2 => SOLO AUTORIZA DIESEL Y CORRIENTE";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, numeroCara);
            ps.setLong(2, familia);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                manguera = new Manguera();
                manguera.setSurtidor(re.getInt("surtidor"));
                manguera.setGrado(re.getInt("grado"));
                manguera.setCara(re.getInt("cara"));
                manguera.setId(re.getInt("manguera"));
                manguera.setProductoPrecio(re.getLong("precio"));
                manguera.setProductoFamiliaDescripcion(re.getString("codigo"));
                manguera.setProductoFamiliaId(re.getLong("familias"));
                manguera.setProductoDescripcion(re.getString("descripcion"));
                manguera.setProductoPublicId(re.getLong("producto_id"));
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return manguera;
    }

    public ProductoBean getProductoByMangueta(int numeroCara, int manguera) throws DAOException {
        ProductoBean producto = null;
        try {

            String sql = "SELECT P.*, PF.CODIGO FAMILIA_DESC "
                    + "FROM SURTIDORES_DETALLES SD \n"
                    + "INNER JOIN PRODUCTOS P ON SD.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN PRODUCTOS_FAMILIAS PF ON PF.ID=P.FAMILIAS\n"
                    + "WHERE cara = ? AND manguera =?";

            //    "-- CARA 1 => SOLO AUTORIZA EXTRA Y DIESEL\n" +
            //    "-- CARA 2 => SOLO AUTORIZA DIESEL Y CORRIENTE";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, numeroCara);
            ps.setInt(2, manguera);

            ResultSet re = ps.executeQuery();
            while (re.next()) {
                producto = new ProductoBean();
                producto.setId(re.getInt("ID"));
                producto.setPrecio(re.getLong("precio"));
                producto.setPrecioPlano(re.getLong("precio"));
                producto.setDescripcion(re.getString("descripcion"));
                producto.setCategoriaId(re.getLong("familias"));
                producto.setCategoriaDesc(re.getString("familia_desc"));
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return producto;
    }

    public ProductoBean getProductoByGrado(int numeroCara, int grado) throws DAOException {
        ProductoBean producto = null;
        try {

            String sql = "SELECT P.*, PF.CODIGO FAMILIA_DESC "
                    + "FROM SURTIDORES_DETALLES SD \n"
                    + "INNER JOIN PRODUCTOS P ON SD.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN PRODUCTOS_FAMILIAS PF ON PF.ID=P.FAMILIAS\n"
                    + "WHERE cara = ? AND grado =?";

            //    "-- CARA 1 => SOLO AUTORIZA EXTRA Y DIESEL\n" +
            //    "-- CARA 2 => SOLO AUTORIZA DIESEL Y CORRIENTE";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, numeroCara);
            ps.setInt(2, grado);

            ResultSet re = ps.executeQuery();
            while (re.next()) {
                producto = new ProductoBean();
                producto.setId(re.getInt("ID"));
                producto.setPrecio(re.getLong("precio"));
                producto.setPrecioPlano(re.getLong("precio"));
                producto.setDescripcion(re.getString("descripcion"));
                producto.setCategoriaId(re.getLong("familias"));
                producto.setCategoriaDesc(re.getString("familia_desc"));
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return producto;
    }

    public ProductoBean getProductoConFamiliaById(long productoId) throws DAOException {
        ProductoBean producto = null;
        try {
            String sql = "SELECT P.*, PF.CODIGO FAMILIA_DESC "
                    + "FROM SURTIDORES_DETALLES SD \n"
                    + "INNER JOIN PRODUCTOS P ON SD.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN PRODUCTOS_FAMILIAS PF ON PF.ID=P.FAMILIAS\n"
                    + "WHERE p.id = ?";

            //    "-- CARA 1 => SOLO AUTORIZA EXTRA Y DIESEL\n" +
            //    "-- CARA 2 => SOLO AUTORIZA DIESEL Y CORRIENTE";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, productoId);

            ResultSet re = ps.executeQuery();
            while (re.next()) {
                producto = new ProductoBean();
                producto.setId(re.getInt("ID"));
                producto.setPrecio(re.getLong("precio"));
                producto.setPrecioPlano(re.getLong("precio"));
                producto.setDescripcion(re.getString("descripcion"));
                producto.setCategoriaId(re.getLong("familias"));
                producto.setCategoriaDesc(re.getString("familia_desc"));
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return producto;
    }

    public boolean getVentaCurso(Surtidor surtidor, Cara cara) {
        boolean tieneVenta = false;
        try {

            String sql = "select id from ventas_curso where cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara.getNumero());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                tieneVenta = true;
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return tieneVenta;
    }

    public ArrayList<Manguera> getEstadoSurtidor(int cara) throws DAOException {
        ArrayList<Manguera> mangueras = new ArrayList<>();
        try {

            String sql = "SELECT SD.ID CONFIGURACIONID, SD.CARA, SD.GRADO, SD.MANGUERA, SD.ESTADO_PUBLICO, SE.DESCRIPCION, P.PUBLICO, PF.CODIGO, P.DESCRIPCION P_DESCRIPCION, P.ID, P.FAMILIAS, P.PRECIO\n"
                    + "FROM SURTIDORES_DETALLES SD \n"
                    + "INNER JOIN PRODUCTOS P ON SD.PRODUCTOS_ID=P.ID\n"
                    + "INNER JOIN PRODUCTOS_FAMILIAS PF ON PF.ID=P.FAMILIAS\n"
                    + "LEFT JOIN SURTIDOR_ESTADO SE ON SE.ID=SD.ESTADO_PUBLICO\n"
                    + "WHERE  cara = ? ";

            //    "-- CARA 1 => SOLO AUTORIZA EXTRA Y DIESEL\n" +
            //    "-- CARA 2 => SOLO AUTORIZA DIESEL Y CORRIENTE";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ResultSet re = ps.executeQuery();
            while (re.next()) {

                Manguera manguera = new Manguera();
                manguera.setCara(re.getInt("cara"));
                manguera.setGrado(re.getInt("grado"));
                manguera.setConfiguracionId(re.getInt("configuracionid"));
                manguera.setId(re.getInt("manguera"));
                manguera.setEstadoPublicoId(re.getInt("estado_publico"));
                manguera.setEstadoPublicoDescripcion(re.getString("descripcion"));

                manguera.setProductoPrecio(re.getLong("precio"));
                manguera.setProductoFamiliaDescripcion(re.getString("codigo"));
                manguera.setProductoFamiliaId(re.getLong("familias"));
                manguera.setProductoDescripcion(re.getString("p_descripcion"));

                mangueras.add(manguera);

            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return mangueras;

    }

    public CatalogoBean getEstadoPublicoCaraGrado(int cara, int grado) throws DAOException {
        CatalogoBean catalogo = null;
        try {

            String sql = "select estado_publico, se.descripcion, se.detalles \n"
                    + "from surtidores_detalles sd \n"
                    + "inner join surtidor_estado se on se.id = sd.estado_publico \n"
                    + "where cara = ? and grado = ?\n";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ps.setInt(2, grado);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                catalogo = new CatalogoBean();
                catalogo.setId(re.getLong("estado_publico"));
                catalogo.setValor(re.getString("descripcion"));
                catalogo.setDescripcion(re.getString("detalles"));
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return catalogo;
    }

    public int getGradoPorEstadoDespacho(Cara cara) throws DAOException {
        int grado = -1;
        try {

            String sql = "SELECT GRADO FROM VENTAS_CURSO WHERE CARA=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara.getNumero());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                grado = re.getInt("grado");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return grado;
    }

    public int getCantidadDespachoVentaCurso(Cara cara) throws DAOException {
        int cantidad = 0;
        try {

            String sql = "SELECT CANTIDAD FROM VENTAS_CURSO WHERE CARA=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara.getNumero());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                cantidad = re.getInt("cantidad");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return cantidad;

    }

    public int getTiempoDuracionTag() throws DAOException {
        int cantidad = 0;
        try {

            String sql = "SELECT valor FROM PARAMETROS WHERE codigo='tiempo_duracion_tag'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                cantidad = re.getInt("valor");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return cantidad;
    }

    public void getEliminaVentaCurso(Cara cara) throws DAOException {
        try {

            String sql = "DELETE FROM VENTAS_CURSO WHERE CARA=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara.getNumero());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
    }

    public String getParametros(String codigo) throws DAOException {
        String requiere = null;
        try {

            String sql = "SELECT valor FROM PARAMETROS WHERE codigo=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, codigo);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                requiere = re.getString("valor");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return requiere;
    }

    public void nivelaDecimales(Manguera get, long acumuladoVolumen) {
        try {

            String sql = "UPDATE SURTIDORES_DETALLES SET acumulado_cantidad=?  WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, acumuladoVolumen);
            ps.setLong(2, get.getConfiguracionId());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getExisteVentaPorToken(String REQUEST_TOKEN) throws DAOException {
        int existe = 0;
        try {

            String sql = "select 1 from ventas_curso v where v.token_process_id = (\n"
                    + "  select id from transacciones t where codigo = ?\n"
                    + ") LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, REQUEST_TOKEN);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                existe = 1;
            }

            if (existe == 0) {
                sql = "select 1 from ventas v where v.token_process_id = (\n"
                        + "  select id from transacciones t where codigo = ?\n"
                        + ") LIMIT 1";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setString(1, REQUEST_TOKEN);
                re = ps.executeQuery();
                while (re.next()) {
                    existe = 2;
                }
            }

            if (existe == 0) {
                sql = "SELECT 1 FROM transacciones WHERE codigo=? LIMIT 1";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setString(1, REQUEST_TOKEN);
                re = ps.executeQuery();
                while (re.next()) {
                    existe = 1;
                }
            }

        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return existe;
    }

    public Venta getVentaPorToken(String REQUEST_TOKEN) throws DAOException {
        Venta venta = null;
        final int PROVEEDOR_PICAFUEL = 1;
        int existe = 0;
        try {

            String sql = "select  v.surtidor, v.cara, v.manguera, v.grado, v.cantidad, v.importe, v.total, v.productos_id, p.familias, pf.codigo familias_desc \n"
                    + "from ventas_curso v \n"
                    + "inner join productos p on p.id=v.productos_id \n"
                    + "inner join productos_familias pf on pf.id=p.familias \n"
                    + "inner join transacciones t on t.id=v.token_process_id \n"
                    + "where  t.codigo=? and t.proveedores_id=?\n"
                    + "LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, REQUEST_TOKEN);
            ps.setInt(2, PROVEEDOR_PICAFUEL);
            ResultSet re = ps.executeQuery();
            while (re.next()) {

                venta = new Venta();
                venta.setSurtidorId(re.getInt("surtidor"));
                venta.setCara(re.getInt("cara"));
                venta.setManguera(re.getInt("manguera"));
                venta.setGrado(re.getInt("grado"));

                venta.setProductoId(re.getLong("productos_id"));
                venta.setFamiliaProductoId(re.getLong("familias"));
                venta.setFamiliaProductoDescripcion(re.getString("familias_desc"));

                venta.setActualPrecio(re.getLong("importe"));
                venta.setActualImporte(re.getLong("total"));
                venta.setActualVolumen(re.getLong("cantidad"));
                venta.setId(2);
                existe = 2;

            }

            if (existe == 0) {
                sql = "select  v.surtidor, v.cara, v.manguera, vd.productos_id, vd.cantidad_precisa, vd.total, vd.precio, pd.familias, pf.codigo familias_desc,  \n"
                        + "t.documento_identificacion_cliente, t.documento_identificacion_conductor, \n"
                        + "t.placa_vehiculo, t.porcentaje_descuento_cliente, t.monto_maximo, t.cantidad_maxima, t.cliente_nombre, t.conductor_nombre \n"
                        + "from ventas v \n"
                        + "inner join ventas_detalles vd on vd.ventas_id = v.id \n"
                        + "inner join productos pd on pd.id = vd.productos_id \n"
                        + "inner join productos_familias pf on pf.id=pd.familias \n"
                        + "inner join transacciones t on t.id=v.token_process_id \n"
                        + "where t.codigo = ? and t.proveedores_id = ?\n";

                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setString(1, REQUEST_TOKEN);
                ps.setInt(2, PROVEEDOR_PICAFUEL);
                re = ps.executeQuery();
                while (re.next()) {
                    venta = new Venta();
                    venta.setSurtidorId(re.getInt("surtidor"));
                    venta.setCara(re.getInt("cara"));
                    venta.setManguera(re.getInt("manguera"));
                    venta.setProductoId(re.getLong("productos_id"));
                    venta.setFamiliaProductoId(re.getLong("familias"));
                    venta.setFamiliaProductoDescripcion(re.getString("familias_desc"));
                    venta.setActualPrecio(re.getLong("precio"));
                    venta.setActualImporte(re.getLong("total"));
                    venta.setActualVolumen(re.getLong("cantidad_precisa"));

                    Autorizacion aut = new Autorizacion();
                    aut.setToken(REQUEST_TOKEN);
                    aut.setDocumentoIdentificacionCliente(re.getString("documento_identificacion_cliente"));
                    aut.setDocumentoIdentificacionConductor(re.getString("documento_identificacion_conductor"));
                    aut.setNombreCliente(re.getString("cliente_nombre"));
                    aut.setNombreConductor(re.getString("conductor_nombre"));
                    aut.setPlacaVehiculo(re.getString("placa_vehiculo"));
                    aut.setMontoMaximo(re.getDouble("monto_maximo"));
                    aut.setCantidadMaxima(re.getDouble("cantidad_maxima"));
                    venta.setAutorizacionToken(aut);

                    existe = 3;
                    venta.setId(3);

                }

            }

            if (existe == 0) {
                sql = "SELECT t.*, p.id productos_id, p.familias, pf.codigo familias_desc \n"
                        + "FROM transacciones t\n"
                        + "inner join surtidores_detalles sd on sd.cara =t.cara and t.grado = sd.grado and sd.surtidor = t.surtidor \n"
                        + "inner join productos p on p.id=sd.productos_id \n"
                        + "inner join productos_familias pf on pf.id=p.familias \n"
                        + "where t.codigo = ? and t.proveedores_id = ? LIMIT 1";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setString(1, REQUEST_TOKEN);
                ps.setInt(2, PROVEEDOR_PICAFUEL);
                re = ps.executeQuery();
                while (re.next()) {
                    venta = new Venta();
                    venta.setId(1);
                    venta.setVentaImporte(0);
                    venta.setSurtidorId(re.getInt("surtidor"));
                    venta.setCara(re.getInt("cara"));
                    venta.setGrado(re.getInt("grado"));

                    venta.setProductoId(re.getLong("productos_id"));
                    venta.setFamiliaProductoId(re.getLong("familias"));
                    venta.setFamiliaProductoDescripcion(re.getString("familias_desc"));

                    venta.setManguera(getMangueraByCaraAndGrado(re.getInt("surtidor"), re.getInt("cara"), re.getInt("grado")));
                    if (re.getString("usado") != null && re.getString("usado").equals("S")) {
                        venta.setVentaFinalizo(true);
                    }
                    venta.setManguera(getMangueraByCaraAndGrado(venta.getSurtidorId(), venta.getCara(), venta.getGrado()));

                    ProductoBean p = getProductoByGrado(venta.getCara(), venta.getGrado());
                    venta.setProductoId(p.getId());
                    venta.setFamiliaProductoId(p.getCategoriaId());
                    venta.setActualPrecio(p.getPrecioPlano());

                    Autorizacion aut = new Autorizacion();
                    aut.setToken(REQUEST_TOKEN);
                    aut.setDocumentoIdentificacionCliente(re.getString("documento_identificacion_cliente"));
                    aut.setDocumentoIdentificacionConductor(re.getString("documento_identificacion_conductor"));

                    aut.setNombreCliente(re.getString("cliente_nombre"));
                    aut.setNombreConductor(re.getString("conductor_nombre"));

                    aut.setPlacaVehiculo(re.getString("placa_vehiculo"));
                    aut.setMontoMaximo(re.getDouble("monto_maximo"));
                    aut.setCantidadMaxima(re.getDouble("cantidad_maxima"));
                    venta.setAutorizacionToken(aut);

                }
            }

        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return venta;
    }

    public Autorizacion getAutorizacionPorToken(String REQUEST_TOKEN) throws DAOException {
        Autorizacion aut = null;
        try {

            String sql = "SELECT * FROM transacciones WHERE codigo=? and proveedores_id=1 LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, REQUEST_TOKEN);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                aut = new Autorizacion();
                aut.setCara(re.getInt("cara"));
                aut.setGrado(re.getInt("grado"));
                aut.setDocumentoIdentificacionCliente(re.getString("documento_identificacion_cliente"));
                aut.setDocumentoIdentificacionConductor(re.getString("documento_identificacion_conductor"));
                aut.setNombreCliente(re.getString("cliente_nombre"));
                aut.setNombreConductor(re.getString("conductor_nombre"));
                aut.setPlacaVehiculo(re.getString("placa_vehiculo"));
                aut.setPrecioUnidad(re.getDouble("precio_unidad"));
                aut.setPorcentajeDescuentoCliente(re.getDouble("porcentaje_descuento_cliente"));
                aut.setMontoMaximo(re.getDouble("monto_maximo"));
                aut.setCantidadMaxima(re.getDouble("cantidad_maxima"));
            }

        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return aut;
    }

    public int getFactorInventario(int SURTIDOR) throws DAOException {
        int factor = 0;
        try {
            String sql = "SELECT factor_inventario FROM SURTIDORES WHERE Surtidor=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, SURTIDOR);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                factor = re.getInt("factor_inventario");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return factor;
    }

    public int getFactorVolumen(int SURTIDOR) throws DAOException {
        int factor = 0;
        try {
            String sql = "SELECT factor_volumen_parcial FROM SURTIDORES WHERE Surtidor=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, SURTIDOR);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                factor = re.getInt("factor_volumen_parcial");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return factor;
    }

    public int getFactorPredeterminacion(int SURTIDOR) throws DAOException {
        int factor = 0;
        try {

            String sql = "SELECT factor_predeterminacion_volumen FROM SURTIDORES WHERE Surtidor=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, SURTIDOR);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                factor = re.getInt("factor_predeterminacion_volumen");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return factor;
    }

    public int getGradoByCaraAndManguera(int surtidor, int cara, int manguera) throws DAOException {
        int grado = -1;
        try {

            String sql = "SELECT grado FROM surtidores_detalles WHERE surtidor=? and cara=? and manguera=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, surtidor);
            ps.setInt(2, cara);
            ps.setInt(3, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                grado = re.getInt("grado");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        NeoService.setLog("SI EL GRADO ES " + grado + " ENTONCES SURTIDOR=" + surtidor + " CARA= " + cara + " MANGUERA=" + manguera);

        return grado;
    }

    public int getMangueraByCaraAndGrado(long surtidor, int cara, int grado) throws DAOException {
        int manguera = -1;
        try {

            String sql = "SELECT manguera FROM surtidores_detalles WHERE surtidor=? and cara=? and grado=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, surtidor);
            ps.setInt(2, cara);
            ps.setInt(3, grado);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                manguera = re.getInt("manguera");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        NeoService.setLog("SI EL GRADO ES " + grado + " ENTONCES SURTIDOR=" + surtidor + " CARA= " + cara + " MANGUERA=" + manguera);

        return manguera;
    }

    public ProductoBean getProductoIdByCaraAndManguera(int surtidor, int cara, int manguera) throws DAOException {
        ProductoBean producto = null;
        try {

            String sql = "SELECT sd.productos_id, p.descripcion "
                    + "FROM surtidores_detalles sd "
                    + "INNER JOIN productos p ON p.id=sd.productos_id "
                    + "WHERE sd.surtidor=? and sd.cara=? and sd.manguera=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, surtidor);
            ps.setInt(2, cara);
            ps.setInt(3, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                producto = new ProductoBean();
                producto.setId(re.getInt("productos_id"));
                producto.setDescripcion(re.getString("descripcion"));
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return producto;
    }

    public int getFactorImporte(int SURTIDOR) throws DAOException {
        int factor = 0;
        try {
            String sql = "SELECT factor_importe_parcial FROM SURTIDORES WHERE Surtidor=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, SURTIDOR);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                factor = re.getInt("factor_importe_parcial");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return factor;
    }

    public int getFactorPrecio(int SURTIDOR) throws DAOException {
        int factor = 0;
        try {
            String sql = "SELECT factor_precio FROM SURTIDORES WHERE Surtidor=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, SURTIDOR);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                factor = re.getInt("factor_precio");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return factor;
    }

    public int getFactorVolumenByConfiguracionId(long configuracion) throws DAOException {
        int factor = 0;
        try {

            String sql = "select factor_volumen_parcial from surtidores_detalles sd"
                    + "inner join surtidores s on s.id=sd.surtidores_id\n"
                    + "where sd.id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, configuracion);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                factor = re.getInt("factor_volumen_parcial");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return factor;
    }

    public Totalizador[] getTotalizadoresByCara(Cara cara) throws DAOException {
        TreeMap<Integer, Totalizador> totalesList = new TreeMap<>();
        try {

            String sql = "SELECT "
                    + "surtidor, cara, manguera, grado, acumulado_venta, acumulado_cantidad, precio, precio2 "
                    + "FROM surtidores_detalles "
                    + "INNER JOIN productos p on p.id=productos_id "
                    + "WHERE cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara.getNumero());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                Totalizador total = new Totalizador();

                total.setCara(re.getInt("cara"));
                total.setManguera(re.getInt("manguera"));
                total.setGrado(re.getInt("grado"));
                total.setAcumuladoVenta(re.getLong("acumulado_venta"));
                total.setAcumuladoVolumen(re.getLong("acumulado_cantidad"));
                total.setPrecio(re.getLong("precio"));
                total.setPrecio2(re.getLong("precio2"));
                totalesList.put(re.getInt("grado"), total);

            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }

        Totalizador[] totales = new Totalizador[totalesList.size()];
        for (Map.Entry<Integer, Totalizador> entry : totalesList.entrySet()) {
            Totalizador total = entry.getValue();
            NeoService.setLog("TOTALIZADOR " + total.getCara() + ": GRADO " + total.getGrado() + " => " + total.getAcumuladoVenta() + "," + total.getAcumuladoVolumen());
            totales[total.getGrado()] = total;
        }
        return totales;
    }

    public boolean registrarAutorizacion(JsonObject request, long proveedorId, boolean preventa, String estado, int surtidor, int cara, int grado) {

        try {

            //TODO: Pendiente por recibir la fecha del servidor en el header
            String sql = "INSERT INTO transacciones( "
                    + "codigo, surtidor, cara, grado, proveedores_id, preventa, estado, "
                    + "documento_identificacion_cliente, documento_identificacion_conductor, "
                    + "placa_vehiculo, precio_unidad, porcentaje_descuento_cliente, "
                    + "monto_maximo, cantidad_maxima, "
                    + "usado, fecha_servidor, fecha_creacion, fecha_uso, metodo_pago, "
                    + "medio_autorizacion, serial_dispositivo, conductor_nombre, "
                    + "cliente_nombre, vehiculo_odometro, trama, codigo_tercero) "
                    + "VALUES("
                    + " ?, ?, ?, ?, ?, ?, ?, "
                    + " ?, ?,"
                    + " ?, ?, ?,"
                    + " ?, ?,"
                    + " ?, now(), now(), null, ?,"
                    + " ?, ?, ?, "
                    + " ?, null, ?, null);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);

            ps.setString(1, request.get("identificadorProceso").getAsString());
            ps.setInt(2, surtidor);
            ps.setInt(3, cara);
            ps.setInt(4, grado);
            ps.setLong(5, proveedorId);
            ps.setBoolean(6, preventa);
            ps.setString(7, estado);

            String parametro = "documentoIdentificacionCliente";
            int pos = 8;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "documentoIdentificacionConductor";
            pos = 9;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "placaVehiculo";
            pos = 10;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "precioUnidad";
            pos = 11;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "montoMaximo";
            pos = 13;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "cantidadMaxima";
            pos = 14;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            pos = 15;
            ps.setNull(pos, Types.NULL);

            parametro = "identificadorFormaPago";
            pos = 16;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setInt(pos, request.get(parametro).getAsInt());
            }

            ps.setString(17, "app");
            ps.setString(18, "APP");

            parametro = "nombreCondutor";
            pos = 19;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "nombreCliente";
            pos = 20;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            ps.setString(21, request.toString());

            ps.executeUpdate();

        } catch (PSQLException ex) {
            return false;
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }

    public void actualizaAutorizacion(long id) {

        try {

            String sql = "UPDATE transacciones SET usado='S', fecha_uso=now() WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Autorizacion getAutorizacion(int numero, int grado) {
        Autorizacion aut = null;
        try {
            String sql = "SELECT ID, CODIGO, proveedores_id, promotor_id  FROM transacciones WHERE CARA=? AND GRADO=? AND USADO IS NULL";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, numero);
            ps.setInt(2, grado);
            NeoService.setLog(NeoService.ANSI_YELLOW + ps.toString() + NeoService.ANSI_RESET);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                aut = new Autorizacion();
                aut.setId(re.getLong("id"));
                aut.setToken(re.getString("codigo"));
                aut.setProveedorId(re.getLong("proveedores_id"));
                aut.setPromotorId(re.getLong("promotor_id"));
            }
        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return aut;
    }

    public int getCantidad(int numero, int grado) {
        int cantidadMaxima = -1;
        try {

            String sql = "SELECT cantidad_maxima FROM transacciones WHERE CARA=? AND GRADO=? AND USADO IS NULL";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, numero);
            ps.setInt(2, grado);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                cantidadMaxima = re.getInt("cantidad_maxima");
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return cantidadMaxima;
    }

    public PredeterminadaBean getPredeterminada(int cara) {
        PredeterminadaBean predeterminada = null;
        try {

            String sql = "SELECT * FROM ventas_predeterminadas WHERE CARA=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                predeterminada = new PredeterminadaBean();
                predeterminada.setPrecio(re.getString("tipo").equals("P"));
                predeterminada.setValor(re.getFloat("valor"));
                if (NeoService.VALIDA_PREDETERMINACION_PORFAMILIA) {
                    predeterminada.setFamiliaProducto(re.getLong("familias"));
                }
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return predeterminada;
    }

    public int getCantidadMonto(int numero, int grado) {
        int cantidadMaxima = -1;
        try {

            String sql = "SELECT monto_maximo FROM transacciones WHERE CARA=? AND GRADO=? AND USADO IS NULL";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, numero);
            ps.setInt(2, grado);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                cantidadMaxima = (int) re.getDouble("monto_maximo");
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return cantidadMaxima;
    }

    public void guardarEstado(Surtidor surtidor, Cara cara, int grado) {
        try {

            String sql = "UPDATE surtidores_detalles "
                    + "SET estado=?, estado_publico=? "
                    + "WHERE surtidor=? and cara=? and grado = ?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, cara.getEstado());
            ps.setLong(2, cara.getPublicEstadoId());
            ps.setLong(3, surtidor.getId());
            ps.setLong(4, cara.getNumero());
            ps.setLong(5, grado);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public JsonObject getParametrosRumbo() {
        JsonObject resultado = null;
        try {

            String sql = "select valor from wacher_parametros where codigo='LAZO_RUMBO'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                resultado = new Gson().fromJson(re.getString("valor"), JsonObject.class);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException | JsonSyntaxException ex) {
            NeoService.setLog(ex.getMessage());
        }
        return resultado;
    }

    public ArrayList<SaltoLecturaBean> getSaltoLectura(SurtidorInventario invt) {
        ArrayList<SaltoLecturaBean> saltos = new ArrayList<>();
        try {

            String sql = "SELECT VALOR, CANTIDAD FROM SALTOS_LECTURAS WHERE CARA=? AND MANGUERA=? AND IMPRESO IS NULL";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, invt.getCara());
            ps.setInt(2, invt.getManguera());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                SaltoLecturaBean salto = new SaltoLecturaBean();
                salto.setValor(re.getFloat("valor"));
                salto.setCantidad(re.getFloat("cantidad"));
                saltos.add(salto);
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return saltos;
    }

    public void actualizaReciboPlaca(Recibo recibo) {
        try {

            String sql = "UPDATE ventas SET placa=? WHERE id = ?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, recibo.getPlaca());
            ps.setLong(2, recibo.getNumero());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public void actualizaReciboMedio(Recibo recibo) {
        try {

            String sql = "UPDATE ventas SET medios_pagos_id=?, voucher=? WHERE id = ?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, recibo.getMedio());
            ps.setString(2, recibo.getVoucher());
            ps.setLong(3, recibo.getNumero());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public void actualizaSaltoLectura() {
        try {

            String sql = "UPDATE SALTOS_LECTURAS SET impreso='S'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public int getParametroInt(String codigo) {
        int valor = 0;
        try {
            String sql = "SELECT valor FROM PARAMETROS WHERE codigo=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                valor = re.getInt("valor");
            }
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (getParametroInt) Exception]: " + s.getMessage());
        }
        return valor;
    }

    public void borrarPredeterminada(int cara) {
        try {

            String sql = "DELETE from ventas_predeterminadas WHERE cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public void borrarPredeterminada(int cara, long familiaProducto) {
        try {

            String sql = "DELETE from ventas_predeterminadas WHERE cara=? and familias=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ps.setLong(2, familiaProducto);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public boolean registrarAutorizacion(JsonObject request, int cara, int grado, String medio, String serial, String odometro) {

        try {

            //TODO: Pendiente por recibir la fecha del servidor en el header
            String sql = "INSERT INTO autorizaciones(\n"
                    + "            codigo, cara, grado, documento_identificacion_cliente, documento_identificacion_conductor, \n"
                    + "            placa_vehiculo, precio_unidad, porcentaje_descuento_cliente, \n"
                    + "            monto_maximo, cantidad_maxima, fecha_creacion, metodo_pago, medio_autorizacion, serial_dispositivo,"
                    + "            conductor_nombre, cliente_nombre, vehiculo_odometro, trama)\n"
                    + "    VALUES (?, ?, ?, ?, ?, \n"
                    + "            ?, ?, ?, \n"
                    + "            ?, ?, now(), ?, ?, ?, "
                    + "            ?, ?, ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);

            ps.setString(1, request.get("identificadorProceso").getAsString());
            ps.setInt(2, cara);
            ps.setInt(3, grado);

            String parametro = "documentoIdentificacionCliente";
            int pos = 4;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "documentoIdentificacionConductor";
            pos = 5;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "placaVehiculo";
            pos = 6;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "precioUnidad";
            pos = 7;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "porcentajeDescuentoCliente";
            pos = 8;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "montoMaximo";
            pos = 9;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "cantidadMaxima";
            pos = 10;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "identificadorFormaPago";
            pos = 11;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setInt(pos, request.get(parametro).getAsInt());
            }

            ps.setString(12, medio);
            ps.setString(13, serial);

            parametro = "nombreConductor";
            pos = 14;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            parametro = "nombreCliente";
            pos = 15;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            ps.setString(16, odometro);
            ps.setString(17, request.toString());

            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog("[ERROR (SurtidorDao)] registrarAutorizacion -> PSQLException : " + ex.getMessage());
            return false;
        } catch (SQLException ex) {
            NeoService.setLog("[ERROR (SurtidorDao)] registrarAutorizacion -> SQLException : " + ex.getMessage());
        }

        return true;
    }

    public int getSurtidorByConfiguracionID(long id) {
        int surtidor = -1;
        try {

            String sql = "SELECT surtidor FROM surtidores_detalles WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                surtidor = re.getInt("surtidor");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return surtidor;
    }

    public void borrarAutorizacionesEnCaraTiempo(int cara, int segundos) {
        try {

            String sql = "update transacciones set usado='N' WHERE cara=? and usado is null and proveedores_id=1 and fecha_creacion <= (now()-'" + segundos + " second'::interval)";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);

            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
    }

    public int getAutorizacionesEnCaraVencimiento(int cara, int segundos) {
        int i = 0;
        try {

            String sql = "select count(1) from transacciones WHERE cara=? and usado is null and proveedores_id=1 and fecha_creacion <= (now()-'" + segundos + " second'::interval)";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);

            ResultSet re = ps.executeQuery();
            while (re.next()) {
                i = re.getInt(1);
            }

        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return i;
    }

    public int getSurtidorPorNumeroCara(int cara) {
        int surtidor = 1;
        try {

            String sql = "SELECT surtidor FROM surtidores_detalles WHERE cara=? LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                surtidor = re.getInt("surtidor");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return surtidor;
    }

    public CatalogoBean getBloqueo(int surtidor, int cara, int grado) {
        CatalogoBean bloqueo = null;
        try {

            String sql = "SELECT bloqueo, manguera, motivo_bloqueo FROM surtidores_detalles WHERE surtidor=? and cara=? and grado=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, surtidor);
            ps.setInt(2, cara);
            ps.setInt(3, grado);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                if (re.getString("bloqueo").equals("S")) {
                    bloqueo = new CatalogoBean();
                    bloqueo.setValor(re.getString("manguera"));
                    bloqueo.setDescripcion(re.getString("motivo_bloqueo"));
                }
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return bloqueo;
    }

    private ArrayList<MediosPagosBean> getMediosPagos(long numero) {

        ArrayList<MediosPagosBean> medios = new ArrayList<>();
        try {

            String sql = "SELECT mp.id, mp.descripcion, v.valor_total, v.numero_comprobante "
                    + "FROM ventas_medios_pagos v "
                    + "INNER JOIN medios_pagos mp on mp.id=medios_pagos_id "
                    + "WHERE ventas_id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, numero);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                MediosPagosBean mp = new MediosPagosBean();
                mp.setValor(re.getFloat("valor_total"));
                mp.setDescripcion(re.getString("descripcion"));
                mp.setVoucher(re.getString("numero_comprobante"));
                medios.add(mp);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return medios;
    }

    public Venta getVentaPorCara(Cara cara) throws DAOException {
        Venta venta = new Venta();
        try {

            String sql = "SELECT  v.* \n"
                    + "FROM ventas_curso v \n"
                    + "WHERE cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, cara.getNumero());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                venta.setId(re.getLong("id"));
                venta.setSurtidorTipoId(Venta.ORIGEN_TIPO.SURTIDOR);
                venta.setConfiguracionId(re.getLong("origen_id"));
                venta.setOperadorId(re.getLong("operario_id"));
                venta.setClienteId(re.getLong("cliente_id"));
                venta.setSurtidorId(re.getInt("surtidor"));
                venta.setCara(re.getInt("cara"));
                venta.setManguera(re.getInt("manguera"));
                venta.setGrado(re.getInt("grado"));
                venta.setProductoId(re.getLong("productos_id"));
                venta.setActualVolumen(re.getLong("cantidad"));
                venta.setActualPrecio(re.getLong("importe"));
                venta.setActualImporte(re.getLong("total"));
                venta.setFechaInicio(re.getDate("fecha_inicio"));
                venta.setAcumuladoVolumenInicial(re.getLong("acumulado_cantidad"));
                venta.setAcumuladoImporteInicial(re.getLong("acumulado_importe"));
                venta.setSincronizado(re.getInt("sincronizado"));
                venta.setJornadaId(re.getLong("jornada_id"));
                venta.setPlaca(re.getString("placa"));

                if (re.getLong("token_process_id") != 0) {
                    Autorizacion aut = new Autorizacion();
                    aut.setId(re.getLong("token_process_id"));
                    venta.setAutorizacionToken(aut);
                } else {
                    venta.setAutorizacionToken(null);
                }
            }

        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return venta;
    }

    public String existUsuarioId(long id) {
        String result = null;
        try {

            String sql = "SELECT NOMBRE FROM PERSONAS WHERE ID=? AND ESTADO='A'";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                result = re.getString("nombre");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return result;
    }

    public long existUsuarioSinEstado(String data) {
        long result = -1;
        try {

            String sql = "SELECT ID FROM PERSONAS WHERE TAG=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, data);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                result = re.getLong("id");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return result;
    }

    public boolean existeUsuarioEnTurno(Long id) {
        boolean result = false;
        try {

            String sql = "select p.id from personas p \n"
                    + "inner join jornadas j on j.personas_id = p.id \n"
                    + "where p.id=? and p.estado = 'A'";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                result = true;
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return result;
    }

    public String existUsuarioSinEstadoNombre(String data) {
        String nombre = "";
        try {

            String sql = "SELECT NOMBRE FROM PERSONAS WHERE TAG=?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, data);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                nombre = re.getString("nombre");
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return nombre;
    }

    public String getParametrosWacher(String codigo) throws DAOException {
        String requiere = null;
        try {

            String sql = "SELECT valor FROM wacher_parametros WHERE codigo=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setString(1, codigo);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                requiere = re.getString("valor");
            }
        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
        return requiere;
    }

    public boolean registrarAutorizacion(JsonObject request, long proveedorId, boolean preventa, String estado, int surtidor, int cara, int grado, String medio, String identificador) {

        try {

            String sql = "INSERT INTO transacciones( "
                    + "codigo, surtidor, cara, grado, proveedores_id, preventa, estado, "
                    + "documento_identificacion_cliente, documento_identificacion_conductor, "
                    + "placa_vehiculo, precio_unidad, porcentaje_descuento_cliente, "
                    + "monto_maximo, cantidad_maxima, "
                    + "usado, fecha_servidor, fecha_creacion, fecha_uso, metodo_pago, "
                    + "medio_autorizacion, serial_dispositivo, conductor_nombre, "
                    + "cliente_nombre, vehiculo_odometro, trama, codigo_tercero) "
                    + "VALUES("
                    + " ?, ?, ?, ?, ?, ?, ?, "
                    + " ?, ?,"
                    + " ?, ?, ?,"
                    + " ?, ?,"
                    + " ?, now(), now(), null, ?,"
                    + " ?, ?, ?, "
                    + " ?, null, ?, null);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);

            ps.setString(1, request.get("identificadorProceso").getAsString());
            ps.setInt(2, surtidor);
            ps.setInt(3, cara);
            ps.setInt(4, grado);
            ps.setLong(5, proveedorId);
            ps.setBoolean(6, preventa);
            ps.setString(7, estado);

            String parametro = "documentoIdentificacionCliente";
            int pos = 8;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }
            parametro = "documentoIdentificacionConductor";
            pos = 9;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }
            parametro = "placaVehiculo";
            pos = 10;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }
            parametro = "precioUnidad";
            pos = 11;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            ps.setDouble(12, 0);
            parametro = "montoMaximo";
            pos = 13;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "cantidadMaxima";
            pos = 14;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setDouble(pos, request.get(parametro).getAsDouble());
            }

            parametro = "usado";
            pos = 15;
            ps.setNull(pos, Types.NULL);

            parametro = "identificadorFormaPago";
            pos = 16;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setInt(pos, 1);
            } else {
                ps.setInt(pos, request.get(parametro).getAsInt());
            }
            //ORIGINALMENTE ESTABA 'APP'
            ps.setString(17, medio);
            ps.setString(18, identificador);
            parametro = "nombreCondutor";
            pos = 19;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }
            parametro = "nombreCliente";
            pos = 20;
            if (request.get(parametro) == null || request.get(parametro).isJsonNull()) {
                ps.setNull(pos, Types.NULL);
            } else {
                ps.setString(pos, request.get(parametro).getAsString());
            }

            ps.setString(21, request.toString());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            NeoService.setLog("com.core.database.impl.SurtidorDao.registrarAutorizacion() ERROR" + "\r\n");
            NeoService.setLog(ex.getMessage());
            NeoService.setLog("\r\n");
            return false;
        } catch (SQLException ex) {
            NeoService.setLog("com.core.database.impl.SurtidorDao.registrarAutorizacion() ERROR" + "\r\n");
            NeoService.setLog(ex.getMessage());
            NeoService.setLog("\r\n");
            return false;
        }
        return true;
    }

    public LinkedHashSet<TareaProgramada> getTareasProgramadas() throws DAOException {

        LinkedHashSet<TareaProgramada> tareas = new LinkedHashSet<>();
        try {

            String sql = "SELECT * FROM cambio_precio WHERE estado='P' and fecha<=now()";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {

                if (re.getInt("cara") != 0) {

                    TareaProgramada tarea = new TareaProgramada();
                    tarea.setId(re.getLong("id"));
                    tarea.setPrecioOriginal(re.getInt("precio_original"));
                    tarea.setProducto(re.getInt("producto"));
                    tarea.setCara(re.getInt("cara"));
                    tarea.setManguera(re.getInt("manguera"));
                    tarea.setGrado(re.getInt("grado"));
                    tareas.add(tarea);

                } else {

                    String sql2 = "SELECT * FROM surtidores_detalles where productos_id=?";
                    PreparedStatement ps2 = NeoService.obtenerConexion().prepareCall(sql2);
                    ps2.setLong(1, re.getInt("producto"));
                    ResultSet re2 = ps2.executeQuery();

                    while (re2.next()) {

                        TareaProgramada tarea = new TareaProgramada();
                        tarea.setId(re.getLong("id"));
                        tarea.setPrecioOriginal(re.getInt("precio_original"));
                        tarea.setProducto(re.getInt("producto"));
                        tarea.setCara(re2.getInt("cara"));
                        tarea.setManguera(re2.getInt("manguera"));
                        tarea.setGrado(re2.getInt("grado"));

                        String sql6 = "update cambio_precio set estado = 'X' WHERE id=?";
                        PreparedStatement ps6 = NeoService.obtenerConexion().prepareCall(sql6);
                        ps6.setLong(1, re.getLong("id"));
                        ps6.executeUpdate();

                        String sql3 = "INSERT INTO public.cambio_precio(tipo,producto,precio_original,cara,manguera,grado,fecha,estado) "
                                + "VALUES(1,?,?,?,?,?,now(),'P') RETURNING id";
                        PreparedStatement ps3 = NeoService.obtenerConexion().prepareCall(sql3);
                        ps3.setLong(1, tarea.getProducto());
                        ps3.setLong(2, tarea.getPrecioOriginal());
                        ps3.setLong(3, tarea.getCara());
                        ps3.setLong(4, tarea.getManguera());
                        ps3.setLong(5, tarea.getGrado());
                        ResultSet re3 = ps3.executeQuery();

                        while (re3.next()) {
                            tarea.setId(re3.getLong("id"));
                        }

                        tareas.add(tarea);

                        // INSERT INTO public.cambio_precio (tipo,producto,precio_original,cara,manguera,grado,fecha,estado) VALUES
                        //(1,58,8500,NULL,NULL,NULL,'2021-09-11 03:47:00.000','P'),
                    }
                }
            }

        } catch (PSQLException ex) {

            if (ex.getMessage().contains("cambio_precio")) {

                try {
                    String sql = "CREATE TABLE public.cambio_precio (\n"
                            + "	id serial NOT NULL,\n"
                            + "	tipo int8 NOT NULL,\n"
                            + "	producto int8 NOT NULL,\n"
                            + "	precio_original int4 NOT NULL,\n"
                            + "	cara int8 NULL,\n"
                            + "	manguera int8 NULL,\n"
                            + "	grado int8 NULL,\n"
                            + "	fecha timestamp NOT NULL,\n"
                            + "	aplicado timestamp NULL,\n"
                            + "	estado varchar(1) NOT NULL, \n"
                            + "	CONSTRAINT tareas_programada_pk PRIMARY KEY (id),\n"
                            + "	CONSTRAINT tareas_programada_fk FOREIGN KEY (producto) REFERENCES public.productos(id)\n"
                            + ");";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.executeUpdate();
                    NeoService.setLog("TABLA DE tareas_programadas CREADA CON EXITO");
                } catch (SQLException ex1) {
                    Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tareas;
    }

    public void setTareaProgramada(long id) throws DAOException {
        try {

            String sql = "update cambio_precio set estado = 'R', aplicado=now() WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DAOException("ERROR: " + ex.getMessage());
        }
    }

    public void actualizarTotalizadores(Manguera value, Manguera value2) throws DAOException {
        NeoService.setLog("TOTALIZADORES ES volumen " + value.getRegistroSistemaVolumen());
        NeoService.setLog("TOTALIZADORES ES ventas  " + value.getRegistroSistemaVentas());
        NeoService.setLog("TOTALIZADORES ES volumen " + value.getRegistroSurtidorVolumen());
        NeoService.setLog("TOTALIZADORES ES ventas  " + value.getRegistroSurtidorVentas());

        NeoService.setLog("TOTALIZADORES II ES volumen " + value2.getRegistroSistemaVolumen());
        NeoService.setLog("TOTALIZADORES II ES ventas  " + value2.getRegistroSistemaVentas());
        NeoService.setLog("TOTALIZADORES II ES volumen " + value2.getRegistroSurtidorVolumen());
        NeoService.setLog("TOTALIZADORES II ES ventas  " + value2.getRegistroSurtidorVentas());

        try {

            String sql = "update ventas_curso set grado=?, productos_id=?, manguera=? where cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, value.getGrado());
            ps.setLong(2, value.getProductoId());
            ps.setLong(3, value.getId());
            ps.setLong(4, value.getCara());
            ps.executeUpdate();

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }

    }

    public int homologacionCara(int cara) {
        try {
            if (existeColumna("surtidores_detalles", "conexion")) {
                String sql = "SELECT conexion FROM surtidores_detalles WHERE cara=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setInt(1, cara);
                ResultSet re = ps.executeQuery();
                while (re.next()) {
                    if (re.getInt("conexion") > 0) {
                        cara = re.getInt("conexion");
                    }
                }
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (DAOException | Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return cara;

    }

    public void registrarTotalizadores(int surtidor, int cara, Long grupoJornada, Manguera manguera) throws DAOException {
        try {

            boolean registrado = false;
            String sql = "SELECT 1 FROM jornadas_inventarios WHERE grupo_jornada=? and manguera =?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, grupoJornada);
            ps.setLong(2, manguera.getId());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                registrado = true;
            }
            if (!registrado) {
                if (manguera.getRegistroSurtidorVentas() != 0) {
                    NeoService.setLog("INSERTANDO JORNADAS INVENTARIOS POR REGISTRO DE MANGUERA");
                    sql = "INSERT INTO jornadas_inventarios\n"
                            + "(grupo_jornada, fecha_inicio, surtidor, cara, manguera, grado, "
                            + "productos_id, familia_id, "
                            + "acumulado_venta_inicial, acumulado_cantidad_inicial, precio_inicial)\n"
                            + "VALUES("
                            + "?, ?, ?, ?, ?, ?, "
                            + "?, ?, "
                            + "?, ?, ?);";
                    ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, grupoJornada);
                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                    ps.setLong(3, surtidor);
                    ps.setLong(4, cara);
                    ps.setLong(5, manguera.getId());
                    ps.setLong(6, manguera.getGrado());
                    ps.setLong(7, manguera.getProductoId());
                    ps.setLong(8, manguera.getProductoFamiliaId());
                    ps.setLong(9, manguera.getRegistroSurtidorVentas());
                    ps.setLong(10, manguera.getRegistroSurtidorVolumen());
                    ps.setLong(11, manguera.getProductoPrecio());
                    ps.executeUpdate();
                } else {
                    long totalizadorVolumenUltimaVenta = getTotalizadorVolumenUltimaVenta(manguera.getId());
                    long totalizadorVentaUltimaVenta = getTotalizadorVentaUltimaVenta(manguera.getId());
                    long precio = getPrecioVentaUltimaVenta(manguera.getId());

                    sql = "INSERT INTO jornadas_inventarios\n"
                            + "(grupo_jornada, fecha_inicio, surtidor, cara, manguera, grado, "
                            + "productos_id, familia_id, "
                            + "acumulado_venta_inicial, acumulado_cantidad_inicial, precio_inicial)\n"
                            + "VALUES("
                            + "?, ?, ?, ?, ?, ?, "
                            + "?, ?, "
                            + "?, ?, ?);";
                    ps = NeoService.obtenerConexion().prepareCall(sql);
                    ps.setLong(1, grupoJornada);
                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                    ps.setLong(3, surtidor);
                    ps.setLong(4, cara);
                    ps.setLong(5, manguera.getId());
                    ps.setLong(6, manguera.getGrado());
                    ps.setLong(7, manguera.getProductoId());
                    ps.setLong(8, manguera.getProductoFamiliaId());
                    ps.setLong(9, totalizadorVolumenUltimaVenta);
                    ps.setLong(10, totalizadorVentaUltimaVenta);
                    ps.setLong(11, precio);
                    ps.executeUpdate();
                }
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }
    }

    public void registrarTotalizadoresFinales(Long grupoJornada, Manguera manguera) throws DAOException {
        try {
            boolean registrado = false;
            String sql = "SELECT 1 FROM jornadas_inventarios WHERE grupo_jornada=? and manguera =? and fecha_fin is not null";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, grupoJornada);
            ps.setLong(2, manguera.getId());
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                registrado = true;
            }

            if (!registrado) {
                sql = "UPDATE jornadas_inventarios\n"
                        + "SET fecha_fin=?, acumulado_venta_final=?, acumulado_cantidad_final=?, precio_final=?\n"
                        + "WHERE manguera=? and fecha_fin is null";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                ps.setLong(2, manguera.getRegistroSurtidorVentas());
                ps.setLong(3, manguera.getRegistroSurtidorVolumen());
                ps.setLong(4, manguera.getProductoPrecio());
                ps.setLong(5, manguera.getId());
                ps.executeUpdate();
            }
        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }
    }

    public void cerrarJornada(long grupoJornada, JsonArray jornadacerradas) throws DAOException {

        try {

            JsonObject jsonActual = null;
            long id = 0;
            String sql1 = "SELECT id, atributos FROM jornadas ORDER BY fecha_inicio asc LIMIT 1";
            PreparedStatement ps1 = NeoService.obtenerConexion().prepareCall(sql1);
            NeoService.setLog("FUNCION DE CIERRE 1");
            ResultSet re1 = ps1.executeQuery();
            while (re1.next()) {
                id = re1.getLong("id");
                jsonActual = Main.GSON.fromJson(re1.getString("atributos"), JsonObject.class);
            }

            if (jsonActual != null) {
                jsonActual.add("totalizadoresFinales", jornadacerradas);

                String sql2 = "UPDATE jornadas SET atributos=?::json WHERE id=?";
                PreparedStatement ps2 = NeoService.obtenerConexion().prepareCall(sql2);
                ps2.setString(1, jsonActual.toString());
                ps2.setLong(2, id);
                NeoService.setLog("FUNCION DE CIERRE 2");
                ps2.executeUpdate();

                String sql3 = "UPDATE jornadas SET fecha_fin=?";
                PreparedStatement ps3 = NeoService.obtenerConexion().prepareCall(sql3);
                ps3.setTimestamp(1, new Timestamp(new Date().getTime()));
                NeoService.setLog("FUNCION DE CIERRE 3");
                ps3.executeUpdate();

            }

            String sql4 = "SELECT * FROM jornadas";
            PreparedStatement ps4 = NeoService.obtenerConexion().prepareCall(sql4);
            NeoService.setLog("FUNCION DE CIERRE 4");
            ResultSet re4 = ps4.executeQuery();
            while (re4.next()) {
                long jornadaId = re4.getLong("id");
                long personasId = re4.getLong("personas_id");
                Date fechaInicio = new Date(re4.getTimestamp("fecha_inicio").getTime());
                Date fechaFinal = new Date(re4.getTimestamp("fecha_fin").getTime());
                int sincronizado = re4.getInt("sincronizado");
                long grupoJornadaDB = re4.getLong("grupo_jornada");
                float saldo = re4.getFloat("saldo");
                long surtidor = re4.getLong("surtidores_id");
                long equiposId = re4.getLong("equipos_id");
                JsonObject atributos = Main.GSON.fromJson(re4.getString("atributos"), JsonObject.class);

                long jornadaHistorica = 0;
                String sql5 = "select max(id) from jornadas_hist";
                PreparedStatement ps5 = NeoService.obtenerConexion().prepareCall(sql5);
                NeoService.setLog("FUNCION DE CIERRE 5");
                ResultSet re5 = ps5.executeQuery();
                while (re5.next()) {
                    jornadaHistorica = re5.getLong(1);
                }
                jornadaHistorica++;

                String sql6 = "INSERT INTO jornadas_hist\n"
                        + "(id, jornada_id, personas_id, fecha_inicio, fecha_fin, sincronizado, grupo_jornada, saldo, surtidores_id, equipos_id, atributos)\n"
                        + "VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::json);";
                PreparedStatement ps6 = NeoService.obtenerConexion().prepareCall(sql6);
                ps6.setLong(1, jornadaHistorica);
                ps6.setLong(2, jornadaId);
                ps6.setLong(3, personasId);
                ps6.setTimestamp(4, new Timestamp(fechaInicio.getTime()));
                ps6.setTimestamp(5, new Timestamp(fechaFinal.getTime()));
                ps6.setLong(6, sincronizado);
                ps6.setLong(7, grupoJornadaDB);
                ps6.setFloat(8, saldo);
                if (surtidor != 0) {
                    ps6.setFloat(9, surtidor);
                } else {
                    ps6.setNull(9, Types.NULL);
                }
                ps6.setLong(10, equiposId);
                ps6.setString(11, atributos.toString());
                NeoService.setLog("FUNCION DE CIERRE 6");
                ps6.executeUpdate();

                String sql7 = "DELETE FROM jornadas where id=?";
                PreparedStatement ps7 = NeoService.obtenerConexion().prepareCall(sql7);
                ps7.setLong(1, jornadaId);
                NeoService.setLog("FUNCION DE CIERRE 7");
                ps7.executeUpdate();

            }

            NeoService.setLog("FUNCION DE CIERRE COMPLETADA");
        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException | JsonSyntaxException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }

    }

    public void registrarInicioJornada(long grupoJornada, long persona, float saldo, JsonArray jsonarray) throws DAOException {
        try {

            boolean registrado = false;
            String sql = "SELECT 1 FROM jornadas";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                registrado = true;
            }
            if (!registrado) {
                sql = "INSERT INTO jornadas\n"
                        + "(id, personas_id, fecha_inicio, fecha_fin, sincronizado, grupo_jornada, saldo, surtidores_id, equipos_id, atributos)\n"
                        + "VALUES(nextval('jornadas_id'), ?, ?, null, 0, ?, ?, null, ?, ?::json)";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, persona);
                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                ps.setLong(3, grupoJornada);
                ps.setFloat(4, saldo);
                ps.setLong(5, Main.credencial.getId());

                JsonObject json = new JsonObject();
                json.addProperty("saldo", saldo);
                json.add("totalizadoresIniciales", jsonarray);
                json.addProperty("turnos_id", grupoJornada);

                ps.setString(6, json.toString());
                ps.executeUpdate();
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }

    }

    public void bloqueoMangueras() throws DAOException {
        try {

            String sql = "UPDATE surtidores_detalles SET bloqueo='S', motivo_bloqueo='CIERRE DE TURNO'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }
    }

    public void desbloqueoMangueras() throws DAOException {
        try {

            String sql = "UPDATE surtidores_detalles SET bloqueo=null, motivo_bloqueo=null";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.executeUpdate();

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }
    }

    public Totalizador[] getTotalizadoresCompletoVentas(long turno, int manguera) throws DAOException {
        Totalizador[] totalizadores = null;
        try {
            String sql = "select surtidor, grado, cara, manguera, p.descripcion, count(1) numero_venta,\n"
                    + "min(vd.precio) precio,\n"
                    + "min(vd.acum_vol_inicial) totalizador_volumen_inicial, max(vd.acum_vol_final) totalizador_volumen_final,  \n"
                    + "min(vd.acum_ven_inicial) totalizador_ventas_inicial , max(vd.acum_ven_final) totalizador_ventas_final\n"
                    + "from ventas v\n"
                    + "inner join ventas_detalles vd on vd.ventas_id = v.id\n"
                    + "inner join productos p on p.id = vd.productos_id \n"
                    + "where jornada_id = ? and manguera = ? \n"
                    + "group by 1, 2, 3, 4, 5\n"
                    + "order by 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, turno);
            ps.setInt(2, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {

                Totalizador totalInicial = new Totalizador();
                totalInicial.setCara(re.getInt("cara"));
                totalInicial.setManguera(re.getInt("manguera"));
                totalInicial.setGrado(re.getInt("grado"));
                totalInicial.setPrecio(re.getLong("precio"));
                totalInicial.setPrecio2(re.getLong("precio"));

                totalInicial.setAcumuladoVenta(re.getLong("totalizador_ventas_inicial"));
                totalInicial.setAcumuladoVolumen(re.getLong("totalizador_volumen_inicial"));

                Totalizador totalizadorFinal = new Totalizador();
                totalizadorFinal.setCara(re.getInt("cara"));
                totalizadorFinal.setManguera(re.getInt("manguera"));
                totalizadorFinal.setGrado(re.getInt("grado"));
                totalizadorFinal.setPrecio(re.getLong("precio"));
                totalizadorFinal.setPrecio2(re.getLong("precio"));

                totalizadorFinal.setAcumuladoVenta(re.getLong("totalizador_ventas_final"));
                totalizadorFinal.setAcumuladoVolumen(re.getLong("totalizador_volumen_final"));

                totalizadores = new Totalizador[2];
                totalizadores[0] = totalInicial;
                totalizadores[1] = totalizadorFinal;
            }
            if (totalizadores == null) {
                sql = "select ji.surtidor, ji.grado, ji.cara, ji.manguera, p.descripcion, 0 numero_venta, precio_inicial precio,\n"
                        + "acumulado_venta_inicial totalizador_ventas_inicial, acumulado_cantidad_inicial totalizador_volumen_inicial,\n"
                        + "acumulado_venta_final totalizador_ventas_final, acumulado_cantidad_final totalizador_volumen_final \n"
                        + "from surtidores_detalles sd \n"
                        + "inner join jornadas_inventarios ji on ji.manguera = sd.manguera \n"
                        + "inner join productos p on p.id = sd.productos_id  \n"
                        + "where ji.grupo_jornada  = ? and sd.manguera = ?";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.setLong(1, turno);
                ps.setInt(2, manguera);
                re = ps.executeQuery();
                while (re.next()) {

                    Totalizador totalizador = new Totalizador();
                    totalizador.setCara(re.getInt("cara"));
                    totalizador.setManguera(re.getInt("manguera"));
                    totalizador.setGrado(re.getInt("grado"));

                    if (re.getLong("precio") != 0) {
                        totalizador.setPrecio(re.getLong("precio"));
                        totalizador.setPrecio2(re.getLong("precio"));
                    } else {
                        long precio = getPrecioPorManguera(manguera);
                        totalizador.setPrecio(precio);
                        totalizador.setPrecio2(precio);
                    }
                    if (re.getLong("totalizador_ventas_final") != 0) {
                        totalizador.setAcumuladoVenta(re.getLong("totalizador_ventas_final"));
                    } else {
                        totalizador.setAcumuladoVenta(getTotalizadorVentaSurtidor(manguera));
                    }
                    if (re.getLong("totalizador_volumen_final") != 0) {
                        totalizador.setAcumuladoVolumen(re.getLong("totalizador_volumen_final"));
                    } else {
                        totalizador.setAcumuladoVolumen(getTotalizadorVolumenSurtidor(manguera));
                    }

                    totalizadores = new Totalizador[2];
                    totalizadores[0] = totalizador;
                    totalizadores[1] = totalizador;
                }
            }

        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("ERROR: " + ex.getMessage());
        }

        return totalizadores;
    }

    public long getPrecioPorManguera(int manguera) {
        long totalizador = 0;
        try {

            String sql = "select p.precio \n"
                    + "from surtidores_detalles sd \n"
                    + "inner join productos p on p.id = sd.productos_id \n"
                    + "where manguera = ?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                totalizador = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totalizador;
    }

    public long getTotalizadorVolumenSurtidor(int manguera) {
        long totalizador = 0;
        try {

            String sql = "select acumulado_cantidad from surtidores_detalles sd where manguera = ?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                totalizador = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totalizador;
    }

    public long getTotalizadorVentaSurtidor(int manguera) {
        long totalizador = 0;
        try {

            String sql = "select acumulado_venta from surtidores_detalles sd where manguera = ?";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setInt(1, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                totalizador = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totalizador;
    }

    public long getUltimoGrupoJornada() {
        long grupoJornada = 0;
        try {
            String sql = "select max(grupo_jornada) from jornadas_inventarios";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                grupoJornada = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return grupoJornada;
    }

    private long getTotalizadorVolumenUltimaVenta(int manguera) {
        long totalizador = 0;
        try {

            String sql = "select vd.acum_vol_inicial  \n"
                    + "from ventas v\n"
                    + "inner join ventas_detalles vd on v.id = vd.ventas_id \n"
                    + "where manguera = ?\n"
                    + "order by v.id desc \n"
                    + "limit 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                totalizador = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totalizador;
    }

    private long getTotalizadorVentaUltimaVenta(int manguera) {
        long totalizador = 0;
        try {

            String sql = "select vd.acum_ven_inicial  \n"
                    + "from ventas v\n"
                    + "inner join ventas_detalles vd on v.id = vd.ventas_id \n"
                    + "where manguera = ?\n"
                    + "order by v.id desc \n"
                    + "limit 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                totalizador = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totalizador;
    }

    private long getPrecioVentaUltimaVenta(int manguera) {
        long totalizador = 0;
        try {

            String sql = "select vd.precio  \n"
                    + "from ventas v\n"
                    + "inner join ventas_detalles vd on v.id = vd.ventas_id \n"
                    + "where manguera = ?\n"
                    + "order by v.id desc \n"
                    + "limit 1";

            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, manguera);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                totalizador = re.getLong(1);
            }
        } catch (PSQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (SQLException ex) {
            NeoService.setLog(ex.getMessage());
        } catch (Exception ex) {
            NeoService.setLog(ex.getMessage());
        }
        return totalizador;
    }

    public TreeMap<Long, DispositivoController> getDispositivos() {
        TreeMap<Long, DispositivoController> dispositivos = new TreeMap<>();
        NeoService.setLog("Listando dispositivos...");
        try {
            String sql = "SELECT * FROM dispositivos WHERE estado='A'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();
            int size = 0;
            while (re.next()) {
                DispositivoController dispositivo;
                dispositivo = new DispositivoController(re.getString("tipos"), re.getString("interfaz"), re.getString("conector"));
                dispositivos.put(re.getLong("id"), dispositivo);
                NeoService.setLog("Listando dispositivos...");
                size++;
            }
            NeoService.setLog("Listando dispositivos encontrados (" + size + ")");
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (getParametroBoolean) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (getParametroBoolean) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (getParametroBoolean) Exception]: " + s.getMessage());
        }
        return dispositivos;
    }

    private String getAtributosVentaEnCurso(int cara, int manguera, int grado) {
        String atributos = null;
        try {
            String sql = "SELECT atributos FROM ventas_curso WHERE cara = ? and manguera = ? and grado = ? order by fecha_inicio";
            PreparedStatement pst = NeoService.obtenerConexion().prepareStatement(sql);
            pst.setInt(1, cara);
            pst.setInt(2, manguera);
            pst.setInt(3, grado);
            ResultSet re = pst.executeQuery();
            if (re.next()) {
                atributos = re.getString(1);
            }
        } catch (SQLException s) {
            System.err.println("[ERROR (getParametroBoolean) Exception]: " + s.getMessage());
        }
        return atributos;
    }

    public boolean esCreditoAutorizacion(long id) {
        boolean estado = false;
        try {
            String sql = "SELECT estado,proveedores_id FROM transacciones WHERE id=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                estado = true;
                if (re.getInt("proveedores_id") == 3) {
                    String estadoS = re.getString("estado");
                    System.out.println(NeoService.ANSI_PURPLE + "[esCreditoAutorizacion]" + estadoS + NeoService.ANSI_RESET);
                    switch (estadoS) {
                        case ESTADO_ANULADA:
                        case ESTADO_MANY_REQUEST_LIBERACION:
                        case ESTADO_REENVIAR_LIBERACION:
                            estado = false;
                            break;
                    }
                }
            }
        } catch (PSQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SurtidorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return estado;
    }

    public boolean existeColumna(String table, String column) throws DAOException {
        try {

            String sql = "select column_name from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = 'public' and TABLE_NAME = '" + table + "' and column_name='" + column + "'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            return re.next();
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (getParametroBoolean) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (getParametroBoolean) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (getParametroBoolean) Exception]: " + s.getMessage());
        }
        return false;
    }
}
