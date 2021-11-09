package love.forte.annotationtool.core;

import java.lang.annotation.Annotation;

/**
 *
 * The factory of {@link AnnotationMetadata}.
 *
 * @author ForteScarlet
 */
public interface AnnotationMetadataFactory {

    /**
     * Get an annotation metadata by annotation type.
     * @param annotationType annotation type.
     * @return {@link AnnotationMetadata}
     */
    <A extends Annotation> AnnotationMetadata<A> getAnnotationMetadata(Class<A> annotationType);
}
