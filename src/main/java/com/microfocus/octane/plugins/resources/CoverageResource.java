package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.SpaceConfiguration;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfiguration;
import com.microfocus.octane.plugins.utils.CoverageUiHelper;
import com.microfocus.octane.plugins.utils.JsonUtils;
import com.microfocus.octane.plugins.utils.PluginConstants;
import com.microfocus.octane.plugins.utils.ResourceUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("coverage")
public class CoverageResource {

    @Context
    private HttpServletRequest httpRequest;

    @Context
    private ServletContext context;

    private String coverageHtml;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getCoverage(@QueryParam("project_id") String projectId,
                              @QueryParam("issuetype_id") String issueTypeId,
                              @QueryParam("issue_key") String issueKey,
                              @QueryParam("issue_id") String issueId) throws IOException {

        ClientConfiguration config = ConfigurationManager.getInstance().getClientConfiguration(getTenantId());
        Optional<WorkspaceConfiguration> optWc = config.getSupportedWorkspaceConfiguration(projectId);

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put(CoverageUiHelper.COVERAGE_STATUS_FIELD, CoverageUiHelper.COVERAGE_STATUS_NO_DATA);
        if (!optWc.isPresent()) {
            contextMap.put(CoverageUiHelper.COVERAGE_STATUS_NO_DATA_MESSAGE, "This project is not supporting ALM Octane Coverage.");
        } else if (!optWc.get().isIssueTypeIdSupported(issueTypeId)) {
            contextMap.put(CoverageUiHelper.COVERAGE_STATUS_NO_DATA_MESSAGE, "This issue type is not supporting ALM Octane Coverage.");
        } else {
            SpaceConfiguration sc = config.getSpaceConfigurationById(optWc.get().getSpaceConfigurationId());
            contextMap = CoverageUiHelper.buildCoverageContextMap(sc, optWc.get(), projectId, issueKey, issueId);
            String status = (String) contextMap.get(CoverageUiHelper.COVERAGE_STATUS_FIELD);
            if (CoverageUiHelper.COVERAGE_STATUS_NO_DATA.equals(status)) {
                contextMap.put(CoverageUiHelper.COVERAGE_STATUS_NO_DATA_MESSAGE, "No corresponding entity is mapped in ALM Octane.");
            }
        }

        return wrapCoverage(contextMap);
    }

    private String wrapCoverage(Map<String, Object> contextMap) throws IOException {
        if (coverageHtml == null) {
            String filename = "/static/coverage.html";
            coverageHtml = ResourceUtils.readFile(context, filename);
        }

        String json = JsonUtils.toJson(contextMap);
        String result = coverageHtml.replace(";//{data}", "=" + json);
        return result;
    }

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

}
