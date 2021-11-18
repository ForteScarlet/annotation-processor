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

package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
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
     * An {@link AnnotationMetadataFactory} instance.
     * <p>
     * <i>In the future, it may be adjusted to obtain dynamic instances through Service Loader loading or other methods.</i>
     */
    AnnotationMetadataFactory FACTORY = new SimpleCacheableAnnotationMetadataFactory();

    /**
     * Get annotation type, Just like {@link Annotation#annotationType()}.
     *
     * @return annotation type
     */
    Class<A> getAnnotationType();


    /**
     * Get {@link RetentionPolicy} of this annotation.
     *
     * @return {@link RetentionPolicy}
     * @see java.lang.annotation.Retention
     */
    RetentionPolicy getRetention();

    /**
     * Get Set<{@link ElementType}> of this annotation.
     *
     * @return Unmodifiable element types.
     * @see java.lang.annotation.Target
     */
    @Unmodifiable
    Set<ElementType> getTargets();


    /**
     * Detect whether there is a target type.
     *
     * @param type type
     * @return true if contains.
     */
    boolean containsTarget(ElementType type);

    /**
     * Check if {@link java.lang.annotation.Documented} is marked.
     *
     * @return True if marked {@link java.lang.annotation.Documented}
     */
    boolean isDocumented();


    /**
     * Check if {@link java.lang.annotation.Inherited} is marked.
     *
     * @return True if marked {@link java.lang.annotation.Inherited}
     */
    boolean isInherited();

    /**
     * Check if {@link java.lang.Deprecated} is marked.
     *
     * @return True if marked {@link java.lang.Deprecated}
     */
    boolean isDeprecated();


    /**
     * Is a repeatable annotation.
     * <p>
     * return true when:
     * <ul>
     *     <li>If this annotation contains {@link java.lang.annotation.Repeatable}</li>
     *     <li>
     *          If the current annotation has a <tt>value</tt> property of the {@link Annotation annotation array} type,
     *          and there is a <tt>@Repeatable</tt> annotation on this annotation type,
     *          and the value of its value is equal to the current annotation
     *     </li>
     * </ul>
     * <p>
     * e.g.:
     * <pre>
     * <code>
     *
     * <tt>@</tt>Target(ElementType.METHOD)
     * <tt>@</tt>Retention(RetentionPolicy.RUNTIME)
     * <tt>@</tt>@interface Elements {
     *      Element[] value();
     * }
     *
     * <tt>@</tt>Target(ElementType.METHOD)
     * <tt>@</tt>Retention(RetentionPolicy.RUNTIME)
     * <tt>@</tt>Repeatable(Elements.class)
     * <tt>@</tt>interface Element {
     *      // ...
     * }
     *
     * </code>
     * <pre/>
     * Both <tt>Element</tt> and <tt>Elements</tt> will be considered repeatable.
     *
     * @return repeatable if true.
     */
    boolean isRepeatable();

    /**
     * From the example of {@link #isRepeatable()}, if the annotation is <tt>Elements<tt>, return true.
     * @return True if the current type is a container in a repeatable annotation.
     */
    boolean isRepeatableContainer();

    /**
     * if {@link #isRepeatable()} return true, this will get the type of repeatable child, or the type of repeat <tt>value</tt>.
     *
     * @return type of repeatable, or null.
     */
    @Nullable
    Class<? extends Annotation> getRepeatableAnnotationType();


    /**
     * Get current annotation type 's all property name.
     *
     * @return property names.
     */
    @Unmodifiable
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
    @Unmodifiable
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
     * @return properties with their default value.
     */
    @Unmodifiable
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
     * @return properties.
     */
    @Unmodifiable
    Map<String, Object> getProperties(@NotNull A annotation) throws ReflectiveOperationException;

    /**
     * Get the property mappings for target type. The key is <tt>targetType</tt>'s property name, value is current type's property.
     *
     * @param targetType mappings target.
     * @return mappings
     */
    @Unmodifiable
    Map<String, String> getPropertyNamingMaps(Class<? extends Annotation> targetType);


    /**
     * Get the property mapping for target type.
     *
     * @param targetType         mapping target.
     * @param targetPropertyName property name of current.
     * @return target name, or null.
     */
    @Nullable
    String getPropertyNamingMap(Class<? extends Annotation> targetType, String targetPropertyName);


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
