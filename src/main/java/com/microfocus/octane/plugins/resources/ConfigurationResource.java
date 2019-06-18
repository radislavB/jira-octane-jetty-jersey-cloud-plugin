package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import com.microfocus.octane.plugins.octane.rest.RestConnector;
import com.microfocus.octane.plugins.octane.rest.UrlConstants;
import com.microfocus.octane.plugins.octane.rest.entities.OctaneEntityCollection;
import com.microfocus.octane.plugins.octane.rest.query.OctaneQueryBuilder;
import com.microfocus.octane.plugins.utils.ConfigConversionUtil;
import com.microfocus.octane.plugins.utils.JsonUtils;
import com.microfocus.octane.plugins.utils.PluginConstants;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;


@Path("configuration")
public class ConfigurationResource {

    @Context
    private HttpServletRequest httpRequest;

    @GET
    @Path("spaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SpaceConfigurationOutgoing> getAllSpaces() {
        List<SpaceConfigurationOutgoing> spaces = ConfigurationManager.getInstance().getSpaceConfigurations(getTenantId())
                .stream().map(c -> ConfigConversionUtil.convert(c)).collect(Collectors.toList());

        return spaces;
    }

    @POST
    @Path("spaces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSpaceConfiguration(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        try {
            SpaceConfiguration spaceConfig = ConfigConversionUtil.convert(spaceConfigurationOutgoing);
            validateSpaceConfigurationConnectivity(spaceConfig);
            ConfigurationManager.getInstance().addSpaceConfiguration(getTenantId(), spaceConfig);
            return Response.ok(ConfigConversionUtil.convert(spaceConfig)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testConnection(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        try {
            SpaceConfiguration spaceConfig = ConfigConversionUtil.convert(spaceConfigurationOutgoing);
            validateSpaceConfigurationConnectivity(spaceConfig);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getAllWorkspaces() {

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

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

    private void validateSpaceConfigurationConnectivity(SpaceConfiguration spaceConfig) {
        try {

                /*String secret = OctaneConfigurationManager.PASSWORD_REPLACE.equals(spaceModel.getClientSecret()) ?
                        OctaneConfigurationManager.getInstance().getConfiguration().getClientSecret() :
                        spaceModel.getClientSecret();*/
            RestConnector restConnector = new RestConnector();
            restConnector.setBaseUrl(spaceConfig.getLocationParts().getBaseUrl());
            restConnector.setCredentials(spaceConfig.getClientId(), spaceConfig.getClientSecret());
            boolean isConnected = restConnector.login();
            if (!isConnected) {
                throw new IllegalArgumentException("Failed to authenticate.");
            } else {
                String getWorspacesUrl = String.format(UrlConstants.PUBLIC_API_SHAREDSPACE_LEVEL_ENTITIES, spaceConfig.getLocationParts().getSpaceId(), "workspaces");
                String queryString = OctaneQueryBuilder.create().addSelectedFields("id").addPageSize(1).build();
                Map<String, String> headers = new HashMap<>();
                headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);


                String entitiesCollectionStr = restConnector.httpGet(getWorspacesUrl, Arrays.asList(queryString), headers).getResponseData();

                OctaneEntityCollection workspaces = JsonUtils.parse(entitiesCollectionStr, OctaneEntityCollection.class);
                if (workspaces.getData().isEmpty()) {
                    throw new IllegalArgumentException("Incorrect shared space ID.");
                }
            }
        } catch (Exception exc) {
            String myErrorMessage = null;
            if (exc.getMessage().contains("platform.not_authorized")) {
                myErrorMessage = "Ensure your credentials are correct.";
            } else if (exc.getMessage().contains("SharedSpaceNotFoundException")) {
                myErrorMessage = "Shared space '" + spaceConfig.getLocationParts().getSpaceId() + "' does not exist.";
            } else if (exc.getCause() != null && exc.getCause() instanceof SSLHandshakeException && exc.getCause().getMessage().contains("Received fatal alert")) {
                myErrorMessage = "Network exception, proxy settings may be missing.";
            } else if (exc.getMessage().startsWith("Connection timed out")) {
                myErrorMessage = "Timed out exception, proxy settings may be misconfigured.";
            } else if (exc.getCause() != null && exc.getCause() instanceof UnknownHostException) {
                myErrorMessage = "Location is not available.";
            } else {
                myErrorMessage = exc.getMessage();
                //errorMsg = "Exception " + exc.getClass().getName() + " : " + exc.getMessage();
                        /*if (exc.getCause() != null) {
                            errorMsg += " . Cause : " + exc.getCause();//"Validate that location is correct.";
                        }*/
            }
            throw new IllegalArgumentException(myErrorMessage);
        }
    }
}
