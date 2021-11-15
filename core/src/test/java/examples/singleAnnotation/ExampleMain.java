package examples.singleAnnotation;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author ForteScarlet
 */
@UserAccountName(name = "forte")
class ExampleMain {

    private static AnnotationTool tool;

    @BeforeAll
    public static void before() {
        tool = AnnotationTools.getAnnotationTool();
    }

    @Test
    public void test1() throws ReflectiveOperationException {
        final Name name = tool.getAnnotation(ExampleMain.class, Name.class);
        assert name != null;
        assert name.value().equals("forte");

        final Name name2 = tool.createAnnotationInstance(Name.class, Collections.singletonMap("value", "forte"));
        assert name.equals(name2);
    }

}
