package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Some Annotation metadata.
 *
 * @author ForteScarlet
 */
@SuppressWarnings("unused")
public interface AnnotationMetadata<A extends Annotation> {
    /**
     *
     * TODO 补充注释。
     *
     */
    AnnotationMetadataFactory FACTORY = new SimpleCacheableAnnotationMetadataFactory();

    /**
     * Get annotation type, Just like {@link Annotation#annotationType()}.
     *
     * @return annotation type
     */
    Class<A> getAnnotationType();

    /**
     * Is a repeatable annotation.
     *
     * @return repeatable if true.
     */
    boolean isRepeatable();


    /**
     * Get current annotation type 's all property name.
     *
     * @return property names. Treat it as <b>immutable</b> plz.
     */
    Set<String> getPropertyNames();

    /**
     * Determine whether a certain property exists.
     *
     * @param name property name.
     * @return exists if true
     */
    boolean containsProperty(@NotNull String name);

    /**
     * Get current annotation type 's all properties with their type.
     *
     * @return properties with their type
     */
    Map<String, Class<?>> getPropertyTypes();

    /**
     * Get the type of the property of the specified property name.
     *
     * @param property property name
     * @return property 's type, or null.
     */
    @Nullable
    Class<?> getPropertyType(@NotNull String property);

    /**
     * Get all properties with their default value (if it exists).
     *
     * @return properties with their default value. Treat it as <b>immutable</b> plz.
     */
    Map<String, Object> getPropertyDefaultValues();

    /**
     * Get default value of property of the specified property name.
     *
     * @param property property name
     * @return the default value, or null
     */
    @Nullable
    Object getPropertyDefaultValue(@NotNull String property);

    /**
     * Get properties of specified annotation instance.
     *
     * @param annotation annotation instance.
     * @return properties. Treat it as <b>immutable</b> plz.
     */
    Map<String, Object> getProperties(@NotNull A annotation);

    /**
     * Get property value of specified annotation instance with property name.
     *
     * @param property   property name
     * @param annotation annotation instance
     * @return property value, or null
     * @throws ReflectiveOperationException ref exception.
     */
    @Nullable
    Object getAnnotationValue(@NotNull String property, @NotNull Annotation annotation) throws ReflectiveOperationException;


    /**
     * Get properties naming map for target annotation type.
     *
     * @param targetAnnotationType target type
     * @return properties naming map, or <b>Immutable</b> empty map.
     */
    Map<String, String> getPropertiesNamingMap(Class<? extends Annotation> targetAnnotationType);


    /**
     * Get instance of {@link AnnotationMetadata} by an annotation type.
     *
     * @param annotationType annotation type
     * @return instance of {@link AnnotationMetadata}
     */
    static <A extends Annotation> AnnotationMetadata<A> resolve(@NotNull Class<A> annotationType) {
        return FACTORY.getAnnotationMetadata(annotationType);
    }

}


class SimpleCacheableAnnotationMetadataFactory implements AnnotationMetadataFactory {
    SimpleCacheableAnnotationMetadataFactory() {
    }

    private final WeakHashMap<Class<? extends Annotation>, AnnotationMetadata<?>> cache = new WeakHashMap<>();

    @SuppressWarnings("unchecked")
    private <A extends Annotation> AnnotationMetadata<A> get(Class<A> annotationType) {
        return (AnnotationMetadata<A>) cache.get(annotationType);
    }

    private <A extends Annotation> AnnotationMetadata<A> put(Class<A> annotationType) {
        final SimpleAnnotationMetadata<A> metadata = new SimpleAnnotationMetadata<>(annotationType);
        cache.put(annotationType, metadata);
        return metadata;
    }

    @Override
    public <A extends Annotation> AnnotationMetadata<A> getAnnotationMetadata(Class<A> annotationType) {
        final AnnotationMetadata<A> metadata = get(annotationType);
        if (metadata == null) {
            synchronized (this) {
                final AnnotationMetadata<A> metadataAgain = get(annotationType);
                if (metadataAgain == null) {
                    return put(annotationType);
                }
                return metadataAgain;
            }
        }
        return metadata;
    }
}
