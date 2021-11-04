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
     * Get annotation instance from an {@link AnnotatedElement}, like {@link Class}.
     *
     * @param exclude exclude annotation class name. will not be edited.
     */
    @Nullable <A extends Annotation> A getAnnotation(AnnotatedElement from, Class<A> annotationType, @NotNull Set<String> exclude);


    
    /**
     * Get annotation instance from an {@link AnnotatedElement}, like {@link Class}.
     * @see #getAnnotation(AnnotatedElement, Class, Set)
     */
    @Nullable
    default <A extends Annotation> A getAnnotation(AnnotatedElement from, Class<A> annotationType) {
        return getAnnotation(from, annotationType, Collections.emptySet());
    }


    /**
     * Get annotation values.
     * @param annotation An annotation instance.
     * @return a mutable map.
     */
    @NotNull
    Map<String, Object> getAnnotationValues(@NotNull Annotation annotation);


    /**
     * Create an annotation proxy instance.
     * @param annotationType annotation type.
     * @param classLoader classLoader.
     * @param properties annotation's properties.
     * @return annotation proxy instance.
     */
    @NotNull
    <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType, @NotNull ClassLoader classLoader, @Nullable Map<String, Object> properties);

    
    /**
     * Create an annotation proxy instance.
     * @param annotationType annotation type.
     * @param properties annotation's properties.
     * @return annotation proxy instance.
     * 
     * @see #createAnnotationInstance(Class, ClassLoader, Map)
     */
    @NotNull
    default <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType, @Nullable Map<String, Object> properties) {
        return createAnnotationInstance(annotationType, annotationType.getClassLoader(), properties);
    }

    /**
     * Create an annotation proxy instance.
     * @param annotationType annotation type.
     * @return annotation proxy instance.
     *
     * @see #createAnnotationInstance(Class, ClassLoader, Map)
     */
    @NotNull
    default <A extends Annotation> A createAnnotationInstance(@NotNull Class<A> annotationType) {
        return createAnnotationInstance(annotationType, annotationType.getClassLoader(), null);
    }



    /**
     * Clean internal annotation instance cache.
     */
    void clearCache();


}
