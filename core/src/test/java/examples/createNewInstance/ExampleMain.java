package examples.createNewInstance;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ForteScarlet
 */
@Element(value = "Hello World", size = 15)
class ExampleMain {
    private static AnnotationTool tool;


    @BeforeAll
    public static void before() {
        tool = AnnotationTools.getAnnotationTool();
    }

    @Test
    public void test1() throws ReflectiveOperationException {
        Map<String, Object> params = new HashMap<>();
        params.put("value", "Hello World");
        params.put("size", 15);
        params.put("name", "ForteScarlet");
        // throws ReflectiveOperationException
        final Element annotationInstance1 = tool.createAnnotationInstance(Element.class, params);
        assert annotationInstance1.name().equals("ForteScarlet");
        assert annotationInstance1.size() == 15;
        assert annotationInstance1.value().equals("Hello World");

        params.remove("name");
        final Element annotationInstance2 = tool.createAnnotationInstance(Element.class, params);
        // default value support.
        assert annotationInstance2.name().equals("forte");
        assert annotationInstance2.size() == 15;
        assert annotationInstance2.value().equals("Hello World");

        final Element annotationInstance3 = tool.createAnnotationInstance(Element.class, params);
        assert annotationInstance2.equals(annotationInstance3);

        final Element nativeElement = ExampleMain.class.getAnnotation(Element.class);
        assert nativeElement.equals(annotationInstance3);
    }

}
