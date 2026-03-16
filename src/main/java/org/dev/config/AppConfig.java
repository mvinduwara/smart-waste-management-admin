package org.dev.config;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {

   public AppConfig() {
       packages("org.dev.controller");
       packages("org.dev.middleware");  
    }

}
