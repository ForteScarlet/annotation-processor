package examples.metadata;

import love.forte.annotationtool.core.AnnotationMetadata;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Example for {@link love.forte.annotationtool.core.AnnotationMetadata}
 *
 * @author ForteScarlet
 */
class ExampleMain {

    @Test
    public void test1() {
        final AnnotationMetadata<IAmAnnotation> metadata = AnnotationMetadata.resolve(IAmAnnotation.class);

        assert int.class.equals(metadata.getPropertyType("size"));
        assert String.class.equals(metadata.getPropertyType("value"));
    }

    @Test
    public void test2() {
        final AnnotationMetadata<IAmAnnotation> metadata = AnnotationMetadata.resolve(IAmAnnotation.class);

        final Set<String> names = metadata.getPropertyNames();
        final HashSet<String> names0 = new HashSet<>(Arrays.asList("size", "value"));

        assert names.equals(names0);
    }

}
