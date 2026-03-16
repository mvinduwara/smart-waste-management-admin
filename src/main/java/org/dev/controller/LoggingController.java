package org.dev.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.service.UserService;

@Path("/auth/login")
public class LoggingController {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)  /// front end -> json data
    @Produces(MediaType.APPLICATION_JSON)  /// back end -> json data
    public Response login(LoggingRequestDTO loggingRequestDTO) {
        return new UserService().validateUser(loggingRequestDTO);
    }
}