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
