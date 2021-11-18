/*
 *  Copyright (c) 2021-2021 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 */

package examples.multipleAnnotation;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author ForteScarlet
 */
class ExampleMain {
    private static AnnotationTool tool;
    private static Method test1;
    private static Method test2;
    private static Method test3;


    @BeforeAll
    public static void before() throws NoSuchMethodException {
        tool = AnnotationTools.getAnnotationTool();
        final Class<ExampleMain> testMainClass = ExampleMain.class;
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
