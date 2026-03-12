package org.dev.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.dev.util.JWTUtil;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        String path = containerRequestContext.getUriInfo().getPath();
        if(path.startsWith("auth")){
            return;
        }

        String authHeader = containerRequestContext.getHeaderString(AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        String token = authHeader.substring("Bearer ".length());
        try{
            JWTUtil.validateToken(token);
        } catch (Exception e) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
