package love.forte.annotationtool.core;

import java.lang.annotation.*;

/**
 *
 * Annotated on annotation type.
 *
 * @author ForteScarlet
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnoMapping {



    @interface Mapping {
        Class<? extends Annotation> value();

    }
}
