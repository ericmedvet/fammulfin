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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.money.Money;
import org.joda.time.LocalDate;

/**
 *
 * @author eric
 */
@Entity
public class Entry extends WithModifications {
  
  @Id
  private Long id;
  @Parent
  private Key<Group> group;
  private LocalDate date;
  @com.sappenin.objectify.annotation.Money
  private Money amount;
  @NotNull @NotBlank @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9]*") @Size(max = 128)
  private String payee;
  private Key<Chapter> chapter;
  //should validate each? see https://github.com/jirutka/validator-collection
  private Set<String> tags = new LinkedHashSet<>();
  private String description;
  private String note;
  //should validate each? see https://github.com/jirutka/validator-collection
  private List<String> attachmentUrls = new ArrayList<>();
  private boolean monthly;
  private Map<Key<RegisteredUser>, BigDecimal> byShares = new LinkedHashMap<>();
  private Map<Key<RegisteredUser>, BigDecimal> forShares = new LinkedHashMap<>();
  private boolean byPercentage;
  private boolean forPercentage;
  
  public Long getId() {
    return id;
  }

  public Key<Group> getGroup() {
    return group;
  }

  public void setGroup(Key<Group> group) {
    this.group = group;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Money getAmount() {
    return amount;
  }

  public void setAmount(Money amount) {
    this.amount = amount;
  }

  public String getPayee() {
    return payee;
  }

  public void setPayee(String payee) {
    this.payee = payee;
  }

  public Key<Chapter> getChapter() {
    return chapter;
  }

  public void setChapter(Key<Chapter> chapter) {
    this.chapter = chapter;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public boolean isMonthly() {
    return monthly;
  }

  public void setMonthly(boolean monthly) {
    this.monthly = monthly;
  }

  public Set<String> getTags() {
    return tags;
  }

  public List<String> getAttachmentUrls() {
    return attachmentUrls;
  }

  public Map<Key<RegisteredUser>, BigDecimal> getByShares() {
    return byShares;
  }

  public Map<Key<RegisteredUser>, BigDecimal> getForShares() {
    return forShares;
  }

  public boolean isByPercentage() {
    return byPercentage;
  }

  public void setByPercentage(boolean byPercentage) {
    this.byPercentage = byPercentage;
  }

  public boolean isForPercentage() {
    return forPercentage;
  }

  public void setForPercentage(boolean forPercentage) {
    this.forPercentage = forPercentage;
  }  

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 61 * hash + Objects.hashCode(this.id);
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
    final Entry other = (Entry) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Entry{" + "id=" + id + ", date=" + date + ", amount=" + amount + '}';
  }

}
