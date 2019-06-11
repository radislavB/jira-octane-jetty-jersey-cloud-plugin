package com.microfocus.octane.plugins.rest;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.microfocus.octane.plugins.rest.pojo.JiraTenantSecurityContext;
import com.microfocus.octane.plugins.utils.PluginConstants;
import com.microfocus.octane.plugins.utils.ResourceUtils;
import com.microfocus.octane.plugins.utils.SecurityContextManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.IOException;


@Path("configuration")
public class ConfigurationResource {

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpheaders;

    @Context
    private Request request;

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

    @Path("admin")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAdminPage() throws IOException {

        String filename = "/WEB-INF/rightPanelTemplate.html";
        String content = ResourceUtils.readFile(context, filename);
        String result = content.replace("{body}","<div>Hello World FROM MF from Admin</div>");

        return result;
    }

    @Path("conf")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getCongPage() throws IOException {

        String filename = "/WEB-INF/rightPanelTemplate.html";
        String content = ResourceUtils.readFile(context, filename);
        String result = content.replace("{body}","<div>Hello World FROM MF from Conf</div>");

        return result;
    }

}
