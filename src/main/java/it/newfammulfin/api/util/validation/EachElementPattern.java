/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api.util.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 *
 * @author eric
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EachElementPatternValidator.class)
public @interface EachElementPattern {
  
    String regexp();
    String message() default "{it.newfammulfin.constraints.EachElementPattern.message}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
    
}