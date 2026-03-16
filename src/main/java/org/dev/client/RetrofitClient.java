package org.dev.client;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.dev.config.AppConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.apache.catalina.LifecycleException;

import java.io.File;

public class RetrofitClient {

    private static final String API_PATH = "/api/v1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(RetrofitClient.SERVER_PORT);
        tomcat.getConnector();

        Context context = tomcat.addWebapp("/", new File("src/main/WebApp").getAbsolutePath());
        Tomcat.addServlet(context, "API_Servlet", new ServletContainer(new AppConfig()));
        context.addServletMappingDecoded(API_PATH + "/*", "API_Servlet");
        try {
            tomcat.start();
            System.out.println("API URL: http://localhost:" + SERVER_PORT + API_PATH);
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }

    }
}