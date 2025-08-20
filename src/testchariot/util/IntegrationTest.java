package util;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
    int expectedSeconds() default 3;
}
