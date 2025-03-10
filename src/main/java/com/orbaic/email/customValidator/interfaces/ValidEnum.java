package com.orbaic.email.customValidator.interfaces;

import com.orbaic.email.customValidator.classes.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE_PARAMETER})
public @interface ValidEnum {
    String message() default "{Invalid Enum}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum> value() default Enum.class;
}
