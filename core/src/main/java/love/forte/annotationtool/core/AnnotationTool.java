package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Annotation tool.
 *
 * @author ForteScarlet
 */
public interface AnnotationTool {

    /**
     * Get annotation instance from {@link AnnotatedElement}. e.g. from {@link Class} or {@link java.lang.reflect.Method}.
     *
     * @param fromElement        annotation fromElement instance.
     * @param annotationType annotation type.
     * @param excludes        excludes annotation class name. They will not be parsing.
     * @return The annotation instance, or null.
     */
    @Nullable <A extends Annotation> A getAnnotation(AnnotatedElement fromElement, Class<A> annotationType, @NotNull Set<String> excludes);


    /**
     * Get annotation instance fromElement {@link AnnotatedElement}. e.g. fromElement {@link Class} or {@link java.lang.reflect.Method}.
     *
     * @see #getAnnotation(AnnotatedElement, Class, Set)
     */
    @Nullable
    default <A extends Annotation> A getAnnotation(AnnotatedElement fromElement, Class<A> annotationType) {
        return getAnnotation(fromElement, annotationType, Collections.emptySet());
    }


    /**
     * Get a repeatable annotation instance from {@link AnnotatedElement}. e.g. from {@link Class} or {@link java.lang.reflect.Method}.
     *
     * @param element        annotation element instance.
     * @param annotationType annotation type.
     * @param excludes        excludes annotation class name. will not be checked.
     * @return The annotation instance, or null.
     */
    @Nullable <A extends Annotation> A getRepeatableAnnotation(AnnotatedElement element, Class<A> annotationType, @NotNull Set<String> excludes);

    /**
     * Get a repeatable annotation instance from {@link AnnotatedElement}. e.g. from {@link Class} or {@link java.lang.reflect.Method}.
     *
     * @param element        annotation element instance.
     * @param annotationType annotation type.
     * @return The annotation instance, or null.
     */
    default @Nullable <A extends Annotation> A getRepeatableAnnotation(AnnotatedElement element, Class<A> annotationType) {
        return getRepeatableAnnotation(element, annotationType, Collections.emptySet());
    }


    /**
     * Get annotation values.
     *
     * @param annotation An annotation instance.
     * @return a mutable map.
     */
    @NotNull
    Map<String, Object> getAnnotationValues(@NotNull Annotation annotation);

    /**
     * Get annotation property names.
     *
     * @param annotation An annotation instance.
     * @return a mutable set.
     */
    @NotNull
    Set<String> getPropertyNames(@NotNull Annotation annotation);


    /**
     * Get annotation type's value types.
     *
     * @param annotationType An annotation instance type.
     * @return a mutable map.
     */
    @NotNull
    Map<String, Class<?>> getAnnotationValueTypes(@NotNull Class<? extends Annotation> annotationType);


    /**
     * Create an annotation proxy instance.
     *
     * @param annotationType annotation type.
     * @param classLoader    classLoader.
     * @param properties     annotation's properties.
     * @param base           base annotation.
     * @return annotation proxy instance.
     */
    @NotNull <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType, @NotNull ClassLoader classLoader, @Nullable Map<String, Object> properties, @Nullable A base);

    /**
     * Create an annotation proxy instance.
     *
     * @param annotationType annotation type.
     * @param classLoader    classLoader.
     * @param properties     annotation's properties.
     * @return annotation proxy instance.
     */
    @NotNull
    default <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType, @NotNull ClassLoader classLoader, @Nullable Map<String, Object> properties) {
        return createAnnotationInstance(annotationType, classLoader, properties, null);
    }


    /**
     * Create an annotation proxy instance.
     *
     * @param annotationType annotation type.
     * @param properties     annotation's properties.
     * @return annotation proxy instance.
     * @see #createAnnotationInstance(Class, ClassLoader, Map)
     */
    @NotNull
    default <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType, @Nullable Map<String, Object> properties) {
        return createAnnotationInstance(annotationType, annotationType.getClassLoader(), properties);
    }

    /**
     * Create an annotation proxy instance.
     *
     * @param annotationType annotation type.
     * @return annotation proxy instance.
     * @see #createAnnotationInstance(Class, ClassLoader, Map)
     */
    @NotNull
    default <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType) {
        return createAnnotationInstance(annotationType, annotationType.getClassLoader(), null);
    }


    /**
     * Clean internal annotation instance cache. (if exists.)
     */
    void clearCache();


}
