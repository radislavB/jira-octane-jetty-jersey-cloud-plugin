package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfiguration;
import com.microfocus.octane.plugins.utils.ConfigurarionUtil;
import com.microfocus.octane.plugins.utils.PluginConstants;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Path("configuration")
public class ConfigurationResource {

    @Context
    private HttpServletRequest httpRequest;

    @GET
    @Path("spaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SpaceConfigurationOutgoing> getAllSpaceConfigurations() {
        List<SpaceConfigurationOutgoing> spaces = ConfigurationManager.getInstance().getSpaceConfigurations(getTenantId())
                .stream().map(c -> ConfigurarionUtil.convert(c)).collect(Collectors.toList());

        return spaces;
    }


    @POST
    @Path("spaces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSpaceConfiguration(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        try {
            SpaceConfiguration spaceConfig = ConfigurarionUtil.validateAndConvert(getTenantId(), spaceConfigurationOutgoing, true);
            ConfigurarionUtil.doFullSpaceConfigurationValidation(getTenantId(), spaceConfig);
            ConfigurationManager.getInstance().addSpaceConfiguration(getTenantId(), spaceConfig);
            return Response.ok(ConfigurarionUtil.convert(spaceConfig)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("spaces/{spaceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSpaceConfiguration(@PathParam("spaceId") String spaceId, SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        try {
            SpaceConfiguration spaceConfig = ConfigurarionUtil.validateAndConvert(getTenantId(), spaceConfigurationOutgoing, false);
            if (!spaceConfig.getId().equals(spaceId)) {
                return Response.status(Response.Status.CONFLICT).entity("Space id in entity should be equal to id in path parameter").build();
            }
            ConfigurarionUtil.doFullSpaceConfigurationValidation(getTenantId(), spaceConfig);
            ConfigurationManager.getInstance().updateSpaceConfiguration(getTenantId(), spaceConfig);
            return Response.ok(ConfigurarionUtil.convert(spaceConfig)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("spaces/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean deleteSpaceConfiguration(@PathParam("id") String id) throws IOException {
        boolean isRemoved = ConfigurationManager.getInstance().removeSpaceConfiguration(getTenantId(), id);
        return isRemoved;
    }

    @POST
    @Path("spaces/test-connection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testSpaceConfiguration(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        try {
            boolean isNewConfig = StringUtils.isEmpty(spaceConfigurationOutgoing.getId());
            SpaceConfiguration spaceConfig = ConfigurarionUtil.validateAndConvert(getTenantId(), spaceConfigurationOutgoing, isNewConfig);
            ConfigurarionUtil.validateSpaceConfigurationConnectivity(spaceConfig);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getAllWorkspaceConfigurations() {

        List list = new ArrayList();

        Map<String, String> map1 = new HashMap<>();
        map1.put("id", "1");
        map1.put("workspaceName", "ws1");
        list.add(map1);

        Map<String, String> map2 = new HashMap<>();
        map2.put("id", "2");
        map2.put("workspaceName", "ws2");
        list.add(map2);

        Map<String, String> map3 = new HashMap<>();
        map3.put("id", "3");
        map3.put("workspaceName", "ws3");
        list.add(map3);


        //return Response.ok(map).build();
        return list;
    }

    @POST
    @Path("spaces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWorkspaceConfiguration(WorkspaceConfiguration workspaceConfiguration) {
        try {
            ConfigurarionUtil.validateWorkspaceConfiguration(getTenantId(), workspaceConfiguration, true);

            WorkspaceConfiguration myConfiguration = ConfigurationManager.getInstance().addWorkspaceConfiguration(getTenantId(), workspaceConfiguration);
            return Response.ok(myConfiguration).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

}
