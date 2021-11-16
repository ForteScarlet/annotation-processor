package examples.repeatableAnnotation;

import java.lang.annotation.*;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Elements.class)
@interface Element {
    String value();

    int size();
}
