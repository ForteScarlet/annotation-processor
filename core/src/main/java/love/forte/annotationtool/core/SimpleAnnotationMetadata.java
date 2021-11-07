package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
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

    private final Class<A> annotationType;
    private transient final Map<String, Class<?>> propertyTypes;
    private transient final Map<String, Object> propertyDefaults;
    private transient final Map<String, Object> methods = new HashMap<>(4);

    public SimpleAnnotationMetadata(Class<A> annotationType) {
        this.annotationType = annotationType;

        final Method[] methods = annotationType.getMethods();
        propertyTypes = new LinkedHashMap<>(methods.length);
        propertyDefaults = new LinkedHashMap<>();

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

            propertyTypes.put(name, returnType);
            final Object defaultValue = method.getDefaultValue();
            if (defaultValue != null) {
                propertyDefaults.put(name, defaultValue);
            }
        }
    }

    @Nullable
    private Method getMethod(String name) {
        final Object method = methods.get(name);
        if (method != null) {
            if (method instanceof Method) {
                return (Method) method;
            } else {
                return null;
            }
        }

        try {
            final Method gotMethod = annotationType.getMethod(name);
            gotMethod.setAccessible(true);
            methods.put(name, gotMethod);
            return gotMethod;
        } catch (NoSuchMethodException e) {
            methods.put(name, 0);
            return null;
        }

    }

    @Override
    public Class<A> getAnnotationType() {
        return annotationType;
    }

    @Override
    public Set<String> getPropertyNames() {
        return new LinkedHashSet<>(propertyTypes.keySet());
    }

    @Override
    public boolean containsProperty(String name) {
        return propertyTypes.containsKey(name);
    }

    @Override
    public Map<String, Class<?>> getPropertyTypes() {
        return new LinkedHashMap<>(propertyTypes);
    }


    @Override
    public @Nullable Class<?> getPropertyType(String name) {
        return propertyTypes.get(name);
    }

    @Override
    public Map<String, Object> getPropertyDefaultValues() {
        return new LinkedHashMap<>(propertyDefaults);
    }

    @Override
    public @Nullable Object getPropertyDefaultValue(String name) {
        final Object def = propertyDefaults.get(name);
        if (def.getClass().isArray()) {
            return ArrayUtil.cloneArray(def);
        }

        return def;
    }


    @Override
    public Object getAnnotationValue(@NotNull String properties, @NotNull Annotation annotation) throws ReflectiveOperationException {
        final Object defaultValue = getPropertyDefaultValue(properties);
        final Method method = getMethod(properties);
        if (method == null) {
            return defaultValue; // Basically, here is null.
        }

        return method.invoke(annotation);
    }

    @Override
    public Map<String, Object> getProperties(A annotation) {
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
    public String toString() {
        return "AnnotationMetadata(" +
                "annotationType=" + annotationType.getName() +
                ", properties=" + propertyTypes +
                // ", propertyDefaults=" + propertyDefaults +
                // ", methods=" + methods +
                ')';
    }
}
