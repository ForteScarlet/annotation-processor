package love.forte.annotationtool.core;

import java.lang.annotation.*;

/**
 * Annotated on annotation type.
 *
 * @author ForteScarlet
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotateMapper {
    Type[] value();


    /**
     * Annotation's mapper.
     * <p>
     * Used on {@link AnnotateMapper#value()}.
     */
    @interface Type {
        /**
         * The type you want to convert to.
         */
        Class<? extends Annotation> value();
    }

    /**
     * Annotation's property's mapper.
     * Should use on annotation's property method.
     */
    @Repeatable(Properties.class)
    @interface Property {

        /**
         * Target annotation type.
         * <p>
         * if {@link AnnotateMapper#value()}'s length <= 1, this can be ignored.
         */
        Class<? extends Annotation> target() default Annotation.class;

        /**
         * Target annotation's property name.
         */
        String value();
    }



    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Properties {
        Property[] value();
    }

}
