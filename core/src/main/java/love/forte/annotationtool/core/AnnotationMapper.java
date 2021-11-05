package love.forte.annotationtool.core;

import java.lang.annotation.Annotation;

/**
 * Annotation properties mapper.
 * <p>
 * annotation type of {@link FROM 'from'} -> An annotation properties for type {@link TO 'to'} by {@link AnnotationMapper#map(Annotation)} -> Annotation type of {@link TO 'to'}.
 *
 * @author ForteScarlet
 */
public interface AnnotationMapper<FROM extends Annotation, TO extends Annotation> {

    /**
     * get mapped annotation properties.
     *
     * @param annotation from annotation
     * @return mapped annotation instance.
     */
    TO map(FROM annotation);

}
