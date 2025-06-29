package ru.julia.currencyexchange.utils.annotation;

import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ActiveProfiles("test")
public @interface ActiveProfile {
}
