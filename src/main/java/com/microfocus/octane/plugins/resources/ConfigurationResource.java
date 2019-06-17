package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.LocationParts;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import com.microfocus.octane.plugins.octane.rest.RestConnector;
import com.microfocus.octane.plugins.octane.rest.UrlConstants;
import com.microfocus.octane.plugins.octane.rest.entities.OctaneEntityCollection;
import com.microfocus.octane.plugins.octane.rest.query.OctaneQueryBuilder;
import com.microfocus.octane.plugins.utils.JsonUtils;
import com.microfocus.octane.plugins.utils.PluginConstants;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;


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

    @GET
    @Path("spaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SpaceConfigurationOutgoing> getAllSpaces() {
        String tenantId = (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
        List<SpaceConfigurationOutgoing> spaces = ConfigurationManager.getInstance().getSpaceConfigurations(tenantId);

        return spaces;
    }

    @POST
    @Path("test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testConnection(SpaceConfigurationOutgoing spaceConf) {
        String tenantId = (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
        String errorMessage = checkSpaceConfiguration(spaceConf);
        if (StringUtils.isEmpty(errorMessage)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(errorMessage).build();
        }
    }

    @GET
    @Path("workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getAllWorkspaces() {
        String tenantId = (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
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


    private String checkSpaceConfiguration(SpaceConfigurationOutgoing spaceModel) {
        String errorMsg = null;
        if (StringUtils.isEmpty(spaceModel.getLocation())) {
            errorMsg = "Location URL is required";
        } else if (StringUtils.isEmpty(spaceModel.getClientId())) {
            errorMsg = "Client ID is required";
        } else if (StringUtils.isEmpty(spaceModel.getClientSecret())) {
            errorMsg = "Client secret is required";
        } else {
            LocationParts locationParts = null;
            try {
                locationParts = parseUiLocation(spaceModel.getLocation());
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }

            if (errorMsg == null) {
                try {

                    /*String secret = OctaneConfigurationManager.PASSWORD_REPLACE.equals(spaceModel.getClientSecret()) ?
                            OctaneConfigurationManager.getInstance().getConfiguration().getClientSecret() :
                            spaceModel.getClientSecret();*/

                    RestConnector restConnector = new RestConnector();
                    restConnector.setBaseUrl(locationParts.getBaseUrl());
                    restConnector.setCredentials(spaceModel.getClientId(), spaceModel.getClientSecret());
                    boolean isConnected = restConnector.login();
                    if (!isConnected) {
                        errorMsg = "Failed to authenticate.";
                    } else {
                        String getWorspacesUrl = String.format(UrlConstants.PUBLIC_API_SHAREDSPACE_LEVEL_ENTITIES, locationParts.getSpaceId(), "workspaces");
                        String queryString = OctaneQueryBuilder.create().addSelectedFields("id").addPageSize(1).build();
                        Map<String, String> headers = new HashMap<>();
                        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);


                        String entitiesCollectionStr = restConnector.httpGet(getWorspacesUrl, Arrays.asList(queryString), headers).getResponseData();

                        OctaneEntityCollection workspaces = JsonUtils.parse(entitiesCollectionStr, OctaneEntityCollection.class);
                        if (workspaces.getData().isEmpty()) {
                            errorMsg = "Incorrect shared space ID.";
                        }

                    }
                } catch (Exception exc) {
                    if (exc.getMessage().contains("platform.not_authorized")) {
                        errorMsg = "Ensure your credentials are correct.";
                    } else if (exc.getMessage().contains("type shared_space does not exist")) {
                        errorMsg = "Shared space '" + locationParts.getSpaceId() + "' does not exist.";
                    } else if (exc.getCause() != null && exc.getCause() instanceof SSLHandshakeException && exc.getCause().getMessage().contains("Received fatal alert")) {
                        errorMsg = "Network exception, proxy settings may be missing.";
                    } else if (exc.getMessage().startsWith("Connection timed out")) {
                        errorMsg = "Timed out exception, proxy settings may be misconfigured.";
                    } else {
                        errorMsg = "Exception " + exc.getClass().getName() + " : " + exc.getMessage();
                        if (exc.getCause() != null) {
                            errorMsg += " . Cause : " + exc.getCause();//"Validate that location is correct.";
                        }
                    }
                }
            }
        }
        return errorMsg;
    }

    private static LocationParts parseUiLocation(String uiLocation) {
        String errorMsg = null;
        try {
            URL url = new URL(uiLocation);
            int contextPos = uiLocation.toLowerCase().indexOf("/ui");
            if (contextPos < 0) {
                errorMsg = "Location url is missing '/ui' part ";
            } else {
                LocationParts parts = new LocationParts();
                parts.setBaseUrl(uiLocation.substring(0, contextPos));
                Map<String, List<String>> queries = splitQuery(url);

                if (queries.containsKey(UrlConstants.PARAM_SHARED_SPACE)) {
                    List<String> sharedSpaceParamValue = queries.get(UrlConstants.PARAM_SHARED_SPACE);
                    if (sharedSpaceParamValue != null && !sharedSpaceParamValue.isEmpty()) {
                        String[] sharedSpaceAndWorkspace = sharedSpaceParamValue.get(0).split("/");
                        if (sharedSpaceAndWorkspace.length == 2 /*p=1001/1002*/ || sharedSpaceAndWorkspace.length == 1 /*p=1001*/) {
                            try {
                                long spaceId = Long.parseLong(sharedSpaceAndWorkspace[0].trim());
                                parts.setSpaceId(spaceId);
                                return parts;
                            } catch (NumberFormatException e) {
                                errorMsg = "Space id must be numeric value";
                            }
                        } else {
                            errorMsg = "Location url has invalid sharedspace/workspace part";
                        }
                    }
                } else {
                    errorMsg = "Location url is missing sharedspace id";
                }
            }
        } catch (Exception e) {
            errorMsg = "Location contains invalid URL ";
        }

        throw new IllegalArgumentException(errorMsg);

    }

    private static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }
}
