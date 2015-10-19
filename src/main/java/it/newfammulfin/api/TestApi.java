/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import it.newfammulfin.model.Entry;
import it.newfammulfin.api.util.OfyService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.LocalDate;

/**
 *
 * @author eric
 */
@Path("/entries")
@Produces(MediaType.APPLICATION_JSON)
public class TestApi {

  @GET
  public Response listAll() {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    List<Entry> entries = OfyService.ofy().load().type(Entry.class).list();
    return Response.ok(entries).build();
  }

  @GET
  @Path("template")
  public Response template() {
    Entry entry = new Entry();
    entry.setDate(new LocalDate(2011, 2, 7));
    entry.setAmount(Money.of(CurrencyUnit.EUR, 2.2));
    return Response.ok(entry).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@Valid Entry entry) {
    System.out.println("creating valid entry");
    OfyService.ofy().save().entities(entry).now();
    return Response.ok(entry).build();
  }

  @POST
  @Path("validate")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response validate(@Valid Entry entry) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    System.out.println("validator is "+validator.getClass().getName());
    System.out.println("validating valid entry: "+entry.getPayee());
    System.out.println(validator.validate(entry));
    return Response.ok(entry).build();
  }

  @POST
  @Path("nocheck")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createNoCheck(Entry entry) {
    OfyService.ofy().save().entities(entry).now();
    return Response.ok(entry).build();
  }

}
