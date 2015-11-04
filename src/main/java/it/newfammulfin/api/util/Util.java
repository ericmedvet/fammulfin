/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import java.math.BigDecimal;
import java.util.Collection;

/**
 *
 * @author eric
 */
public class Util {
  
  public static BigDecimal sum(Collection<BigDecimal> values) {
    BigDecimal sum = new BigDecimal(0);
    for (BigDecimal value : values) {
      sum = sum.add(value);
    }
    return sum;
  }
  
  public static boolean containsNotZero(Collection<BigDecimal> values) {
    for (BigDecimal value : values) {
      if (value.compareTo(BigDecimal.ZERO)!=0) {
        return true;
      }
    }
    return false;
  }

  public static BigDecimal remainder(Collection<BigDecimal> values, BigDecimal sum) {
    for (BigDecimal number : values) {
      sum = sum.subtract(number);
    }
    return sum;
  }
  
}
