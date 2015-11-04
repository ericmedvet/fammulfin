/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.google.common.base.Joiner;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import it.newfammulfin.api.util.GroupRetrieverRequestFilter;
import it.newfammulfin.api.util.OfyService;
import it.newfammulfin.api.util.RetrieveGroup;
import it.newfammulfin.api.util.Util;
import it.newfammulfin.model.Chapter;
import it.newfammulfin.model.Entry;
import it.newfammulfin.model.EntryOperation;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
  private static final String CSV_CHAPTERS_SEPARATOR = " > ";
  private static final String CSV_TAGS_SEPARATOR = " > ";
  public static final int DEFAULT_SHARE_SCALE = 4;

  @GET
  public Response readAll(@PathParam("groupId") @NotNull Long groupId) {
    //TODO add querying capability
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
    entry.getTags().add("Fun");
    entry.getByShares().put(userKey, entry.getAmount().getAmount());
    for (Key<RegisteredUser> otherSserKey : group.getUsersMap().keySet()) {
      entry.getForShares().put(otherSserKey, BigDecimal.ZERO);
    }
    checkAndBalanceZeroShares(entry.getForShares(), entry.getAmount().getAmount());
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

  //debug method, should remove in production
  @DELETE
  @Produces(MediaType.TEXT_PLAIN)
  public Response deleteAll() {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    List<Key<?>> keys = new ArrayList<>();
    keys.addAll(OfyService.ofy().load().type(Entry.class).ancestor(group).keys().list());
    keys.addAll(OfyService.ofy().load().type(EntryOperation.class).ancestor(group).keys().list());
    keys.addAll(OfyService.ofy().load().type(Chapter.class).ancestor(group).keys().list());
    OfyService.ofy().delete().keys(keys).now();
    return Response.ok(String.format("Removed %d entities.", keys.size())).build();
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
    if (!existingEntry.getId().equals(id) || !existingEntry.getId().equals(entry.getId())) {
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
  public Response readOperations(@PathParam("id") @NotNull Long id) {
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

  private <K> boolean checkAndBalanceZeroShares(final Map<K, BigDecimal> shares, BigDecimal expectedSum) {
    if (shares.isEmpty()) {
      return false;
    }
    boolean equalShares = false;
    if (!Util.containsNotZero(shares.values())) {
      equalShares = true;
      expectedSum = expectedSum.setScale(Math.max(DEFAULT_SHARE_SCALE, expectedSum.scale()));
      for (Map.Entry<K, BigDecimal> shareEntry : shares.entrySet()) {
        shareEntry.setValue(expectedSum.divide(BigDecimal.valueOf(shares.size()), RoundingMode.DOWN));
      }
    }
    K largestKey = shares.keySet().iterator().next();
    for (Map.Entry<K, BigDecimal> share : shares.entrySet()) {
      if (share.getValue().abs().compareTo(shares.get(largestKey).abs()) > 0) {
        largestKey = share.getKey();
      }
    }
    BigDecimal remainder = Util.remainder(shares.values(), expectedSum);
    if (remainder.compareTo(BigDecimal.ZERO) != 0) {
      shares.put(largestKey, shares.get(largestKey).add(remainder));
    }
    return equalShares;
  }

  @POST
  @Consumes("text/csv")
  @Produces(MediaType.TEXT_PLAIN)
  public Response importFromCsv(String csvData) {
    //TODO add invertSign query param
    //TODO remove empty chapter
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
    //check users
    final Set<String> userIds = new HashSet<>();
    for (String columnName : records.get(0).toMap().keySet()) {
      if (columnName.startsWith("by:")) {
        String userId = columnName.replaceFirst("by:", "");
        if (!group.getUsersMap().keySet().contains(Key.create(RegisteredUser.class, userId))) {
          return Response
                  .status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity(String.format("User %s not found in this group.", userId))
                  .build();
        }
        userIds.add(userId);
      }
    }
    //build chapters
    final Set<String> chapterStringsSet = new HashSet<>();
    for (CSVRecord record : records) {
      chapterStringsSet.add(record.get("chapters"));
    }
    final List<Key<?>> createdKeys = new ArrayList<>();
    try {
      OfyService.ofy().transact(new Work<List<Key<?>>>() {
        @Override
        public List<Key<?>> run() {
          for (String chapterStrings : chapterStringsSet) {
            List<String> pieces = Arrays.asList(chapterStrings.split(CSV_CHAPTERS_SEPARATOR));
            Key<Chapter> parentChapterKey = null;
            for (int i = 0; i < pieces.size(); i++) {
              String partialChapterString = Joiner.on(CSV_CHAPTERS_SEPARATOR).join(pieces.subList(0, i + 1));
              Key<Chapter> chapterKey = chapterStringsMap.get(partialChapterString);
              if (chapterKey == null) {
                chapterKey = OfyService.ofy().load().type(Chapter.class)
                        .ancestor(group)
                        .filter("name", pieces.get(i))
                        .filter("parentChapterKey", parentChapterKey)
                        .keys().first().now();
                chapterStringsMap.put(
                        partialChapterString,
                        chapterKey);
              }
              if (chapterKey == null) {
                Chapter chapter = new Chapter(pieces.get(i), Key.create(group), parentChapterKey);
                OfyService.ofy().save().entity(chapter).now();
                chapterKey = Key.create(chapter);
                createdKeys.add(chapterKey);
                LOG.info(String.format("%s created.", chapter));
              }
              chapterStringsMap.put(
                      partialChapterString,
                      chapterKey);
              parentChapterKey = chapterKey;
            }
          }
          //build entries
          DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YY");
          Key<Group> groupKey = Key.create(group);
          for (CSVRecord record : records) {
            Entry entry = new Entry();
            entry.setGroupKey(groupKey);
            entry.setDate(LocalDate.parse(record.get("date"), formatter));
            entry.setAmount(Money.of(CurrencyUnit.of(record.get("currency").toUpperCase()), Double.parseDouble(record.get("value"))));
            entry.setChapterKey(chapterStringsMap.get(record.get("chapters")));
            entry.setPayee(record.get("payee"));
            int scale = Math.max(DEFAULT_SHARE_SCALE, entry.getAmount().getScale());
            for (String tag : record.get("tags").split(CSV_TAGS_SEPARATOR)) {
              if (!tag.trim().isEmpty()) {
                entry.getTags().add(tag);
              }
            }
            entry.setDescription(record.get("description"));
            entry.setNote(record.get("notes"));
            //by shares
            for (String userId : userIds) {
              String share = record.get("by:" + userId);
              double value;
              if (share.contains("%")) {
                entry.setByPercentage(true);
                value = Double.parseDouble(share.replace("%", ""));
                value = entry.getAmount().getAmount().doubleValue() * value / 100d;
              } else {
                value = Double.parseDouble(share);
              }
              entry.getByShares().put(Key.create(RegisteredUser.class, userId), BigDecimal.valueOf(value).setScale(scale, RoundingMode.DOWN));
            }
            boolean equalByShares = checkAndBalanceZeroShares(entry.getByShares(), entry.getAmount().getAmount());
            entry.setByPercentage(entry.isByPercentage()||equalByShares);
            //for shares
            for (String userId : userIds) {
              String share = record.get("for:" + userId);
              double value;
              if (share.contains("%")) {
                entry.setForPercentage(true);
                value = Double.parseDouble(share.replace("%", ""));
                value = entry.getAmount().getAmount().doubleValue() * value / 100d;
              } else {
                value = Double.parseDouble(share);
              }
              entry.getForShares().put(Key.create(RegisteredUser.class, userId), BigDecimal.valueOf(value).setScale(scale, RoundingMode.DOWN));
            }
            boolean equalForShares = checkAndBalanceZeroShares(entry.getForShares(), entry.getAmount().getAmount());
            entry.setForPercentage(entry.isForPercentage()||equalForShares);
            OfyService.ofy().save().entity(entry).now();
            createdKeys.add(Key.create(entry));
            EntryOperation operation = new EntryOperation(
                    Key.create(group),
                    Key.create(entry),
                    new Date(),
                    Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName()));
            OfyService.ofy().save().entity(operation).now();
            LOG.info(String.format("%s created.", entry));
          }
          return createdKeys;
        }
      });
      //count keys
      int numberOfCreatedChapters = 0;
      int numberOfCreatedEntries = 0;
      for (Key<?> key : createdKeys) {
        if (key.getKind().equals(Entry.class.getSimpleName())) {
          numberOfCreatedEntries = numberOfCreatedEntries + 1;
        } else if (key.getKind().equals(Chapter.class.getSimpleName())) {
          numberOfCreatedChapters = numberOfCreatedChapters + 1;
        }
      }
      return Response.ok(String.format("Done: %d chapters and %d entries created.", numberOfCreatedChapters, numberOfCreatedEntries)).build();
    } catch (RuntimeException e) {
      LOG.warning(String.format("Unexpected %s: %s.", e.getClass().getSimpleName(), e.getMessage()));
      return Response
              .status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(String.format("Unexpected %s: %s.", e.getClass().getSimpleName(), e.getMessage()))
              .build();
    }
  }

}
