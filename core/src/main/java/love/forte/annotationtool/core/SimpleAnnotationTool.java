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

import love.forte.annotationtool.annotation.AnnotationMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * This is the default implementation of the library for the {@link AnnotationTool} and is based on the {@link Proxy JDK Proxy} which implements the functionality required by the {@link AnnotationTool}.
 * <p>
 * This implementation internally caches the final {@link Annotation} instance by default (except for the result of {@link #createAnnotationInstance(Class, ClassLoader, Map, Annotation) createAnnotationInstance(...) } ). In {@link AnnotationTools}, two {@link LinkedHashMap} are used as caches by default.
 * You can change this default by providing another Map, for example using {@link HashMap}.
 *
 * <p>
 * {@link SimpleAnnotationTool} is <b>not thread-safe</b>.
 *
 * @author ForteScarlet
 */
class SimpleAnnotationTool implements AnnotationTool {
    private static final Set<String> EXCLUDE_META_ANNOTATION;
    private final Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap;
    private final Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap;
    private final Converters converters;


    static {
        final HashSet<String> set = new HashSet<>(16);
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

        // this lib's meta-annotation
        set.add(AnnotationMapper.class.getName());
        set.add(AnnotationMapper.Properties.class.getName());
        set.add(AnnotationMapper.Property.class.getName());

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


    /**
     * 获取一个annotation
     * TODO 如果要获取的是可重复注解, 填充子元素
     *
     * @param fromElement    annotation fromElement instance.
     * @param annotationType annotation type.
     * @param excludes       excludes annotation class name. They will not be parsing.
     * @param <A>            annotation type
     * @return annotation instance.
     */
    @Override
    public <A extends Annotation> @Nullable A getAnnotation(AnnotatedElement fromElement, Class<A> annotationType, @Nullable Set<String> excludes) throws ReflectiveOperationException {
        // TODO repeatable parent annotation

        final A gotAnnotationWithoutDeep = getAnnotationDirectly(fromElement, annotationType);
        if (gotAnnotationWithoutDeep != null) {
            return gotAnnotationWithoutDeep;
        }

        final Set<String> realExclude = resolveExclude(excludes);

        Set<String> currentExcludes;

        // Find annotations from this.
        // 这里面已经不可能存在所需注解, 遍历这些注解，将他们作为目标来查询。
        final Annotation[] annotations = fromElement.getAnnotations();

        for (Annotation annotation : annotations) {
            currentExcludes = new HashSet<>(realExclude);
            if (checkExclude(annotation, currentExcludes)) {
                continue;
            }

            final A deepAnnotation = getAnnotationFromAnnotation(annotation, fromElement, annotationType, currentExcludes);
            if (deepAnnotation != null) {
                final A deepAnnotationProxy = checkAnnotationProxy(deepAnnotation);
                saveCache(fromElement, deepAnnotationProxy);
                return deepAnnotationProxy;
            }
        }


        nullCache(fromElement, annotationType);
        return null;
    }


    // private <A extends Annotation> @Nullable A getRepeatableAnnotation(AnnotatedElement fromElement, Class<A> annotationType, @Nullable Set<String> excludes) throws ReflectiveOperationException {
    //     // TODO
    //     return null;
    // }


    // @SuppressWarnings("unchecked")
    // @Nullable
    // private Class<? extends Annotation> getRepeatableChildType(Class<? extends Annotation> annotationType) {
    //     final AnnotationMetadata<? extends Annotation> metadata = AnnotationMetadata.resolve(annotationType);
    //     if (!metadata.isRepeatable()) {
    //         return null;
    //     }
    //
    //     final Class<?> valueType = metadata.getPropertyType("value");
    //     if (valueType == null) {
    //         return null;
    //     }
    //
    //     Class<?> componentType;
    //
    //     if (valueType.isArray() && (componentType = valueType.getComponentType()).isAnnotation()) {
    //         final Repeatable repeatableAnnotation = componentType.getAnnotation(Repeatable.class);
    //         if (repeatableAnnotation != null && repeatableAnnotation.value().equals(annotationType)) {
    //             return (Class<? extends Annotation>) componentType;
    //         }
    //     }
    //
    //     return null;
    // }

    private <A extends Annotation> @Nullable A getAnnotationDirectly(AnnotatedElement fromElement, @NotNull Class<A> annotationType) throws ReflectiveOperationException {
        if (EXCLUDE_META_ANNOTATION.contains(annotationType.getName())) {
            // If you really want to get meta-annotation, just get, but no deep, no proxy, no cache.
            return fromElement.getAnnotation(annotationType);
        }

        if (isNull(fromElement, annotationType)) {
            return null;
        }

        // find cache
        final A cached = getCache(fromElement, annotationType);
        if (cached != null) return cached;

        // Try to get it directly.
        final A directly = fromElement.getAnnotation(annotationType);
        if (directly != null) {
            final AnnotationMetadata<A> metadata = AnnotationMetadata.resolve(annotationType);
            if (metadata.isRepeatableContainer()) {
                // container
                final Class<? extends Annotation> repeatableAnnotationType = metadata.getRepeatableAnnotationType();
                final List<? extends Annotation> children = getAnnotations(fromElement, repeatableAnnotationType);
                final Map<String, Object> properties = new HashMap<>(metadata.getProperties(directly));
                final Object value = properties.get("value");

                Class<?> valueClass;
                if (!children.isEmpty() && value != null && (valueClass = value.getClass()).isArray() && valueClass.getComponentType().equals(repeatableAnnotationType)) {
                    final int size = children.size();
                    Object newArray = Array.newInstance(repeatableAnnotationType, size);
                    for (int i = 0; i < size; i++) {
                        Array.set(newArray, i, children.get(i));
                    }
                    properties.put("value", newArray);
                }
                final A proxiedAnnotation = proxy(annotationType, annotationType.getClassLoader(), directly, properties);
                saveCache(fromElement, proxiedAnnotation);
                return proxiedAnnotation;
            } else {
                // not a repeatable container
                final A resultAnnotation = checkAnnotationProxy(directly);
                saveCache(fromElement, resultAnnotation);
                return resultAnnotation;
            }
        }

        return null;
    }


    /**
     * Get an annotation from other annotation instance.
     */
    private <A extends Annotation> A getAnnotationFromAnnotation(Annotation fromInstance, AnnotatedElement from, Class<A> annotationType, Set<String> excludes) throws ReflectiveOperationException {
        // 首先尝试获取缓存
        A cache = getCache(from, annotationType);
        if (cache != null) {
            return cache;
        }

        if (isNull(from, annotationType)) {
            return null;
        }

        // 先尝试直接获取
        A annotation = from.getAnnotation(annotationType);


        // 如果存在直接返回，否则查询
        if (annotation != null) {
            // 首先将其转化为对应的代理实例
            return checkAnnotationProxy(mapping(fromInstance, annotation));
        }

        // 看看是否存在映射
        final A tryMappedAnnotation = mapping(fromInstance, fromInstance.annotationType(), annotationType);
        if (tryMappedAnnotation != null) {
            return tryMappedAnnotation;
        }


        return getAnnotation(fromInstance.annotationType(), annotationType, excludes);
    }


    /**
     * @param element        annotation element instance. e.g. from {@link Class} or {@link java.lang.reflect.Method}.
     * @param annotationType annotation type. if repeatable, should be the subtype.
     * @param excludes       excludes annotation class name. will not be checked.
     * @param <A>            annotation type
     * @return annotation instances.
     */
    @Override
    public <A extends Annotation> @NotNull List<A> getAnnotations(AnnotatedElement element, Class<A> annotationType, @NotNull Set<String> excludes) throws ReflectiveOperationException {
        if (EXCLUDE_META_ANNOTATION.contains(annotationType.getName())) {
            // If you really want to get meta-annotation, just get, but no deep, no proxy, no cache.
            return Arrays.asList(element.getAnnotationsByType(annotationType));
        }

        // check repeatable
        final Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
        final Class<? extends Annotation> repeatParentType;
        final AnnotationMetadata<? extends Annotation> parentAnnotationType;

        if (repeatable != null) {
            // find cache for parent repeatable type.
            repeatParentType = repeatable.value();
            parentAnnotationType = AnnotationMetadata.resolve(repeatParentType);

            if (isNull(element, repeatParentType)) {
                return emptyList();
            }

            final Annotation cachedRepeatableAnnotation = getCache(element, repeatParentType);
            if (cachedRepeatableAnnotation != null) {
                final List<A> repeatAnnotationValues = getRepeatAnnotationValues(parentAnnotationType, cachedRepeatableAnnotation);
                if (repeatAnnotationValues != null) {
                    return repeatAnnotationValues;
                }
            }

        } else {
            repeatParentType = null;
            parentAnnotationType = null;
        }

        // no cache, or not repeatable annotation.

        List<A> allAnnotations = new ArrayList<>(8);

        Set<String> currentExcludes;

        // find from this annotations
        final Annotation[] annotations = element.getAnnotations();
        for (Annotation annotation : annotations) {
            currentExcludes = new HashSet<>(excludes);
            if (checkExclude(annotation, currentExcludes)) {
                continue;
            }
            if (annotation.annotationType().equals(repeatParentType)) {
                // is parent
                List<A> values = getRepeatAnnotationValues(parentAnnotationType, annotation);
                if (values != null) {
                    for (A value : values) {
                        allAnnotations.add(checkAnnotationProxy(value));
                    }
                }
            } else if (annotation.annotationType().equals(annotationType)) {
                //noinspection unchecked
                allAnnotations.add((A) checkAnnotationProxy(annotation));

            } else {
                // find annotations from annotation
                getAnnotationsFromAnnotation(annotation, annotationType, currentExcludes, allAnnotations);
            }

        }

        return allAnnotations;
    }

    private static <A extends Annotation> List<A> getRepeatAnnotationValues(AnnotationMetadata<?> metadata, Annotation parent) throws ReflectiveOperationException {
        final Object value = metadata.getAnnotationValue("value", parent);
        if (value != null) {
            //noinspection unchecked
            final A[] valueArray = (A[]) value;
            switch (valueArray.length) {
                case 0:
                    return emptyList();
                case 1:
                    return Collections.singletonList(valueArray[0]);
                default:
                    return new ArrayList<>(Arrays.asList(valueArray));
            }
        }
        return null;
    }


    private <A extends Annotation> void getAnnotationsFromAnnotation(Annotation sourceAnnotation, Class<A> targetType, Set<String> excludes, List<A> includeList) throws ReflectiveOperationException {
        // 先看看，这个注解本身是否就是目标
        final A tryMapping = mapping(sourceAnnotation, sourceAnnotation.annotationType(), targetType);
        if (tryMapping != null) {
            // 本身就是目标, 添加后直接返回, 不再深入
            includeList.add(checkAnnotationProxy(tryMapping));
            return;
        }
        // 不是目标，则根据此注解上面的元素再继续找。

        Set<String> currentExcludes;

        for (Annotation annotation : sourceAnnotation.annotationType().getAnnotations()) {
            currentExcludes = new HashSet<>(excludes);
            if (checkExclude(annotation, currentExcludes)) {
                continue;
            }

            getAnnotationsFromAnnotation(annotation, targetType, currentExcludes, includeList);
        }
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
        return (T) this.cacheMap.getOrDefault(from, emptyMap()).get(annotatedType);
    }


    /**
     * 判断是否获取不到
     *
     * @param from          {@link AnnotatedElement}
     * @param annotatedType annotation class
     */
    private <T extends Annotation> boolean isNull(AnnotatedElement from, Class<T> annotatedType) {
        return nullCacheMap.getOrDefault(from, emptySet()).contains(annotatedType);
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
        nullCacheMap.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(annotatedType);
    }


    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <A extends Annotation> Map<String, Object> getAnnotationValues(@NotNull A annotation) throws ReflectiveOperationException {
        if (annotation instanceof Proxy) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return ((AnnotationInvocationHandler) invocationHandler).getMemberValuesMap();
            }

        }
        final AnnotationMetadata<A> metadata = (AnnotationMetadata<A>) AnnotationMetadata.resolve(annotation.annotationType());
        return metadata.getProperties(annotation);
    }


    @Override
    public @NotNull Map<String, Class<?>> getAnnotationPropertyTypes(@NotNull Class<? extends Annotation> annotationType) {
        final AnnotationMetadata<? extends Annotation> metadata = AnnotationMetadata.resolve(annotationType);
        return metadata.getPropertyTypes();
    }


    @Override
    public @NotNull Set<String> getProperties(@NotNull Annotation annotation) throws ReflectiveOperationException {
        return getAnnotationValues(annotation).keySet();
    }


    @Override
    public <A extends Annotation> @NotNull A createAnnotationInstance(@NotNull Class<A> annotationType, @NotNull ClassLoader classLoader, @Nullable Map<String, Object> properties, @Nullable A base) throws ReflectiveOperationException {
        return proxy(annotationType, classLoader, base, properties);
    }


    //region Proxy
    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A proxy(Class<A> annotationType,
                                                  ClassLoader classLoader,
                                                  @Nullable A baseAnnotation,
                                                  @Nullable Map<String, Object> params) throws ReflectiveOperationException {

        return (A) Proxy.newProxyInstance(classLoader,
                new Class[]{annotationType},
                new AnnotationInvocationHandler(annotationType, params, baseAnnotation));
    }


    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A checkAnnotationProxy(A annotation) throws ReflectiveOperationException {

        if (annotation instanceof Proxy) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return annotation;
            }
        }

        return proxy((Class<A>) annotation.annotationType(), annotation.annotationType().getClassLoader(), annotation, emptyMap());
    }
    //endregion


    //region Mapping

    /**
     * Try resolve an annotation to target type.
     *
     * @return target annotation.
     */
    private <F extends Annotation, T extends Annotation> T mapping(F sourceAnnotation, Class<? extends Annotation> sourceAnnotationType, Class<T> targetType) throws ReflectiveOperationException {
        // final Class<? extends Annotation> sourceAnnotationType = sourceAnnotation.annotationType();
        final AnnotationMapper sourceAnnotationMapper = getAnnotation(sourceAnnotationType, AnnotationMapper.class);

        // can not map
        if (sourceAnnotationMapper == null) {
            return null;
        }

        Set<Class<? extends Annotation>> mapperTargets = new HashSet<>(Arrays.asList(sourceAnnotationMapper.value()));
        if (mapperTargets.contains(targetType)) {
            final AnnotationMetadata<T> targetMetadata = AnnotationMetadata.resolve(targetType);
            final Map<String, Class<?>> targetPropertyTypes = targetMetadata.getPropertyTypes();


            // get source values.
            final AnnotationMetadata<? extends Annotation> sourceMetadata = AnnotationMetadata.resolve(sourceAnnotationType);
            final Map<String, Object> sourceAnnotationValues = getAnnotationValues(sourceAnnotation);
            final Map<String, Object> targetValues = new HashMap<>(sourceAnnotationValues.size());


            final Map<String, String> namingMaps = sourceMetadata.getPropertyNamingMaps(targetType);
            namingMaps.forEach((targetKey, sourceKey) -> {
                Object targetValue = converters.convert(sourceAnnotationValues.get(sourceKey), targetPropertyTypes.get(targetKey));
                targetValues.put(targetKey, targetValue);
            });

            return proxy(targetType, targetType.getClassLoader(), null, targetValues);
        }

        // not contains, find from other mapper target.
        for (Class<? extends Annotation> mapperTarget : mapperTargets) {
            final Annotation otherTarget = mapping(sourceAnnotation, sourceAnnotation.annotationType(), mapperTarget);
            if (otherTarget != null) {
                final T findFromOtherMapper = mapping(otherTarget, otherTarget.annotationType(), targetType);
                if (findFromOtherMapper != null) {
                    return findFromOtherMapper;
                }
            }
        }


        return null;
    }


    /**
     * Map <tt>source</tt> to <tt>target</tt>.
     * 其中，<tt>target</tt> 注解实例是从 <tt>source</tt> 注解实例上获取到的。
     * 因此，在提供 <tt>target</tt> 的基础上，通过 <tt>source</tt> 对 <tt>target</tt> 进行映射。
     * 如果无法映射，即既未标注 {@link AnnotationMapper}, 也没有直接标记目标注解，得到null。
     */
    @SuppressWarnings("unchecked")
    private <F extends Annotation, T extends Annotation> T mapping(F source, T target) throws ReflectiveOperationException {
        final Map<String, Object> sourceValues = getAnnotationValues(source);

        final AnnotationMetadata<T> targetMetadata = (AnnotationMetadata<T>) AnnotationMetadata.resolve(target.annotationType());
        final AnnotationMetadata<F> sourceMetadata = (AnnotationMetadata<F>) AnnotationMetadata.resolve(source.annotationType());

        // mappings.
        final Map<String, String> propertyNamingMaps = sourceMetadata.getPropertyNamingMaps(target.annotationType());

        // target values
        final Map<String, Object> targetValues = new HashMap<>(getAnnotationValues(target));

        for (Map.Entry<String, String> entry : propertyNamingMaps.entrySet()) {
            final String sourceValueKey = entry.getKey();
            final String targetValueKey = entry.getValue();

            final Class<?> targetType = targetMetadata.getPropertyType(targetValueKey);
            if (targetType != null) {
                final Object targetValue = converters.convert(sourceValues.get(sourceValueKey), targetType);
                targetValues.put(targetValueKey, targetValue);
            }
        }

        return (T) createAnnotationInstance(target.annotationType(), targetValues);
    }


    @Override
    public void clearCache() {
        cacheMap.clear();
        nullCacheMap.clear();
    }
    //endregion


    private static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    private static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    private static <T> List<T> emptyList() {
        return Collections.emptyList();
    }


    private static Set<String> resolveExclude(@Nullable Set<String> exclude) {
        final Set<String> realExclude = new HashSet<>(EXCLUDE_META_ANNOTATION);
        if (exclude != null) {
            realExclude.addAll(exclude);
        }
        return realExclude;
    }

    private static boolean checkExclude(Annotation annotation, Set<String> exclude) {
        return checkExclude(annotation.annotationType().getName(), exclude);
    }


    private static boolean checkExclude(String name, Set<String> exclude) {
        boolean contains = exclude.contains(name);
        exclude.add(name);
        return contains;
    }


}

