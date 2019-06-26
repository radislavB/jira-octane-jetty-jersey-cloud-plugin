package com.microfocus.octane.plugins.managers.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpaceConfigurationOutgoing {

    private String id;
    private String name;
    private String location;
    private String clientId;
    private String clientSecret;

    public String getLocation() {
        return location;
    }

    public SpaceConfigurationOutgoing setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public SpaceConfigurationOutgoing setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public SpaceConfigurationOutgoing setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getId() {
        return id;
    }

    public SpaceConfigurationOutgoing setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SpaceConfigurationOutgoing setName(String name) {
        this.name = name;
        return this;
    }
}
