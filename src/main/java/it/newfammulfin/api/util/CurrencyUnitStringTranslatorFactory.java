/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.translate.CreateContext;
import com.googlecode.objectify.impl.translate.LoadContext;
import com.googlecode.objectify.impl.translate.NullSafeTranslator;
import com.googlecode.objectify.impl.translate.SaveContext;
import com.googlecode.objectify.impl.translate.SkipException;
import com.googlecode.objectify.impl.translate.Translator;
import com.googlecode.objectify.impl.translate.TranslatorFactory;
import com.googlecode.objectify.impl.translate.TypeKey;
import com.googlecode.objectify.repackaged.gentyref.GenericTypeReflector;
import org.joda.money.CurrencyUnit;

/**
 *
 * @author eric
 */
public class CurrencyUnitStringTranslatorFactory implements TranslatorFactory<CurrencyUnit, String> {

  @Override
  public Translator<CurrencyUnit, String> create(TypeKey<CurrencyUnit> tk, CreateContext cc, Path path) {
    if (CurrencyUnit.class.isAssignableFrom(GenericTypeReflector.erase(tk.getType()))) {
      return new CurrencyUnitStringTranslator();
    }
    return null;
  }

  private static class CurrencyUnitStringTranslator extends NullSafeTranslator<CurrencyUnit, String> {

    @Override
    protected CurrencyUnit loadSafe(String code, LoadContext lc, Path path) throws SkipException {
      return CurrencyUnit.getInstance(code);
    }

    @Override
    protected String saveSafe(CurrencyUnit currencyUnit, boolean bln, SaveContext sc, Path path) throws SkipException {
      return currencyUnit.getCode();
    }

  }

}
