package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.KeyValueItem;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.octane.descriptors.OctaneEntityTypeManager;
import com.microfocus.octane.plugins.octane.rest.OctaneRestService;
import com.microfocus.octane.plugins.octane.rest.entities.OctaneEntityCollection;
import com.microfocus.octane.plugins.utils.PluginConstants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Path("octane")
public class OctaneMetadataResource {

    @Context
    private HttpServletRequest httpRequest;


    @GET
    @Path("/workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<KeyValueItem> getOctaneWorkspacess(@QueryParam("space-configuration-id") String spaceConfigurationId) {
        SpaceConfiguration spaceConfig = ConfigurationManager.getInstance().getSpaceConfigurationByIdOrThrowException(getTenantId(), spaceConfigurationId);
        OctaneEntityCollection workspaces = OctaneRestService.getWorkspaces(spaceConfig);
        List<KeyValueItem> items = workspaces.getData().stream().map(w -> new KeyValueItem(w.getId(), w.getName())).collect(Collectors.toList());
        return items;
    }

    @GET
    @Path("/possible-jira-fields")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getPossibleJiraFields(@QueryParam("space-configuration-id") String spaceConfigurationId, @QueryParam("workspace-id") long workspaceId) {
        SpaceConfiguration spaceConfig = ConfigurationManager.getInstance().getSpaceConfigurationByIdOrThrowException(getTenantId(), spaceConfigurationId);
        Collection<String> fieldName = OctaneRestService.getPossibleJiraFields(spaceConfig, workspaceId);
        return fieldName;
    }

    @GET
    @Path("/supported-types")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getSupportedTypes(@QueryParam("space-configuration-id") String spaceConfigurationId,
                                         @QueryParam("workspace-id") long workspaceId,
                                         @QueryParam("udf-name") String udfName) {
        SpaceConfiguration spaceConfig = ConfigurationManager.getInstance().getSpaceConfigurationByIdOrThrowException(getTenantId(), spaceConfigurationId);
        Collection<String> types = OctaneRestService.getSupportedOctaneTypes(spaceConfig, workspaceId, udfName);
        List<String> names = types.stream().map(t -> OctaneEntityTypeManager.getByTypeName(t).getLabel()).sorted().collect(Collectors.toList());
        return names;
    }

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

}
