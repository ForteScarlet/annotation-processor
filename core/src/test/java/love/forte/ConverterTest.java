package love.forte;

import cn.hutool.core.convert.Convert;
import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationTools;
import love.forte.annotationtool.core.Converters;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.util.Collections;
import java.util.Map;


/**
 * @author ForteScarlet
 */
@MyAnnotation(age = 114)
public class ConverterTest {

    @Test
    public void test1() {
        Converters converters = new HutoolConverters();
        final Integer convert = converters.convert("123", int.class);
        assert convert != null;
        assert 124 == convert + 1;
    }


    @Test
    public void test3() throws ReflectiveOperationException {
        final AnnotationTool annotationTool = AnnotationTools.getAnnotationTool();
        final Map<String, Object> pm = Collections.singletonMap("age", 1);
        final MyAnnotation annotationInstance = annotationTool.createAnnotationInstance(MyAnnotation.class, pm);

        assert annotationInstance.age() == 1;

        final Map<String, Object> annotationValues = annotationTool.getAnnotationValues(annotationInstance);
        System.out.println(annotationValues);

        final MyAnnotation annotation2 = ConverterTest.class.getAnnotation(MyAnnotation.class);
        final Map<String, Object> values2 = annotationTool.getAnnotationValues(annotation2);
        System.out.println(values2);
    }


}


class HutoolConverters implements Converters {

    @Override
    public <FROM, TO> TO convert(Class<FROM> from, @NotNull FROM instance, @NotNull Class<TO> to) {
        return Convert.convert(to, instance);
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
    String name() default "forte";
    int age();
}