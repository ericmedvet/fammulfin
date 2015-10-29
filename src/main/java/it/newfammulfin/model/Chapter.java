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
import com.googlecode.objectify.annotation.Parent;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author eric
 */
@Entity
public class Chapter {
  
  @Id
  private Long id;
  @Index @NotNull @NotBlank @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9]*") @Size(max = 128)
  private String name;
  @Parent
  private Key<Group> groupKey;
  @Index
  private Key<Chapter> parentChapterKey;

  public Chapter() {
  }

  public Chapter(String name, Key<Group> groupKey, Key<Chapter> parentChapterKey) {
    this.name = name;
    this.groupKey = groupKey;
    this.parentChapterKey = parentChapterKey;
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

  public Key<Group> getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(Key<Group> groupKey) {
    this.groupKey = groupKey;
  }

  public Key<Chapter> getParentChapterKey() {
    return parentChapterKey;
  }

  public void setParentChapterKey(Key<Chapter> parentChapterKey) {
    this.parentChapterKey = parentChapterKey;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + Objects.hashCode(this.id);
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
    final Chapter other = (Chapter) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Chapter{" + "id=" + id + ", name=" + name + '}';
  }
  
}
