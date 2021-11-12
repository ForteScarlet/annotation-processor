package love.forte.annotationtool.core;

import jdk.jfr.AnnotationElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.reflect.*;
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


    /**
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
        if (EXCLUDE_META_ANNOTATION.contains(annotationType.getName())) {
            // If you really want to get meta-annotation, just get, but no deep, no proxy.
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
            // proxy, cache, and return
            final A resultAnnotation = checkAnnotationProxy(directly);
            saveCache(fromElement, resultAnnotation);
            return resultAnnotation;
        }

        // get nothing directly


        Set<String> realExclude = resolveExclude(excludes);


        // Find annotations from this.
        // 这里面已经不可能存在所需注解, 遍历这些注解，将他们作为目标来查询。
        final Annotation[] annotations = fromElement.getAnnotations();

        for (Annotation annotation : annotations) {
            if (checkExclude(annotation, realExclude, true)) {
                continue;
            }

            final A deepAnnotation = getAnnotationFromAnnotation(annotation, fromElement, annotationType, realExclude, false);
            if (deepAnnotation != null) {
                final A deepAnnotationProxy = checkAnnotationProxy(deepAnnotation);
                saveCache(fromElement, deepAnnotationProxy);
                return deepAnnotationProxy;
            }
        }


        nullCache(fromElement, annotationType);
        // TODO
        return null;
    }


    /**
     * @param element        annotation element instance. e.g. from {@link Class} or {@link java.lang.reflect.Method}.
     * @param annotationType annotation type. if repeatable, should be the subtype.
     * @param excludes       excludes annotation class name. will not be checked.
     * @param <A>            annotation type
     * @return annotation instances.
     */
    @Override
    public <A extends Annotation> List<A> getAnnotations(AnnotatedElement element, Class<A> annotationType, @NotNull Set<String> excludes) throws ReflectiveOperationException {
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

        List<A> allAnnotations = new ArrayList<>(8);


        if (repeatParentType != null) {
            // yes, repeatable, but no cache, get instance for its parent type.
            final Annotation parentAnnotation = getAnnotation(element, repeatParentType);
            if (parentAnnotation != null) {
                final List<A> values = getRepeatAnnotationValues(parentAnnotationType, parentAnnotation);
                return values == null ? emptyList() : values;
            }
        } else {
            // not repeatable, find all from annotations.
            for (Annotation annotation : annotationType.getAnnotations()) {
                if (checkExclude(annotation, excludes, true)) {
                    continue;
                }
                if (annotation.annotationType().equals(annotationType)) {
                    final A tryMapped = mapping(annotation, annotationType);
                    // try mapping
                    mapping(annotation, annotationType);
                }

                // getAnnotationFromAnnotation(annotation, element, annotationType, )
            }

        }

        // TODO
        return allAnnotations;
        // return repeatableAnnotation;
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

    /**
     * Get an annotation from other annotation instance.
     */
    private <A extends Annotation> A getAnnotationFromAnnotation(Annotation fromInstance, AnnotatedElement from, Class<A> annotationType, Set<String> excludes, boolean saveCache) throws ReflectiveOperationException {
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
            annotation = checkAnnotationProxy(mapping(fromInstance, annotation));
            if (saveCache) {
                saveCache(from, annotation);
            }
            return annotation;
        }


        // annotations
        final Annotation[] annotations = from.getAnnotations();

        // find AnnotateMapper, only get the first
        for (Annotation anno : annotations) {
            if (checkExclude(anno, excludes, true)) {
                continue;
            }

            final A got = getAnnotationFromAnnotation(anno, from, annotationType, excludes, saveCache);
            if (got != null) {

            }
            // }
        }


        // if this annotation annotated @AnnotateMapper, treat it as extendable annotation.
        boolean extendable = false; // annotationType.isAnnotationPresent(AnnotateMapper.class);

        // 获取target注解
        Target target = annotationType.getAnnotation(Target.class);
        // 判断这个注解能否标注在其他注解上，如果不能，则不再深入获取
        if (target != null) {
            for (ElementType elType : target.value()) {
                if (elType == ElementType.TYPE || elType == ElementType.ANNOTATION_TYPE) {
                    extendable = true;
                    break;
                }
            }
        }


        annotation = extendable ? getAnnotationFromArrays(fromInstance, annotations, annotationType, excludes) : null;


        // 如果最终不是null，计入缓存
        if (annotation != null) {
            annotation = mapping(fromInstance, checkAnnotationProxy(annotation));
            if (saveCache) {
                saveCache(from, annotation);
            }
        } else {
            nullCache(from, annotationType);
        }

        return annotation;
    }



    /**
     * 从数组中获取指定注解实例。
     */
    // @SuppressWarnings("unchecked")
    private <A extends Annotation> A getAnnotationFromArrays(@Nullable Annotation from, Annotation[] array, Class<A> annotationType, Set<String> exclude) throws ReflectiveOperationException {
        //先浅查询第一层
        //全部注解
        List<Annotation> annotations = new ArrayList<>(array.length);
        for (Annotation a : array) {
            if (a.annotationType().equals(annotationType) && !checkExclude(a, exclude, true)) {
                annotations.add(from != null ? mapping(from, a) : a);
            }
        }

        if (annotations.isEmpty()) {
            return null;
        }

        for (Annotation annotation : annotations) {
            exclude.add(annotation.annotationType().getName());
        }

        //如果浅层查询还是没有，递归查询

        for (Annotation a : annotations) {
            if (checkExclude(a, exclude, true)) {
                continue;
            }
            A annotationGet = getAnnotationFromAnnotation(a, a.annotationType(), annotationType, exclude, true);
            if (annotationGet != null) {
                return annotationGet;
            }
        }

        //如果还是没有找到，返回null
        return null;
    }


    private <A extends Annotation> List<A> getAnnotationsFromAnnotation(Annotation annotation, AnnotationElement fromElement, Class<A> annotationType, Set<String> excludes) {

        // TODO
        return emptyList();
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
    public @NotNull Map<String, Object> getAnnotationValues(@NotNull Annotation annotation) throws ReflectiveOperationException {
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
    public @NotNull Map<String, Class<?>> getAnnotationPropertyTypes(@NotNull Class<? extends Annotation> annotationType) {
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


    private <F extends Annotation, T extends Annotation> boolean checkMappable(F sourceAnnotation, Class<T> targetType) throws ReflectiveOperationException {
        final Class<? extends Annotation> sourceAnnotationType = sourceAnnotation.annotationType();
        return checkMappable(sourceAnnotationType, targetType);
    }


    private <F extends Annotation, T extends Annotation> boolean checkMappable(Class<F> sourceAnnotationType, Class<T> targetType) throws ReflectiveOperationException {
        final AnnotationMapper sourceAnnotationMapper = getAnnotation(sourceAnnotationType, AnnotationMapper.class);
        if (sourceAnnotationMapper == null) {
            return false;
        }

        for (Class<? extends Annotation> type : sourceAnnotationMapper.value()) {
            if (type.equals(targetType)) return true;
        }

        return false;
    }


    /**
     * @param sourceAnnotation source annotation.
     * @param targetSubType    the repeatable subtype
     * @return is if can
     */
    private <F extends Annotation, T extends Annotation> boolean checkRepeatableMappable(F sourceAnnotation, Class<T> targetSubType) throws ReflectiveOperationException {
        final Class<? extends Annotation> sourceAnnotationType = sourceAnnotation.annotationType();
        final Method value;
        try {
            value = sourceAnnotationType.getMethod("value");
            value.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return false;
        }

        final Class<?> valueReturnType = value.getReturnType();
        if (!valueReturnType.isArray()) return false;

        final Class<?> componentType = valueReturnType.getComponentType();
        if (!componentType.isAnnotation()) return false;

        //noinspection unchecked
        return checkMappable((Class<? extends Annotation>) componentType, targetSubType);
    }


    /**
     * Resolve an annotation to target type.
     *
     * @return target annotation.
     */
    private <F extends Annotation, T extends Annotation> T mapping(F sourceAnnotation, Class<T> targetType) throws ReflectiveOperationException {
        final AnnotationMetadata<T> targetMetadata = AnnotationMetadata.resolve(targetType);
        final Map<String, Class<?>> targetPropertyTypes = targetMetadata.getPropertyTypes();

        // get source values.
        final Map<String, Object> sourceAnnotationValues = getAnnotationValues(sourceAnnotation);
        final Map<String, Object> targetValues = new HashMap<>(sourceAnnotationValues.size());

        final Class<? extends Annotation> sourceAnnotationType = sourceAnnotation.annotationType();
        final AnnotationMapper sourceAnnotationMapper = getAnnotation(sourceAnnotationType, AnnotationMapper.class);
        final int sourceAnnotationMapperTypes = sourceAnnotationMapper == null ? 0 : sourceAnnotationMapper.value().length;

        for (Method method : sourceAnnotationType.getMethods()) {
            String propertyName = method.getName();
            // final AnnotationMapper.Properties propertyMappings = getAnnotations(method, AnnotationMapper.Properties.class);
            final AnnotationMapper.Properties propertyMappings = null; // TODO // getAnnotations(method, AnnotationMapper.Properties.class);
            final Object sourceValue = sourceAnnotationValues.get(propertyName);

            // if mappings not null
            if (propertyMappings != null) {
                final AnnotationMapper.Property[] properties = propertyMappings.value();
                final AnnotationMapper.Property property = Arrays.stream(properties)
                        .filter(p -> sourceAnnotationMapperTypes == 1 || p.target().equals(targetType))
                        .findFirst()
                        .orElse(null);

                if (property != null) {
                    if (sourceValue == null) {
                        continue;
                    }

                    final String valueKey = property.value();
                    putIfPropertyContains(valueKey, sourceValue, targetPropertyTypes, targetValues);

                    continue;
                }
            }

            // mappings was null
            putIfPropertyContains(propertyName, sourceValue, targetPropertyTypes, targetValues);
        }

        return proxy(targetType, targetType.getClassLoader(), null, targetValues);
    }


    /**
     * 将一个可重复注解的收集注解实例的所有子集转化为目标结果. 丢弃掉父注解。
     */
    @SuppressWarnings("unchecked")
    private <A extends Annotation> List<A> mappingRepeatableSub(Annotation sourceParentAnnotation, Class<A> targetSubtype) {
        // 得到子类型.
        final Class<? extends Annotation> annotationType = sourceParentAnnotation.annotationType();
        try {
            final Method value = annotationType.getMethod("value");
            value.setAccessible(true);
            final A[] valueArray = (A[]) value.invoke(sourceParentAnnotation);
            List<A> list = new ArrayList<>(valueArray.length);
            for (A v : valueArray) {
                list.add(mapping(v, targetSubtype));
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    private void putIfPropertyContains(String propertyName, Object sourceValue, Map<String, Class<?>> targetPropertyTypes, Map<String, Object> targetValues) {
        final Class<?> targetPropertyType = targetPropertyTypes.get(propertyName);
        if (targetPropertyType != null) {
            targetValues.put(propertyName, converters.convert(sourceValue, targetPropertyType));
        }
    }


    /**
     * Map <tt>source</tt> to <tt>target</tt>.
     * 其中，<tt>target</tt> 注解实例是从 <tt>source</tt> 注解实例上获取到的。
     * 因此，在提供 <tt>target</tt> 的基础上，通过 <tt>source</tt> 对 <tt>target</tt> 进行映射。
     * 如果无法映射，即既未标注 {@link AnnotationMapper}, 也没有直接标记目标注解，得到null。
     */
    @SuppressWarnings("unchecked")
    private <F extends Annotation, T extends Annotation> T mapping(F source, T target) throws ReflectiveOperationException {
        final Class<F> fromAnnotationType = (Class<F>) source.annotationType();

        // 看看有没有from中的属性里有没有能映射为T的
        final AnnotationMetadata<F> metadata = AnnotationMetadata.resolve(fromAnnotationType);




        Map<String, String> mapper = findPropertyMapper(fromAnnotationType, target.annotationType());
        final Map<String, Object> sourceValues = getAnnotationValues(source);

        final AnnotationMetadata<T> targetMetadata = (AnnotationMetadata<T>) AnnotationMetadata.resolve(target.annotationType());

        final Set<String> propertyNames = targetMetadata.getPropertyNames();


        // todo
        return null;
    }


    /**
     * 尝试得到一个注解上的 {@link AnnotationMapper} 注解。
     */
    private AnnotationMapper getMapper(Class<? extends Annotation> annotationType) throws ReflectiveOperationException {
        return getAnnotation(annotationType, AnnotationMapper.class);
    }


    /**
     * Find {@link AnnotationMapper.Properties} for <tt>targetType</tt>.
     *
     * @param annotationType type
     * @param targetType     target
     * @return property map, or empty map.
     */
    private Map<String, String> findPropertyMapper(Class<? extends Annotation> annotationType, Class<? extends Annotation> targetType) throws ReflectiveOperationException {
        final AnnotationMapper mapper = getMapper(annotationType);
        if (mapper == null) {
            return Collections.emptyMap();
        }
        final Class<? extends Annotation>[] values = mapper.value();
        if (Arrays.stream(values).noneMatch(targetType::equals)) {
            return Collections.emptyMap();
        }

        Map<String, String> properties = new LinkedHashMap<>(8);
        // annotationType.getMethods()


        return Collections.emptyMap();
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

    private static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    private static Object toArray(Class<?> type, @NotNull List<?> list) {
        final Object array = Array.newInstance(type, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }

    private static Set<String> resolveExclude(@Nullable Set<String> exclude) {
        final Set<String> realExclude = new HashSet<>(EXCLUDE_META_ANNOTATION);
        if (exclude != null) {
            realExclude.addAll(exclude);
        }
        return realExclude;
    }

    private static boolean checkExclude(Annotation annotation, Set<String> exclude, boolean orAppend) {
        return checkExclude(annotation.annotationType().getName(), exclude, orAppend);
    }


    private static boolean checkExclude(String name, Set<String> exclude, boolean orAppend) {
        boolean contains = exclude.contains(name);
        if (orAppend) {
            exclude.add(name);
        }
        return contains;
    }


}

