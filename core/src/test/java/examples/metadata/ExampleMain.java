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

    @SuppressWarnings("deprecation")
    @Test
    public void test3() {
        final AnnotationMetadata<?> metadata = AnnotationMetadata.resolve(OtherAnnotation.class);
        assert metadata.isDeprecated();
        assert metadata.isDocumented();
        assert metadata.isInherited();
        assert !metadata.isRepeatable();
        assert !metadata.isRepeatableContainer();
    }

    @Test
    public void test4() {
        final AnnotationMetadata<?> elementMetadata = AnnotationMetadata.resolve(Element.class);
        assert elementMetadata.isRepeatable();
        assert !elementMetadata.isRepeatableContainer();

        final AnnotationMetadata<?> elementsMetadata = AnnotationMetadata.resolve(Elements.class);
        assert elementsMetadata.isRepeatable();
        assert elementsMetadata.isRepeatableContainer();

        assert Element.class.equals(elementsMetadata.getRepeatableAnnotationType());
        assert Elements.class.equals(elementMetadata.getRepeatableAnnotationType());
    }

}

