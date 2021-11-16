package examples.annotationTools;

import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationToolConfiguration;
import love.forte.annotationtool.core.AnnotationTools;
import love.forte.annotationtool.core.Converters;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

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
