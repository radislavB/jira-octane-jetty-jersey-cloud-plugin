package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.utils.ResourceUtils;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("coverage")
public class CoverageResource {

    @Context
    private ServletContext context;

    private String frameTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getCoverage(@QueryParam("project_id") String projectId,
                              @QueryParam("issuetype_id") String issueTypeId,
                              @QueryParam("issue_key") String issueKey,
                              @QueryParam("issue_id") String issueId) throws IOException {

        if (frameTemplate == null) {
            String filename = "/static/frameTemplate.html";
            frameTemplate = ResourceUtils.readFile(context, filename);
        }

        String result = frameTemplate.replace("{body}", "<div>Hello World FROM MF from Radi to Daniel3</div>");

        return result;
    }

}
