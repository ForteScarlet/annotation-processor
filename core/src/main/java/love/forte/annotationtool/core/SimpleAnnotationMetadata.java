/*
 *  Copyright (c) 2021-2021 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   除非适用法律要求或书面同意，根据许可证分发的软件
 *   许可证下分发的软件是以 "原样" 为基础的。
 *   没有任何形式的保证或条件，无论是明示还是暗示。
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 *   许可证下的权限和限制。
 */

package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Simple implement for {@link AnnotationMetadata}
 *
 * @author ForteScarlet
 */
final class SimpleAnnotationMetadata<A extends Annotation> implements AnnotationMetadata<A>, Serializable {

    private WeakReference<Class<A>> annotationType;
    private final String annotationTypeName;
    private transient final WeakReference<Class<?>> repeatableAnnotationType;
    private transient final String repeatableAnnotationTypeName;
    private transient final RetentionPolicy retentionPolicy;
    private transient final Set<ElementType> targets;
    // repeatable-container | repeatable | deprecated | inherited | documented
    private transient final byte marks;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private transient final Map<String, Class<?>> propertyTypes;
    private transient final Map<String, Object> propertyDefaults;
    private transient final Map<String, Method> methods;
    private transient final Map<Class<? extends Annotation>, Map<String, String>> namingMaps;

    SimpleAnnotationMetadata(Class<A> annotationType) {
        this.annotationType = new WeakReference<>(annotationType);
        this.annotationTypeName = annotationType.getName();
        final Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
        boolean rpa = repeatable != null;
        boolean rpaCon = false;
        Class<?> repeatableType = null;
        if (repeatable != null) {
            repeatableType = repeatable.value();
        }

        retentionPolicy = getRetentionPolicy0(annotationType);
        this.targets = getTargets0(annotationType);

        final Method[] methods = annotationType.getMethods();
        this.methods = new LinkedHashMap<>(methods.length);
        Map<String, Class<?>> propertyTypes = new LinkedHashMap<>(methods.length);
        Map<String, Object> propertyDefaults = new LinkedHashMap<>();
        Map<Class<? extends Annotation>, Map<String, String>> namingMaps = new LinkedHashMap<>(methods.length);



        for (Method method : methods) {
            final String name = method.getName();
            final Class<?> returnType = method.getReturnType();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 0) {
                continue;
            }
            if ("toString".equals(name) || "hashCode".equals(name) || "annotationType".equals(name)) {
                continue;
            }

            if (!rpa) {
                if ("value".equals(name) && returnType.isArray()) {
                    // check repeatable
                    final Class<?> componentType = returnType.getComponentType();
                    if (componentType.isAnnotation()) {
                        final Repeatable repeatableAnnotation = componentType.getAnnotation(Repeatable.class);
                        if (repeatableAnnotation != null && repeatableAnnotation.value().equals(annotationType)) {
                            repeatableType = componentType;
                            rpa = true;
                            rpaCon = true;
                        }
                    }
                }
            }


            propertyTypes.put(name, returnType);
            final Object defaultValue = method.getDefaultValue();
            if (defaultValue != null) {
                propertyDefaults.put(name, defaultValue);
            }

            method.setAccessible(true);
            this.methods.put(name, method);
            final AnnotationMapper mapper = annotationType.getAnnotation(AnnotationMapper.class);
            final Class<? extends Annotation> defaultMapType;
            if (mapper == null) {
                defaultMapType = null;
            } else {
                final Class<? extends Annotation>[] values = mapper.value();
                if (values.length != 1) {
                    defaultMapType = null;
                } else {
                    defaultMapType = values[0];
                }
            }

            // namingMap
            resolveNamingMaps(method, defaultMapType, namingMaps);
        }

        this.repeatableAnnotationType = repeatableType == null ? null : new WeakReference<>(repeatableType);
        this.repeatableAnnotationTypeName = repeatableType == null ? null : repeatableType.getName();
        this.marks = getMarks0(annotationType, rpa, rpaCon);


        this.propertyTypes = toUnmodifiable(propertyTypes);
        this.propertyDefaults = toUnmodifiable(propertyDefaults);
        this.namingMaps = toUnmodifiable(namingMaps);
    }


    //region Init functions
    private static <K, V> Map<K, V> toUnmodifiable(Map<K, V> map) {
        switch (map.size()) {
            case 0:
                return Collections.emptyMap();
            case 1:
                final Map.Entry<K, V> first = map.entrySet().iterator().next();
                return Collections.singletonMap(first.getKey(), first.getValue());
            default:
                return map;
        }
    }

    private static void resolveNamingMaps(Method method, Class<? extends Annotation> defaultMapType, Map<Class<? extends Annotation>, Map<String, String>> namingMaps) {
        String name = method.getName();
        final AnnotationMapper.Property[] properties = method.getAnnotationsByType(AnnotationMapper.Property.class);
        if (properties != null && properties.length > 0) {
            for (AnnotationMapper.Property property : properties) {
                Class<? extends Annotation> target = property.target();
                if (target == null) {
                    if (defaultMapType != null) {
                        target = defaultMapType;
                    } else { // 无法确定属性的默认映射目标
                        throw new IllegalStateException("Unable to determine the default mapping target of the property.");
                    }
                }
                String targetName = property.value();
                namingMaps.computeIfAbsent(target, k -> new LinkedHashMap<>()).merge(targetName, name, (v1, v2) -> {
                    throw new IllegalStateException("Duplicate mapping target: " + v1 + " -> " + targetName + " vs " + v2 + " -> " + targetName);
                });
            }
        }
    }

    private static RetentionPolicy getRetentionPolicy0(Class<? extends Annotation> annotationType) {
        final Retention retention = annotationType.getAnnotation(Retention.class);
        if (retention != null) {
            return retention.value();
        } else {
            // the default value
            return RetentionPolicy.CLASS;
        }
    }


    private static Set<ElementType> getTargets0(Class<? extends Annotation> annotationType) {
        final Target targetAnnotation = annotationType.getAnnotation(Target.class);
        Set<ElementType> targets;
        if (targetAnnotation != null) {
            final ElementType[] elements = targetAnnotation.value();
            switch (elements.length) {
                case 0:
                    targets = Collections.emptySet();
                    break;
                case 1:
                    targets = Collections.singleton(elements[0]);
                    break;
                default:
                    targets = new HashSet<>(Arrays.asList(elements));
            }
        } else {
            targets = Collections.emptySet();
        }
        return targets;
    }

    private static byte getMarks0(Class<? extends Annotation> annotationType, boolean repeatable, boolean repeatableContainer) {
        byte marks = 0;
        if (annotationType.isAnnotationPresent(Documented.class)) {
            marks |= 1;
        }
        if (annotationType.isAnnotationPresent(Inherited.class)) {
            marks |= 2;
        }
        if (annotationType.isAnnotationPresent(Deprecated.class)) {
            marks |= 4;
        }
        if (repeatable) {
            marks |= 8;
        }
        if (repeatableContainer) {
            marks |= 16;
        }
        return marks;
    }
    //endregion


    @Override
    public boolean isDocumented() {
        return (marks & 1) != 0;
    }

    @Override
    public boolean isInherited() {
        return (marks & 2) != 0;
    }

    @Override
    public boolean isDeprecated() {
        return (marks & 4) != 0;
    }

    @Override
    public boolean isRepeatable() {
        return (marks & 8) != 0;
    }

    @Override
    public boolean isRepeatableContainer() {
        return (marks & 16) != 0;
    }

    @Override
    public RetentionPolicy getRetention() {
        return retentionPolicy;
    }


    @Override
    public @Unmodifiable Set<ElementType> getTargets() {
        switch (targets.size()) {
            case 0:
            case 1:
                return targets;
            default:
                return new HashSet<>(targets);
        }
    }

    @Override
    public boolean containsTarget(ElementType type) {
        return targets.contains(type);
    }

    @Nullable
    private Method getMethod(String name) {
        final Method method = methods.get(name);
        if (method != null) {
            return method;
        }

        try {
            final Method gotMethod = getAnnotationType().getMethod(name);
            gotMethod.setAccessible(true);
            methods.put(name, gotMethod);
            return gotMethod;
        } catch (NoSuchMethodException e) {
            return null;
        }

    }


    @Override
    public @Nullable Class<?> getRepeatableAnnotationType() {
        if (repeatableAnnotationType == null) {
            return null;
        }
        final Class<?> type = repeatableAnnotationType.get();
        if (type == null) {
            throw new IllegalStateException(new ClassNotFoundException("annotation repeatable type class(" + repeatableAnnotationTypeName + ") has been recycled."));
        }
        return type;
    }

    @Override
    public Class<A> getAnnotationType() {
        if (annotationType == null) {
            throw new IllegalStateException(new ClassNotFoundException("annotation type class(" + annotationTypeName + ") has been recycled."));
        }
        final Class<A> got = annotationType.get();
        if (got == null) {
            annotationType = null;
            throw new IllegalStateException(new ClassNotFoundException("annotation type class(" + annotationTypeName + ") has been recycled."));
        }
        return got;
    }

    @Override
    public Set<String> getPropertyNames() {
        return new LinkedHashSet<>(propertyTypes.keySet());
    }

    @Override
    public boolean containsProperty(@NotNull String name) {
        Objects.requireNonNull(name, "name should not be null");

        return propertyTypes.containsKey(name);
    }

    @Override
    public Map<String, Class<?>> getPropertyTypes() {
        return new LinkedHashMap<>(propertyTypes);
    }


    @Override
    public @Nullable Class<?> getPropertyType(@NotNull String property) {
        Objects.requireNonNull(property, "name should not be null");

        return propertyTypes.get(property);
    }

    @Override
    public Map<String, Object> getPropertyDefaultValues() {
        return new LinkedHashMap<>(propertyDefaults);
    }

    @Override
    public @Nullable Object getPropertyDefaultValue(@NotNull String property) {
        Objects.requireNonNull(property, "name should not be null");

        final Object def = propertyDefaults.get(property);
        if (def != null && def.getClass().isArray()) {
            return ArrayUtil.cloneArray(def);
        }

        return def;
    }


    @Override
    public Object getAnnotationValue(@NotNull String property, @NotNull Annotation annotation) throws InvocationTargetException, IllegalAccessException {
        Objects.requireNonNull(property, "properties should not be null");
        Objects.requireNonNull(annotation, "annotation should not be null");

        final InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        if (handler instanceof AnnotationInvocationHandler) {
            return ((AnnotationInvocationHandler) handler).get(property);
        }

        final Object defaultValue = getPropertyDefaultValue(property);
        final Method method = getMethod(property);
        if (method == null) {
            return defaultValue; // Basically, here is null.
        }

        return method.invoke(annotation);
    }

    @Override
    public Map<String, Object> getProperties(@NotNull A annotation) throws ReflectiveOperationException {
        Objects.requireNonNull(annotation, "annotation should not be null");

        if (Proxy.isProxyClass(annotation.getClass())) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return ((AnnotationInvocationHandler) invocationHandler).getMemberValuesMap();
            }
        }

        return getMemberValues(annotation);
    }


    private Map<String, Object> getMemberValues(@NotNull Annotation instance) throws ReflectiveOperationException {
        final Set<String> names = getPropertyNames();
        if (names.isEmpty()) {
            return Collections.emptyMap();
        }

        final LinkedHashMap<String, Object> memberValues = new LinkedHashMap<>(names.size());


        for (String name : names) {
            Object value = getAnnotationValue(name, instance);
            if (value == null) {
                value = getPropertyDefaultValue(name);
                if (value == null) {
                    throw new IncompleteAnnotationException(getAnnotationType(), name);
                }
            }

            memberValues.put(name, value);
        }

        return memberValues;
    }


    @Override
    public @Unmodifiable Map<String, String> getPropertyNamingMaps(Class<? extends Annotation> targetType) {
        final Map<String, String> namingMap = namingMaps.getOrDefault(targetType, namingMaps.getOrDefault(Annotation.class, Collections.emptyMap()));

        final AnnotationMetadata<? extends Annotation> targetMetadata = AnnotationMetadata.resolve(targetType);
        final Set<String> targetNames;
        if (targetMetadata instanceof SimpleAnnotationMetadata) {
            targetNames = ((SimpleAnnotationMetadata<? extends Annotation>) targetMetadata).propertyTypes.keySet();
        } else {
            targetNames = targetMetadata.getPropertyNames();
        }


        final Map<String, String> map = new LinkedHashMap<>(namingMap);
        for (String targetName : targetNames) {
            if (!map.containsKey(targetName) && propertyTypes.containsKey(targetName)) {
                map.put(targetName, targetName);
            }
        }
        return map;
    }

    @Override
    public @Nullable String getPropertyNamingMap(Class<? extends Annotation> targetType, String targetPropertyName) {
        final String targetMapping = namingMaps.getOrDefault(targetType, Collections.emptyMap()).get(targetPropertyName);
        if (targetMapping != null) {
            return targetMapping;
        }

        if (containsProperty(targetType, targetPropertyName) && propertyTypes.containsKey(targetPropertyName)) {
            return targetPropertyName;
        }

        return null;
    }

    private boolean containsProperty(Class<? extends Annotation> targetType, String property) {
        final AnnotationMetadata<? extends Annotation> targetMetadata = AnnotationMetadata.resolve(targetType);
        if (targetMetadata instanceof SimpleAnnotationMetadata) {
            return ((SimpleAnnotationMetadata<? extends Annotation>) targetMetadata).propertyTypes.containsKey(property);
        } else {
            return targetMetadata.getPropertyNames().contains(property);
        }

    }

    @Override
    public String toString() {
        return "AnnotationMetadata(" +
                "annotationType=" + annotationTypeName +
                ", properties=" + propertyTypes +
                ')';
    }
}
