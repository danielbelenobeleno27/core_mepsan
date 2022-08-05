package com.core.app.server.filter;

import com.butter.bean.ErrorResponse;
import com.butter.bean.Utils;
import com.core.app.NeoService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class AuthorizationFilter {

    private final String HEADER = "Authorization";
    private final String FECHA = "Fecha";
    private final String UUID = "Uuid";

    private final String PREFIX = "Bearer ";
    private final String SECRET = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCfcLR/jbzXdkIUJfUfGRjQYUGxD15t7Qw42Cen7CS2q1+RO5+ZYH/hcGmRX+2k3XvhN5nGirJ0KdZqu3uNpiRZrpTz8Au5TAReWd6rEghwisCT76fTygh4nKfu1217lLqTob7ZKG+sv6IPHJB3eD51FJMOYIoSEDB+nNCRLCz40j+LD8mscqLqzJSB5ukevulsUAg11IA2p+y4uLWPduOeSMXaqLtI2NQNU3IbN6vHpKoXM8csK7J7N2DBpNYYD/CKfPN9KpyNsksyVH4v5jzM27/UrikzqAQvFMa54dwJCH7JgaX5zK0HEstf6+awwEdNXi40QN2bGBA6dntRuLgrAgMBAAECggEAB+tL0uaAOOU2yhst3g2NgYT9yr2TWHQr5XUeH5ArEOCD9cK3eqFrDfPpLfgwc74TmRbEjk8RtSGQn3jz+g/L0C+hAQPXraxsdcpZ0WHVsJaldnqylfCYTOVQXKrL44d6RAIGaKntDLd/D8vAvaAgpLOfIVl013uBCY/80xyj7WmaiIdrOpoDTRsPK43QPMC1bSqyFg6QAxpA0wAdrjzqqkPEcTRrV2C/onG9/rhrQS8VPqFcKem0LUkYGkGDSl8XFj/1XJLlaZogwaeHCihuYdZC0nhveAFOOftCZ3JQMgIQdXYEHSB2TZS3fTs3au5XfkRSuOFCB3/rt8RaodiagQKBgQDSOsbeC3mv/kcf6bTBGiVFux41cTInaAAuL9jsrSHt5oq7g+9L+/opZedaiXqqdsF5R/2uRhdznPzlOkqdwwtO18g/4GdzP/6VbUhDY3CNhPvL0WCNztt414bjHTZe9wbXXBvxPg4KB4mcZXAvctvlFsG9w+Ycd+/Hpnd+vX8xFQKBgQDCJypszFJ0h/vkNJsRhAlRycrWw3byVQxhomLggJts1YGaPHEAwg/jlkGxm7hOTCjfMqi8J3hc8A9GjiVsYxeIHIzrh/rKgVRhg4W+XwbW4SpjLXsIsaLPGmiQNazYTxFCTxuM6zOIBzKovkpGfMZpnE40ieg6OhLHWCZdir4UPwKBgQDQ8x6Jv7oZFJC4xV6kt0nofszNrU5+ID5dqazfwE0C2at2KPIFAdNopiJGuHI1T4eI9ebL+lqAzL4HT+Kt3xg0KwCbjH0F7csp1c6HPCS4ZD6oG9jt2XYGK6Gjxnst9DTWhHQ5WMY3bbp2udPBNVYd4np6FGV4UjT+qRQH4myy6QKBgDUeFgNSGzT6hzW2Fr7CCvKvbKk5LO+7yPllPfwB84PhHeYepyP38/TmBO8clV4GhCAccgGYItv6aZN6DE8NBUEd3ogJ2dlRbM6I9yxQP0TGsZwUs1Z7kvlqVjUuGi3L5ALqNbVmG+EZZwi3lwaV2IrIUrUZDcnwXXbgbQ6miL5tAoGBAMgrbzy8MY3BR0cBAurFCjVmeH89EVFn+ADQP8e5rQxvo4rCdMJ9sfqz7VJqbOLnsU76E89i9ePM62BF1oh/HQIIT63Rq7uMwItJeuByjxTDz/gM00mDZ/sT3I+eGDucItZBN1AcTzW0IGaCL87ZYDHCk+Fm3VjjIklZ9HuyPx/C";
    private final String DEVITECH_SECRET = "-----BEGIN RSA PRIVATE KEY-----MIIEpAIBAAKCAQEAiskm5R0382UD7tEXgTVQHYAYucbYy/beKUndz7Ku2PVUvrKvull35Ql+LpZKah1E7+46M31pxs61Xc4L+fIHn3y0ywwz3uHP/g71vtD6LFy4fTZLirZKfA6WsipUaRoPC6Zye0itE2pvuEXo5uvZdGq+rkwGqQL4SRKRwh0ZX4Xhmtq00NyThPqD7DN+9ZbSkDShm3HkAhhAYPqfvcvPGCiNTEPuEG9Y3cXivkdCBlKhAeTGrvoFkNwKCdd5ZWbLv6LAxuHecu6XHhVMId4NGxCH2XKh2AUv6eJ/gjwkfIbrdO7FdmCe6QC8vWOH9PxXHrWEbFDpCvSHWIvR4qKyZQIDAQABAoIBAQCHXz+uGbr6kVyttIv9vzffHpR/mULcaHc4xNE0B3FfNKWtwPOBjEVTRdgrrvL04Ineknt4v+rOPdBQqGusKHVhDq32pHdv/sj3YjY4IvTzEpntoGk86yRqL3y0Wm+tePqV/YwLTs9rcdV5Y8+SdxjL4lcOAiA4+SmfdRpxwhp+vc+tknfgcs+tSbbDHyDhgQwnsUCDPs5CLgTU9/gqTlUPEezyXZLaWSCttu1cuPUZwRED9Hs2NjXSj1O2HTHAg3Z3TApcnFkG4hVDUBFJFJAFtwpAzrWoSZVZP4s/S4YdkC3hVVIM6bo52jRr0Ci3w3UTuINFuXpNX/O7CYyvvF4BAoGBANpAcpVyMgbutx6g+5Qak49WRfZOaxBH/BwbYuRBYG2PkkANsSoRWRrDUlZY+vGkIXhHp4F4N/7SjFHrpQ/1KvYoN81/A3kZ4c5L1cR7PtTnXAUXNqGlhNALj9jMbCzlhaBFHl87Mz8SQoBCiB/zBYiMYjdGwoqGm8/ESQM8zLWBAoGBAKLKLl565on5uvKUfiPCee1RlBDvGnO4+LM6OF0z0gUSUxhMFklv5zYcT6Vi6f3R6QgPpkG5GNLscNxoHrOIz0Zz496P1ZAgA8DXpCMsKsRD1vu5n9seKienvkqXRP2axK5PEupu4wCuVomiXnlHvLYjY5esT6CouWLOzvbuC1blAoGAPrRXd2Joxx8ck3sy7Jk6HetujFZ5YiMcZsLjharW1oNyRF7qsKhtTkghxtcnufcq+pCzqnnstJSvZfXq5YvNvQ1PAwZj7A4olwmosBussKSMBpZlxsl0QAWiXWpWBgwneSWClV+/2HYZjxoOXAeJZnLW4QS+behAqc++HmUAd4ECgYEAgDdCIkQmhBHfvuRaHYw1QEf6mQPaD79mkrOOZUpFZp0yOXbkLt8meqX9zUOFDNdh9WluB2HkPWzgz5hqZfmhV9o7ZbZf/O5aRm8R5moJHSBZmVZwo8K0bRtfc5yFSEG4G5pIScEgpg6qNilew6NO7R4eeP3MkbuSmFJPDIodAEkCgYA4TZ4h7EgBTAG0+INsTLcVyaLozaiPt9z4XUUbKbYPGaULABoLqBixFiPJgbt2hkf3DNMT42/f744Qxj/tlwzIUx60giiF9iH4obTgJUQlRX4ZW3R7n9FHq+TV/a/eiLYhvcU873zJ7tc+RIoCwsET9HzoJilA/ueNldooEnTvcw==-----END RSA PRIVATE KEY-----";
    private final String IDENTIFICADOR_APLICACION_KEY = "identificadorAplicacion"; // Se debe ingresar el valor del identificador de aplicación generado
    private final String IDENTIFICADOR_APLICACION_VALUE = "b09bacb9-0532-4fed-8fad-96d87467d05a";

    public ErrorResponse doFilterInternal(HttpExchange operation) {
        
        if(true){
            return null;
        }
        
        String[] requiere = new String[]{HEADER, FECHA, UUID};

        ErrorResponse error = existsHeader(operation.getRequestHeaders(), requiere);
        if (error == null) {
            try {

                if (operation.getRemoteAddress().getHostName().equals("localhost")
                        || operation.getRemoteAddress().getAddress().getHostAddress().equals("127.0.0.1")
                        || operation.getRemoteAddress().getAddress().getHostAddress().equals("0:0:0:0:0:0:0:1")
                        || operation.getRemoteAddress().getHostName().equals("192.168.0.5")
                        || operation.getRemoteAddress().getHostName().equals("192.168.0.14")) {

                    String key = operation.getRequestHeaders().get(HEADER).get(0).replaceAll(PREFIX, "").trim();
                    if (key.equals(DEVITECH_SECRET)) {
                        error = null;
                    } else {
                        error = new ErrorResponse();
                        error.setCodigo(ErrorResponse.ERROR_40017_ID);
                        error.setMensaje(ErrorResponse.ERROR_40017_DESC_PK);
                    }
                } else {

                    String jwt = operation.getRequestHeaders().get(HEADER).get(0).replaceAll(PREFIX, "").trim();
                    Claims clamins = DecodeTokenMethodTwo(jwt);
                    String identificadorAplicacionToken = clamins.get(IDENTIFICADOR_APLICACION_KEY, String.class);
                    if (identificadorAplicacionToken == null || !identificadorAplicacionToken.equalsIgnoreCase(IDENTIFICADOR_APLICACION_VALUE)) {
                        error = new ErrorResponse();
                        error.setCodigo(ErrorResponse.ERROR_40016_ID);
                        error.setMensaje(ErrorResponse.ERROR_40016_DESC);
                    }
                }
            } catch (ExpiredJwtException es) {
                NeoService.setLog(es.getMessage());
                error = new ErrorResponse();
                error.setCodigo(ErrorResponse.ERROR_40018_ID);
                error.setMensaje(ErrorResponse.ERROR_40018_DESC);
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException es) {
                NeoService.setLog(es.getMessage());
                error = new ErrorResponse();
                error.setCodigo(ErrorResponse.ERROR_40017_ID);
                error.setMensaje(ErrorResponse.ERROR_40017_DESC);
            } catch (Exception ex) {
                NeoService.setLog(ex.getMessage());
                error = new ErrorResponse();
                error.setCodigo(ErrorResponse.ERROR_40017_ID);
                error.setMensaje(ErrorResponse.ERROR_40017_DESC);
            }
        }
        return error;
    }

    private static String GetPrivateKeySA(String parse) throws Exception {
        return parse.replaceAll("-----END PRIVATE KEY-----", "").replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\n", "");
    }

    public Claims DecodeTokenMethodTwo(final String jwtToken) throws Exception {
        return Jwts.parser().setSigningKey(GetPrivateKeyMethodTwo(SECRET)).parseClaimsJws(jwtToken).getBody();
    }

    private static PrivateKey GetPrivateKeyMethodTwo(String key) throws Exception, NoSuchAlgorithmException, InvalidKeySpecException {
        final String secret = GetPrivateKeySA(key);
        byte[] buffer = Base64.getDecoder().decode(secret);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        return privKey;
    }

    private ErrorResponse existsHeader(Headers request, String[] requiere) {
        ErrorResponse error = null;

        for (String field : requiere) {
            boolean exite = false;
            for (Map.Entry<String, List<String>> entry : request.entrySet()) {
                Object key = entry.getKey();
                Object val = entry.getValue();
                if (key.toString().equalsIgnoreCase(field)) {
                    switch (field) {
                        case FECHA:
                            String fecha = val.toString().replaceAll("[\\[\\]]", "");
                            exite = Utils.validarDate(fecha);
                            if (!exite) {
                                field = field + ", formato esperado (yyyy-MM-ddTHH:mm:ss.SSSZ) ";
                            }
                            break;
                        case UUID:
                            String value = val.toString().replaceAll("[\\[\\]]", "");
                            exite = Utils.validarUuid(value);
                            break;
                        default:
                            exite = true;
                            break;
                    }

                }
            }
            if (!exite) {
                error = new ErrorResponse();
                error.setStatusCode(ErrorResponse.SC_UNAUTHORIZED);
                error.setCodigo(ErrorResponse.ERROR_40015_ID);
                error.setMensaje(ErrorResponse.ERROR_VALOR_INCORRECTO_PARA + field);
                break;
            }
        }
        return error;
    }

}
