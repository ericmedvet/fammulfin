/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.validation;

import it.newfammulfin.api.util.validation.Shares;
import java.math.BigDecimal;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author eric
 */
public class SharesValidator implements ConstraintValidator<Shares, Map<?, BigDecimal>>{

  @Override
  public void initialize(Shares a) {
  }

  @Override
  public boolean isValid(Map<?, BigDecimal> map, ConstraintValidatorContext cvc) {
    BigDecimal sum = new BigDecimal(0);
    if (map!=null) {
      for (BigDecimal value : map.values()) {
        sum = sum.add(value);
        if (value.compareTo(BigDecimal.ZERO)<0) {
          return false;
        }
      }
    }
    return sum.compareTo(BigDecimal.ONE)==0;
  }
  
}
