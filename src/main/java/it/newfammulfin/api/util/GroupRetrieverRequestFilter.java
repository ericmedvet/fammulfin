/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.googlecode.objectify.Key;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author eric
 */
@Provider
@RetrieveGroup
public class GroupRetrieverRequestFilter implements ContainerRequestFilter {
  
  public static final String GROUP = "group";

  @Context
  private SecurityContext securityContext;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (requestContext.getUriInfo().getPathParameters().get("groupId") == null || requestContext.getUriInfo().getPathParameters().get("groupId").isEmpty()) {
      requestContext.abortWith(Response
              .status(Response.Status.BAD_REQUEST)
              .entity(String.format("Missing groupId."))
              .type(MediaType.TEXT_PLAIN)
              .build());
      return;
    }
    String groupIdString = requestContext.getUriInfo().getPathParameters().get("groupId").get(0);
    Long groupId;
    try {
      groupId = Long.parseLong(groupIdString);
    } catch (NumberFormatException ex) {
      requestContext.abortWith(Response
              .status(Response.Status.BAD_REQUEST)
              .entity(String.format("Malformed groupId."))
              .type(MediaType.TEXT_PLAIN)
              .build());
      return;
    }
    Group group = OfyService.ofy().load().type(Group.class).id(groupId).now();
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    if ((group == null) || (!group.getMasterUserKey().equals(userKey) && !group.getUsersMap().containsKey(userKey))) {
      requestContext.abortWith(Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Group with id %d does not exist.", groupId))
              .type(MediaType.TEXT_PLAIN)
              .build());
      return;
    }
    requestContext.setProperty(GROUP, group);
  }

}
