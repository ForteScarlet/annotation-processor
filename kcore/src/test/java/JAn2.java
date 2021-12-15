import love.forte.annotationtool.AnnotationMapper;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@JAn(age = 15, name = "")
@AnnotationMapper(JAn.class)
@Repeatable(JAn2.Container.class)
public @interface JAn2 {

    @AnnotationMapper.Property(value = "name", target = JAn.class)
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @interface Container {
        JAn2[] value();
    }
}
