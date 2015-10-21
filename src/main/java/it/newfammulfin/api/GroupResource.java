/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.googlecode.objectify.Key;
import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.util.List;
import java.util.logging.Logger;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author eric
 */
@Path("groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {

  //extract user from the securitycontext; see http://www.nextinstruction.com/custom-jersey-security-filter.html
  // and https://jersey.java.net/documentation/latest/security.html#d0e12179
  // and http://porterhead.blogspot.it/2013/01/writing-rest-services-in-java-part-6.html
  @Context
  private SecurityContext securityContext;
  private static final Logger LOG = Logger.getLogger(GroupResource.class.getName());

  @GET
  public Response listAll() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    List<Group> groups = OfyService.ofy().load().type(Group.class).filter("masterUserKey", userKey).list();
    return Response.ok(groups).build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") @NotNull Long id) {
    Group group = OfyService.ofy().load().type(Group.class).id(id).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((group==null)||!group.getMasterUserKey().equals(userKey)) {
      return Response.ok().build();
    }
    return Response.ok(group).build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") @NotNull Long id) {
    Group group = OfyService.ofy().load().type(Group.class).id(id).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((group==null)||!group.getMasterUserKey().equals(userKey)) {
      LOG.warning(String.format("User %s attempted to delete group %s.", securityContext.getUserPrincipal().getName(), group));
      return Response.status(Response.Status.FORBIDDEN).entity("Cannot delete groups whose master is not you.").type(MediaType.TEXT_PLAIN).build();      
    }
    //check for no entries or remove entries
    if (true) {
      throw new UnsupportedOperationException("To be implemented");
    }
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Valid @NotNull Group group) {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    group.setMasterUserKey(userKey);
    group.getUsersMap().clear();
    group.getUsersMap().put(userKey, getDefaultNickname(userKey));
    OfyService.ofy().save().entity(group).now();
    return Response.ok(group).build();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@PathParam("id") @NotNull Long id, @Valid @NotNull Group group) {
    if (!id.equals(group.getId())) {
      LOG.warning(String.format("User %s attempted to modify group %s.", securityContext.getUserPrincipal().getName(), group));
      return Response.status(Response.Status.FORBIDDEN).entity("Cannot modify groups whose master is not you.").type(MediaType.TEXT_PLAIN).build();      
    }
    Group existingGroup = OfyService.ofy().load().type(Group.class).id(group.getId()).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((existingGroup==null)||!existingGroup.getMasterUserKey().equals(userKey)) {
      LOG.warning(String.format("User %s attempted to modify group %s.", securityContext.getUserPrincipal().getName(), group));
      return Response.status(Response.Status.FORBIDDEN).entity("Cannot modify groups whose master is not you.").type(MediaType.TEXT_PLAIN).build();
    }
    group.setMasterUserKey(userKey);
    if (!group.getUsersMap().containsKey(userKey)) {
      group.getUsersMap().put(userKey, getDefaultNickname(userKey));
    }
    OfyService.ofy().save().entity(group).now();
    return Response.ok(group).build();
  }

  private String getDefaultNickname(Key<RegisteredUser> userKey) {
    RegisteredUser registeredUser = OfyService.ofy().load().key(userKey).now();
    String nickname = registeredUser.getFirstName();
    if ((nickname == null) || (nickname.isEmpty())) {
      nickname = registeredUser.getName().replaceFirst("@.*", "");
    }
    return nickname;
  }

}
