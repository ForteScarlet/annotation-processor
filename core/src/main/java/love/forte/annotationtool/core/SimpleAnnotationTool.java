package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Simple implementation of {@link AnnotationTool}.
 * <p>
 * {@link SimpleAnnotationTool} is thread unsafe.
 *
 * @author ForteScarlet
 */
class SimpleAnnotationTool implements AnnotationTool {
    private static final Set<String> EXCLUDE_META_ANNOTATION;
    private final Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap;
    private final Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap;
    private final Converters converters;


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
                         Converters converters
    ) {
        this.cacheMap = cacheMap;
        this.nullCacheMap = nullCacheMap;
        this.converters = converters;
    }


    @Override
    public <A extends Annotation> @Nullable A getAnnotation(AnnotatedElement from, Class<A> annotationType, @NotNull Set<String> exclude) {
        if (EXCLUDE_META_ANNOTATION.contains(annotationType.getName())) {
            // If you really want to get meta-annotation, just get, but no deep.
            return from.getAnnotation(annotationType);
        }

        if (isNull(from, annotationType)) {
            return null;
        }

        // find cache
        final A cached = getCache(from, annotationType);
        if (cached != null) return cached;

        Objects.requireNonNull(exclude, "exclude set cannot be null");


        Set<String> realExclude = new HashSet<>(EXCLUDE_META_ANNOTATION);
        realExclude.addAll(exclude);

        // TODO
        return null;
    }


    // private


    /**
     * 从某个类上获取注解对象。注解可以深度递归。
     * <p>
     * 如果存在多个继承注解，则优先获取浅层第一个注解，如果浅层不存在，则返回第一个获取到的注解。
     * <p>
     * 请尽可能保证仅存在一个或者一种继承注解，否则获取到的类型将不可控。
     *
     * @return 获取到的第一个注解对象
     */
    private <A extends Annotation> A getAnnotation(Annotation fromInstance, AnnotatedElement from, Class<A> annotationType, Set<String> exclude) {
        // 首先尝试获取缓存
        A cache = getCache(from, annotationType);
        if (cache != null) {
            return cache;
        }


        if (isNull(from, annotationType)) {
            return null;
        }

        //先尝试直接获取
        A annotation = from.getAnnotation(annotationType);


        Class<? extends Annotation> childrenValueAnnotateType;
        //如果存在直接返回，否则查询
        if (annotation != null) {
            // 首先将其转化为对应的代理实例
            annotation = checkAnnotationProxy(annotation);

            // return mappingAndSaveCache(fromInstance, from, annotation);
            return null; // TODO
        }


        // 获取target注解
        Target target = annotationType.getAnnotation(Target.class);
        // 判断这个注解能否标注在其他注解上，如果不能，则不再深入获取
        boolean annotateAble = false;
        if (target != null) {
            for (ElementType elType : target.value()) {
                if (elType == ElementType.TYPE || elType == ElementType.ANNOTATION_TYPE) {
                    annotateAble = true;
                    break;
                }
            }
        }

        Annotation[] annotations = from.getAnnotations();
        annotation = annotateAble ? getAnnotationFromArrays(fromInstance, annotations, annotationType, exclude) : null;


        // 如果最终不是null，计入缓存
        if (annotation != null) {
            annotation = null; // TODO mappingAndSaveCache(fromInstance, from, annotation);
        } else {
            nullCache(from, annotationType);
        }

        return annotation;
    }


    /**
     * 从数组中获取指定注解实例。
     */
    // @SuppressWarnings("unchecked")
    private <A extends Annotation> A getAnnotationFromArrays(@Nullable Annotation from, Annotation[] array, Class<A> annotationType, Set<String> exclude) {
        //先浅查询第一层
        //全部注解
        Annotation[] annotations = Arrays.stream(array)
                .filter(a -> !exclude.contains(a.annotationType().getName()))
                //如果此注解的类型就是我要的，直接放过
                .filter(a -> a.annotationType().equals(annotationType))
                .peek(a -> {
                    if (from != null) {
                        mapping(from, a);
                    }
                }).toArray(Annotation[]::new);


        if (annotations.length == 0) {
            return null;
        }

        for (Annotation annotation : annotations) {
            exclude.add(annotation.annotationType().getName());
        }

        //如果浅层查询还是没有，递归查询

        for (Annotation a : annotations) {
            A annotationGet = getAnnotation(a, a.annotationType(), annotationType, exclude);
            if (annotationGet != null) {
                return annotationGet;
            }
        }

        //如果还是没有找到，返回null
        return null;
    }


    /**
     * 从缓存中获取缓存注解
     *
     * @param from          来源
     * @param annotatedType 注解类型
     * @return 注解缓存，可能为null
     */
    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getCache(AnnotatedElement from, Class<T> annotatedType) {
        Map<Class<? extends Annotation>, Annotation> cacheMap = this.cacheMap.get(from);
        if (cacheMap != null) {
            return (T) cacheMap.get(annotatedType);
        }
        return null;
    }


    /**
     * 判断是否获取不到
     *
     * @param from          {@link AnnotatedElement}
     * @param annotatedType annotation class
     */
    private <T extends Annotation> boolean isNull(AnnotatedElement from, Class<T> annotatedType) {
        final Set<Class<? extends Annotation>> classes = nullCacheMap.get(from);
        if (classes == null || classes.isEmpty()) {
            return false;
        }
        return classes.contains(annotatedType);
    }

    /**
     * 记录一条缓存记录。
     * <p>
     * thread unsafe.
     */
    private void saveCache(AnnotatedElement from, Annotation annotation) {
        final Map<Class<? extends Annotation>, Annotation> cacheMap = this.cacheMap.computeIfAbsent(from, k -> new LinkedHashMap<>());
        cacheMap.put(annotation.annotationType(), annotation);
    }


    /**
     * 记录一个得不到的缓存
     * <p>
     * thread unsafe.
     *
     * @param from          {@link AnnotatedElement}
     * @param annotatedType annotation class
     */
    private <T extends Annotation> void nullCache(AnnotatedElement from, Class<T> annotatedType) {
        final Set<Class<? extends Annotation>> classes = nullCacheMap.computeIfAbsent(from, k -> new LinkedHashSet<>());
        classes.add(annotatedType);
    }


    @Override
    public @NotNull Map<String, Object> getAnnotationValues(@NotNull Annotation annotation) {
        if (annotation instanceof Proxy) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return ((AnnotationInvocationHandler) invocationHandler).getMemberValuesMap();
            }

        }
        final Class<? extends Annotation> annotationType = annotation.annotationType();
        final AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annotationType, annotation);

        return handler.getMemberValuesMap();
    }


    @Override
    public @NotNull Map<String, Class<?>> getAnnotationValueTypes(@NotNull Class<? extends Annotation> annotationType) {
        final LinkedHashMap<String, Class<?>> newMemberValues = new LinkedHashMap<>();
        final Method[] methods = annotationType.getMethods();

        for (Method method : methods) {
            final String name = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            final Class<?> returnType = method.getReturnType();

            if (parameterTypes.length > 0) {
                continue;
            }
            if ("toString".equals(name) || "hashCode".equals(name) || "annotationType".equals(name)) {
                continue;
            }

            // instance null
            newMemberValues.putIfAbsent(name, returnType);
        }


        return newMemberValues;
    }


    @Override
    public @NotNull Set<String> getPropertyNames(@NotNull Annotation annotation) {
        return getAnnotationValues(annotation).keySet();
    }


    @Override
    public <A extends Annotation> @NotNull A createAnnotationInstance(@NotNull Class<A> annotationType, @NotNull ClassLoader classLoader, @Nullable Map<String, Object> properties, @Nullable A base) {
        return proxy(annotationType, classLoader, base, properties);
    }


    //region Proxy
    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A proxy(Class<A> annotationType,
                                                  ClassLoader classLoader,
                                                  A baseAnnotation,
                                                  Map<String, Object> params) {

        return (A) Proxy.newProxyInstance(classLoader,
                new Class[]{annotationType},
                new AnnotationInvocationHandler(annotationType, params, baseAnnotation));
    }


    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A checkAnnotationProxy(A annotation) {

        if (annotation instanceof Proxy) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return annotation;
            }
        }

        return (A) proxy((Class<A>) annotation.annotationType(), annotation.annotationType().getClassLoader(), annotation, emptyMap());
    }
    //endregion


    private <F extends Annotation, T extends Annotation> T mapping(F from, T to) {
        AnnotationMappers mappers = null;
        final Class<? extends Annotation> fromAnnotationType = from.annotationType();
        final AnnotateMapper annotateMapper = getAnnotation(fromAnnotationType, AnnotateMapper.class);
        if (annotateMapper != null) {
            // find mapper
            final AnnotateMapper.Type[] mappings = annotateMapper.value();
            for (AnnotateMapper.Type typeMapper : mappings) {
                if (typeMapper.value().equals(fromAnnotationType)) {

                    break;
                }
            }

            if (mappers == null) {
                mappers = NoConverterAnnotationMappers.createInstance(this);
            }

        } else {

            mappers = NoConverterAnnotationMappers.createInstance(this);

        }

        // todo
        return null;
    }

    /**
     * 执行注解映射
     */
    private static <FROM extends Annotation, TO extends Annotation> TO mapping(FROM from, TO to, AnnotationMapper<FROM, TO> mapper) {
        return mapper.map(from);
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

