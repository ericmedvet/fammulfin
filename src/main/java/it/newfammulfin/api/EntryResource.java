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
import java.util.Collection;
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
    if (!existingEntry.getId().equals(id)||!existingEntry.getId().equals(entry.getId())) {
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

  private <K> void checkAndBalanceZeroShares(final Map<K, BigDecimal> shares) {
    if (shares.isEmpty()) {
      return;
    }
    boolean allZero = true;
    for (BigDecimal bigDecimal : shares.values()) {
      if (bigDecimal.compareTo(BigDecimal.ZERO) != 0) {
        allZero = false;
        break;
      }
    }
    if (!allZero) {
      return;
    }
    for (Map.Entry<K, BigDecimal> share : shares.entrySet()) {
      share.setValue(BigDecimal.valueOf((long)Math.pow(10, DEFAULT_SHARE_SCALE), DEFAULT_SHARE_SCALE).divide(BigDecimal.valueOf(shares.size()), RoundingMode.DOWN));
    }
  }

  @POST
  @Consumes("text/csv")
  @Produces(MediaType.TEXT_PLAIN)
  public Response importFromCsv(String csvData) {
    Group group = (Group) requestContext.getProperty(GroupRetrieverRequestFilter.GROUP);
    Map<String, Key<Chapter>> chapterStringsMap = new HashMap<>();
    List<CSVRecord> records;
    try {
      records = CSVParser.parse(csvData, CSVFormat.DEFAULT.withHeader()).getRecords();
    } catch (IOException e) {
      return Response
              .status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(String.format("Unexpected %s: %s.", e.getClass().getSimpleName(), e.getMessage()))
              .build();
    }
    //check users
    Set<String> userIds = new HashSet<>();
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
    Set<String> chapterStringsSet = new HashSet<>();
    for (CSVRecord record : records) {
      chapterStringsSet.add(record.get("chapters"));
    }
    List<Key<Chapter>> createdChapterKeys = new ArrayList<>();
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
          createdChapterKeys.add(chapterKey);
          LOG.info(String.format("%s created.", chapter));
        }
        chapterStringsMap.put(
                partialChapterString,
                chapterKey);
        parentChapterKey = chapterKey;
      }
    }
    //build entries
    List<Key<Entry>> createdEntryKeys = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YY");
    Key<Group> groupKey = Key.create(group);
    for (CSVRecord record : records) {
      Entry entry = new Entry();
      entry.setGroupKey(groupKey);
      entry.setDate(LocalDate.parse(record.get("date"), formatter));
      entry.setAmount(Money.of(CurrencyUnit.of(record.get("currency").toUpperCase()), Double.parseDouble(record.get("value"))));
      entry.setChapterKey(chapterStringsMap.get(record.get("chapters")));
      entry.setPayee(record.get("payee"));
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
          value = value / 100d;
        } else {
          value = Double.parseDouble(share);
          value = value / entry.getAmount().getAmount().doubleValue();
        }
        entry.getByShares().put(Key.create(RegisteredUser.class, userId), BigDecimal.valueOf(value).setScale(DEFAULT_SHARE_SCALE, RoundingMode.DOWN));
      }
      checkAndBalanceZeroShares(entry.getByShares());
      checkAndAdjustShares(entry.getByShares());
      //for shares
      for (String userId : userIds) {
        String share = record.get("for:" + userId);
        double value;
        if (share.contains("%")) {
          entry.setForPercentage(true);
          value = Double.parseDouble(share.replace("%", ""));
          value = value / 100d;
        } else {
          value = Double.parseDouble(share);
          value = value / entry.getAmount().getAmount().doubleValue();
        }
        entry.getForShares().put(Key.create(RegisteredUser.class, userId), BigDecimal.valueOf(value).setScale(DEFAULT_SHARE_SCALE, RoundingMode.DOWN));
      }
      checkAndBalanceZeroShares(entry.getForShares());
      checkAndAdjustShares(entry.getForShares());
      OfyService.ofy().save().entity(entry).now();
      createdEntryKeys.add(Key.create(entry));
      EntryOperation operation = new EntryOperation(
              Key.create(group),
              Key.create(entry),
              new Date(),
              Key.create(RegisteredUser.class, securityContext.getUserPrincipal().getName()));
      OfyService.ofy().save().entity(operation).now();
      LOG.info(String.format("%s created.", entry));
    }
    return Response.ok(String.format("Done: %d chapters and %d entries created.", createdChapterKeys.size(), createdEntryKeys.size())).build();
  }

}
