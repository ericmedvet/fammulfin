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
import it.newfammulfin.model.Entry;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.math.BigDecimal;
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
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.joda.money.Money;
import org.joda.time.LocalDate;

/**
 *
 * @author eric
 */
@Path("groups/{groupId}/entries")
@Produces(MediaType.APPLICATION_JSON)
@RetrieveGroup
public class EntryResource {

  @Context
  private SecurityContext securityContext;
  @Context
  private ContainerRequestContext requestContext;
  private static final Logger LOG = Logger.getLogger(EntryResource.class.getName());

  @GET
  public Response readAll(@PathParam("groupId") @NotNull Long groupId) {
    Group group = (Group)requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    List<Entry> entries = OfyService.ofy().load().type(Entry.class).ancestor(group).list();
    return Response.ok(entries).build();
  }

  @GET
  @Path("template")
  public Response template(@PathParam("groupId") @NotNull Long groupId) {
    Group group = (Group)requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    Entry entry = new Entry();
    entry.setAmount(Money.of(group.getDefaultCurrencyUnit(), 2.20));
    entry.setDate(LocalDate.now());
    entry.setPayee("Pub");
    entry.setDescription("Beer");
    entry.getByShares().put(userKey, BigDecimal.ONE);
    for (Key<RegisteredUser> groupUserKey : group.getUsersMap().keySet()) {
      entry.getForShares().put(groupUserKey, BigDecimal.ONE.divide(BigDecimal.valueOf(group.getUsersMap().size())));
    }
    return Response.ok(entry).build();
  }

  @GET
  @Path("{id}")
  public Response read(@PathParam("id") @NotNull Long id) {
    return null;
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") @NotNull Long id) {
    return null;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Valid @NotNull Group group) {
    return null;
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@PathParam("id") @NotNull Long id, @Valid @NotNull Group group) {
    return null;
  }

}
