package com.microfocus.octane.plugins.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microfocus.octane.plugins.rest.pojo.JiraTenantSecurityContext;
import com.microfocus.octane.plugins.utils.PluginConstants;
import com.microfocus.octane.plugins.utils.SecurityContextManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("lifecycle")
public class LifeCycleResource {

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

        String tenantId = (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
        SecurityContextManager.getInstance().uninstall(tenantId);
    }
}
