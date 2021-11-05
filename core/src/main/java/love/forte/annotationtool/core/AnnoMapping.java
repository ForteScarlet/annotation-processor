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
    Mapping[] mappings();


    @interface Mapping {
        /**
         * The type you want to convert to.
         */
        Class<? extends Annotation> value();

        /**
         * The Annotation type mapper for {@link #value}.
         *
         * 范型必须是确定的Annotation type, 且必须有一个无参构造用来实例化s。
         */
        Class<? extends AnnotationMapper> mapper() default AnnotationMapper.class;


        /**
         * The Annotation type mapper's name from {@link AnnotationMappers} for {@link #value}.
         *
         * Use {@link #mapper()} first.
         *
         */
        String mapperName() default "";
    }
}
