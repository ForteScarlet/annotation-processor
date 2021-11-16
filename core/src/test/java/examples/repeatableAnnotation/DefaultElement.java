package examples.repeatableAnnotation;

import love.forte.annotationtool.core.AnnotationMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@AnnotationMapper(MyElement.class)
@MyElement(name = "default", length = 50)
@interface DefaultElement {
    @AnnotationMapper.Property(value = "name", target = MyElement.class)
    String defValue() default "default";

    @AnnotationMapper.Property(value = "length", target = MyElement.class)
    int defLength() default 10;
}
