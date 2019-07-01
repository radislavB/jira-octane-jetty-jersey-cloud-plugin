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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfiguration {

    private String id;
    private String spaceConfigurationId;
    private KeyValueItem workspace;
    private String octaneUdf;
    private List<String> octaneEntityTypes;
    private List<KeyValueItem> jiraIssueTypes;
    private List<KeyValueItem> jiraProjects;

    @JsonIgnore
    private Set<String> jiraIssueTypesIds;

    @JsonIgnore
    private Set<String> jiraProjectsIds;


    public boolean isProjectIdSupported(String projectId) {
        if (jiraProjectsIds == null) {
            jiraProjectsIds = jiraProjects.stream().map(KeyValueItem::getId).collect(Collectors.toSet());
        }
        return jiraProjectsIds.contains(projectId);
    }

    public boolean isIssueTypeIdSupported(String issueTypeId) {
        if (jiraIssueTypesIds == null) {
            jiraIssueTypesIds = jiraIssueTypes.stream().map(KeyValueItem::getId).collect(Collectors.toSet());
        }
        return jiraIssueTypesIds.contains(issueTypeId);
    }

    public String getOctaneUdf() {
        return octaneUdf;
    }

    public WorkspaceConfiguration setOctaneUdf(String octaneUdf) {
        this.octaneUdf = octaneUdf;
        return this;
    }

    public List<String> getOctaneEntityTypes() {
        return octaneEntityTypes;
    }

    public WorkspaceConfiguration setOctaneEntityTypes(List<String> octaneEntityTypes) {
        this.octaneEntityTypes = octaneEntityTypes;
        return this;
    }

    public List<KeyValueItem> getJiraIssueTypes() {
        return jiraIssueTypes;
    }

    public WorkspaceConfiguration setJiraIssueTypes(List<KeyValueItem> jiraIssueTypes) {
        this.jiraIssueTypes = jiraIssueTypes;
        return this;
    }

    public List<KeyValueItem> getJiraProjects() {
        return jiraProjects;
    }

    public WorkspaceConfiguration setJiraProjects(List<KeyValueItem> jiraProjects) {
        this.jiraProjects = jiraProjects;
        return this;
    }

    public String getId() {
        return id;
    }

    public WorkspaceConfiguration setId(String id) {
        this.id = id;
        return this;
    }

    public String getSpaceConfigurationId() {
        return spaceConfigurationId;
    }

    public WorkspaceConfiguration setSpaceConfigurationId(String spaceConfigurationId) {
        this.spaceConfigurationId = spaceConfigurationId;
        return this;
    }

    public KeyValueItem getWorkspace() {
        return workspace;
    }

    public WorkspaceConfiguration setWorkspace(KeyValueItem workspace) {
        this.workspace = workspace;
        return this;
    }
}
