package com.microfocus.octane.plugins.resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.microfocus.octane.plugins.utils.JwtUtils;
import com.microfocus.octane.plugins.utils.PluginConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
       HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        boolean validationFailed = false;
        if (!skipValidation(req)) {
            String jwtToken = tryExtractTokenFromAuthorizationHeaders(req);
            if (jwtToken == null) {
                jwtToken = tryExtractTokenFromQueryString(req);
            }

            if (jwtToken != null) {
                try {
                    DecodedJWT decodedJWT = JwtUtils.validateToken(req, jwtToken);
                    req.setAttribute(PluginConstants.TENANT_ID, decodedJWT.getIssuer());

                } catch (Exception e) {
                    validationFailed = true;
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is invalid.");
                    //res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            } else {
                validationFailed = true;
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is missing.");
            }
        }

        if (!validationFailed) {
            filterChain.doFilter(request, response);
        }
    }

    private static boolean skipValidation(HttpServletRequest req) {
        //return true;
        return req.getMethod().equals("POST") && req.getRequestURI().equals("/resources/lifecycle/installed");
    }

    private static String tryExtractTokenFromQueryString(HttpServletRequest req) {

        String parameter = req.getParameter("jwt");
        return parameter;
    }

    private static String tryExtractTokenFromAuthorizationHeaders(HttpServletRequest req) {
        String jwtHeaderStart = "JWT ";
        String authorizationHeader = req.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(jwtHeaderStart)) {
            return authorizationHeader.substring(jwtHeaderStart.length());
        } else {
            return null;
        }
    }

    @Override
    public void destroy() {

    }
}
