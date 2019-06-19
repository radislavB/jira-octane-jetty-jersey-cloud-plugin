package com.microfocus.octane.plugins.utils;

import com.microfocus.octane.plugins.managers.pojo.LocationParts;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import com.microfocus.octane.plugins.octane.rest.UrlConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConfigConversionUtil {

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

    public static SpaceConfiguration convert(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
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
        spaceConf.setId(spaceConfigurationOutgoing.getId());
        spaceConf.setName(spaceConfigurationOutgoing.getName());
        spaceConf.setLocation(spaceConfigurationOutgoing.getLocation());
        spaceConf.setClientSecret(spaceConfigurationOutgoing.getClientSecret());
        spaceConf.setClientId(spaceConfigurationOutgoing.getClientId());
        spaceConf.setLocationParts(parseUiLocation(spaceConfigurationOutgoing.getLocation()));
        return spaceConf;
    }

    public static SpaceConfigurationOutgoing convert(SpaceConfiguration internalSpaceConf) {
        SpaceConfigurationOutgoing spaceConf = new SpaceConfigurationOutgoing();
        spaceConf.setId(internalSpaceConf.getId());
        spaceConf.setName(internalSpaceConf.getName());
        spaceConf.setLocation(internalSpaceConf.getLocation());
        spaceConf.setClientSecret(internalSpaceConf.getClientSecret());
        spaceConf.setClientId(internalSpaceConf.getClientId());
        return spaceConf;
    }
}
