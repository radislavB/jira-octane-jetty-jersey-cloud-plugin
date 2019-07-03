package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.pojo.ClientConfiguration;
import com.microfocus.octane.plugins.managers.pojo.WorkspaceConfiguration;
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

    private String frameTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getCoverage(@QueryParam("project_id") String projectId,
                              @QueryParam("issuetype_id") String issueTypeId,
                              @QueryParam("issue_key") String issueKey,
                              @QueryParam("issue_id") String issueId) throws IOException {

        ClientConfiguration config = ConfigurationManager.getInstance().getClientConfiguration(getTenantId());
        Optional<WorkspaceConfiguration> optWc = config.getSupportedWorkspaceConfiguration(projectId, issueTypeId);
        String body = null;
        if (optWc.isPresent()) {

        } else {
            body = "Project/Issue Type does not support ALM Octane Coverage";
        }

        return wrapElement("<div>Hello World FROM MF from Radi to Daniel3</div>");
    }

    private String wrapElement(String body) throws IOException {
        if (frameTemplate == null) {
            String filename = "/static/frameTemplate.html";
            frameTemplate = ResourceUtils.readFile(context, filename);
        }

        String result = frameTemplate.replace("{body}", body);
        return result;
    }

    private String getTenantId() {
        return (String) httpRequest.getAttribute(PluginConstants.TENANT_ID);
    }

}
