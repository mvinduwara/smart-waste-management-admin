package org.dev.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.dto.PickupRequestDTO;
import org.dev.service.PickupService;
import org.dev.service.DriverService;
import retrofit2.Call;

import java.util.List;

@Path("/pickup")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PickupController {

    private final PickupService pickupService = new PickupService();
    private DriverService driverService = new DriverService();

    @POST
    @Path("/create")
    public Response createPickupRequest(PickupRequestDTO dto) {
        return pickupService.createPickupRequest(dto);
    }

    @GET
    @Path("/active/{userId}")
    public Response getActiveUserRequest(@PathParam("userId") int userId) {
        return pickupService.getActiveRequestByUser(userId);
    }

    @PUT
    @Path("/cancel/{requestId}")
    public Response cancelRequest(@PathParam("requestId") int requestId) {
        return pickupService.cancelPickupRequest(requestId);
    }

    @GET
    @Path("/available")
    public Response getAvailableRequests() {
        return pickupService.getAvailableRequests();
    }

    @PUT
    @Path("/accept/{requestId}/{driverId}")
    public Response acceptJob(@PathParam("requestId") int requestId, @PathParam("driverId") int driverId) {
        return pickupService.acceptPickupRequest(requestId, driverId);
    }

    @GET
    @Path("/driver/active/{driverId}")
    public Response getActiveDriverRequest(@PathParam("driverId") int driverId) {
        return pickupService.getActiveDriverRequest(driverId);
    }

    @PUT
    @Path("/complete/{requestId}")
    public Response completeJob(@PathParam("requestId") int requestId) {
        return pickupService.completePickupRequest(requestId);
    }

    @GET
    @Path("/seller/{userId}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSellerHistory(@PathParam("userId") int userId) {
        return pickupService.getSellerHistorySummary(userId);
    }

    @GET
    @Path("/user/{userId}")
    public Response getUserRequests(@PathParam("userId") int userId) {
        return pickupService.getUserRequests(userId);
    }
}
