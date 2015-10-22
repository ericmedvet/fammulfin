/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.googlecode.objectify.Key;
import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.model.RegisteredUser;
import java.util.logging.Logger;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author eric
 */
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class RegisteredUserResource {

  @Context
  private SecurityContext securityContext;
  private static final Logger LOG = Logger.getLogger(RegisteredUserResource.class.getName());

  @POST
  public Response register() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    RegisteredUser registeredUser = OfyService.ofy().load().key(userKey).now();
    if (registeredUser == null) {
      registeredUser = new RegisteredUser(securityContext.getUserPrincipal().getName());
      OfyService.ofy().save().entity(registeredUser).now();
      LOG.info(String.format("New %s created.", registeredUser));
    } else {
      LOG.info(String.format("%s already exists: skipping creation.", registeredUser));
    }
    LOG.info(String.format("%s created.", registeredUser));
    return Response.ok(registeredUser).build();
  }

  @PUT
  @Path("me")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@Valid RegisteredUser registeredUser) {
    if (!securityContext.getUserPrincipal().getName().equals(registeredUser.getName())) {
      LOG.warning(String.format("User %s attempted to modify user %s.",
              securityContext.getUserPrincipal().getName(),
              registeredUser.getName()));
      return Response
              .status(Response.Status.FORBIDDEN)
              .entity("Cannot modify other user than you.")
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    OfyService.ofy().save().entity(registeredUser).now();
    LOG.info(String.format("%s updated.", registeredUser));
    return Response.ok(registeredUser).build();
  }

  @GET
  @Path("me")
  public Response get() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    RegisteredUser registeredUser = OfyService.ofy().load().key(userKey).now();
    return Response.ok(registeredUser).build();
  }
  
  @GET
  @Path("key")
  public Response searchKey(@QueryParam("q") @NotNull String id) {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, id);
    return Response.ok(userKey).build();
  }

}
