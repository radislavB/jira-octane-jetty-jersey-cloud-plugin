package com.microfocus.octane.plugins.resources;


import com.microfocus.octane.plugins.utils.ResourceUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("coverage")
public class CoverageResource {

    @Context
    private ServletContext context;

    @Context
    private HttpServletRequest httpRequest;

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIt() throws IOException {

        Map<String, String[]> params = httpRequest.getParameterMap();

        String filename = "/WEB-INF/frameTemplate.html";
        String content = ResourceUtils.readFile(context, filename);
        String result = content.replace("{body}", "<div>Hello World FROM MF from Radi to Daniel3</div>");

        return result;
    }

}
