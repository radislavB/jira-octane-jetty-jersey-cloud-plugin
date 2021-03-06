package com.microfocus.octane.plugins;

import com.microfocus.octane.plugins.managers.ConfigurationManager;
import com.microfocus.octane.plugins.managers.SecurityContextManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application. The Java
 * command that is used for launching should fire this main method.
 */
public class Main {

    //need to set VM option :  -DrepositoryFolder=c:\Temp\5\
    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) throws Exception{
        log.info("main is started");
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "7071";
        }

        final Server server = new Server(Integer.parseInt(webPort));
        final WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);

        final String webappDirLocation = "src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        //root.setErrorHandler(new ErrorHandler());

        server.setHandler(root);

        initServices();

        System.out.println("Before Jetty start.");
        server.start();
        System.out.println("After Jetty start.");
        server.join();
    }

    private static void initServices(){

        String repositoryFolder = System.getProperty("repositoryFolder");
        SecurityContextManager.getInstance().init(repositoryFolder);
        ConfigurationManager.getInstance().init(repositoryFolder);
    }
}
