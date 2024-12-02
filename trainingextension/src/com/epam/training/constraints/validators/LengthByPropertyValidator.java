package com.epam.training.constraints.validators;

import com.epam.training.constraints.annotations.LengthByProperty;
import de.hybris.platform.core.Registry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LengthByPropertyValidator implements ConstraintValidator<LengthByProperty, String> {

    private int value;

    @Override
    public void initialize(LengthByProperty constraintAnnotation) {
        value = Registry.getCurrentTenant().getConfig().getInt("valid.string.length", Integer.MAX_VALUE);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {

        return s == null || s.length() <= value;
    }
}
