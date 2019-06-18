package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;

import java.io.IOException;
import java.util.List;

public class ConfigurationManager extends BaseManager<ClientConfiguration> {

    public static ConfigurationManager instance = new ConfigurationManager();
    private final String FILE_PREFIX = "config_";

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }


    public SpaceConfiguration addSpaceConfiguration(String clientKey, SpaceConfiguration spaceConfiguration) throws IOException {

        spaceConfiguration.setId(generateId(spaceConfiguration));
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getSpaces().add(spaceConfiguration);
        save(clientKey, conf);
        return spaceConfiguration;
    }

    private String generateId(SpaceConfiguration spaceConfiguration) {
        String key = spaceConfiguration.getLocationParts().getBaseUrl() + "?p=" + spaceConfiguration.getLocationParts().getSpaceId();
        return key.toLowerCase();
    }

    public SpaceConfiguration updateSpaceConfiguration(String clientKey, SpaceConfiguration spaceConfiguration) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getSpaces().add(spaceConfiguration);
        save(clientKey, conf);
        return spaceConfiguration;
    }


    public List<SpaceConfiguration> getSpaceConfigurations(String clientKey) {
        return getItemOrCreateNew(clientKey).getSpaces();
    }

    @Override
    protected Class<ClientConfiguration> getTypeClass() {
        return ClientConfiguration.class;
    }

    @Override
    protected String getItemFilePrefix() {
        return FILE_PREFIX;
    }

}
