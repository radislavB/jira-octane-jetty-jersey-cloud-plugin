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

package com.microfocus.octane.plugins.octane.rest;

import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.octane.descriptors.OctaneEntityTypeDescriptor;
import com.microfocus.octane.plugins.octane.descriptors.OctaneEntityTypeManager;
import com.microfocus.octane.plugins.octane.rest.entities.OctaneEntity;
import com.microfocus.octane.plugins.octane.rest.entities.OctaneEntityCollection;
import com.microfocus.octane.plugins.octane.rest.entities.groups.GroupEntityCollection;
import com.microfocus.octane.plugins.octane.rest.query.*;
import com.microfocus.octane.plugins.utils.ConfigurarionUtil;
import com.microfocus.octane.plugins.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class OctaneRestService {

    private static final Logger log = LoggerFactory.getLogger(OctaneRestService.class);
    public static int SPACE_CONTEXT = -1;

    private RestConnector getRestConnector(SpaceConfiguration spaceConfiguration) {
        return ConfigurarionUtil.prepareConnection(spaceConfiguration);
    }

    public GroupEntityCollection getCoverage(SpaceConfiguration spaceConfiguration, OctaneEntity octaneEntity, OctaneEntityTypeDescriptor typeDescriptor, long workspaceId) {
        //http://localhost:8080/api/shared_spaces/1001/workspaces/1002/runs/groups?query="test_of_last_run={product_areas={(id IN '2001')}}"&group_by=status

        String url = String.format(UrlConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, spaceConfiguration.getLocationParts().getSpaceId(), workspaceId, "runs/groups");
        Map<String, String> headers = createHeaderMapWithOctaneClientType();
        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);

        OctaneQueryBuilder queryBuilder = OctaneQueryBuilder.create()
                .addGroupBy("status")
                .addQueryCondition(new CrossQueryPhrase("test_of_last_run", new CrossQueryPhrase(typeDescriptor.getTestReferenceField(), createGetEntityCondition(octaneEntity))))
                .addQueryCondition(new RawTextQueryPhrase("!test_of_last_run={null}"));

        String queryParam = queryBuilder.build();

        String responseStr = getRestConnector(spaceConfiguration).httpGet(url, Arrays.asList(queryParam), headers).getResponseData();
        GroupEntityCollection col = JsonUtils.parse(responseStr, GroupEntityCollection.class);
        return col;
    }


    public GroupEntityCollection getNativeStatusCoverageForRunsWithoutStatus(SpaceConfiguration spaceConfiguration, OctaneEntity octaneEntity, OctaneEntityTypeDescriptor typeDescriptor, long workspaceId) {
        //https://localhost:8080/api/shared_spaces/1001/workspaces/1002/runs/groups?&query=%22test_of_last_run={covered_requirement={path=%270000000001OT0002K9*%27}};!test_of_last_run={null};status={null}%22&group_by=native_status

        String url = String.format(UrlConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, spaceConfiguration.getLocationParts().getSpaceId(), workspaceId, "runs/groups");
        Map<String, String> headers = createHeaderMapWithOctaneClientType();
        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);

        OctaneQueryBuilder queryBuilder = OctaneQueryBuilder.create()
                .addGroupBy("native_status")
                .addQueryCondition(new CrossQueryPhrase("test_of_last_run", new CrossQueryPhrase(typeDescriptor.getTestReferenceField(), createGetEntityCondition(octaneEntity))))
                .addQueryCondition(new RawTextQueryPhrase("!test_of_last_run={null}"))
                .addQueryCondition(new RawTextQueryPhrase("status={null}"));

        String queryParam = queryBuilder.build();

        String responseStr = getRestConnector(spaceConfiguration).httpGet(url, Arrays.asList(queryParam), headers).getResponseData();
        GroupEntityCollection col = JsonUtils.parse(responseStr, GroupEntityCollection.class);
        return col;
    }

    public int getTotalTestsCount(SpaceConfiguration spaceConfiguration, OctaneEntity octaneEntity, OctaneEntityTypeDescriptor typeDescriptor, long workspaceId) {
        //http://localhost:8080/api/shared_spaces/1001/workspaces/1002/tests?fields=id&limit=1&query="((covered_content={(path='0000000000XC*')});((!(subtype='test_suite'))))"

        String url = String.format(UrlConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, spaceConfiguration.getLocationParts().getSpaceId(), workspaceId, "tests");
        Map<String, String> headers = createHeaderMapWithOctaneClientType();
        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);

        String queryParam = OctaneQueryBuilder.create()
                .addQueryCondition(new CrossQueryPhrase(typeDescriptor.getTestReferenceField(), createGetEntityCondition(octaneEntity)))
                //.addQueryCondition(new NegativeQueryPhrase(new LogicalQueryPhrase("subtype", "test_suite")))
                .addPageSize(1)
                .addSelectedFields("id")
                .build();

        String responseStr = getRestConnector(spaceConfiguration).httpGet(url, Arrays.asList(queryParam), headers).getResponseData();
        OctaneEntityCollection col = JsonUtils.parse(responseStr, OctaneEntityCollection.class);
        return col.getTotalCount();
    }

    private QueryPhrase createGetEntityCondition(OctaneEntity octaneEntity) {
        String path = octaneEntity.getString("path");
        return new LogicalQueryPhrase("path", path + "*");
    }

    public OctaneEntityCollection getEntitiesByCondition(SpaceConfiguration spaceConfiguration, long workspaceId, String collectionName, Collection<QueryPhrase> conditions, Collection<String> fields) {

        String queryCondition = OctaneQueryBuilder.create().addQueryConditions(conditions).addSelectedFields(fields).build();
        String url;
        if (SPACE_CONTEXT == workspaceId) {
            url = String.format(UrlConstants.PUBLIC_API_SHAREDSPACE_LEVEL_ENTITIES,
                    spaceConfiguration.getLocationParts().getSpaceId(), collectionName);
        } else {
            url = String.format(UrlConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES,
                    spaceConfiguration.getLocationParts().getSpaceId(), workspaceId, collectionName);
        }

        Map<String, String> headers = createHeaderMapWithOctaneClientType();
        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);

        String responseStr = getRestConnector(spaceConfiguration).httpGet(url, Arrays.asList(queryCondition), headers).getResponseData();
        OctaneEntityCollection col = JsonUtils.parse(responseStr, OctaneEntityCollection.class);
        return col;
    }

    public List<String> getSupportedOctaneTypes(SpaceConfiguration spaceConfiguration, long workspaceId, String udfName) {
        long spaceId = spaceConfiguration.getLocationParts().getSpaceId();
        String entityCollectionUrl = String.format(UrlConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, spaceId, workspaceId, "metadata/fields");
        Map<String, String> headers = createHeaderMapWithOctaneClientType();
        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);

        QueryPhrase fieldNameCondition = new LogicalQueryPhrase("name", udfName);

        QueryPhrase typeCondition = new InQueryPhrase("entity_name", OctaneEntityTypeManager.getSupportedTypes());
        String queryCondition = OctaneQueryBuilder.create().addQueryCondition(fieldNameCondition).addQueryCondition(typeCondition).build();
        String entitiesCollectionStr = getRestConnector(spaceConfiguration).httpGet(entityCollectionUrl, Arrays.asList(queryCondition), headers).getResponseData();
        OctaneEntityCollection fields = JsonUtils.parse(entitiesCollectionStr, OctaneEntityCollection.class);
        List<String> foundTypes = fields.getData().stream().map(e -> e.getString("entity_name")).collect(Collectors.toList());

        return foundTypes;
    }

    private Map<String, String> createHeaderMapWithOctaneClientType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("HPECLIENTTYPE", "HPE_CI_CLIENT");
        return headers;
    }

    public Set<String> getPossibleJiraFields(SpaceConfiguration spaceConfiguration, long workspaceId) {
        //https://mqalb011sngx.saas.hpe.com/api/shared_spaces/3004/workspaces/2002/metadata/fields?&query=%22field_type=%27string%27;is_user_defined=true;(entity_name+IN+%27feature%27,%27application_module%27,%27requirement_document%27,%27story%27)%22
        long spaceId = spaceConfiguration.getLocationParts().getSpaceId();
        String entityCollectionUrl = String.format(UrlConstants.PUBLIC_API_WORKSPACE_LEVEL_ENTITIES, spaceId, workspaceId, "metadata/fields");
        Map<String, String> headers = createHeaderMapWithOctaneClientType();
        headers.put(RestConnector.HEADER_ACCEPT, RestConnector.HEADER_APPLICATION_JSON);

        String queryCondition = OctaneQueryBuilder.create()
                .addQueryCondition(new LogicalQueryPhrase("field_type", "string"))
                .addQueryCondition(new LogicalQueryPhrase("is_user_defined", true))
                .addQueryCondition(new InQueryPhrase("entity_name", OctaneEntityTypeManager.getSupportedTypes()))
                .build();

        String collectionStr = getRestConnector(spaceConfiguration).httpGet(entityCollectionUrl, Arrays.asList(queryCondition), headers).getResponseData();
        OctaneEntityCollection fields = JsonUtils.parse(collectionStr, OctaneEntityCollection.class);
        Set<String> foundJiraNames = fields.getData().stream().map(e -> e.getString("name")).filter(n -> n.toLowerCase().contains("jira")).collect(Collectors.toSet());

        return foundJiraNames;
    }

}