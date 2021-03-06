/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.BigDecimalLongTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.ReadablePartialTranslatorFactory;
import com.sappenin.objectify.translate.JodaMoneyEmbeddedEntityTranslatorFactory;
import it.newfammulfin.api.EntryResource;
import it.newfammulfin.model.Chapter;
import it.newfammulfin.model.Entry;
import it.newfammulfin.model.EntryOperation;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;

/**
 *
 * @author eric
 */
public class OfyService {

  static {
    ObjectifyService.factory().getTranslators().add(new JodaMoneyEmbeddedEntityTranslatorFactory());
    ObjectifyService.factory().getTranslators().add(new ReadablePartialTranslatorFactory());
    ObjectifyService.factory().getTranslators().add(new CurrencyUnitStringTranslatorFactory());
    ObjectifyService.factory().getTranslators().add(new BigDecimalLongTranslatorFactory((long)Math.pow(10, EntryResource.DEFAULT_SHARE_SCALE+1)));
    ObjectifyService.register(Entry.class);
    ObjectifyService.register(EntryOperation.class);
    ObjectifyService.register(Chapter.class);
    ObjectifyService.register(RegisteredUser.class);
    ObjectifyService.register(Group.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

}
