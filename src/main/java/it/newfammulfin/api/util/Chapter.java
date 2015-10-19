/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import it.newfammulfin.model.Group;
import it.newfammulfin.model.WithModifications;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author eric
 */
public class Chapter extends WithModifications {
  
  @Id
  private Long id;
  @NotNull @NotBlank @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9]*") @Size(max = 128)
  private String name;
  @Parent
  private Key<Group> group;
  private Key<Chapter> parentChapter;

  public Chapter(Long id, String name, Key<Chapter> parentChapter) {
    this.id = id;
    this.name = name;
    this.parentChapter = parentChapter;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Key<Group> getGroup() {
    return group;
  }

  public void setGroup(Key<Group> group) {
    this.group = group;
  }

  public Key<Chapter> getParentChapter() {
    return parentChapter;
  }

  public void setParentChapter(Key<Chapter> parentChapter) {
    this.parentChapter = parentChapter;
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
