/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author eric
 */
@Entity
public class EntryOperation {
  
  @Id
  private Long id;
  @Parent
  private Key<Group> groupKey;
  private Key<Entry> entryKey;
  private Date date;
  private Key<RegisteredUser> userKey;

  public EntryOperation() {
  }

  public EntryOperation(Key<Group> groupKey, Key<Entry> entryKey, Date date, Key<RegisteredUser> userKey) {
    this.groupKey = groupKey;
    this.entryKey = entryKey;
    this.date = date;
    this.userKey = userKey;
  }

  public Long getId() {
    return id;
  }

  public Key<Group> getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(Key<Group> groupKey) {
    this.groupKey = groupKey;
  }

  public Key<Entry> getEntryKey() {
    return entryKey;
  }

  public void setEntryKey(Key<Entry> entryKey) {
    this.entryKey = entryKey;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Key<RegisteredUser> getUserKey() {
    return userKey;
  }

  public void setUserKey(Key<RegisteredUser> userKey) {
    this.userKey = userKey;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.id);
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
    final EntryOperation other = (EntryOperation) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "EntryOperation{" + "id=" + id + ", entryKey=" + entryKey + ", date=" + date + ", userKey=" + userKey + '}';
  }
  
}
