package examples.repeatableAnnotation;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author ForteScarlet
 */
@DefaultElement
@MyElement(name = "forte", length = 20)
@Element(value = "element1", size = 501)
@Element(value = "element2", size = 502)
@Element(value = "element3", size = 503)
class ExampleMain {
    private static final AnnotationTool tool = AnnotationTools.getAnnotationTool();

    @BeforeEach
    public void each() {
        System.out.println();
        System.out.println("=================================");
        System.out.println();
    }

    @Test
    public void test1() throws ReflectiveOperationException {
        final Elements elements = tool.getAnnotation(ExampleMain.class, Elements.class);
        System.out.println(elements);
        assert elements != null;
        assert elements.value().length == 5;
    }

    @Test
    public void test2() throws ReflectiveOperationException {
        final List<Element> elementList = tool.getAnnotations(ExampleMain.class, Element.class);
        assert elementList.size() == 5;
        for (Element element : elementList) {
            System.out.println(element);
        }
    }

}
