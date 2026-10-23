package org.example.myapp.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.myapp.dto.BlockUserRequest;
import org.example.myapp.service.BlockUserService;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BlockUserController {

    @Inject
    BlockUserService blockUserService;

    @Inject
    SecurityIdentity identity;

    private Long getLoggedInUserId() {
        return Long.valueOf(identity.getPrincipal().getName());
    }

    @POST
    @Path("/block")
    @RolesAllowed({"USER", "ADMIN"})
    public Response block(@Valid BlockUserRequest req) {
        Long currentUserId = getLoggedInUserId();
        blockUserService.block(currentUserId, req);
        return Response.ok().build();
    }

    @DELETE
    @Path("/unblock")
    @RolesAllowed({"USER", "ADMIN"})
    public Response unblock(@Valid BlockUserRequest req) {
        Long currentUserId = getLoggedInUserId();
        blockUserService.unblock(currentUserId, req);
        return Response.ok().build();
    }
}
