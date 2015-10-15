/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.joda.money.Money;
import org.joda.time.LocalDate;

/**
 *
 * @author eric
 */
@Entity
public class Entry {
  
  @Id
  private Long id;
  private LocalDate date;
  @com.sappenin.objectify.annotation.Money
  private Money amount;
  @NotNull @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9]*") @Size(min = 5)
  private String payee;
  private String description;
  private String note;

  public Long getId() {
    return id;
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

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.id);
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

}
