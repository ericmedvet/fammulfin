/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.model.Entry;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author eric
 */
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {

  @GET
  public Response listAll() {
    //extract user from the securitycontext; see http://www.nextinstruction.com/custom-jersey-security-filter.html
    // and https://jersey.java.net/documentation/latest/security.html#d0e12179
    // and http://porterhead.blogspot.it/2013/01/writing-rest-services-in-java-part-6.html
    List<Entry> entries = OfyService.ofy().load().type(Entry.class).list();
    return Response.ok(entries).build();
  }
}
