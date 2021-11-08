package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Some Annotation metadata.
 *
 * TODO 补充注释
 *
 * @author ForteScarlet
 */
@SuppressWarnings("unused")
public interface AnnotationMetadata<A extends Annotation> {
    /**
     * Get annotation type, Just like {@link Annotation#annotationType()}.
     * @return annotation type
     */
    Class<A> getAnnotationType();

    /**
     * Is a repeatable annotation.
     * @return repeatable if true.
     */
    boolean isRepeatable();


    // AnnotationMetadata<?> isRepeatableRootType();


    // AnnotationMetadata<?> isRepeatableSubtype();


    /**
     * Get current annotation type 's all property name.
     * @return property names
     */
    Set<String> getPropertyNames();

    /**
     * Determine whether a certain property exists.
     * @param name property name.
     * @return exists if true
     */
    boolean containsProperty(@NotNull String name);

    /**
     * Get current annotation type 's all properties with their type.
     * @return properties with their type
     */
    Map<String, Class<?>> getPropertyTypes();

    /**
     * Get the type of the property of the specified property name.
     * @param property property name
     * @return property 's type, or null.
     */
    @Nullable
    Class<?> getPropertyType(@NotNull String property);

    /**
     * Get all properties with their default value (if it exists).
     * @return properties with their default value
     */
    Map<String, Object> getPropertyDefaultValues();

    /**
     * Get default value of property of the specified property name.
     * @param property property name
     * @return the default value, or null
     */
    @Nullable
    Object getPropertyDefaultValue(@NotNull String property);

    /**
     * Get properties of specified annotation instance.
     * @param annotation annotation instance.
     * @return properties
     */
    Map<String, Object> getProperties(@NotNull A annotation);

    /**
     * Get property value of specified annotation instance with property name.
     * @param property property name
     * @param annotation annotation instance
     * @return property value, or null
     * @throws ReflectiveOperationException ref exception.
     */
    Object getAnnotationValue(@NotNull String property, @NotNull Annotation annotation) throws ReflectiveOperationException;


    /**
     * Get instance of {@link AnnotationMetadata} by an annotation type.
     * @param annotationType annotation type
     * @return instance of {@link AnnotationMetadata}
     */
    static <A extends Annotation> AnnotationMetadata<A> resolve(@NotNull Class<A> annotationType) {
        return new SimpleAnnotationMetadata<>(annotationType);
    }

}
