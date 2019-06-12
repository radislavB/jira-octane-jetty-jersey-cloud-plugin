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
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JwtUtils {


    public static DecodedJWT validateToken(HttpServletRequest request, String token) {

        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            JiraTenantSecurityContext securityContext = SecurityContextManager.getInstance().getSecurityContext(decodedJWT.getIssuer());

            //validate qsh if exist
            String qsh = extractQsh(decodedJWT);
            if (qsh != null) {
                String computedQsh = computeQsh(request);
                if (!qsh.equals(computedQsh)) {
                    throw new JWTVerificationException("QSH validation is failed");
                }
            }

            //main validations
            Algorithm algorithm = Algorithm.HMAC256(securityContext.getSharedSecret());
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(decodedJWT.getIssuer()).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw e;
        }
    }

    private static String computeQsh(HttpServletRequest request) {
        String canonicalMethod = request.getMethod().toUpperCase();
        String canonicalUrl = request.getRequestURI();
        Map<String, String[]> params = request.getParameterMap();
        String canonicalParams = params.keySet().stream()
                .filter(p -> !p.toLowerCase().equals("jwt"))
                .sorted()
                .map(p -> urlEncode(p) + "=" + Stream.of(params.get(p)).sorted().map(v -> urlEncode(v)).collect(Collectors.joining(",")))
                .collect(Collectors.joining("&"));
        String canonicalString = Arrays.asList(canonicalMethod, canonicalUrl, canonicalParams).stream().collect(Collectors.joining("&"));
        String sha256hex = DigestUtils.sha256Hex(canonicalString);
        return sha256hex;

    }

    private static String extractQsh(DecodedJWT decodedJWT) {
        String str = StringUtils.newStringUtf8(Base64.decodeBase64(decodedJWT.getPayload()));
        final ObjectMapper mapper = new ObjectMapper();
        Map map = null;
        try {
            map = mapper.readValue(str, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String qsh = (String) map.get("qsh");
        return qsh;
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
