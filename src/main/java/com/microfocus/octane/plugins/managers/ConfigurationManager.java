package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ConfigurationManager extends BaseManager<ClientConfiguration> {

    public static ConfigurationManager instance = new ConfigurationManager();
    private final String FILE_PREFIX = "config_";

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }

    public SpaceConfiguration addSpaceConfiguration(String clientKey, SpaceConfiguration spaceConfiguration) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getSpaces().add(spaceConfiguration);
        save(clientKey, conf);
        return spaceConfiguration;
    }

    public SpaceConfiguration updateSpaceConfiguration(String clientKey, SpaceConfiguration spaceConfiguration) throws IOException {
        SpaceConfiguration conf = getSpaceConfigurationByIdOrThrowException(clientKey, spaceConfiguration.getId());

        conf.setName(spaceConfiguration.getName());
        conf.setLocation(spaceConfiguration.getLocation());
        conf.setLocationParts(spaceConfiguration.getLocationParts());
        conf.setClientId(spaceConfiguration.getClientId());
        conf.setClientSecret(spaceConfiguration.getClientSecret());
        save(clientKey, getItemOrCreateNew(clientKey));
        return conf;

    }

    public boolean removeSpaceConfiguration(String clientKey, String spaceConfigurationId) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        Optional<SpaceConfiguration> opt = getSpaceConfigurationById(clientKey, spaceConfigurationId);
        if (opt.isPresent()) {
            conf.getSpaces().remove(opt.get());
            save(clientKey, conf);
            return true;
        } else {
            return false;
        }
    }


    public List<SpaceConfiguration> getSpaceConfigurations(String clientKey) {
        return getItemOrCreateNew(clientKey).getSpaces();
    }

    public Optional<SpaceConfiguration> getSpaceConfigurationById(String clientKey, String spaceConfigurationId) {
        if (StringUtils.isEmpty(spaceConfigurationId)) {
            throw new IllegalArgumentException("Space configuration id should not be empty");
        }
        return getItemOrCreateNew(clientKey).getSpaces().stream().filter(c -> spaceConfigurationId.equals(c.getId())).findFirst();
    }

    public SpaceConfiguration getSpaceConfigurationByIdOrThrowException(String clientKey, String spaceConfigurationId) {
        if (StringUtils.isEmpty(spaceConfigurationId)) {
            throw new IllegalArgumentException("Space configuration id should not be empty");
        }
        Optional<SpaceConfiguration> opt = getItemOrCreateNew(clientKey).getSpaces().stream().filter(c -> spaceConfigurationId.equals(c.getId())).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        } else {
            throw new RuntimeException(String.format("Space configuration '%s' not found", spaceConfigurationId));
        }
    }

    public WorkspaceConfiguration addWorkspaceConfiguration(String clientKey, WorkspaceConfiguration workspaceConfiguration) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getWorkspaces().add(workspaceConfiguration);
        save(clientKey, conf);
        return workspaceConfiguration;
    }

    public WorkspaceConfiguration updateWorkspaceConfiguration(String clientKey, WorkspaceConfiguration workspaceConfiguration) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        WorkspaceConfiguration workspaceConf = getWorkspaceConfigurationByIdOrThrowException(clientKey, workspaceConfiguration.getId());
        conf.getWorkspaces().remove(conf);
        conf.getWorkspaces().add(workspaceConfiguration);
        save(clientKey, conf);

        return workspaceConf;
    }

    public boolean removeWorkspaceConfiguration(String clientKey, String workspaceConfigurationId) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        Optional<WorkspaceConfiguration> opt = getWorkspaceConfigurationById(clientKey, workspaceConfigurationId);
        if (opt.isPresent()) {
            conf.getSpaces().remove(opt.get());
            save(clientKey, conf);
            return true;
        } else {
            return false;
        }
    }

    public Optional<WorkspaceConfiguration> getWorkspaceConfigurationById(String clientKey, String workspaceConfigurationId) {
        if (StringUtils.isEmpty(workspaceConfigurationId)) {
            throw new IllegalArgumentException("Workspace configuration id should not be empty");
        }
        return getItemOrCreateNew(clientKey).getWorkspaces().stream().filter(c -> workspaceConfigurationId.equals(c.getId())).findFirst();
    }

    public WorkspaceConfiguration getWorkspaceConfigurationByIdOrThrowException(String clientKey, String workspaceConfigurationId) {
        if (StringUtils.isEmpty(workspaceConfigurationId)) {
            throw new IllegalArgumentException("Workspace configuration id should not be empty");
        }
        Optional<WorkspaceConfiguration> opt = getItemOrCreateNew(clientKey).getWorkspaces().stream().filter(c -> workspaceConfigurationId.equals(c.getId())).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        } else {
            throw new RuntimeException(String.format("Workspace configuration '%s' not found", workspaceConfigurationId));
        }
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
