package com.bookie.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSeasonValidator.class)
public @interface ValidSeason {
  String message() default "Invalid season format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

class ValidSeasonValidator implements ConstraintValidator<ValidSeason, String> {
  @Override
  public boolean isValid(String season, ConstraintValidatorContext context) {
    if (season == null || season.length() != 4) return false;
    if (!season.matches("\\d{4}")) return false;

    int start = Integer.parseInt(season.substring(0, 2));
    int end = Integer.parseInt(season.substring(2, 4));
    return end == (start + 1) % 100;
  }
}
