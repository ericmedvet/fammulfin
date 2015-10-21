/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 *
 * @author eric
 */
public class MoneyConverter implements JsonSerializer<Money>, JsonDeserializer<Money> {
  
  private final static String VALUE_FIELD = "value";
  private final static String CURRENCY_FIELD = "currency";

  @Override
  public JsonElement serialize(Money money, Type type, JsonSerializationContext jsc) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(VALUE_FIELD, new JsonPrimitive(money.getAmount()));
    jsonObject.add(CURRENCY_FIELD, new JsonPrimitive(money.getCurrencyUnit().getCurrencyCode()));
    return jsonObject;
  }

  @Override
  public Money deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
    if (!(jsonElement instanceof JsonObject)) {
      throw new JsonParseException("Json object expected");
    }
    JsonObject jsonObject = (JsonObject)jsonElement;
    if (!jsonObject.has(VALUE_FIELD)||!jsonObject.has(CURRENCY_FIELD)) {
      throw new JsonParseException("Fields missing (expected "+VALUE_FIELD+" and "+CURRENCY_FIELD+")");
    }
    Money money = Money.of(
            CurrencyUnit.getInstance(jsonObject.get(CURRENCY_FIELD).getAsString()),
            jsonObject.get(VALUE_FIELD).getAsDouble());
    return money;
  }
  
}
