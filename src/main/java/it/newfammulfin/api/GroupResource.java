/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.googlecode.objectify.Key;
import it.newfammulfin.api.util.GroupRetrieverRequestFilter;
import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.api.util.RetrieveGroup;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.util.LinkedHashSet;
import java.util.Set;
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
import javax.ws.rs.container.ContainerRequestContext;
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

  @Context
  private SecurityContext securityContext;
  @Context
  private ContainerRequestContext requestContext;
  private static final Logger LOG = Logger.getLogger(GroupResource.class.getName());

  @GET
  public Response readAll() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    Set<Group> groups = new LinkedHashSet<>();
    groups.addAll(OfyService.ofy().load().type(Group.class).filter("masterUserKey", userKey).list());
    groups.addAll(OfyService.ofy().load().type(Group.class).filter("usersMap." + userKey.toWebSafeString() + " !=", null).list());
    return Response.ok(groups).build();
  }

  @GET
  @Path("{groupId}")
  @RetrieveGroup
  public Response read() {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    return Response.ok(group).build();
  }

  @DELETE
  @Path("{groupId}")
  @RetrieveGroup
  public Response delete() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    if (!group.getMasterUserKey().equals(userKey)) {
      return Response
              .status(Response.Status.FORBIDDEN)
              .entity(String.format("Cannot delete group if you are not the master user."))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    //check for no entries or remove entries
    if (true) {
      throw new UnsupportedOperationException("To be implemented");
    }
    LOG.info(String.format("%s deleted.", group));
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Valid @NotNull Group group) {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    group.setMasterUserKey(userKey);
    if (group.getUsersMap().containsKey(userKey)) {
      group.getUsersMap().put(userKey, getDefaultNickname(userKey));
    }
    OfyService.ofy().save().entity(group).now();
    LOG.info(String.format("%s created.", group));
    return Response.ok(group).build();
  }

  @PUT
  @Path("{groupId}")
  @RetrieveGroup
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@Valid @NotNull Group group) {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    Group existingGroup = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    if (!existingGroup.getMasterUserKey().equals(userKey)) {
      return Response
              .status(Response.Status.FORBIDDEN)
              .entity(String.format("Cannot modify group if you are not the master user."))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    if (!existingGroup.getId().equals(group.getId())) {
      LOG.warning(String.format("User %s attempted to change group id: %d in path, %d in payload.",
              securityContext.getUserPrincipal().getName(),
              existingGroup.getId(),
              group.getId()));
      return Response
              .status(Response.Status.CONFLICT)
              .entity("Cannot change group id.")
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    group.setMasterUserKey(existingGroup.getMasterUserKey());
    if (!group.getUsersMap().containsKey(userKey)) {
      group.getUsersMap().put(userKey, getDefaultNickname(userKey));
    }
    OfyService.ofy().save().entity(group).now();
    LOG.info(String.format("%s updated.", group));
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
