package org.dev.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.service.AdminService;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminController {

    private final AdminService adminService = new AdminService();

    @POST
    @Path("/login")
    public Response login(LoggingRequestDTO loginRequest) {
        return adminService.validateAdmin(loginRequest);
    }

    @jakarta.ws.rs.GET
    @Path("/requests/{status}")
    public Response getRequests(@jakarta.ws.rs.PathParam("status") String status) {
        return adminService.getRequestsByStatus(status.toUpperCase());
    }

    @GET
    @Path("/users")
    public Response getAllUsers() {
        return adminService.getAllUsers();
    }

    @GET
    @Path("/drivers")
    public Response getAllDrivers() {
        return adminService.getAllDrivers();
    }

    @GET
    @Path("/user/{id}")
    public Response getUserProfile(@PathParam("id") int id) {
        return adminService.getUserById(id);
    }

    @GET
    @Path("/driver/{id}")
    public Response getDriverProfile(@PathParam("id") int id) {
        return adminService.getDriverById(id);
    }
}