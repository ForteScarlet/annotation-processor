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
@AnnotationMapper(AccountName.class)
public @interface UserAccountName {

    @AnnotationMapper.Property("myName")
    String name();

}
