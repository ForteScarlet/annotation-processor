package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
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
    private transient final Map<String, Method> methods = new HashMap<>(4);

    public SimpleAnnotationMetadata(Class<A> annotationType) {
        this.annotationType = new WeakReference<>(annotationType);
        this.annotationTypeName = annotationType.getName();
        boolean rpa = annotationType.isAnnotationPresent(Repeatable.class);


        final Method[] methods = annotationType.getMethods();
        propertyTypes = new LinkedHashMap<>(methods.length);
        propertyDefaults = new LinkedHashMap<>();

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
        }

        this.repeatableAnnotationType = repeatableChildType;
        repeatable = rpa;
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
    public Map<String, Object> getProperties(@NotNull A annotation) {
        Objects.requireNonNull(annotation, "annotation should not be null");

        if (Proxy.isProxyClass(annotation.getClass())) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            if (invocationHandler instanceof AnnotationInvocationHandler) {
                return ((AnnotationInvocationHandler) invocationHandler).getMemberValuesMap();
            }
        }


        // TODO
        return null;
    }

    @Override
    public @Unmodifiable Map<String, String> getPropertyNamingMaps(Class<? extends Annotation> targetType) {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public @Nullable String getPropertyNamingMap(Class<? extends Annotation> targetType, String propertyName) {
        // TODO
        return null;
    }

    @Override
    public String toString() {
        return "AnnotationMetadata(" +
                "annotationType=" + annotationTypeName +
                ", properties=" + propertyTypes +
                // ", propertyDefaults=" + propertyDefaults +
                // ", methods=" + methods +
                ')';
    }
}
