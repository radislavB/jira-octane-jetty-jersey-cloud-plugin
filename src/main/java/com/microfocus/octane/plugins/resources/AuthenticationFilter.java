package com.microfocus.octane.plugins.resources;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.microfocus.octane.plugins.utils.JwtUtils;
import com.microfocus.octane.plugins.utils.PluginConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    private static final Logger log = LogManager.getLogger();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        log.info(req.getRequestURI());

        boolean validationFailed = false;
        if (!skipValidation(req)) {
            String jwtToken = tryExtractTokenFromAuthorizationHeaders(req);
            if (jwtToken == null) {
                jwtToken = tryExtractTokenFromQueryString(req);
            }

            if (jwtToken != null) {
                try {
                    DecodedJWT decodedJWT = JwtUtils.validateToken(req, jwtToken);
                    //TODO print user id in log
                    req.setAttribute(PluginConstants.USER_ID, decodedJWT.getSubject());//https://almoctanedev.atlassian.net/rest/api/2/user?accountId=557058:7364e317-e22e-45c7-a23d-d11532f46848
                    req.setAttribute(PluginConstants.TENANT_ID, decodedJWT.getIssuer());
                } catch (Exception e) {
                    validationFailed = true;
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is invalid.");
                    log.error("Unathorized access to : ");
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
        return req.getMethod().equals("POST") && req.getRequestURI().equals("/rest/lifecycle/installed");
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
