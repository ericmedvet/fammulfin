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
import it.newfammulfin.model.Chapter;
import it.newfammulfin.model.Entry;
import it.newfammulfin.model.Group;
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

/**
 *
 * @author eric
 */
@Path("groups/{groupId}/chapters")
@Produces(MediaType.APPLICATION_JSON)
@RetrieveGroup
public class ChapterResource {

  @Context
  private SecurityContext securityContext;
  @Context
  private ContainerRequestContext requestContext;
  private static final Logger LOG = Logger.getLogger(ChapterResource.class.getName());

  @GET
  public Response readAll(@PathParam("groupId") @NotNull Long groupId) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    List<Chapter> chapters = OfyService.ofy().load().type(Chapter.class).ancestor(group).list();
    return Response.ok(chapters).build();
  }

  @GET
  @Path("{id:[0-9]+}")
  public Response read(@PathParam("id") @NotNull Long id) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Chapter chapter = OfyService.ofy().load().type(Chapter.class).parent(group).id(id).now();
    if ((chapter == null) || (!chapter.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Chapter with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    return Response.ok(chapter).build();
  }

  @DELETE
  @Path("{id:[0-9]+}")
  public Response delete(@PathParam("id") @NotNull Long id) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Chapter chapter = OfyService.ofy().load().type(Chapter.class).parent(group).id(id).now();
    if ((chapter == null) || (!chapter.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Chapter with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    int numberOfEntries = OfyService.ofy().load().type(Entry.class).ancestor(group).filter("chapterKey", Key.create(chapter)).count();
    if (numberOfEntries>0) {
      return Response
              .status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(String.format("Chapter has %d entries: cannot delete.", numberOfEntries))
              .type(MediaType.TEXT_PLAIN)
              .build();      
    }
    OfyService.ofy().delete().entity(chapter).now();
    LOG.info(String.format("%s deleted.", chapter));
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(final @Valid @NotNull Chapter chapter) {
    final Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    chapter.setGroupKey(Key.create(group));
    OfyService.ofy().save().entity(chapter).now();
    LOG.info(String.format("%s created.", chapter));
    return Response.ok(chapter).build();
  }

  @PUT
  @Path("{id:[0-9]+}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@PathParam("id") @NotNull Long id, @Valid @NotNull Chapter chapter) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Chapter existingChapter = OfyService.ofy().load().type(Chapter.class).parent(group).id(id).now();
    if ((existingChapter == null) || (!existingChapter.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Chapter with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    if (!existingChapter.getId().equals(id)||!existingChapter.getId().equals(chapter.getId())) {
      LOG.warning(String.format("User %s attempted to change chapter id: %d in path, %d in payload.",
              securityContext.getUserPrincipal().getName(),
              existingChapter.getId(),
              chapter.getId()));
      return Response
              .status(Response.Status.CONFLICT)
              .entity("Cannot change chapter id.")
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    chapter.setGroupKey(Key.create(group));
    OfyService.ofy().save().entity(chapter).now();
    LOG.info(String.format("%s updated.", chapter));
    return Response.ok(chapter).build();
  }

}
