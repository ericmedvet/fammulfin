/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import it.newfammulfin.api.util.converters.MoneyConverter;
import it.newfammulfin.api.util.converters.CurrencyUnitConverter;
import it.newfammulfin.api.util.converters.KeyConverter;
import com.fatboyindustrial.gsonjodatime.LocalDateConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.LocalDate;

/**
 *
 * @author eric
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GsonReaderWriter<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

  private final Gson gson;

  public GsonReaderWriter() {
    //should probably inject or something similar
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Money.class, new MoneyConverter());
    gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateConverter());
    gsonBuilder.registerTypeAdapter(CurrencyUnit.class, new CurrencyUnitConverter());
    gsonBuilder.registerTypeAdapter(Key.class, new KeyConverter());
    gsonBuilder.enableComplexMapKeySerialization();
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
  
  @Override
  public void writeTo(
          T t,
          Class<?> type,
          Type genericType,
          Annotation[] annotations,
          MediaType mediaType,
          MultivaluedMap<String, Object> httpHeaders,
          OutputStream entityStream)
          throws IOException, WebApplicationException {
    httpHeaders.get("Content-Type").add("charset=UTF-8");
    entityStream.write(gson.toJson(t).getBytes("UTF-8"));
  }

  @Override
  public long getSize(
          T t,
          Class<?> type,
          Type genericType,
          Annotation[] annotations,
          MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(
          Class<?> type,
          Type genericType,
          Annotation[] annotations,
          MediaType mediaType) {
    return true;
  }

}
