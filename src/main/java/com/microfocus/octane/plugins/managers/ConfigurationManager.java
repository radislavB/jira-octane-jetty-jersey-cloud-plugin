package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfigurationOutgoing;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigurationManager extends BaseManager<ClientConfiguration> {

    public static ConfigurationManager instance = new ConfigurationManager();

    private Map<String, ClientConfiguration> configurations = new HashMap<>();
    private final String FILE_PREFIX = "config_";

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }


    public void addSpaceConfiguration(String clientKey, SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        SpaceConfiguration spaceConf = new SpaceConfiguration();
        spaceConf.setId(UUID.randomUUID().toString());
        spaceConf.setLocation(spaceConfigurationOutgoing.getLocation());
        spaceConf.setClientSecret(spaceConfigurationOutgoing.getClientSecret());
        spaceConf.setClientId(spaceConfigurationOutgoing.getClientId());


        getItemOrCreateNew(clientKey).getSpaces().add(spaceConf);
    }

    private SpaceConfiguration convert(SpaceConfigurationOutgoing spaceConfigurationOutgoing) {
        SpaceConfiguration spaceConf = new SpaceConfiguration();
        spaceConf.setId(StringUtils.isEmpty(spaceConfigurationOutgoing.getId()) ? UUID.randomUUID().toString() : spaceConfigurationOutgoing.getId());
        spaceConf.setLocation(spaceConfigurationOutgoing.getLocation());
        spaceConf.setClientSecret(spaceConfigurationOutgoing.getClientSecret());
        spaceConf.setClientId(spaceConfigurationOutgoing.getClientId());
        return spaceConf;
    }

    private SpaceConfigurationOutgoing convert(SpaceConfiguration internalSpaceConf) {
        SpaceConfigurationOutgoing spaceConf = new SpaceConfigurationOutgoing();
        spaceConf.setId(internalSpaceConf.getId());
        spaceConf.setLocation(internalSpaceConf.getLocation());
        spaceConf.setClientSecret(internalSpaceConf.getClientSecret());
        spaceConf.setClientId(internalSpaceConf.getClientId());
        return spaceConf;
    }

    public List<SpaceConfigurationOutgoing> getSpaceConfigurations(String clientKey) {
        List<SpaceConfigurationOutgoing> list = getItemOrCreateNew(clientKey).getSpaces().stream().map(c -> convert(c)).collect(Collectors.toList());
        return list;
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
