package examples.repeatableAnnotation;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author ForteScarlet
 */
public class TestMain {
    private static AnnotationTool tool;
    private static Method test1;
    private static Method test2;
    private static Method test3;


    @BeforeAll
    public static void before() throws NoSuchMethodException {
        tool = AnnotationTools.getAnnotationTool();
        final Class<TestMain> testMainClass = TestMain.class;
        test1 = testMainClass.getMethod("test1");
        test2 = testMainClass.getMethod("test2");
        test3 = testMainClass.getMethod("test3");
    }

    @Element("A")
    @Element("B")
    @Test
    public void test1() throws ReflectiveOperationException {
        final List<Element> annotations = tool.getAnnotations(test1, Element.class);
        System.out.println(annotations);
        assert annotations.size() == 2;
    }

    @Element("A")
    @Test
    public void test2() throws ReflectiveOperationException {
        final List<Element> annotations = tool.getAnnotations(test2, Element.class);
        System.out.println(annotations);
        assert annotations.size() == 1;
    }

    @Element("A")
    @MyElement(name = "C")
    @Test
    public void test3() throws ReflectiveOperationException {
        final List<Element> annotations = tool.getAnnotations(test3, Element.class);
        System.out.println(annotations);
        assert annotations.size() == 2;
    }


}
