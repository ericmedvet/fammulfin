/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.newfammulfin.api.util.validation.EachValuePattern;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.money.CurrencyUnit;

/**
 *
 * @author eric
 */
@Entity
public class Group {
  
  @Id
  private Long id;
  @NotNull @NotBlank @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9]*") @Size(max = 128)
  private String name;
  @Index
  private Key<RegisteredUser> masterUserKey;
  //should validate nicknames?
  @Index @EachValuePattern(regexp = "[a-zA-Z][\\w'_.-]{0,127}")
  private Map<Key<RegisteredUser>, String> usersMap = new LinkedHashMap<>(); //userKey, nickname
  @NotNull
  private CurrencyUnit defaultCurrencyUnit;

  public Group() {
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Key<RegisteredUser> getMasterUserKey() {
    return masterUserKey;
  }

  public void setMasterUserKey(Key<RegisteredUser> masterUserKey) {
    this.masterUserKey = masterUserKey;
  }

  public CurrencyUnit getDefaultCurrencyUnit() {
    return defaultCurrencyUnit;
  }

  public void setDefaultCurrencyUnit(CurrencyUnit defaultCurrencyUnit) {
    this.defaultCurrencyUnit = defaultCurrencyUnit;
  }

  public Map<Key<RegisteredUser>, String> getUsersMap() {
    return usersMap;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.id);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Group other = (Group) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Group{" + "id=" + id + ", name=" + name + '}';
  }
  
}
