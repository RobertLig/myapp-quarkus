package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.BlockUserRequest;
import org.example.myapp.service.BlockUserService;

@Path("/block")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BlockUserController {

    @Inject
    BlockUserService blockUserService;

    @POST
    public Response block(BlockUserRequest req) {
        blockUserService.block(req);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{blockerId}/{blockedId}")
    public Response unblock(
            @PathParam("blockerId") Long blockerId,
            @PathParam("blockedId") Long blockedId) {

        blockUserService.unblock(blockerId, blockedId);
        return Response.ok().build();
    }
}
