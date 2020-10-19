package com.microfocus.octane.plugins.resources;

import com.microfocus.octane.plugins.managers.pojo.JiraTenantSecurityContext;
import com.microfocus.octane.plugins.utils.PluginConstants;
import com.microfocus.octane.plugins.managers.SecurityContextManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * lifecycle resource
 */
@Path("lifecycle")
public class LifeCycleResource {

    @Context
    private HttpServletRequest httpRequest;

    @POST
    @Path("installed")
    @Consumes(MediaType.APPLICATION_JSON)
    public void installed(JiraTenantSecurityContext securityContext) throws IOException {
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
