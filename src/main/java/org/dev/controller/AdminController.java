package org.dev.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
}