/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.validation;

import java.util.Map;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author eric
 */
public class EachValuePatternValidator implements ConstraintValidator<EachValuePattern, Map<?, String>>{
  
  private Pattern pattern;

  @Override
  public void initialize(EachValuePattern eachValuePattern) {
    pattern = Pattern.compile(eachValuePattern.regexp());
  }

  @Override
  public boolean isValid(Map<?, String> map, ConstraintValidatorContext cvc) {
    if (map!=null) {
      for (String value : map.values()) {
        if (!pattern.matcher(value).matches()) {
          return false;
        }
      }
    }
    return true;
  }

}
