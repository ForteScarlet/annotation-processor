package love.forte.annotationtool.core;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Just simple mapped.
 *
 * @author ForteScarlet
 */
public final class NoConverterAnnotationMappers implements AnnotationMappers /*AnnotationMapper<Annotation, Annotation>*/ {
    private final AnnotationTool tool;

    NoConverterAnnotationMappers(AnnotationTool tool) {
        this.tool = tool;
    }

    public static <F extends Annotation, T extends Annotation> AnnotationMappers createInstance(AnnotationTool tool) {
        return new NoConverterAnnotationMappers(tool);
    }

    public static <F extends Annotation, T extends Annotation> AnnotationMappers createInstance() {
        return createInstance(AnnotationTools.getAnnotationTool());
    }

    @Override
    public <F extends Annotation, T extends Annotation> T map(F fromAnnotation, Class<T> toType, @Nullable T base) {
        final Map<String, Object> annotationValues = tool.getAnnotationValues(fromAnnotation);
        return tool.createAnnotationInstance(toType, toType.getClassLoader(), annotationValues, base);
    }
}
