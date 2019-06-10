package com.microfocus.octane.plugins.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microfocus.octane.plugins.rest.pojo.JiraTenantSecurityContext;
import com.microfocus.octane.plugins.utils.JwtUtils;
import com.microfocus.octane.plugins.utils.PluginConstants;
import com.microfocus.octane.plugins.utils.SecurityContextManager;
import com.sun.deploy.net.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.io.IOException;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("lifecycle")
public class LifeCycleResource {

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpheaders;

    @Context
    private HttpServletRequest httpRequest;

    @POST
    @Path("installed")
    @Consumes(MediaType.APPLICATION_JSON)
    public void installed(String rawSecurityContext) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JiraTenantSecurityContext securityContext = mapper.readValue(rawSecurityContext, JiraTenantSecurityContext.class);
        SecurityContextManager.getInstance().install(securityContext);
    }

    @POST
    @Path("uninstalled")
    @Consumes(MediaType.APPLICATION_JSON)
    public void uninstalled(String body) {

        DecodedJWT decodedJWT = (DecodedJWT)httpRequest.getAttribute(PluginConstants.JWT_ATTRIBUTE);
        SecurityContextManager.getInstance().uninstall(decodedJWT.getIssuer());
    }
}
