package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfigurationManager extends BaseManager<ClientConfiguration> {

    public static ConfigurationManager instance = new ConfigurationManager();
    private final String FILE_PREFIX = "config_";

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }

    public SpaceConfiguration addSpaceConfiguration(String clientKey, SpaceConfiguration spaceConfiguration) throws IOException {

        spaceConfiguration.setId(UUID.randomUUID().toString());
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getSpaces().add(spaceConfiguration);
        save(clientKey, conf);
        return spaceConfiguration;
    }

    public boolean removeSpaceConfiguration(String clientKey, String spaceConfigurationId) throws IOException {
        if (StringUtils.isEmpty(spaceConfigurationId)) {
            throw new IllegalArgumentException("Space configuration id should not be empty");
        }
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        Optional<SpaceConfiguration> opt = conf.getSpaces().stream().filter(s -> spaceConfigurationId.equals(s.getId())).findFirst();
        if (opt.isPresent()) {
            conf.getSpaces().remove(opt.get());
            save(clientKey, conf);
            return true;
        } else {
            return false;
        }
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
