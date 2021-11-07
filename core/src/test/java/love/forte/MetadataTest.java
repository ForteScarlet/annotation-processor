package love.forte;

import love.forte.annotationtool.core.AnnotationMetadata;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author ForteScarlet
 */
public class MetadataTest {

    @Test
    public void test1() {
        final AnnotationMetadata<MyAnnotation> metadata = AnnotationMetadata.resolve(MyAnnotation.class);
        System.out.println(metadata);

        System.out.println(metadata.getPropertyNames());
        System.out.println(metadata.getPropertyDefaultValues());
        System.out.println(metadata.getPropertyTypes());


    }
}
