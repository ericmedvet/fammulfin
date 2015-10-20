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
import javax.ws.rs.GET;
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
@Path("groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {

  //extract user from the securitycontext; see http://www.nextinstruction.com/custom-jersey-security-filter.html
  // and https://jersey.java.net/documentation/latest/security.html#d0e12179
  // and http://porterhead.blogspot.it/2013/01/writing-rest-services-in-java-part-6.html
  @Context
  private SecurityContext securityContext;

  @GET
  public Response listAll() {
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    List<Group> groups = OfyService.ofy().load().type(Group.class).filterKey(userKey).list();
    return Response.ok(groups).build();
  }
}
