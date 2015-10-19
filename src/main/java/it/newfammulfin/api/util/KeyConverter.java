/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.stringifier.KeyStringifier;
import java.lang.reflect.Type;
import org.joda.money.CurrencyUnit;

/**
 *
 * @author eric
 */
public class KeyConverter implements JsonSerializer<Key<?>>, JsonDeserializer<Key<?>> {
  
  private final KeyStringifier keyStingifier = new KeyStringifier(); 
  
  @Override
  public JsonElement serialize(Key<?> key, Type type, JsonSerializationContext jsc) {
    return new JsonPrimitive(keyStingifier.toString(key));
  }

  @Override
  public Key<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
    if (!(jsonElement instanceof JsonPrimitive)) {
      throw new JsonParseException("Json object expected");
    }
    return keyStingifier.fromString(jsonElement.getAsString());
  }
  
}
