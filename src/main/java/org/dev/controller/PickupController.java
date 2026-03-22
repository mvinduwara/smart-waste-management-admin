package org.dev.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.dto.PickupRequestDTO;
import org.dev.service.PickupService;
import retrofit2.Call;

import java.util.List;

@Path("/pickup")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PickupController {

    private final PickupService pickupService = new PickupService();

    @POST
    @Path("/create")
    public Response createPickupRequest(PickupRequestDTO dto) {
        return pickupService.createPickupRequest(dto);
    }

    @GET
    @Path("/user/{userId}")
    public Response getUserRequests(@PathParam("userId") int userId) {
        return pickupService.getRequestsByUser(userId);
    }
}
