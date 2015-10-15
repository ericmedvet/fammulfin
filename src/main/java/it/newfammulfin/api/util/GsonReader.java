/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.fatboyindustrial.gsonjodatime.LocalDateConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.joda.money.Money;
import org.joda.time.LocalDate;

/**
 *
 * @author eric
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GsonReader<T> implements MessageBodyReader<T> {

  private Gson gson;

  public GsonReader() {
    //should probably inject or something similar
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Money.class, new MyMoneyConverter());
    gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateConverter());
    gson = gsonBuilder.create();
  }

  
  @Override
  public boolean isReadable(
          Class<?> type,
          Type genericType,
          Annotation[] annotations,
          MediaType mediaType) {
    return true;
  }

  @Override
  public T readFrom(
          Class<T> type,
          Type genericType,
          Annotation[] annotations,
          MediaType mediaType,
          MultivaluedMap<String, String> httpHeaders,
          InputStream inputStream)
          throws IOException, WebApplicationException {
    return gson.fromJson(new InputStreamReader(inputStream), type);
  }
  
}
