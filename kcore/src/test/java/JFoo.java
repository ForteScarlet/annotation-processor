import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JFoo {
    String name();
}
