package com.microfocus.octane.plugins.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microfocus.octane.plugins.rest.pojo.JiraTenantSecurityContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Map;

public class JwtUtils {





    public static DecodedJWT validateToken(String token) {

        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            JiraTenantSecurityContext securityContext = SecurityContextManager.getInstance().getSecurityContext(decodedJWT.getIssuer());

            //TODO : validate qsh


            Algorithm algorithm = Algorithm.HMAC256(securityContext.getSharedSecret());
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(decodedJWT.getIssuer()).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw e;
        }
    }
    private static void computeQsh(){


    }
    private static void extractQsh(DecodedJWT decodedJWT) {
        String str = StringUtils.newStringUtf8(Base64.decodeBase64(decodedJWT.getPayload()));
        final ObjectMapper mapper = new ObjectMapper();
        Map map = null;
        try {
            map = mapper.readValue(str, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String qsh = (String) map.get("qsh");
    }
}
