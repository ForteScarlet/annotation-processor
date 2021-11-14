package examples.repeatableAnnotation;

import love.forte.annotationtool.core.AnnotationMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ForteScarlet
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationMapper(Element.class)
public @interface MyElement {

    @AnnotationMapper.Property(value = "value", target = Element.class)
    String name();

}
