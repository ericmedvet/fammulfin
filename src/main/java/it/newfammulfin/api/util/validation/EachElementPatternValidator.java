/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.validation;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author eric
 */
public class EachElementPatternValidator implements ConstraintValidator<EachElementPattern, Collection<String>>{
  
  private Pattern pattern;

  @Override
  public void initialize(EachElementPattern eachElementPattern) {
    pattern = Pattern.compile(eachElementPattern.regexp());
  }

  @Override
  public boolean isValid(Collection<String> strings, ConstraintValidatorContext cvc) {
    if (strings!=null) {
      for (String string : strings) {
        if (!pattern.matcher(string).matches()) {
          return false;
        }
      }
    }
    return true;
  }

}
