package com.microfocus.octane.plugins.rest;


import com.microfocus.octane.plugins.utils.PluginConstants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;


@Path("configuration")
public class ConfigurationResource {


    @Context
    private HttpServletRequest httpRequest;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getConfigurationPage() {
        String tenantId = (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
        Map<String, String> map = new HashMap<>();
        map.put("a1", "a2");
        map.put("b1", "b2");

        //return Response.ok(map).build();
        return map;
    }
}
