/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.converters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.joda.money.CurrencyUnit;

/**
 *
 * @author eric
 */
public class CurrencyUnitConverter implements JsonSerializer<CurrencyUnit>, JsonDeserializer<CurrencyUnit> {
  
  @Override
  public JsonElement serialize(CurrencyUnit currencyUnit, Type type, JsonSerializationContext jsc) {
    return new JsonPrimitive(currencyUnit.getCurrencyCode());
  }

  @Override
  public CurrencyUnit deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
    if (!(jsonElement instanceof JsonPrimitive)) {
      throw new JsonParseException("Json object expected");
    }
    return CurrencyUnit.getInstance(jsonElement.getAsString());
  }
  
}
