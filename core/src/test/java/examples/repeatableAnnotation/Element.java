package examples.repeatableAnnotation;

import java.lang.annotation.*;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Repeatable(Elements.class)
public @interface Element {
    String value();
}
