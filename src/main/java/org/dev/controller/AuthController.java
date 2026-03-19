package org.dev.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.dev.dto.LoggingRequestDTO;
import org.dev.entity.Driver;
import org.dev.entity.User;
import org.dev.service.UserService;
import org.dev.service.DriverService;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    // Instantiate the service we just built
    private final UserService userService = new UserService();
    private final DriverService driverService = new DriverService();

    // API Endpoint: POST http://localhost:8080/api/auth/register
    @POST
    @Path("/register")
    public Response register(User newUser) {
        // Passes the incoming JSON (mapped to User object) to the service
        return userService.registerUser(newUser);
    }

    // API Endpoint: POST http://localhost:8080/api/auth/login
    @POST
    @Path("/login")
    public Response login(LoggingRequestDTO loginRequest) {
        // Passes the incoming username/password to the service
        return userService.validateUser(loginRequest);
    }

    @POST
    @Path("/driver/register")
    public Response registerDriver(Driver newDriver) {
        return driverService.registerDriver(newDriver);
    }

    @POST
    @Path("/driver/login")
    public Response loginDriver(LoggingRequestDTO loginRequest) {
        return driverService.validateDriver(loginRequest);
    }
}