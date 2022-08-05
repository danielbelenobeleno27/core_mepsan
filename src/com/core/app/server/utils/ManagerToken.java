/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.app.server.utils;

import java.security.Key;
import java.sql.Date;

import javax.crypto.spec.SecretKeySpec; 

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Base64;

/**
 *
 * @author usuario
 */
public class ManagerToken {

    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    public static byte[] GetSignigKeyToDecode(String secret) {
        return  Base64.getDecoder().decode(secret);
    }

    public static String GenerateToken(String secret) {
        final Key signingKey = GetSignigKeyToEncode(secret);
        final long nowMillis = System.currentTimeMillis();
        final Date now = new Date(nowMillis);
        final long expMillis = nowMillis + 5000000;
        final Date exp = new Date(expMillis);

        final JwtBuilder builder = Jwts.builder()
                .setId("id_test")
                .setIssuedAt(now)
                .setSubject("token de prueba")
                .setIssuer("isanchez")
                .claim("identificadorAplicacion", "12345678")
                .signWith(SIGNATURE_ALGORITHM, signingKey)
                .setExpiration(exp);
        return builder.compact();
    }

    public static Claims DeserializeToken(String secret, String jwtToken) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
        return Jwts.parser().setSigningKey(GetSignigKeyToDecode(secret)).parseClaimsJws(jwtToken).getBody();
    }
 

    private static Key GetSignigKeyToEncode(String secret) {
        final byte[] apiKeySecretBytes =  Base64.getDecoder().decode(secret);
        return new SecretKeySpec(apiKeySecretBytes, SIGNATURE_ALGORITHM.getJcaName());
    }
}
