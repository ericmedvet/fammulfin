/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.validation;

import it.newfammulfin.api.util.Util;
import it.newfammulfin.model.Entry;
import java.math.BigDecimal;
import java.util.Collection;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author eric
 */
public class SharesValidator implements ConstraintValidator<Shares, Entry> {

  @Override
  public void initialize(Shares a) {
  }

  @Override
  public boolean isValid(Entry entry, ConstraintValidatorContext cvc) {
    boolean outcome = true;
    outcome = outcome&&(Util.sum(entry.getByShares().values()).compareTo(entry.getAmount().getAmount()) == 0);
    outcome = outcome&&(Util.sum(entry.getForShares().values()).compareTo(entry.getAmount().getAmount()) == 0);
    if (entry.getAmount().getAmount().compareTo(BigDecimal.ZERO)==0) {
      outcome = outcome&&Util.containsNotZero(entry.getByShares().values());
      outcome = outcome&&(!Util.containsNotZero(entry.getForShares().values()));
    }
    return outcome;
  }

}
