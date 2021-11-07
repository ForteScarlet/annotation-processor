package love.forte.annotationtool.core;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * Annotation Mappers.
 *
 * @author ForteScarlet
 */
public interface AnnotationMappers {
    <F extends Annotation, T extends Annotation> T map(F fromAnnotation, Class<T> toType, @Nullable T base);

    default <F extends Annotation, T extends Annotation> T map(F fromAnnotation, Class<T> toType) {
        return map(fromAnnotation, toType, null);
    }
}
