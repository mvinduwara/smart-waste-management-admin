package org.dev.controller;

import jakarta.ws.rs.*;
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
    public Response updateProfile(@PathParam("id") int id, Driver data) {
        return driverService.updateProfile(id, data);
    }

    @GET
    @Path("/profile/{id}")
    public Response getProfile(@PathParam("id") int id) {
        return driverService.getDriverProfile(id);
    }

    @PUT
    @Path("/{id}/fcm-token")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateFcmToken(@PathParam("id") int driverId, String fcmToken) {
        return driverService.updateFcmToken(driverId, fcmToken);
    }

    @GET
    @Path("/{id}/earnings")
    public Response getEarnings(@PathParam("id") int id) {
        return driverService.getDriverEarningsSummary(id);
    }
}