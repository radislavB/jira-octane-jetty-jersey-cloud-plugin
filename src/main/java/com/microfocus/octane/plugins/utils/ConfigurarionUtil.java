package com.microfocus.octane.plugins.utils;

import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.*;
import com.microfocus.octane.plugins.octane.descriptors.OctaneEntityTypeDescriptor;
import com.microfocus.octane.plugins.octane.descriptors.OctaneEntityTypeManager;
import com.microfocus.octane.plugins.octane.rest.OctaneRestService;
import com.microfocus.octane.plugins.octane.rest.UrlConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

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

    public static SpaceConfiguration validateAndConvertToInternal(String clientKey, SpaceConfigurationOutgoing sco, boolean isNew) {
        if (StringUtils.isEmpty(sco.getLocation())) {
            throw new IllegalArgumentException("Location URL is required");
        } else if (StringUtils.isEmpty(sco.getClientId())) {
            throw new IllegalArgumentException("Client ID is required");
        } else if (StringUtils.isEmpty(sco.getClientSecret())) {
            throw new IllegalArgumentException("Client secret is required");
        } else if (StringUtils.isEmpty(sco.getName())) {
            throw new IllegalArgumentException("Name is required");
        }

        if (isNew) {
            if (StringUtils.isNotEmpty(sco.getId())) {
                throw new IllegalArgumentException("New space configuration cannot contain configuration id");
            }
            sco.setId(UUID.randomUUID().toString());
        } else {
            if (StringUtils.isEmpty(sco.getId())) {
                throw new IllegalArgumentException("Configuration id is missing");
            }
            sco.setId(sco.getId());
        }

        String clientSecret = sco.getClientSecret();
        if (PluginConstants.PASSWORD_REPLACEMENT.equals(clientSecret) && !isNew) {
            Optional<SpaceConfiguration> opt = ConfigurationManager.getInstance().getSpaceConfigurationById(clientKey, sco.getId());
            if (opt.isPresent()) {
                clientSecret = opt.get().getClientSecret();
            }
        }

        SpaceConfiguration sc = new SpaceConfiguration().setId(sco.getId())
                .setName(sco.getName())
                .setLocation(sco.getLocation())
                .setClientId(sco.getClientId())
                .setLocationParts(parseUiLocation(sco.getLocation()))
                .setClientSecret(clientSecret);
        return sc;
    }

    public static SpaceConfigurationOutgoing convertToOutgoing(SpaceConfiguration sc) {
        SpaceConfigurationOutgoing sco = new SpaceConfigurationOutgoing()
                .setId(sc.getId())
                .setName(sc.getName())
                .setLocation(sc.getLocation())
                .setClientSecret(PluginConstants.PASSWORD_REPLACEMENT)
                .setClientId(sc.getClientId());
        return sco;
    }

    public static void doFullSpaceConfigurationValidation(String clientKey, SpaceConfiguration spaceConfiguration) {
        validateSpaceUrlIsUnique(clientKey, spaceConfiguration);
        validateSpaceNameIsUnique(clientKey, spaceConfiguration);
        validateSpaceConfigurationConnectivity(spaceConfiguration);
    }

    private static void validateSpaceUrlIsUnique(String clientKey, SpaceConfiguration spaceConfiguration) {
        Optional<SpaceConfiguration> opt = ConfigurationManager.getInstance().getSpaceConfigurations(clientKey).stream()
                .filter((s -> !s.getId().equals(spaceConfiguration.getId()) //don't check the same configuration
                        && s.getLocationParts().getKey().equals(spaceConfiguration.getLocationParts().getKey())))
                .findFirst();

        if (opt.isPresent()) {
            String msg = String.format("Space is already defined in space configuration '%s'", opt.get().getName());
            throw new IllegalArgumentException(msg);
        }
    }

    private static void validateSpaceNameIsUnique(String clientKey, SpaceConfiguration spaceConfiguration) {
        Optional<SpaceConfiguration> opt = ConfigurationManager.getInstance().getSpaceConfigurations(clientKey).stream()
                .filter((s -> !s.getId().equals(spaceConfiguration.getId()) //don't check the same configuration
                        && s.getName().equals(spaceConfiguration.getName())))
                .findFirst();

        if (opt.isPresent()) {
            String msg = String.format("Name '%s' is already in use by another space configuration.", spaceConfiguration.getName());
            throw new IllegalArgumentException(msg);
        }
    }

    public static void validateSpaceConfigurationConnectivity(SpaceConfiguration spaceConfig) {
        OctaneRestService.getWorkspaces(spaceConfig);
    }

    public static WorkspaceConfiguration validateAndConvertToInternal(WorkspaceConfigurationOutgoing wco, boolean isNew) {
        if (StringUtils.isEmpty(wco.getOctaneUdf())) {
            throw new IllegalArgumentException("Octane UDF is required");
        } else if (StringUtils.isEmpty(wco.getSpaceConfigurationId())) {
            throw new IllegalArgumentException("Space configuration ID is required");
        } else if (wco.getJiraIssueTypes() == null || wco.getJiraIssueTypes().isEmpty()) {
            throw new IllegalArgumentException("Jira issue types are required");
        } else if (wco.getJiraProjects() == null || wco.getJiraProjects().isEmpty()) {
            throw new IllegalArgumentException("Jira projects are required");
        } else if (wco.getWorkspace() == null || StringUtils.isEmpty(wco.getWorkspace().getId())) {
            throw new IllegalArgumentException("Workspace is required");
        }

        if (isNew) {
            if (StringUtils.isNotEmpty(wco.getId())) {
                throw new IllegalArgumentException("New workspace configuration cannot contain configuration id");
            }
            wco.setId(UUID.randomUUID().toString());
        } else {
            if (StringUtils.isEmpty(wco.getId())) {
                throw new IllegalArgumentException("Configuration id is missing");
            }
        }


        List<String> octaneTypeKeys = wco.getOctaneEntityTypesLabels().stream()
                .map(label -> OctaneEntityTypeManager.getByLabel(label).getTypeName())
                .collect(Collectors.toList());



        WorkspaceConfiguration wc = new WorkspaceConfiguration()
                .setId(wco.getId())
                .setOctaneUdf(wco.getOctaneUdf())
                .setJiraIssueTypes(wco.getJiraIssueTypes())
                .setJiraProjects(wco.getJiraProjects())
                .setOctaneEntityTypes(octaneTypeKeys)
                .setSpaceConfigurationId(wco.getSpaceConfigurationId())
                .setWorkspace(wco.getWorkspace());
        return wc;
    }

    public static WorkspaceConfigurationOutgoing convertToOutgoing(WorkspaceConfiguration wc) {

        List<String> octaneTypes = wc.getOctaneEntityTypes().stream()
                .map(typeName -> {
                    OctaneEntityTypeDescriptor desc = OctaneEntityTypeManager.getByTypeName(typeName);
                    return desc == null ? "" : desc.getLabel();
                })
                .sorted().collect(Collectors.toList());

        WorkspaceConfigurationOutgoing wco = new WorkspaceConfigurationOutgoing()
                .setId(wc.getId())
                .setOctaneUdf(wc.getOctaneUdf())
                .setJiraIssueTypes(wc.getJiraIssueTypes())
                .setJiraProjects(wc.getJiraProjects())
                .setOctaneEntityTypesLabels(octaneTypes)
                .setSpaceConfigurationId(wc.getSpaceConfigurationId())
                .setWorkspace(wc.getWorkspace());
        return wco;
    }

}
