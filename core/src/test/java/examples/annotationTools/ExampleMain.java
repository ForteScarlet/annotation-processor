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

package examples.annotationTools;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationToolConfiguration;
import love.forte.annotationtool.core.AnnotationTools;
import love.forte.annotationtool.core.Converters;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author ForteScarlet
 */
public class ExampleMain {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test1() {
        final AnnotationTool tool = AnnotationTools.getAnnotationTool();
        final AnnotationTool tool2 = AnnotationTools.getAnnotationTool();
        assert tool != null;
        assert tool == tool2;
    }

    @Test
    public void test2() {
        final AnnotationToolConfiguration config = new AnnotationToolConfiguration();
        // set converters
        config.setConverters(Converters.nonConverters());

        // set mutable cache map.
        config.setCacheMap(new HashMap<>());
        // set mutable cache map.
        config.setNullCacheMap(new LinkedHashMap<>());

        final AnnotationTool tool = AnnotationTools.getAnnotationTool(config);

    }

}
