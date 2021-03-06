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
import com.microfocus.octane.plugins.octane.rest.OctaneRestService;
import com.microfocus.octane.plugins.octane.rest.RestConnector;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SpaceConfiguration {

    private String id;
    private String name;
    private String location;
    private LocationParts locationParts;
    private String clientId;
    private String clientSecret;

    @JsonIgnore
    private RestConnector restConnector;

    public String getLocation() {
        return location;
    }

    public SpaceConfiguration setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public SpaceConfiguration setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public SpaceConfiguration setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getId() {
        return id;
    }

    public SpaceConfiguration setId(String id) {
        this.id = id;
        return this;
    }

    public LocationParts getLocationParts() {
        return locationParts;
    }

    public SpaceConfiguration setLocationParts(LocationParts locationParts) {
        this.locationParts = locationParts;
        return this;
    }

    public String getName() {
        return name;
    }

    public SpaceConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    @JsonIgnore
    public RestConnector getRestConnector() {
        if (restConnector == null) {
            restConnector = OctaneRestService.getRestConnector(getLocationParts().getBaseUrl(), getClientId(), getClientSecret());
        }
        return restConnector;
    }
}
