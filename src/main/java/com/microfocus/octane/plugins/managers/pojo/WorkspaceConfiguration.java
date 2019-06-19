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

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfiguration {

    private long workspaceId;
    private String workspaceName;
    private String octaneUdf;
    private List<String> octaneEntityTypes;
    private List<String> jiraIssueTypes;
    private List<String> jiraProjects;

    @JsonIgnore
    private SpaceConfiguration spaceConfiguration;

    public long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getOctaneUdf() {
        return octaneUdf;
    }

    public void setOctaneUdf(String octaneUdf) {
        this.octaneUdf = octaneUdf;
    }

    public List<String> getOctaneEntityTypes() {
        return octaneEntityTypes;
    }

    public void setOctaneEntityTypes(List<String> octaneEntityTypes) {
        this.octaneEntityTypes = octaneEntityTypes;
    }

    public List<String> getJiraIssueTypes() {
        return jiraIssueTypes;
    }

    public void setJiraIssueTypes(List<String> jiraIssueTypes) {
        this.jiraIssueTypes = jiraIssueTypes;
    }

    public List<String> getJiraProjects() {
        return jiraProjects;
    }

    public void setJiraProjects(List<String> jiraProjects) {
        this.jiraProjects = jiraProjects;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    @JsonIgnore
    public SpaceConfiguration getSpaceConfiguration() {
        return spaceConfiguration;
    }

    public void setSpaceConfiguration(SpaceConfiguration spaceConfiguration) {
        this.spaceConfiguration = spaceConfiguration;
    }
}
