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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author eric
 */
@Path("user")
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
    return Response.ok(registeredUser).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@Valid RegisteredUser updatedRegisteredUser) {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    RegisteredUser registeredUser = OfyService.ofy().load().key(userKey).now();
    if (!securityContext.getUserPrincipal().getName().equals(updatedRegisteredUser.getName())) {
      LOG.warning(String.format("User %s attempted to modify user %s.", securityContext.getUserPrincipal().getName(), updatedRegisteredUser.getName()));
      return Response.status(Response.Status.FORBIDDEN).entity("Cannot modify other user than you.").type(MediaType.TEXT_PLAIN).build();
    }
    OfyService.ofy().save().entity(updatedRegisteredUser).now();
    LOG.info(String.format("%s updated.", updatedRegisteredUser));
    return Response.ok(updatedRegisteredUser).build();
  }

  @GET
  public Response get() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    RegisteredUser registeredUser = OfyService.ofy().load().key(userKey).now();
    return Response.ok(registeredUser).build();
  }

}
