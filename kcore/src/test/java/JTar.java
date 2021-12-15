import love.forte.annotationtool.AnnotationMapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@AnnotationMapper({JBar.class, JFoo.class})
public @interface JTar {
    @AnnotationMapper.Property(target = JFoo.class, value = "name")
    String name() default "forli";

    @AnnotationMapper.Property(target = JBar.class, value = "age")
    int age() default 17;

}
