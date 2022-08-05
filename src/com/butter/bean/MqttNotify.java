/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butter.bean;

import com.core.app.NeoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.core.database.DAOException;
import java.util.Arrays;
import org.eclipse.paho.client.mqttv3.*;

/**
 *
 * @author novus
 */
public class MqttNotify implements MqttCallback {

    boolean DEBUG = true;
    MqttClient client;
    CredencialBean credencial;

    public static void main(String[] args) {
        byte[] array1 = new byte[]{0, 0x1, 3, 45};
        byte[] array2 = new byte[]{0, 2, 3, 45};
    }
    String topico;
    String serverUrl = "tcp://repositorio.neoline.co:1883";
    String mqttUserName = "mosquitoneo";
    String mqttPassword = "baygon";

    public MqttNotify(CredencialBean credencial) {
        this.credencial = credencial;
    }

    public void suscribex() {
        try {
            String clientId = MqttClient.generateClientId();
            if (clientId == null || clientId.length() >= 23) {
                int numero = (int) (Math.random() * 1000 + 1);
                clientId = "fkasdfoi345333" + numero;
                NeoService.setLog("CLIENTE MQTT > " + clientId);
            }

            client = new MqttClient(serverUrl, clientId);

            MqttConnectOptions connOpts = setUpConnectionOptions(mqttUserName, mqttPassword);
            client.connect(connOpts);

            client.setCallback(this);
            topico = "/NEO/EMPRESA/" + credencial.getEmpresas_id() + "/EQUIPO/" + credencial.getId() + "/#";
            client.subscribe(topico);
            if (DEBUG) {
                NeoService.setLog("SUSCRITO A " + topico);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void desconectar(String motivo) {
        if (DEBUG) {
            NeoService.setLog("DESCONECTADO POR " + motivo + " " + topico);
        }
        try {
            if (client != null || client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            //e.printStackTrace();
        }
    }

    public void publish(String topic) {
        try {
            String clientId = MqttClient.generateClientId();
            if (clientId == null || clientId.length() >= 23) {
                int numero = (int) (Math.random() * 1000 + 1);
                clientId = "fkasdfoi345333" + numero;
                NeoService.setLog("CLIENTE MQTT > " + clientId);
            }

            client = new MqttClient(serverUrl, clientId);
            MqttConnectOptions connOpts = setUpConnectionOptions(mqttUserName, mqttPassword);
            client.connect(connOpts);

            MqttMessage msn = new MqttMessage(new byte[0]);
            msn.setRetained(true);
            client.publish(topic, msn);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String json) {
        try {

            MqttMessage msn = new MqttMessage(json.getBytes());
            msn.setRetained(false);
            msn.setQos(0);
            client.publish(topic, msn);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static MqttConnectOptions setUpConnectionOptions(String username, String password) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        return connOpts;
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        try {
            if (message != null && message.getPayload().length > 0) {
                NeoService.setLog("[MQTT TOPIC  ]" + topic);
                NeoService.setLog("[MQTT RECEIVE]" + message.toString());
                JsonParser parser = new JsonParser();
                JsonObject o = parser.parse(message.toString()).getAsJsonObject();

                int tipo = o.get("type").getAsInt();
                ClientWSAsync async;
                String url;
                String id;
                switch (tipo) {
                    case 1:
                        //DETALLES DE UN PRODUCTO
                        try {
                        JsonObject obj = o.get("info").getAsJsonObject().get("producto").getAsJsonObject();
                        id = obj.get("id").getAsString();
                        descargaFullProductV2(id);
                    } catch (Exception e) {
                        NeoService.setLog("Error Mqtt:" + e.getMessage());
                    } finally {
                        publish(topic);
                    }
                    break;
                    case 2:
                        //CAMBIA TODA LA INFORMACION DE LA EMPRESA
                        try {
                        SetupAsync set2 = new SetupAsync(null);
                        set2.descargaPersonal();
                    } catch (Exception e) {
                        NeoService.setLog("Error Mqtt:" + e.getMessage());
                    } finally {
                        publish(topic);
                    }
                    break;
                    case 3:
                        //DESCARGA LOS PRODUCTOS ASOCIADOS A ESTE POS
                        id = o.get("info").getAsJsonObject().get("productos_empresas").getAsJsonObject().get("id").getAsString();
                        url = Butter.SECURE_END_POINT_PRODUCTOS_EMPRESAS + "/" + credencial.getEmpresas_id() + "/" + id;
                        async = new ClientWSAsync(
                                Butter.ENABLE_HTTPS,
                                "CONSULTA DE PRODUCTOS EMPRESAS",
                                credencial,
                                url,
                                Butter.GET,
                                new JsonObject(),
                                Butter.ENABLE_DEBUG
                        );
                        async.start();

                        try {
                            async.join();
                            JsonObject response = async.getResponse().get("data").getAsJsonObject();

                            boolean success = async.getResponse().get("success").getAsBoolean();
                            ProductoBean prd = new ProductoBean();
                            if (success) {
                                prd.setId(response.get("productos_id").getAsLong());
                                prd.setPrecio(response.get("precio").getAsFloat());
                                prd.setCantidadMaxima(response.get("cantidad_maxima").getAsFloat());
                                prd.setCantidadMaxima(response.get("cantidad_minima").getAsFloat());
                                prd.setDispensado(response.get("sequimiento").getAsString());
                                prd.setEstado(response.get("disponible").getAsString().equals("S") ? "A" : "I");
                            } else {
                                prd.setId(Long.parseLong(id));
                            }

                            DescargaFullProduct(prd.getId() + "");

                            SetupDao dao = new SetupDao();
                            dao.update(prd, credencial);

                            NeoService.setLog(response.toString());
                            publish(topic);
                        } catch (DAOException e) {
                            NeoService.setLog("Error Mqtt:" + e.getMessage());
                        } finally {
                            publish(topic);
                            async = null;
                        }
                        break;
                    case 4:
                        //CAMBIA LOS INVENTARIOS EN LA BODEGA
                        try {
                        id = o.get("info").getAsJsonObject().get("bodegas_productos").getAsJsonObject().get("id").getAsString();
                        MovimientosDao mdao = new MovimientosDao();
                        ProductoBean temp = mdao.findProductByIdActive(Long.parseLong(id));
                        if (temp == null) {
                            temp = descargaProductoWS(id);
                        }
                        SetupAsync set = new SetupAsync(null, null);
                        set.descargarInvetarioBodega(id);
                        publish(topic);

                    } catch (DAOException e) {
                        NeoService.setLog("Error Mqtt:" + e.getMessage());
                    } finally {
                        publish(topic);
                        async = null;
                    }
                    break;
                    case 5:
                        try {
                        SetupAsync set = new SetupAsync(null);
                        set.descargaDatos();
                    } catch (Exception e) {
                        NeoService.setLog("Error Mqtt:" + e.getMessage());
                    } finally {
                        publish(topic);
                    }
                    //CAMBIA TODA LA INFORMACION DE LA EMPRESA
                    break;
                    case 6:
                        //RESERVADO PARA CONSECUTIVOS
                        try {
                        SetupAsync set3 = new SetupAsync(null);
                        set3.getConsecutivos();
                    } catch (Exception e) {
                        NeoService.setLog("Error Mqtt:" + e.getMessage());
                    } finally {
                        publish(topic);
                    }
                    break;
                    default:
                        publish(topic);
                        NeoService.setLog("OPERACION NO SOPORTADA: " + topic);
                        NeoService.setLog("DATA RECEIVE: " + message.toString());
                        break;
                }

            }
        } catch (Exception a) {
            NeoService.setLog(a.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }

    private ProductoBean descargaFullProductV2(String id) {
        SetupAsync async = new SetupAsync(null, null);
        ProductoBean pr = async.descargaProductoWSV2(id);

        return pr;
    }

    private ProductoBean descargaProductoWS(String id) {
        SetupAsync async = new SetupAsync(null, null);
        ProductoBean pr = async.descargaProductoWS(id);
        return pr;
    }

    private void DescargaFullProduct(String id) {
        ProductoBean bean = descargaProductoWS(id);

        if (bean.isIngrediente()) {
            descargaProductoPadre(id);
        }

        SetupAsync set = new SetupAsync(null, null);
        set.descargarInvetarioBodega(id);

        if (bean != null) {
            if (bean.getIngredientes() != null && !bean.getIngredientes().isEmpty()) {
                MovimientosDao mdao = new MovimientosDao();
                try {
                    mdao.limpiarIngredientes(Long.parseLong(id));
                } catch (DAOException ex) {
                    NeoService.setLog("Error al limpiar los ingredientes");
                }
                for (ProductoBean ingrediente : bean.getIngredientes()) {
                    try {
                        ProductoBean temp = mdao.findProductByIdActive(ingrediente.getId());
                        if (temp == null) {
                            temp = descargaProductoWS(ingrediente.getId() + "");
                        } else {
                            NeoService.setLog("[MQTT INGREDIENTES] INGREDIENTE YA SE ENCUENTRA " + ingrediente.getId());
                        }
                        if (temp != null) {
                            temp.setProducto_compuesto_id(ingrediente.getProducto_compuesto_id());
                            temp.setProducto_compuesto_cantidad(ingrediente.getProducto_compuesto_cantidad());
                            mdao.integrarProdcto(bean, temp);
                        }
                    } catch (DAOException e) {
                        NeoService.setLog("ERROR EE:" + e.getMessage());
                    }

                }
            }

            try {
                SetupDao dao = new SetupDao();
                dao.actualiazaEstado(bean);
            } catch (DAOException a) {
            }
        }
    }

    private void descargaProductoPadre(String id) {
        String url = Butter.SECURE_END_POINT_PRODUCTOS_PADRES + "/" + id;
        ClientWSAsync async = new ClientWSAsync(
                Butter.ENABLE_HTTPS,
                "DESCARGA LA INFO DE LOS PADRES DEL INGREDIENTE",
                credencial,
                url,
                Butter.GET,
                new JsonObject(),
                Butter.ENABLE_DEBUG
        );
        async.start();

        try {
            async.join();
        } catch (Exception a) {
        }
        boolean success = async.getResponse().get("success").getAsBoolean();

        if (success) {
            JsonArray response = async.getResponse().get("Productos").getAsJsonArray();
            for (JsonElement elemento : response) {
                try {
                    JsonObject json = elemento.getAsJsonObject();
                    ProductoBean ingrediente = new ProductoBean();
                    ingrediente.setId(json.get("productos_id").getAsLong());

                    DescargaFullProduct(ingrediente.getId() + "");
                } catch (Exception a) {
                }
            }
        }

    }

}
