/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.security.Principal;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author eric
 */
@Entity
public class RegisteredUser extends WithModifications implements Principal {
  
  @Id @NotNull
  private String name;
  @NotNull @NotBlank @Size(max = 128)
  private String firstName;
  @NotNull @NotBlank @Size(max = 128)
  private String lastName;

  public RegisteredUser(String name) {
    this.name = name;
  }

  public RegisteredUser() {
  }

  @Override
  public String getName() {
    return name;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + Objects.hashCode(this.name);
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
    final RegisteredUser other = (RegisteredUser) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RegisteredUser{" + "name=" + name + '}';
  }

 }
