package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.Map;
import java.util.Set;

/**
 * Some Annotation metadata.
 *
 * @author ForteScarlet
 */
public interface AnnotationMetadata<A extends Annotation> {

    Class<A> getAnnotationType();

    Set<String> getPropertyNames();

    boolean containsProperty(String name);

    Map<String, Class<?>> getPropertyTypes();

    @Nullable
    Class<?> getPropertyType(String name);

    Map<String, Object> getPropertyDefaultValues();

    @Nullable
    Object getPropertyDefaultValue(String name);

    Map<String, Object> getProperties(A annotation);

    /**
     *
     * @param properties
     * @param annotation
     * @return
     * @throws ReflectiveOperationException ref exception.
     */
    Object getAnnotationValue(@NotNull String properties, @NotNull A annotation) throws ReflectiveOperationException;


    static <A extends Annotation> AnnotationMetadata<A> resolve(@NotNull Class<A> annotationType) {
        return new SimpleAnnotationMetadata<>(annotationType);
    }

}
