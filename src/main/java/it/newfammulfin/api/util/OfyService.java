/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.sappenin.objectify.translate.JodaMoneyEmbeddedEntityTranslatorFactory;
import it.newfammulfin.model.Chapter;
import it.newfammulfin.model.Entry;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.RegisteredUser;

/**
 *
 * @author eric
 */
public class OfyService {

  static {
    ObjectifyService.factory().getTranslators().add(new JodaMoneyEmbeddedEntityTranslatorFactory());
    ObjectifyService.register(Entry.class);
    ObjectifyService.register(Chapter.class);
    ObjectifyService.register(RegisteredUser.class);
    ObjectifyService.register(Group.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

}
