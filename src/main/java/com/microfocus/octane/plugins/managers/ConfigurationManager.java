package com.microfocus.octane.plugins.managers;

import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigurationManager extends BaseManager<ClientConfiguration> {

    public final static ConfigurationManager instance = new ConfigurationManager();
    private final static String FILE_NAME = "configuration.json";

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }

    public ClientConfiguration getClientConfiguration(String clientKey) {
        return getItem(clientKey);
    }

    public SpaceConfiguration addSpaceConfiguration(String clientKey, SpaceConfiguration spaceConfiguration) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getSpaces().add(spaceConfiguration);
        save(clientKey, conf);
        return spaceConfiguration;
    }

    public SpaceConfiguration updateSpaceConfiguration(String clientKey, SpaceConfiguration updatedSpaceConfiguration) throws IOException {
        SpaceConfiguration conf = getSpaceConfigurationByIdOrThrowException(clientKey, updatedSpaceConfiguration.getId());
        getClientConfiguration(clientKey).getSpaces().remove(conf);
        getClientConfiguration(clientKey).getSpaces().add(updatedSpaceConfiguration);
        save(clientKey, getItemOrCreateNew(clientKey));
        return conf;
    }

    public boolean removeSpaceConfiguration(String clientKey, String spaceConfigurationId) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        Optional<SpaceConfiguration> opt = getSpaceConfigurationById(clientKey, spaceConfigurationId);
        if (opt.isPresent()) {
            List<WorkspaceConfiguration> workspaceConfigs = conf.getWorkspaces().stream()
                    .filter(wc->spaceConfigurationId.equals(wc.getSpaceConfigurationId()))
                    .collect(Collectors.toList());
            conf.getSpaces().remove(opt.get());
            conf.getWorkspaces().removeAll(workspaceConfigs);
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

    public List<WorkspaceConfiguration> getWorkspaceConfigurations(String clientKey) {
        return getItemOrCreateNew(clientKey).getWorkspaces();
    }

    public WorkspaceConfiguration addWorkspaceConfiguration(String clientKey, WorkspaceConfiguration workspaceConfiguration) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        conf.getWorkspaces().add(workspaceConfiguration);
        save(clientKey, conf);
        return workspaceConfiguration;
    }

    public WorkspaceConfiguration updateWorkspaceConfiguration(String clientKey, WorkspaceConfiguration updatedWc) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        WorkspaceConfiguration existingWc = getWorkspaceConfigurationByIdOrThrowException(clientKey, updatedWc.getId());
        conf.getWorkspaces().remove(existingWc);
        conf.getWorkspaces().add(updatedWc);
        save(clientKey, conf);
        return updatedWc;
    }

    public boolean removeWorkspaceConfiguration(String clientKey, String workspaceConfigurationId) throws IOException {
        ClientConfiguration conf = getItemOrCreateNew(clientKey);
        Optional<WorkspaceConfiguration> opt = getWorkspaceConfigurationById(clientKey, workspaceConfigurationId);
        if (opt.isPresent()) {
            conf.getWorkspaces().remove(opt.get());
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
    protected String getItemFileName() {
        return FILE_NAME;
    }
}
