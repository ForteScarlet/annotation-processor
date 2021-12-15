import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(JAn.Container.class)
public @interface JAn {

    int age();
    String name();

    @Retention(RetentionPolicy.RUNTIME)
    @interface Container {
        JAn[] value();
    }
}
