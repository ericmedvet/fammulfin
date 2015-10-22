/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.googlecode.objectify.Key;
import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.api.util.validation.Shares;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author eric
 */
@Path("groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {

  @Context
  private SecurityContext securityContext;
  private static final Logger LOG = Logger.getLogger(GroupResource.class.getName());

  @GET
  public Response listAll() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    Set<Group> groups = new LinkedHashSet<>();
    groups.addAll(OfyService.ofy().load().type(Group.class).filter("masterUserKey", userKey).list());
    groups.addAll(OfyService.ofy().load().type(Group.class).filter("usersMap."+userKey.toWebSafeString()+" !=", null).list());
    return Response.ok(groups).build();
  }

  @GET
  @Path("{id}")
  public Response read(@PathParam("id") @NotNull Long id) {
    Group group = OfyService.ofy().load().type(Group.class).id(id).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((group==null)||(!group.getMasterUserKey().equals(userKey)&&!group.getUsersMap().containsKey(userKey))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Group with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    return Response.ok(group).build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") @NotNull Long id) {
    Group group = OfyService.ofy().load().type(Group.class).id(id).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((group==null)||!group.getMasterUserKey().equals(userKey)) {
      if (group!=null) {
        LOG.warning(String.format("User %s attempted to delete group %s.",
                securityContext.getUserPrincipal().getName(),
                group));
      }
      if (group.getUsersMap().containsKey(userKey)) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(String.format("Cannot delete group if you are not the master user.", id))
                .type(MediaType.TEXT_PLAIN)
                .build();        
      }
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Group with id %d does not exist.", id))
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
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@PathParam("id") @NotNull Long id, @Valid @NotNull Group group) {
    if (!id.equals(group.getId())) {
      LOG.warning(String.format("User %s attempted to change group id: %d in path, %d in payload.",
              securityContext.getUserPrincipal().getName(),
              id,
              group.getId()));
      return Response
              .status(Response.Status.CONFLICT)
              .entity("Cannot change group id.")
              .type(MediaType.TEXT_PLAIN)
              .build();      
    }
    Group existingGroup = OfyService.ofy().load().type(Group.class).id(group.getId()).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((existingGroup==null)||!existingGroup.getMasterUserKey().equals(userKey)) {
      if (existingGroup!=null) {
        LOG.warning(String.format("User %s attempted to modify group %s.",
                securityContext.getUserPrincipal().getName(),
                group));
      }
      if (existingGroup.getUsersMap().containsKey(userKey)) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(String.format("Cannot modify group if you are not the master user.", id))
                .type(MediaType.TEXT_PLAIN)
                .build();        
      }
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Group with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    group.setMasterUserKey(userKey);
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
