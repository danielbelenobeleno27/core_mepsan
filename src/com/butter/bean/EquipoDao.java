package com.butter.bean;

import com.core.app.NeoService;
import com.google.gson.JsonObject;
import com.neo.app.bean.Cara;
import com.core.database.DAOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.util.PSQLException;

public class EquipoDao {

    public CredencialBean create(CredencialBean bean) throws DAOException {
        try {
            String sql = "INSERT INTO equipos(\n"
                    + "            id, empresas_id, serial_equipo, almacenamientos_id, estado, equipos_tipos_id, \n"
                    + "            equipos_protocolos_id, mac, ip, port, create_user, create_date, \n"
                    + "            update_user, update_date, token, password, factor_precio, factor_importe, \n"
                    + "            factor_inventario, lector_ip, lector_port, impresora_ip, impresora_port, \n"
                    + "            url_foto, autorizado)\n"
                    + "    VALUES (?, NULL, ?, NULL, ?, NULL, \n"
                    + "            NULL, ?, NULL, NULL, ?, NOW(), \n"
                    + "            NULL, NULL, ?, ?, NULL, NULL, \n"
                    + "            NULL, NULL, NULL, NULL, NULL, \n"
                    + "            NULL, ?) RETURNING ID";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, 1);
            ps.setString(2, bean.getReferencia());
            ps.setString(3, Butter.ACTIVE);
            ps.setString(4, bean.getMac());
            ps.setLong(5, 1);
            ps.setString(6, bean.getToken() != null ? bean.getToken() : "");
            ps.setString(7, bean.getPassword() != null ? bean.getPassword() : "");
            ps.setString(8, bean.isAutorizado() ? "S" : "N");

            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean.setEquipos_id(re.getLong("id"));
            }
        } catch (PSQLException s) {
            throw new DAOException("11." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("12." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("13." + s.getMessage());
        }
        return bean;
    }

    public void createEmpresas(EmpresaBean bean) throws DAOException {
        try {
            String sql = "INSERT INTO public.empresas(\n"
                    + "            id, razon_social, nit, localizacion, estado, cantidad_sucursales, \n"
                    + "            ciudades_id, empresas_id, create_user, create_date, update_user, \n"
                    + "            update_date, empresas_tipos_id, url_foto, dominio, alias, correo, direccion, telefono)\n"
                    + "    VALUES (?, ?, ?, ?, 'A', 1, \n"
                    + "            ?, null, 1, now(), null, \n"
                    + "            null, 5, ?, ?, ?, ?, ?, ?);";

            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

            ps.setLong(1, bean.getId());
            ps.setString(2, bean.getRazonSocial());
            ps.setString(3, bean.getNit());
            ps.setString(4, bean.getLocalizacion());
            ps.setLong(5, bean.getCiudadId());
            ps.setString(6, bean.getUrlFotos());
            ps.setString(7, bean.getDominioId() + "");
            ps.setString(8, bean.getAlias());
            ps.setString(9, bean.getCorreo());
            ps.setString(10, bean.getDireccion());
            ps.setString(11, bean.getTelefonos());

            ps.executeUpdate();

        } catch (PSQLException s) {
            if (!s.getMessage().contains("empresas_pk")) {
                throw new DAOException("createEmpresas 11." + s.getMessage());
            } else {

                try {
                    String sql = "UPDATE public.empresas\n"
                            + "   SET razon_social=?, nit=?, localizacion=?, \n"
                            + "       ciudades_id=?, url_foto=?, dominio=?, alias=?, correo=?, direccion=?, telefono=?\n"
                            + " WHERE ID=?";

                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                    ps.setString(1, bean.getRazonSocial());
                    ps.setString(2, bean.getNit());
                    ps.setString(3, bean.getLocalizacion());
                    ps.setLong(4, bean.getCiudadId());
                    ps.setString(5, bean.getUrlFotos());
                    ps.setString(6, bean.getDominioId() + "");
                    ps.setString(7, bean.getAlias());
                    ps.setString(8, bean.getCorreo());
                    ps.setString(9, bean.getDireccion());
                    ps.setString(10, bean.getTelefonos());
                    ps.setLong(11, bean.getId());

                    ps.executeUpdate();
                } catch (SQLException e) {
                    NeoService.setLog(e.getMessage());
                }
            }
        } catch (SQLException s) {
            throw new DAOException("createEmpresas 12." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("createEmpresas 13." + s.getMessage());
        }

        try {

            String sql = "INSERT INTO descriptores(\n"
                    + "            id, empresas_id, header, footer)\n"
                    + "    VALUES (?, ?, ?, ?);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, bean.getDescriptorId());
            ps.setLong(2, bean.getId());
            ps.setString(3, bean.getDescriptorHeader());
            ps.setString(4, bean.getDescriptorFooter());
            ps.executeUpdate();

        } catch (SQLException s) {
            if (s.getMessage().contains("descriptores")) {
                try {
                    String sql = "UPDATE public.descriptores\n"
                            + "   SET empresas_id=?, header=?, footer=?\n"
                            + " WHERE id=?";
                    PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
                    ps.setLong(1, bean.getId());
                    ps.setString(2, bean.getDescriptorHeader());
                    ps.setString(3, bean.getDescriptorFooter());
                    ps.setLong(4, bean.getDescriptorId());
                    ps.executeUpdate();
                } catch (SQLException a) {
                    NeoService.setLog("566. Error al actualizar los descriptores");
                }
            } else {
                NeoService.setLog("567. Error al actualizar los descriptoresº");
            }
            NeoService.setLog("error ciudades insert");
        }

        if (bean.getContacto() != null) {
            try {
                for (ContactoBean contacto : bean.getContacto()) {
                    try {
                        String sql = "INSERT INTO public.contactos(\n"
                                + "            id, empresas_id, personas_id, tipo, etiqueta, contacto, estado, principal)\n"
                                + "    VALUES (?, ?, null, ?, ?, ?, ?, ?);";
                        PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                        ps.setLong(1, contacto.getId());
                        ps.setLong(2, bean.getId());
                        ps.setInt(3, contacto.getTipo());
                        ps.setString(4, contacto.getEtiqueta().toUpperCase().trim());
                        ps.setString(5, contacto.getContacto().toUpperCase().trim());
                        ps.setString(6, contacto.getEstado());
                        ps.setString(7, contacto.isPrincipal() ? "S" : "N");
                        ps.executeUpdate();

                    } catch (PSQLException s) {

                        if (s.getMessage().contains("correos_pkey")) {
                            String sql = "UPDATE public.contactos\n"
                                    + "   SET empresas_id=?, personas_id=null, tipo=?, etiqueta=?, contacto=?, \n"
                                    + "       estado=?, principal=?\n"
                                    + " WHERE id=?";
                            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                            ps.setLong(1, bean.getId());
                            ps.setInt(2, contacto.getTipo());
                            ps.setString(3, contacto.getEtiqueta().toUpperCase().trim());
                            ps.setString(4, contacto.getContacto().toUpperCase().trim());
                            ps.setString(5, contacto.getEstado());
                            ps.setString(6, contacto.isPrincipal() ? "S" : "N");
                            ps.setLong(7, contacto.getId());

                            ps.executeUpdate();
                        }
                    }

                    asignarContacto(bean, contacto);

                }
            } catch (SQLException ex) {
                throw new DAOException("455." + ex.getMessage());
            }
        }
    }

    public CredencialBean update(CredencialBean bean, long id) throws DAOException {
        try {
            if (id == 0) {
                String sql = "UPDATE public.equipos\n"
                        + "   SET  token=?, password=?, autorizado=?, empresas_id=?\n"
                        + " WHERE id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                ps.setString(1, bean.getToken() != null ? bean.getToken() : "");
                ps.setString(2, bean.getPassword() != null ? bean.getPassword() : "");
                ps.setString(3, bean.isAutorizado() ? "S" : "N");
                if (bean.getEmpresas_id() != null) {
                    ps.setLong(4, bean.getEmpresas_id());
                } else {
                    ps.setNull(4, java.sql.Types.NULL);
                }
                ps.setLong(5, bean.getId());
                int r = ps.executeUpdate();

            } else {
                String sql = "UPDATE public.equipos\n"
                        + "   SET  token=?, password=?, autorizado=?, id=?, empresas_id=?\n"
                        + " WHERE id=?";
                PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);

                ps.setString(1, bean.getToken() != null ? bean.getToken() : "");
                ps.setString(2, bean.getPassword() != null ? bean.getPassword() : "");
                ps.setString(3, bean.isAutorizado() ? "S" : "N");
                ps.setLong(4, bean.getEquipos_id());
                ps.setLong(5, bean.getEmpresas_id());
                ps.setLong(6, id);

                NeoService.setLog(ps.toString());

                int r = ps.executeUpdate();
            }

        } catch (PSQLException s) {
            throw new DAOException("21." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("22." + s.getMessage());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(EquipoDao.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("23." + ex.getMessage());
        }
        return bean;
    }

    public CredencialBean findToken(CredencialBean bean) throws DAOException {
        bean.setRegistroPrevio(false);
        try {
            String sql = "SELECT ID, SERIAL_EQUIPO, TOKEN, PASSWORD, AUTORIZADO, EMPRESAS_ID, 0 BODEGA_ID\n"
                    + "FROM \n"
                    + "EQUIPOS LIMIT 1";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bean.setId(re.getLong("ID"));
                bean.setReferencia(re.getString("SERIAL_EQUIPO"));
                bean.setToken(re.getString("TOKEN"));
                bean.setPassword(re.getString("PASSWORD"));
                bean.setEmpresas_id(re.getLong("EMPRESAS_ID"));
                bean.setBodegaId(re.getLong("BODEGA_ID"));
                if (re.getString("AUTORIZADO") != null) {
                    bean.setAutorizado(re.getString("AUTORIZADO").equals("S"));
                } else {
                    bean.setAutorizado(false);
                }
                bean.setRegistroPrevio(true);
            }

        } catch (PSQLException s) {
            throw new DAOException("31." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("32." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("33." + s.getMessage());
        }
        return bean;
    }

    public BodegaBean findBodegax(long id) throws DAOException {
        BodegaBean bodega = null;
        try {
            String sql = "SELECT * FROM BODEGAS WHERE ID=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                bodega = new BodegaBean();
                bodega.setId(re.getLong("ID"));
                bodega.setDescripcion(re.getString("descripcion"));
                bodega.setCodigo(re.getString("codigo"));

            }
        } catch (PSQLException s) {
            throw new DAOException("31." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("32." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("33." + s.getMessage());
        }
        return bodega;
    }

    public Cara getSurtidorEstado(int pump, int face) throws DAOException {
        Cara cara = null;
        try {
            String sql = "select s.id s_id, s.surtidor, cara, manguera, sd.estado, sd.productos_id, p.descripcion\n"
                    + "from surtidores s\n"
                    + "inner join surtidores_detalles sd on s.id=sd.surtidores_id\n"
                    + "inner join productos p on p.id= sd.productos_id and s.surtidor=? and cara=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setInt(1, pump);
            ps.setInt(2, face);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                cara = new Cara();
                cara.setNumero(re.getInt("cara"));
            }

        } catch (PSQLException s) {
            throw new DAOException("31." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("32." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("33." + s.getMessage());
        }
        return cara;
    }

    public EmpresaBean findEmpresa(CredencialBean bean) throws DAOException {
        EmpresaBean empresa = null;
        try {
            String sql = "SELECT * FROM EMPRESAS WHERE ID=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, bean.getEmpresas_id());
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                empresa = new EmpresaBean();
                empresa.setId(re.getLong("id"));
                empresa.setRazonSocial(re.getString("razon_social"));
                empresa.setNit(re.getString("nit"));
                empresa.setDominioId(re.getLong("dominio_id"));
                empresa.setCodigo(re.getString("codigo_empresa"));
            }

            int TIPO_CONTACTO_TELEFONO = 2;
            int TIPO_CONTACTO_DIRECCION = 3;

            if (empresa != null) {
                sql = "SELECT * FROM CONTACTOS WHERE PRINCIPAL='S'";
                ps = NeoService.obtenerConexion().prepareStatement(sql);
                re = ps.executeQuery();

                while (re.next()) {
                    if (empresa.getContacto() == null) {
                        empresa.setContacto(new ArrayList<>());
                    }

                    ContactoBean cont = new ContactoBean();
                    cont.setId(re.getLong("id"));
                    cont.setEtiqueta(re.getString("etiqueta"));
                    cont.setContacto(re.getString("contacto"));
                    cont.setTipo(re.getInt("tipo"));
                    empresa.getContacto().add(cont);

                    if (re.getInt("tipo") == TIPO_CONTACTO_TELEFONO) {
                        empresa.setTelefonoPrincipal(re.getString("contacto"));
                    }

                    if (re.getInt("tipo") == TIPO_CONTACTO_DIRECCION) {
                        empresa.setDireccionPrincipal(re.getString("contacto"));
                    }

                }
            }

        } catch (PSQLException s) {
            throw new DAOException("31." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("32." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("33." + s.getMessage());
        }
        return empresa;
    }

    private void asignarContacto(EmpresaBean empresa, ContactoBean contacto) {

        if (contacto.isPrincipal() && contacto.getTipo() == Butter.CONTACTO_TIPO_DIRECCION) {
            empresa.setDireccionPrincipal(contacto.getContacto());
        }

        if (contacto.isPrincipal() && contacto.getTipo() == Butter.CONTACTO_TIPO_TELEFONO) {
            empresa.setTelefonoPrincipal(contacto.getContacto());
        }

    }

    public ArrayList<TransmisionBean> getTransmisiones() throws DAOException {
        ArrayList<TransmisionBean> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM TRANSMISION WHERE SINCRONIZADO=0 and tipo = 'CIERRE_JORNADA' ORDER BY ID";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ResultSet re = ps.executeQuery();

            while (re.next()) {
                TransmisionBean tx = new TransmisionBean();
                tx.setId(re.getLong("id"));
                tx.setRequest(re.getString("request"));
                tx.setUrl(re.getString("url"));
                tx.setMethod(re.getString("method"));
                tx.setReintentos(re.getInt("reintentos"));
                tx.setFechaTrasmitido(re.getTimestamp("fecha_trasmitido"));
                lista.add(tx);
            }

        } catch (PSQLException s) {
            throw new DAOException("TXDAO41." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("TXDAO42." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("TXDAO43." + s.getMessage());
        }
        return lista;
    }

    public void guardarTransmision(CredencialBean credencial, JsonObject request, String url, String method) throws DAOException {
        try {
            String sql = "INSERT INTO public.transmision(\n"
                    + "             equipo_id, request, response, sincronizado, fecha_generado, \n"
                    + "            fecha_trasmitido, fecha_ultima, url, method, reintentos)\n"
                    + "    VALUES (?, ?, NULL, 0, now(), \n"
                    + "            NULL, NULL, ?, ?, 0);";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setLong(1, credencial.getId());
            ps.setString(2, request.toString());
            ps.setString(3, url);
            ps.setString(4, method);
            ps.executeUpdate();
        } catch (PSQLException s) {
            throw new DAOException("41." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("42." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("43." + s.getMessage());
        }
    }

    public void guardarTransmisionTexto(CredencialBean credencial, String request, String url, String method, String tipo, String jornada) throws DAOException {
        try {
            String sql = "SELECT * FROM transmision WHERE tipo=? and codigo=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, tipo);
            ps.setString(2, jornada);
            ResultSet re = ps.executeQuery();
            boolean existe = re.next();
            if (!existe) {
                sql = "INSERT INTO transmision(\n"
                        + "             equipo_id, request, response, sincronizado, fecha_generado, \n"
                        + "            fecha_trasmitido, fecha_ultima, url, method, reintentos, tipo, codigo)\n"
                        + "    VALUES (?, ?, NULL, 0, ?, \n"
                        + "            NULL, NULL, ?, ?, 0,?,?);";
                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setLong(1, credencial.getId());
                ps.setString(2, request);
                ps.setTimestamp(3, new Timestamp(new Date().getTime()));
                ps.setString(4, url);
                ps.setString(5, method);
                ps.setString(6, tipo);
                ps.setString(7, jornada);
                ps.executeUpdate();
            } else {
                sql = "update transmision set sincronizado = 0, reintentos = reintentos + 1  where tipo = ? and codigo = ?";
                ps = NeoService.obtenerConexion().prepareStatement(sql);
                ps.setString(1, tipo);
                ps.setString(2, jornada);
                ps.executeUpdate();
            }
        } catch (PSQLException s) {
            throw new DAOException("41." + s.getMessage());
        } catch (SQLException s) {
            throw new DAOException("42." + s.getMessage());
        } catch (Exception s) {
            throw new DAOException("43." + s.getMessage());
        }
    }

    public void updateTransmision(long serial, JsonObject response, int sincronizado, Date txdate, int reintentos) throws DAOException {
        try {
            String sql = "UPDATE public.transmision\n"
                    + "   SET response=?, sincronizado=?,\n"
                    + "       fecha_trasmitido=?, fecha_ultima=now(), reintentos=?\n"
                    + " WHERE id=?;";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            if (response != null) {
                ps.setString(1, response.toString());
            } else {
                ps.setNull(1, Types.NULL);
            }
            ps.setInt(2, sincronizado);
            ps.setTimestamp(3, new Timestamp(txdate.getTime()));
            ps.setInt(4, reintentos);
            ps.setLong(5, serial);
            ps.executeUpdate();
        } catch (PSQLException s) {
            Logger.getLogger(EquipoDao.class.getName()).log(Level.SEVERE, null, s);
            throw new DAOException("41." + s.getMessage());
        } catch (SQLException s) {
            Logger.getLogger(EquipoDao.class.getName()).log(Level.SEVERE, null, s);
            throw new DAOException("42." + s.getMessage());
        } catch (Exception s) {
            Logger.getLogger(EquipoDao.class.getName()).log(Level.SEVERE, null, s);
            throw new DAOException("43." + s.getMessage());
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
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (getParametroInt) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (getParametroInt) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (getParametroInt) Exception]: " + s.getMessage());
        }
        return valor;
    }

    public boolean getParametroBoolean(String codigo) {
        boolean valor = false;
        try {
            String sql = "SELECT valor FROM PARAMETROS WHERE codigo=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet re = ps.executeQuery();
            while (re.next()) {
                valor = re.getString("valor").equals("S");
            }
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (getParametroBoolean) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (getParametroBoolean) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (getParametroBoolean) Exception]: " + s.getMessage());
        }
        return valor;
    }

    public String getParametroString(String codigo) {
        String valor = null;
        try {
            String sql = "SELECT valor FROM PARAMETROS WHERE codigo=?";
            PreparedStatement ps = NeoService.obtenerConexion().prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet re = ps.executeQuery();
            if (re.next()) {
                valor = re.getString("valor");
            }
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (getParametroString) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (getParametroString) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (getParametroString) Exception]: " + s.getMessage());
        }
        return valor;
    }

    public void actualizarBasededatos() {
        try {
            createSurtidorHistorico();
            actualizarPrecioHistorico();
            agregarColumnasTransmision();
            agregarColumnaConexion();
            agregarColumnaFactorPredeterminacion();
            agregarColumnaAtributos();
        } catch (DAOException e) {
            NeoService.setLog(e.getMessage());
            Logger.getLogger(EquipoDao.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void agregarColumnaConexion() throws DAOException {
        try {
            if (!existeColumna("surtidores_detalles", "conexion")) {
                String sql = "alter table surtidores_detalles add column conexion int;";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (agregarColumnaConexion) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (agregarColumnaConexion) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (agregarColumnaConexion) Exception]: " + s.getMessage());
        }
    }

    public void createSurtidorHistorico() throws DAOException {
        try {
            if (!existeColumna("jornadas_inventarios", "manguera")) {
                String sql = "DROP TABLE IF EXISTS jornadas_inventarios;\n";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();

                NeoService.setLog("SE HA BORRADO LA TABLE JORNADAS_INVENTARIOS");
                sql = "CREATE TABLE jornadas_inventarios (\n"
                        + "	id serial NOT NULL,\n"
                        + "	fecha_inicio timestamp without time zone NOT NULL,\n"
                        + "	fecha_fin    timestamp without time zone,\n"
                        + "	grupo_jornada bigint NOT NULL,\n"
                        + "	surtidor int4 NOT NULL,\n"
                        + "	cara int4 NOT NULL,\n"
                        + "	manguera int4 NOT NULL,\n"
                        + "	grado int4 NOT NULL,\n"
                        + "	productos_id int8 NOT NULL,\n"
                        + "	familia_id int8 NOT NULL,\n"
                        + "	acumulado_venta_inicial float8 NOT NULL,\n"
                        + "	acumulado_cantidad_inicial float8 NOT NULL,\n"
                        + "	acumulado_venta_final float8,  \n"
                        + "	acumulado_cantidad_final float8,  \n"
                        + "	CONSTRAINT jornadas_inventarios_pk PRIMARY KEY (id)\n"
                        + ");";
                ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (createSurtidorHistorico) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (createSurtidorHistorico) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (createSurtidorHistorico) Exception]: " + s.getMessage());
        }
    }

    private void actualizarPrecioHistorico() throws DAOException {
        try {
            if (!existeColumna("jornadas_inventarios", "precio_inicial")) {
                String sql = "alter table jornadas_inventarios add column precio_inicial numeric(12,3)";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
            if (!existeColumna("jornadas_inventarios", "precio_final")) {
                String sql = "alter table jornadas_inventarios add column precio_final numeric(12,3)";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (actualizarPrecioHistorico) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (actualizarPrecioHistorico) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (actualizarPrecioHistorico) Exception]: " + s.getMessage());
        }
    }

    private void agregarColumnasTransmision() throws DAOException {
        try {
            if (!existeColumna("transmision", "tipo")) {
                String sql = "alter table transmision add column tipo varchar(100)";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
            if (!existeColumna("transmision", "codigo")) {
                String sql = "alter table transmision add column codigo varchar(100)";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
        } catch (PSQLException s) {
            NeoService.setLog("[ERROR (agregarColumnasTransmision) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (agregarColumnasTransmision) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            NeoService.setLog("[ERROR (agregarColumnasTransmision) Exception]: " + s.getMessage());
        }
    }

    private boolean existeColumna(String table, String column) throws DAOException {
        try {
            String sql = "select column_name from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = 'public' and TABLE_NAME = '" + table + "' and column_name='" + column + "'";
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            return re.next();
        } catch (PSQLException s) {
            System.err.println("[ERROR (existeColumna) PSQLException]: " + s.getMessage());
        } catch (SQLException s) {
            System.err.println("[ERROR (existeColumna) SQLException]: " + s.getMessage());
        } catch (Exception s) {
            System.err.println("[ERROR (existeColumna) Exception]: " + s.getMessage());
        }
        return false;
    }

    private void agregarColumnaAtributos() throws DAOException {
        try {
            if (!existeColumna("ventas_curso", "atributos")) {
                String sql = "alter table ventas_curso add column atributos json;";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
            if (!existeColumna("ventas", "atributos")) {
                String sql = "alter table ventas add column atributos json;";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (agregarColumnaAtributos) Exception]: " + s.getMessage());
        }
    }

    private void agregarColumnaFactorPredeterminacion() throws DAOException {
        try {
            if (!existeColumna("surtidores", "factor_predeterminacion_volumen")) {
                String sql = "alter table surtidores add column factor_predeterminacion_volumen integer default 100;";
                PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
                ps.executeUpdate();
            }
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (agregarColumnaFactorPredeterminacion) Exception]: " + s.getMessage());
        }
    }

    private Long obtenerSiguienteId(String tabla) {
        Long id = 1L;
        try {
            String sql = "SELECT MAX(ID) FROM ".concat(tabla);
            PreparedStatement ps = NeoService.obtenerConexion().prepareCall(sql);
            ResultSet re = ps.executeQuery();
            if (re.next()) {
                id = re.getLong(1) + 1;
            }
        } catch (SQLException s) {
            NeoService.setLog("[ERROR (obtenerSiguienteId) Exception]: " + s.getMessage());
        }
        return id;
    }
}
