package com.microfocus.octane.plugins.utils;

import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.LocationParts;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import com.microfocus.octane.plugins.octane.rest.RestConnector;
import com.microfocus.octane.plugins.octane.rest.UrlConstants;
import com.microfocus.octane.plugins.octane.rest.entities.OctaneEntityCollection;
import com.microfocus.octane.plugins.octane.rest.query.OctaneQueryBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.*;

public class ConfigurarionUtil {

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

    public static SpaceConfiguration validateAndConvert(String clientKey, SpaceConfigurationOutgoing spaceConfigurationOutgoing, boolean isNew) {
        if (StringUtils.isEmpty(spaceConfigurationOutgoing.getLocation())) {
            throw new IllegalArgumentException("Location URL is required");
        } else if (StringUtils.isEmpty(spaceConfigurationOutgoing.getClientId())) {
            throw new IllegalArgumentException("Client ID is required");
        } else if (StringUtils.isEmpty(spaceConfigurationOutgoing.getClientSecret())) {
            throw new IllegalArgumentException("Client secret is required");
        } else if (StringUtils.isEmpty(spaceConfigurationOutgoing.getName())) {
            throw new IllegalArgumentException("Name is required");
        }

        SpaceConfiguration spaceConf = new SpaceConfiguration();
        if (isNew) {
            if (StringUtils.isNotEmpty(spaceConfigurationOutgoing.getId())) {
                throw new IllegalArgumentException("New space configuration cannot contain configuration id");
            }
            spaceConf.setId(UUID.randomUUID().toString());
        } else {
            if (StringUtils.isEmpty(spaceConfigurationOutgoing.getId())) {
                throw new IllegalArgumentException("Configuration id is missing");
            }
            spaceConf.setId(spaceConfigurationOutgoing.getId());
        }

        spaceConf.setName(spaceConfigurationOutgoing.getName());
        spaceConf.setLocation(spaceConfigurationOutgoing.getLocation());
        spaceConf.setClientId(spaceConfigurationOutgoing.getClientId());
        spaceConf.setLocationParts(parseUiLocation(spaceConfigurationOutgoing.getLocation()));

        String clientSecret = spaceConfigurationOutgoing.getClientSecret();
        if (PluginConstants.PASSWORD_REPLACEMENT.equals(clientSecret) && !isNew) {
            Optional<SpaceConfiguration> opt = ConfigurationManager.getInstance().getSpaceConfigurationById(clientKey, spaceConfigurationOutgoing.getId());
            if (opt.isPresent()) {
                clientSecret = opt.get().getClientSecret();
            }
        }
        spaceConf.setClientSecret(clientSecret);
        return spaceConf;
    }

    public static SpaceConfigurationOutgoing convert(SpaceConfiguration internalSpaceConf) {
        SpaceConfigurationOutgoing spaceConf = new SpaceConfigurationOutgoing();
        spaceConf.setId(internalSpaceConf.getId());
        spaceConf.setName(internalSpaceConf.getName());
        spaceConf.setLocation(internalSpaceConf.getLocation());
        spaceConf.setClientSecret(PluginConstants.PASSWORD_REPLACEMENT);
        spaceConf.setClientId(internalSpaceConf.getClientId());
        return spaceConf;
    }

    public static void validateSpaceUrlIsUnique(String clientKey, SpaceConfiguration spaceConfiguration) {
        Optional<SpaceConfiguration> opt = ConfigurationManager.getInstance().getSpaceConfigurations(clientKey).stream()
                .filter((s -> !s.getId().equals(spaceConfiguration.getId()) //don't check the same configuration
                        && s.getLocationParts().getKey().equals(spaceConfiguration.getLocationParts().getKey())))
                .findFirst();

        if (opt.isPresent()) {
            String msg = String.format("Space is already defined in space configuration '%s'", opt.get().getName());
            throw new IllegalArgumentException(msg);
        }

    }

    public static void validateSpaceConfigurationConnectivity(SpaceConfiguration spaceConfig) {
        try {
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
