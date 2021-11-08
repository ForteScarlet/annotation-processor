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
    public void test1() throws NoSuchMethodException {



        final Method myName = ChildAnno.class.getMethod("myName");
        final AnnotationMapper.Properties properties = tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class);

    }

    @Test
    public void test2() throws NoSuchMethodException {
        final Class<ChildAnno> childAnnoClass = ChildAnno.class;
        final Method myName = childAnnoClass.getMethod("myName");

        long n1 = t(() -> tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class));
        long n2 = t(() -> tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class));
        long n3 = t(() -> tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class));
        long n4 = t(() -> tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class));
        long n5 = t(() -> tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class));
        long n6 = t(() -> tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class));

        System.out.println("n1 -> " + n1 + "\tns");
        System.out.println("n2 -> " + n2 + "\tns");
        System.out.println("n3 -> " + n3 + "\tns");
        System.out.println("n4 -> " + n4 + "\tns");
        System.out.println("n5 -> " + n5 + "\tns");
        System.out.println("n6 -> " + n6 + "\tns");

        final AnnotationMapper.Properties properties = tool.getRepeatableAnnotation(myName, AnnotationMapper.Properties.class);

        System.out.println(properties);
        assert properties != null;
        System.out.println("======");
        for (AnnotationMapper.Property property : properties.value()) {
            System.out.println(property);
        }

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
@Retention(RetentionPolicy.RUNTIME)
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