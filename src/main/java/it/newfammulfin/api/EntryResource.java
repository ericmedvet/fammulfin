/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import it.newfammulfin.api.util.GroupRetrieverRequestFilter;
import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.api.util.RetrieveGroup;
import it.newfammulfin.model.Chapter;
import it.newfammulfin.model.Entry;
import it.newfammulfin.model.EntryOperation;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    List<Entry> entries = OfyService.ofy().load().type(Entry.class).ancestor(group).list();
    return Response.ok(entries).build();
  }

  @GET
  @Path("template")
  public Response template(@PathParam("groupId") @NotNull Long groupId) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Key<RegisteredUser> userKey = Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName());
    Entry entry = new Entry();
    entry.setAmount(Money.of(group.getDefaultCurrencyUnit(), 2.20));
    entry.setDate(LocalDate.now());
    entry.setPayee("Pub");
    entry.setDescription("Beer");
    entry.getByShares().put(userKey, BigDecimal.ONE);
    for (Key<RegisteredUser> groupUserKey : group.getUsersMap().keySet()) {
      entry.getForShares().put(groupUserKey, BigDecimal.valueOf(1000, 3).divide(BigDecimal.valueOf(group.getUsersMap().size()), RoundingMode.DOWN));
    }
    checkAndAdjustShares(entry.getByShares());
    checkAndAdjustShares(entry.getForShares());
    return Response.ok(entry).build();
  }

  @GET
  @Path("{id:[0-9]+}")
  public Response read(@PathParam("id") @NotNull Long id) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Entry entry = OfyService.ofy().load().type(Entry.class).parent(group).id(id).now();
    if ((entry == null) || (!entry.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Entry with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    return Response.ok(entry).build();
  }

  @DELETE
  @Path("{id:[0-9]+}")
  public Response delete(@PathParam("id") @NotNull Long id) {
    final Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    final Entry entry = OfyService.ofy().load().type(Entry.class).parent(group).id(id).now();
    if ((entry == null) || (!entry.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Entry with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    OfyService.ofy().transact(new Work<Entry>() {
      @Override
      public Entry run() {
        List<EntryOperation> operations = OfyService.ofy().load().type(EntryOperation.class).ancestor(group).filter("entryKey", Key.create(entry)).list();
        OfyService.ofy().delete().entity(entry).now();
        OfyService.ofy().delete().entities(operations).now();
        LOG.info(String.format("%s deleted.", entry));
        return entry;
      }
    });
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(final @Valid @NotNull Entry entry) {
    final Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    entry.setGroupKey(Key.create(group));
    //validate users
    if (!group.getUsersMap().keySet().containsAll(entry.getByShares().keySet())
            || !group.getUsersMap().keySet().containsAll(entry.getForShares().keySet())) {
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(String.format("Entry for/by contains unknown users."))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    OfyService.ofy().transact(new Work<Entry>() {
      @Override
      public Entry run() {
        OfyService.ofy().save().entity(entry).now();
        EntryOperation operation = new EntryOperation(
                Key.create(group),
                Key.create(entry),
                new Date(),
                Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName()));
        OfyService.ofy().save().entity(operation).now();
        LOG.info(String.format("%s created.", entry));
        return entry;
      }
    });
    return Response.ok(entry).build();
  }

  @PUT
  @Path("{id:[0-9]+}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@PathParam("id") @NotNull Long id, final @Valid @NotNull Entry entry) {
    final Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Entry existingEntry = OfyService.ofy().load().type(Entry.class).parent(group).id(id).now();
    if ((existingEntry == null) || (!existingEntry.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Entry with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    if (!existingEntry.getId().equals(id)) {
      LOG.warning(String.format("User %s attempted to change entry id: %d in path, %d in payload.",
              securityContext.getUserPrincipal().getName(),
              existingEntry.getId(),
              entry.getId()));
      return Response
              .status(Response.Status.CONFLICT)
              .entity("Cannot change entry id.")
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    //validate users
    if (!group.getUsersMap().keySet().containsAll(entry.getByShares().keySet())
            || !group.getUsersMap().keySet().containsAll(entry.getForShares().keySet())) {
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(String.format("Entry for/by contains unknown users."))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    entry.setGroupKey(Key.create(group));
    OfyService.ofy().transact(new Work<Entry>() {
      @Override
      public Entry run() {
        OfyService.ofy().save().entity(entry).now();
        EntryOperation operation = new EntryOperation(
                Key.create(group),
                Key.create(entry),
                new Date(),
                Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName()));
        OfyService.ofy().save().entity(operation).now();
        LOG.info(String.format("%s updated.", entry));
        return entry;
      }
    });
    return Response.ok(entry).build();
  }

  @GET
  @Path("{id:[0-9]+}/operations")
  public Response listOperations(@PathParam("id") @NotNull Long id) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Entry entry = OfyService.ofy().load().type(Entry.class).parent(group).id(id).now();
    if ((entry == null) || (!entry.getGroupKey().equals(Key.create(group)))) {
      return Response
              .status(Response.Status.NOT_FOUND)
              .entity(String.format("Entry with id %d does not exist.", id))
              .type(MediaType.TEXT_PLAIN)
              .build();
    }
    List<EntryOperation> operations = OfyService.ofy().load().type(EntryOperation.class).ancestor(group).filter("entryKey", Key.create(entry)).list();
    return Response.ok(operations).build();
  }

  private BigDecimal remainder(Collection<BigDecimal> numbers) {
    int maxScale = 0;
    for (BigDecimal number : numbers) {
      if (number.scale() > maxScale) {
        maxScale = number.scale();
      }
    }
    BigDecimal one = BigDecimal.valueOf((int) Math.pow(10, maxScale), maxScale);
    for (BigDecimal number : numbers) {
      one = one.subtract(number);
    }
    return one;
  }

  private <K> void checkAndAdjustShares(final Map<K, BigDecimal> shares) {
    if (shares.isEmpty()) {
      return;
    }
    K largestKey = shares.keySet().iterator().next();
    for (Map.Entry<K, BigDecimal> share : shares.entrySet()) {
      if (share.getValue().compareTo(shares.get(largestKey)) > 0) {
        largestKey = share.getKey();
      }
    }
    BigDecimal remainder = remainder(shares.values());
    if (remainder.compareTo(BigDecimal.ZERO) != 0) {
      shares.put(largestKey, shares.get(largestKey).add(remainder));
    }
  }

  @POST
  @Consumes("text/csv")
  @Produces(MediaType.TEXT_PLAIN)
  public Response importFromCsv(String csvData) {
    final Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    final Map<String, Key<Chapter>> chapterStringsMap = new HashMap<>();
    final List<CSVRecord> records;
    try {
      records = CSVParser.parse(csvData, CSVFormat.DEFAULT.withHeader()).getRecords();
    } catch (IOException e) {
      return Response
              .status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(String.format("Unexpected %s: %s.", e.getClass().getSimpleName(), e.getMessage()))
              .build();
    }
    //build chapters
    for (CSVRecord record : records) {
      chapterStringsMap.put(record.get("chapters"), null);
    }
    final List<Key<Chapter>> createdChapterKeys = new ArrayList<>();
    OfyService.ofy().transact(new Work() {
      @Override
      public Object run() {
        for (String chapterStrings : chapterStringsMap.keySet()) {
          String[] pieces = chapterStrings.split(" > ");
          Key<Chapter> parentChapterKey = null;
          for (int i = 0; i < pieces.length; i++) {
            Chapter chapter = OfyService.ofy().load().type(Chapter.class)
                    .ancestor(group)
                    .filter("name", pieces[i])
                    .filter("parentChapterKey", parentChapterKey)
                    .first().now();
            System.out.printf("looking for %s son of %s\n", pieces[i], parentChapterKey);
            if (chapter == null) {
              chapter = new Chapter(pieces[i], Key.create(group), parentChapterKey);
              OfyService.ofy().save().entity(chapter).now();
              createdChapterKeys.add(Key.create(chapter));
              System.out.printf("Saved %s with key %s\n", chapter, Key.create(chapter));
              // see https://cloud.google.com/appengine/articles/life_of_write
            }
            parentChapterKey = Key.create(chapter);
          }
          chapterStringsMap.put(chapterStrings, parentChapterKey);
        }
        return null;
      }
    });
    //build entries
    return Response.ok(String.format("Done: %d chapters and %d entries created.", createdChapterKeys.size(), 0)).build();
  }

}
