package love.forte;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ForteScarlet
 */
public class TestMain {

    @Test
    public void i18nTest() {
        final Locale locale = Locale.getDefault();
        final ResourceBundle myProp = ResourceBundle.getBundle("i18n.messages", locale, this.getClass().getClassLoader());

        final TestProp prop = new TestProp(myProp);
        System.out.println(prop.get("name"));
        System.out.println(prop.get("age"));
    }
}
