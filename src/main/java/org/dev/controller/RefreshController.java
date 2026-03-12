package org.dev.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.dto.TokenDTO;
import org.dev.util.JWTUtil;

@Path("/refresh")
public class RefreshController {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reNewAccessToke(TokenDTO dto){
        try{
            String email = JWTUtil.validateToken(dto.getRequestToken()); /// subject
            String accessToken = JWTUtil.generateToken(email);
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(accessToken);
            tokenDTO.setRequestToken(dto.getRequestToken());
            return Response.ok().entity(tokenDTO).build();
        }catch (Exception e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

}
