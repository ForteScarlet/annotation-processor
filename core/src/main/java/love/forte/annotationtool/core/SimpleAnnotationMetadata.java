package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
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

    private static final int REPEAT_ABLE_TYPE_NO = 0;
    private static final int REPEAT_ABLE_TYPE_ROOT = 1;
    private static final int REPEAT_ABLE_TYPE_CHILD = 2;

    private final Class<A> annotationType;
    private transient final boolean repeatable;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private transient final int repeatableType;
    private transient final Map<String, Class<?>> propertyTypes;
    private transient final Map<String, Object> propertyDefaults;
    private transient final Map<String, Object> methods = new HashMap<>(4);

    public SimpleAnnotationMetadata(Class<A> annotationType) {
        this.annotationType = annotationType;
        boolean rpa = false;
        int rpat = REPEAT_ABLE_TYPE_NO;

        if (annotationType.isAnnotationPresent(Repeatable.class)) {
            rpat = REPEAT_ABLE_TYPE_ROOT;
            rpa = true;
        }


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

            if (!rpa && "value".equals(name) && returnType.isArray()) {
                // check repeatable
                final Class<?> componentType = returnType.getComponentType();
                if (componentType.isAnnotation()) {
                    final Repeatable repeatableAnnotation = componentType.getAnnotation(Repeatable.class);
                    if (repeatableAnnotation != null && repeatableAnnotation.value().equals(annotationType)) {

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

        repeatable = rpa;
        repeatableType = rpat;
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
    public boolean isRepeatable() {
        return repeatable;
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
    public Object getAnnotationValue(@NotNull String property, @NotNull Annotation annotation) throws ReflectiveOperationException {
        Objects.requireNonNull(property, "properties should not be null");
        Objects.requireNonNull(annotation, "annotation should not be null");

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
    public Map<String, String> getPropertiesNamingMap(Class<? extends Annotation> targetAnnotationType) {

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
