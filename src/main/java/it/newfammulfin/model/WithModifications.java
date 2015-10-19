/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.model;

import com.googlecode.objectify.Key;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author eric
 */
public class WithModifications {
  
  private final SortedMap<Date, Key<RegisteredUser>> modifications = new TreeMap<>();

  public SortedMap<Date, Key<RegisteredUser>> getModifications() {
    return modifications;
  }

}
