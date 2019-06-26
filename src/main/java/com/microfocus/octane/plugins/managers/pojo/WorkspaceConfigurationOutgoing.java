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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfigurationOutgoing {

    private String id;
    private String spaceConfigurationId;
    private KeyValueItem workspace;
    private String octaneUdf;
    private List<String> octaneEntityTypesLabels;
    private List<KeyValueItem> jiraIssueTypes;
    private List<KeyValueItem> jiraProjects;


    public String getOctaneUdf() {
        return octaneUdf;
    }

    public WorkspaceConfigurationOutgoing setOctaneUdf(String octaneUdf) {
        this.octaneUdf = octaneUdf;
        return this;
    }

    public List<String> getOctaneEntityTypesLabels() {
        return octaneEntityTypesLabels;
    }

    public WorkspaceConfigurationOutgoing setOctaneEntityTypesLabels(List<String> octaneEntityTypesLabels) {
        this.octaneEntityTypesLabels = octaneEntityTypesLabels;
        return this;
    }

    public List<KeyValueItem> getJiraIssueTypes() {
        return jiraIssueTypes;
    }

    public WorkspaceConfigurationOutgoing setJiraIssueTypes(List<KeyValueItem> jiraIssueTypes) {
        this.jiraIssueTypes = jiraIssueTypes;
        return this;
    }

    public List<KeyValueItem> getJiraProjects() {
        return jiraProjects;
    }

    public WorkspaceConfigurationOutgoing setJiraProjects(List<KeyValueItem> jiraProjects) {
        this.jiraProjects = jiraProjects;
        return this;
    }

    public String getId() {
        return id;
    }

    public WorkspaceConfigurationOutgoing setId(String id) {
        this.id = id;
        return this;
    }

    public String getSpaceConfigurationId() {
        return spaceConfigurationId;
    }

    public WorkspaceConfigurationOutgoing setSpaceConfigurationId(String spaceConfigurationId) {
        this.spaceConfigurationId = spaceConfigurationId;
        return this;
    }

    public KeyValueItem getWorkspace() {
        return workspace;
    }

    public WorkspaceConfigurationOutgoing setWorkspace(KeyValueItem workspace) {
        this.workspace = workspace;
        return this;
    }
}
