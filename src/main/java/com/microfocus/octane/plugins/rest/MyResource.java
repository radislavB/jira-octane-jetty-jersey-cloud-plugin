package com.microfocus.octane.plugins.rest;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.microfocus.octane.plugins.rest.pojo.JiraTenantSecurityContext;
import com.microfocus.octane.plugins.utils.JwtUtils;
import com.microfocus.octane.plugins.utils.SecurityContextManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("resource")
public class MyResource {

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpheaders;

    @Context
    private Request request;

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
    public String getIt() {

        DecodedJWT decodedJWT = JwtUtils.validateToken(JwtUtils.extractTokenFromUri(uriInfo));
        MultivaluedMap<String, String> map = uriInfo.getQueryParameters();
        String canonicalMethod = request.getMethod().toUpperCase();
        JiraTenantSecurityContext securityContext = SecurityContextManager.getInstance().getSecurityContext(decodedJWT.getIssuer());
        String baseUrl = securityContext.getBaseUrl();
        String fullUrl = uriInfo.getRequestUri().toString();



        String str = "<!DOCTYPE html> <html lang=\"en\"> <head> <link rel=\"stylesheet\" href=\"https://unpkg.com/@atlaskit/css-reset@2.0.0/dist/bundle.css\" media=\"all\">" +
                "<script src=\"https://connect-cdn.atl-paas.net/all.js\" async></script> </head> <body> <section id=\"content\" class=\"ac-content\">" +
                "<div>Hello World FROM MF from Radi to Daniel2</div> </section> </body> </html>";
        return str;
    }

}
