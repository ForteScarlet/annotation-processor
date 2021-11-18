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

package examples.repeatableAnnotation;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
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
