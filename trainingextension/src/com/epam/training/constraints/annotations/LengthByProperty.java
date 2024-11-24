package com.epam.training.constraints.annotations;

import com.epam.training.constraints.validators.LengthByPropertyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LengthByPropertyValidator.class)
@Documented
public @interface LengthByProperty {

    String message() default "{com.epam.training.constraints.annotations.LengthByProperty.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int value() default 0;
}
