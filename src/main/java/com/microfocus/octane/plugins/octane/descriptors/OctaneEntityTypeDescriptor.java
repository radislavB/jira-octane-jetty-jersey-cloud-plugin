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

package com.microfocus.octane.plugins.octane.descriptors;

public class OctaneEntityTypeDescriptor {

    /***
     * Type name
     */
    private String typeName;

    /***
     * Alias
     */
    private String alias;

    /**
     * Short type key , for example US, F, etc
     */
    private String typeAbbreviation;

    /**
     * User friendly label
     */
    private String label;

    /**
     * color used to color typeAbbreviation in coverage widget
     */
    private String typeColor;

    /**
     * used to build url to navigate to entity
     */
    private String nameForNavigation;

    /**
     * name of test tab, used to build url of tests for specific entity
     */
    private String testTabName;

    /**
     * Reference field of test to specific entity, used to build coverage query
     */
    private String testReferenceField;


    public OctaneEntityTypeDescriptor(String typeName, String alias, String typeAbbreviation, String label, String typeColor, String nameForNavigation, String testTabName, String testReferenceField) {
        this.typeName = typeName;
        this.alias = alias;
        this.label = label;
        this.typeAbbreviation = typeAbbreviation;
        this.typeColor = typeColor;
        this.nameForNavigation = nameForNavigation;
        this.testTabName = testTabName;
        this.testReferenceField = testReferenceField;
    }

    public String getTypeAbbreviation() {
        return typeAbbreviation;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeColor() {
        return typeColor;
    }

    public String getNameForNavigation() {
        return nameForNavigation;
    }

    public String getTestTabName() {
        return testTabName;
    }

    public String buildEntityUrl(String baseUrl, long spaceId, long workspaceId, String entityId) {
        String octaneEntityUrl = String.format("%s/ui/?p=%s/%s#/entity-navigation?entityType=%s&id=%s",
                baseUrl, spaceId, workspaceId,
                this.getNameForNavigation(), entityId);
        return octaneEntityUrl;
    }

    public String buildTestTabEntityUrl(String baseUrl, long spaceId, long workspaceId, String entityId) {
        return buildEntityUrl(baseUrl, spaceId, workspaceId, entityId) + "&tabName=" + getTestTabName();
    }

    public String getTestReferenceField() {
        return testReferenceField;
    }

    public String getLabel() {
        return label;
    }


    public String getAlias() {
        return alias;
    }
}
