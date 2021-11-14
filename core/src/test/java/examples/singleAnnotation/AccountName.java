package examples.singleAnnotation;

import love.forte.annotationtool.core.AnnotationMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@AnnotationMapper(Name.class)
public @interface AccountName {

    @AnnotationMapper.Property("value")
    String myName();

    int age() default 18;
}
