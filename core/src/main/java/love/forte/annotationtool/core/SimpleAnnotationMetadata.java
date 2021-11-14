package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Repeatable;
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
    private transient final boolean repeatable;
    private transient final Class<?> repeatableAnnotationType;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private transient final Map<String, Class<?>> propertyTypes;
    private transient final Map<String, Object> propertyDefaults;
    private transient final Map<String, Method> methods;
    private transient final Map<Class<? extends Annotation>, Map<String, String>> namingMaps;

    public SimpleAnnotationMetadata(Class<A> annotationType) {
        this.annotationType = new WeakReference<>(annotationType);
        this.annotationTypeName = annotationType.getName();
        boolean rpa = annotationType.isAnnotationPresent(Repeatable.class);


        final Method[] methods = annotationType.getMethods();
        this.methods = new LinkedHashMap<>(methods.length);
        Map<String, Class<?>> propertyTypes = new LinkedHashMap<>(methods.length);
        Map<String, Object> propertyDefaults = new LinkedHashMap<>();
        Map<Class<? extends Annotation>, Map<String, String>> namingMaps = new LinkedHashMap<>(methods.length);

        Class<?> repeatableChildType = null;

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


            if (!rpa && "value".equals(name) && returnType.isArray()) {
                // check repeatable
                final Class<?> componentType = returnType.getComponentType();
                if (componentType.isAnnotation()) {
                    final Repeatable repeatableAnnotation = componentType.getAnnotation(Repeatable.class);
                    if (repeatableAnnotation != null && repeatableAnnotation.value().equals(annotationType)) {
                        repeatableChildType = componentType;
                        rpa = true;
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
                        throw new IllegalStateException("Duplicate mapping target: " + v1 + " -> " + targetName +" vs " + v2 + " -> " + targetName);
                    });
                }

            }
        }
        this.repeatableAnnotationType = repeatableChildType;
        repeatable = rpa;

        if (propertyTypes.isEmpty()) {
            this.propertyTypes = Collections.emptyMap();
        } else {
            this.propertyTypes = propertyTypes;
        }

        if (propertyDefaults.isEmpty()) {
            this.propertyDefaults = Collections.emptyMap();
        } else {
            this.propertyDefaults = propertyDefaults;
        }

        if (namingMaps.isEmpty()) {
            this.namingMaps = Collections.emptyMap();
        } else {
            this.namingMaps = namingMaps;
        }


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
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public @Nullable Class<?> getRepeatableAnnotationType() {
        return repeatableAnnotationType;
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
        final Map<String, String> namingMap = namingMaps.get(targetType);

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
