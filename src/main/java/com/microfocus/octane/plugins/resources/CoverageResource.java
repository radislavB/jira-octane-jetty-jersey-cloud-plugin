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

    private String coverageEmptyHtml;
    private String coverageExistHtml;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getCoverage(@QueryParam("project_id") String projectId,
                              @QueryParam("issuetype_id") String issueTypeId,
                              @QueryParam("issue_key") String issueKey,
                              @QueryParam("issue_id") String issueId) throws IOException {

        ClientConfiguration config = ConfigurationManager.getInstance().getClientConfiguration(getTenantId());
        Optional<WorkspaceConfiguration> optWc = config.getSupportedWorkspaceConfiguration(projectId);

        String emptyCoverageMessage = null;
        Map<String, Object> contextMap = null;
        boolean coverageExist = false;
        if (!optWc.isPresent()) {
            emptyCoverageMessage = "This project is not supporting ALM Octane Coverage.";
        } else if (!optWc.get().isIssueTypeIdSupported(issueTypeId)) {
            emptyCoverageMessage = "This issue type is not supporting ALM Octane Coverage.";
        } else {
            SpaceConfiguration sc = config.getSpaceConfigurationById(optWc.get().getSpaceConfigurationId());
            contextMap = CoverageUiHelper.buildCoverageContextMap(sc, optWc.get(), projectId, issueKey, issueId);
            String status = (String) contextMap.get("status");
            if (CoverageUiHelper.COVERAGE_STATUS_NO_DATA.equals(status)) {
                emptyCoverageMessage = "No corresponding entity is mapped in ALM Octane.";
            } else {
                coverageExist = true;

            }
        }

        return coverageExist ? wrapCoverage(contextMap) : wrapEmptyCoverage(emptyCoverageMessage);
    }

    private String wrapEmptyCoverage(String body) throws IOException {
        if (coverageEmptyHtml == null) {
            String filename = "/static/coverageEmpty.html";
            coverageEmptyHtml = ResourceUtils.readFile(context, filename);
        }

        String result = coverageEmptyHtml.replace("{body}", body);
        return result;
    }

    private String wrapCoverage(Map<String, Object> contextMap) throws IOException {
        if (coverageExistHtml == null) {
            String filename = "/static/coverage.html";
            coverageExistHtml = ResourceUtils.readFile(context, filename);
        }

        String json = JsonUtils.toJson(contextMap);
        String result = coverageExistHtml.replace(";//{data}", "='" + json + "'");
        return result;
    }

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

}
