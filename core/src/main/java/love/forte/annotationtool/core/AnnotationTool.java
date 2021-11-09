package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Annotation tool interface.
 * <p>
 * The Annotation tool interface provides some abstract methods to define some operations for annotations,
 * such as {@link #getAnnotation(AnnotatedElement, Class, Set) getting an annotation instance},
 * {@link #getProperties(Annotation) getting the properties of an annotation}
 * or {@link #createAnnotationInstance(Class, ClassLoader, Map, Annotation) directly constructing an annotation instance}, etc.
 * <p>
 * You can implement this interface any way you like,
 * but of course, the library provides a default implementation, as well as an implementation of the instance provided by {@link AnnotationTools#getAnnotationTool()}: {@link SimpleAnnotationTool}.
 * <p>
 * Note that all return values in the interface are considered immutable when they are of type {@link java.util.Collection} or {@link Map}.
 * Because they are indeed immutable when they are empty or have only one element. e.g. {@link Collections#emptyMap()} and {@link Collections#emptySet()}.
 *
 * @see SimpleAnnotationTool
 * @see AnnotationTools
 * @author ForteScarlet
 */
public interface AnnotationTool {

    /**
     * Get annotation instance from {@link AnnotatedElement}. e.g. from {@link Class} or {@link java.lang.reflect.Method}.
     *
     * @param fromElement    annotation fromElement instance.
     * @param annotationType annotation type.
     * @param excludes       excludes annotation class name. They will not be parsing.
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
     * @param excludes       excludes annotation class name. will not be checked.
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
     * @return annotation property values. Treat it as <b>immutable</b> plz.
     */
    @NotNull
    Map<String, Object> getAnnotationValues(@NotNull Annotation annotation);

    /**
     * Get annotation property names.
     *
     * @param annotation An annotation instance.
     * @return property name set. Treat it as <b>immutable</b> plz.
     */
    @NotNull
    Set<String> getProperties(@NotNull Annotation annotation);


    /**
     * Get annotation type's value types.
     *
     * @param annotationType An annotation instance type.
     * @return name-type map. Treat it as <b>immutable</b> plz.
     */
    @NotNull
    Map<String, Class<?>> getAnnotationPropertyTypes(@NotNull Class<? extends Annotation> annotationType);


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
