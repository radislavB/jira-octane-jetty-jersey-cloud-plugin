package com.microfocus.octane.plugins.rest;


import com.microfocus.octane.plugins.utils.JwtUtils;

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

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIt() {
        JwtUtils.validateToken(JwtUtils.extractTokenFromUri(uriInfo));
        MultivaluedMap<String, String> map = uriInfo.getQueryParameters();


        String str = "<!DOCTYPE html> <html lang=\"en\"> <head> <link rel=\"stylesheet\" href=\"https://unpkg.com/@atlaskit/css-reset@2.0.0/dist/bundle.css\" media=\"all\">" +
                "<script src=\"https://connect-cdn.atl-paas.net/all.js\" async></script> </head> <body> <section id=\"content\" class=\"ac-content\">" +
                "<div>Hello World FROM MF from Radi to Daniel2</div> </section> </body> </html>";
        return str;
    }

}
