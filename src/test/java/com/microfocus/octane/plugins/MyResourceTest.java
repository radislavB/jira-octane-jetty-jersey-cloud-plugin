/*package com.microfocus.octane.plugins;

import javax.ws.rs.core.Application;

import com.microfocus.octane.plugins.resources.MyResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class MyResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }


    @Test
    public void testGetIt() {
        final String responseMsg = target().path("resources/resource").request().get(String.class);

        assertEquals("Hello, Heroku!", responseMsg);
    }
}*/
