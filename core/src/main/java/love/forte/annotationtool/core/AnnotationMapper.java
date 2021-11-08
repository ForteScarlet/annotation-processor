package love.forte.annotationtool.core;

import java.lang.annotation.*;

/**
 * Annotated on annotation type.
 * Just like... An annotation extends other annotation?
 *
 * @author ForteScarlet
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationMapper {
    Class<? extends Annotation>[] value();



    /**
     * Annotation's property's mapper.
     * Should use on annotation's property method.
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Properties.class)
    @interface Property {

        /**
         * Target annotation type.
         * <p>
         * if {@link AnnotationMapper#value()}'s length <= 1, this can be ignored.
         */
        Class<? extends Annotation> target() default Annotation.class;

        /**
         * Target annotation's property name.
         */
        String value();
    }



    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Properties {
        Property[] value();
    }

}
