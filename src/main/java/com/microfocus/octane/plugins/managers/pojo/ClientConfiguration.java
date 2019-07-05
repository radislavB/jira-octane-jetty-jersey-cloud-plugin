/*
 *     Copyright 2018 EntIT Software LLC, a Micro Focus company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.microfocus.octane.plugins.managers.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientConfiguration {

    private List<SpaceConfiguration> spaces = new ArrayList<>();
    private List<WorkspaceConfiguration> workspaces = new ArrayList<>();

    public List<SpaceConfiguration> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<SpaceConfiguration> spaces) {
        this.spaces = spaces;
    }

    public List<WorkspaceConfiguration> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<WorkspaceConfiguration> workspaces) {
        this.workspaces = workspaces;
    }

    @JsonIgnore
    public Optional<WorkspaceConfiguration> getSupportedWorkspaceConfiguration(String projectId) {
        Optional<WorkspaceConfiguration> wcOpt = getWorkspaces().stream().filter(w -> w.isProjectIdSupported(projectId)).findFirst();
        return wcOpt;
    }

    @JsonIgnore
    public SpaceConfiguration getSpaceConfigurationById(String spaceConfigurationId) {
        Optional<SpaceConfiguration> scOpt = getSpaces().stream().filter(s -> s.getId().equals(spaceConfigurationId)).findFirst();
        if (!scOpt.isPresent()) {
            throw new NoSuchElementException("Space configuration with id '" + spaceConfigurationId + "' is not found");
        }
        return scOpt.get();
    }
}
