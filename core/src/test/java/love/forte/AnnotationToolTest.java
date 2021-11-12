package love.forte;

import love.forte.annotationtool.core.AnnotationMapper;
import love.forte.annotationtool.core.AnnotationTool;
import love.forte.annotationtool.core.AnnotationToolConfiguration;
import love.forte.annotationtool.core.AnnotationTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author ForteScarlet
 */
@ChildAnno(myAge = "114514", myName = "forte")
public class AnnotationToolTest {

    private static AnnotationTool tool;

    @BeforeAll
    static void init() {
        final HutoolConverters converters = new HutoolConverters();
        final AnnotationToolConfiguration config = new AnnotationToolConfiguration();
        config.setConverters(converters);
        tool = AnnotationTools.getAnnotationTool(config);
    }

    @Test
    public void test1() throws ReflectiveOperationException {

        final Method myName = ChildAnno.class.getMethod("myName");
        final List<AnnotationMapper.Property> properties = tool.getAnnotations(myName, AnnotationMapper.Property.class);

    }

    @Test
    public void test2() throws ReflectiveOperationException {
        final Class<ChildAnno> childAnnoClass = ChildAnno.class;
        final Method myName = childAnnoClass.getMethod("myName");

        final List<AnnotationMapper.Property> properties = tool.getAnnotations(myName, AnnotationMapper.Property.class);

        System.out.println(properties);
        assert properties != null;
        System.out.println("======");
        for (AnnotationMapper.Property property : properties) {
            System.out.println(property);
        }
    }

    @Test
    @Ele("A")
    // @Ele("B")
    // @Ele("C")
    public void test3() throws NoSuchMethodException {
        final Method method = AnnotationToolTest.class.getMethod("test3");
        final ELes eles = method.getDeclaredAnnotation(ELes.class);
        System.out.println(eles);

        final Ele[] eleArray = method.getDeclaredAnnotationsByType(Ele.class);
        System.out.println(Arrays.toString(eleArray));

        final Ele ele = method.getDeclaredAnnotation(Ele.class);
        System.out.println(ele);

    }

    private static long t(Runnable r) {
        final long start = System.nanoTime();
        r.run();
        return System.nanoTime() - start;
    }

}


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@interface ParentAnno {
    String name();
    int age();
}


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AnnotationMapper(ParentAnno.class)
@interface ChildAnno {

    @Ele("ele1")
    @Ele("ele2")
    @MyProp(v = "hi")
    @AnnotationMapper.Property(value = "name")
    String myName();

    @AnnotationMapper.Property(value = "age", target = ParentAnno.class)
    @AnnotationMapper.Property(value = "age")
    String myAge();

}


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationMapper(AnnotationMapper.Property.class)
@interface MyProp {
    @AnnotationMapper.Property("value")
    String v();

}

@Target(ElementType.METHOD)
// @Retention(RetentionPolicy.RUNTIME)
@AnnotationMapper(AnnotationMapper.Property.class)
@Repeatable(ELes.class)
@interface Ele {

    @AnnotationMapper.Property("value")
    String value() default "DDD";

}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface ELes {
    Ele[] value() default {};
}