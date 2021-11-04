package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Simple implementation of {@link AnnotationTool}.
 *
 * {@link SimpleAnnotationTool} is not real thread safe.
 *
 * @author ForteScarlet
 */
class SimpleAnnotationTool implements AnnotationTool {
    private static final Set<String> EXCLUDE_META_ANNOTATION;
    private final Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap;
    private final Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap;
    private final Converters converters;
    private final boolean mixAllRepeatable;


    static {
        final HashSet<String> set = new HashSet<>(4);
        // Java meta-annotation
        set.add("java.lang.annotation.Documented");
        set.add("java.lang.annotation.Retention");
        set.add("java.lang.annotation.Target");
        set.add("java.lang.annotation.Deprecated");
        set.add("java.lang.annotation.Inherited");
        set.add("java.lang.annotation.Repeatable");

        // kotlin meta-annotation
        set.add("kotlin.annotation.Target");
        set.add("kotlin.annotation.Retention");
        set.add("kotlin.annotation.MustBeDocumented");
        set.add("kotlin.annotation.Repeatable");

        EXCLUDE_META_ANNOTATION = set;
    }

    SimpleAnnotationTool(Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap,
                         Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap,
                         Converters converters,
                         boolean mixAllRepeatable
    ) {
        this.cacheMap = cacheMap;
        this.nullCacheMap = nullCacheMap;
        this.mixAllRepeatable = mixAllRepeatable;
        this.converters = converters;
    }


    @Override
    public <A extends Annotation> @Nullable A getAnnotation(AnnotatedElement from, Class<A> annotationType, @NotNull Set<String> exclude) {
        if (EXCLUDE_META_ANNOTATION.contains(annotationType.getName())) {
            // If you really want to get a meta-annotation, just get, but no deep.
            return from.getAnnotation(annotationType);
        }

        if (nullCacheMap.getOrDefault(from, emptySet()).contains(annotationType)) {
            return null;
        }

        // find cache
        final Annotation cached = cacheMap.getOrDefault(from, emptyMap()).get(annotationType);
        if (cached != null) {
            //noinspection unchecked
            return (A) cached;
        }

        Objects.requireNonNull(exclude, "exclude set cannot be null");

        Set<String> realExclude = new HashSet<>(EXCLUDE_META_ANNOTATION);
        realExclude.addAll(exclude);

        // TODO
        return null;
    }


    @Override
    public @NotNull Map<String, Object> getAnnotationValues(@NotNull Annotation annotation) {
        if (annotation instanceof Proxy) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return new LinkedHashMap<>(((AnnotationInvocationHandler) invocationHandler).getMemberValuesMap());
            }

        }

        // Not a proxy, create.
        final Class<? extends Annotation> annotationType = annotation.annotationType();
        final Annotation annotationProxy = proxy(annotationType, annotationType.getClassLoader(), annotation, Collections.emptyMap());

        return getAnnotationValues(annotationProxy);
    }


    @Override
    public <A extends Annotation> @NotNull A createAnnotationInstance(@NotNull Class<A> annotationType, @NotNull ClassLoader classLoader, @Nullable Map<String, Object> properties) {
        return proxy(annotationType, classLoader, null, properties);
    }



    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T proxy(Class<T> annotationType,
                                                 ClassLoader classLoader,
                                                 Annotation baseAnnotation,
                                                 Map<String, Object> params) {

        return (T) Proxy.newProxyInstance(classLoader,
                new Class[]{annotationType},
                new AnnotationInvocationHandler(annotationType, params, baseAnnotation));
    }



    @Override
    public void clearCache() {
        cacheMap.clear();
        nullCacheMap.clear();
    }



    private static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }
    private static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }
}

