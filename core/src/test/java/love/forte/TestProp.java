package love.forte;

import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * @author ForteScarlet
 */
public class TestProp {

    private final ResourceBundle myProp;

    public TestProp(ResourceBundle myProp) {
        this.myProp = myProp;
    }

    public String get(@PropertyKey(resourceBundle = "i18n.messages") String key) {
        return myProp.getString(key);
    }

}
