/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.converters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.objectify.Key;
import java.lang.reflect.Type;

/**
 *
 * @author eric
 */
public class KeyConverter implements JsonSerializer<Key<?>>, JsonDeserializer<Key<?>> {

  @Override
  public JsonElement serialize(Key<?> key, Type type, JsonSerializationContext jsc) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("raw", key.toWebSafeString());
    jsonObject.addProperty("id", key.getId());
    return jsonObject;
  }

  @Override
  public Key<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
    if (!(jsonElement instanceof JsonObject)) {
      throw new JsonParseException("Json object expected");
    }
    if (((JsonObject)jsonElement).get("raw")==null) {
      throw new JsonParseException("Json object expected");
    }
    String rawString = ((JsonObject)jsonElement).get("raw").getAsString();
    return Key.create(rawString);
  }

}
