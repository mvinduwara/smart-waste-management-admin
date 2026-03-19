package org.dev.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.dev.entity.Driver;
import org.dev.service.DriverService;

@Path("/driver")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DriverController {

    private final DriverService driverService = new DriverService();

    // API Endpoint: PUT http://localhost:8080/api/driver/profile/{id}
    @PUT
    @Path("/profile/{id}")
    public Response updateProfile(@PathParam("id") int driverId, Driver updatedData) {
        return driverService.updateProfile(driverId, updatedData);
    }
}