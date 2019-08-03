package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfigurationOutgoing;
import com.microfocus.octane.plugins.utils.ConfigurarionUtil;
import com.microfocus.octane.plugins.utils.PluginConstants;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
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
                .stream().map(c -> ConfigurarionUtil.convertToOutgoing(c)).collect(Collectors.toList());

        return spaces;
    }

    @POST
    @Path("spaces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSpaceConfiguration(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        try {
            SpaceConfiguration spaceConfig = ConfigurarionUtil.validateAndConvertToInternal(getTenantId(), spaceConfigurationOutgoing, true);
            ConfigurarionUtil.doFullSpaceConfigurationValidation(getTenantId(), spaceConfig);
            ConfigurationManager.getInstance().addSpaceConfiguration(getTenantId(), spaceConfig);
            return Response.ok(ConfigurarionUtil.convertToOutgoing(spaceConfig)).build();
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
            SpaceConfiguration spaceConfig = ConfigurarionUtil.validateAndConvertToInternal(getTenantId(), spaceConfigurationOutgoing, false);
            if (!spaceConfig.getId().equals(spaceId)) {
                return Response.status(Response.Status.CONFLICT).entity("Space id in entity should be equal to id in path parameter").build();
            }
            ConfigurarionUtil.doFullSpaceConfigurationValidation(getTenantId(), spaceConfig);
            ConfigurationManager.getInstance().updateSpaceConfiguration(getTenantId(), spaceConfig);
            return Response.ok(ConfigurarionUtil.convertToOutgoing(spaceConfig)).build();
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
            SpaceConfiguration spaceConfig = ConfigurarionUtil.validateAndConvertToInternal(getTenantId(), spaceConfigurationOutgoing, isNewConfig);
            ConfigurarionUtil.validateSpaceConfigurationConnectivity(spaceConfig);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkspaceConfigurationOutgoing> getAllWorkspaceConfigurations() {

        Map<String, String> spaceConfigurationId2Name = getSpaceConfigurationId2Name();
        List<WorkspaceConfigurationOutgoing> spaces = ConfigurationManager.getInstance().getWorkspaceConfigurations(getTenantId())
                .stream().map(c -> ConfigurarionUtil.convertToOutgoing(c, spaceConfigurationId2Name)).collect(Collectors.toList());

        return spaces;
    }

    @POST
    @Path("workspaces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addWorkspaceConfiguration(WorkspaceConfigurationOutgoing wco) {
        try {
            WorkspaceConfiguration wc = ConfigurarionUtil.validateAndConvertToInternal(wco, true);
            wc = ConfigurationManager.getInstance().addWorkspaceConfiguration(getTenantId(), wc);
            WorkspaceConfigurationOutgoing outputWco = ConfigurarionUtil.convertToOutgoing(wc, getSpaceConfigurationId2Name());
            return Response.ok(outputWco).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("workspaces/{workspaceConfigurationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWorkspaceConfiguration(@PathParam("workspaceConfigurationId") String workspaceConfigurationId, WorkspaceConfigurationOutgoing wco) {
        try {
            WorkspaceConfiguration wc = ConfigurarionUtil.validateAndConvertToInternal(wco, false);
            if (!wco.getId().equals(workspaceConfigurationId)) {
                return Response.status(Response.Status.CONFLICT).entity("Workspace configuration id in entity should be equal to id in path parameter").build();
            }
            WorkspaceConfiguration updatedWc = ConfigurationManager.getInstance().updateWorkspaceConfiguration(getTenantId(), wc);
            WorkspaceConfigurationOutgoing outputWco = ConfigurarionUtil.convertToOutgoing(updatedWc, getSpaceConfigurationId2Name());
            return Response.ok(outputWco).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("workspaces/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean deleteWorkspaceConfiguration(@PathParam("id") String id) throws IOException {
        boolean isRemoved = ConfigurationManager.getInstance().removeWorkspaceConfiguration(getTenantId(), id);
        return isRemoved;
    }

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

    private Map<String, String> getSpaceConfigurationId2Name() {
        Map<String, String> spaceConfigurationId2Name = ConfigurationManager.getInstance().getSpaceConfigurations(getTenantId()).stream()
                .collect(Collectors.toMap(SpaceConfiguration::getId, SpaceConfiguration::getName));
        return spaceConfigurationId2Name;
    }

}
