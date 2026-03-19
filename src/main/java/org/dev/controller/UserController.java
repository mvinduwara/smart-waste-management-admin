package org.dev.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.dev.entity.User;
import org.dev.service.UserService;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    private final UserService userService = new UserService();

    // API Endpoint: PUT http://localhost:8080/api/user/profile/{id}
    @PUT
    @Path("/profile/{id}")
    public Response updateProfile(@PathParam("id") int userId, User updatedData) {
        return userService.updateProfile(userId, updatedData);
    }
}