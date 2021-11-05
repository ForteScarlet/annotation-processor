package love.forte.annotationtool.core;


import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * 注解动态代理实例
 *
 * @author ForteScarlet
 */
@SuppressWarnings("unused")
public class AnnotationInvocationHandler implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1145141919810L;
    private static final Set<String> OBJ_METHOD = new HashSet<>(Arrays.asList("toString", "hashCode", "annotationType"));

    private final Class<? extends Annotation> type;
    private final Map<String, Object> memberValues;
    @Nullable
    private final Annotation baseAnnotation;

    private transient volatile Method[] memberMethods = null;

    Map<String, Object> getMemberValuesMap() {
        return memberValues;
    }

    public Object get(String key) {
        return memberValues.get(key);
    }

    <T extends Annotation> AnnotationInvocationHandler(
            Class<T> annotationType,
            Map<String, Object> memberValues,
            @Nullable Annotation baseAnnotation
    ) {
        this.type = annotationType;
        this.baseAnnotation = baseAnnotation;

        this.memberValues = resolveMemberValues(memberValues, annotationType, baseAnnotation);
    }

    <T extends Annotation> AnnotationInvocationHandler(
            Class<T> annotationType,
            Map<String, Object> memberValues
    ) {
        this(annotationType, memberValues, null);
    }

    private static LinkedHashMap<String, Object> resolveMemberValues(@Nullable Map<String, Object> memberValues, Class<? extends Annotation> annotationType, @Nullable Annotation instance) {
        final LinkedHashMap<String, Object> newMemberValues = new LinkedHashMap<>();

        final Method[] methods = annotationType.getMethods();

        if (instance != null) {
            try {
                for (Method method : methods) {
                    final String name = method.getName();
                    Class<?>[] parameterTypes = method.getParameterTypes();

                    final boolean doContinue = nameFound(name, parameterTypes, memberValues, newMemberValues);
                    if (doContinue) {
                        continue;
                    }

                    if (!newMemberValues.containsKey(name)) {
                        method.setAccessible(true);
                        newMemberValues.put(name, method.invoke(instance));
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }


        } else {
            for (Method method : methods) {
                final String name = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                final boolean doContinue = nameFound(name, parameterTypes, memberValues, newMemberValues);
                if (doContinue) {
                    continue;
                }

                // instance null
                final Object defaultValue = method.getDefaultValue();
                if (defaultValue != null) {
                    newMemberValues.putIfAbsent(name, defaultValue);
                } else {
                    // null, check member values.
                    if (!newMemberValues.containsKey(name)) {
                        throw new IncompleteAnnotationException(annotationType, name);
                    }
                }
            }
        }


        return newMemberValues;
    }

    private static boolean nameFound(String name, Class<?>[] parameterTypes, Map<String, Object> memberValues, Map<String, Object> newMemberValues) {
        if (parameterTypes.length > 0) {
            return true;
        }
        if (OBJ_METHOD.contains(name)) {
            return true;
        }

        if (memberValues != null) {
            final Object value = memberValues.get(name);
            if (value != null) {
                newMemberValues.put(name, value);
            }
        }

        return false;
    }

    /**
     * 一个注解的代理逻辑实例。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        final String name = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        //noinspection AlibabaUndefineMagicConstant
        if ("equals".equals(name) && parameterTypes.length == 1 && parameterTypes[0].equals(Object.class)) {
            return this.equalsImpl(args[0]);
        } else if (parameterTypes.length != 0) {
            // same as sun.reflect.annotation.AnnotationInvocationHandler
            throw new AssertionError("Too many parameters for an annotation method");
        } else {
            switch (name) {
                case "toString":
                    return this.toStringImpl();
                case "hashCode":
                    return this.hashCodeImpl();
                case "annotationType":
                    return this.type;
                default:
                    Object value = this.memberValues.get(name);
                    if (value == null) {
                        if (baseAnnotation != null) {
                            return method.invoke(baseAnnotation);
                        }

                        final Object defaultValue = method.getDefaultValue();
                        if (defaultValue != null) {
                            return defaultValue;
                        }

                        throw new IncompleteAnnotationException(this.type, name);
                    }


                    if (value.getClass().isArray() && Array.getLength(value) != 0) {
                        value = this.cloneArray(value);
                    }

                    return value;
            }

        }
    }

    private boolean equalsImpl(Object other) {
        if (this == other) {
            return true;
        } else if (!this.type.isInstance(other)) {
            return false;
        } else {
            Method[] methods = this.getMemberMethods();

            for (Method method : methods) {
                String methodName = method.getName();
                Object value = this.memberValues.get(methodName);
                Object otherValue;
                AnnotationInvocationHandler otherHandler = this.asOneOfUs(other);
                if (otherHandler != null) {
                    otherValue = otherHandler.memberValues.get(methodName);
                } else {
                    try {
                        otherValue = method.invoke(other);
                    } catch (InvocationTargetException var11) {
                        return false;
                    } catch (IllegalAccessException var12) {
                        throw new AssertionError(var12);
                    }
                }

                if (!memberValueEquals(value, otherValue)) {
                    return false;
                }
            }
            return true;
        }
    }


    private AnnotationInvocationHandler asOneOfUs(Object other) {
        if (Proxy.isProxyClass(other.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(other);
            if (handler instanceof AnnotationInvocationHandler) {
                return (AnnotationInvocationHandler) handler;
            }
        }
        return null;
    }

    private Method[] getMemberMethods() {
        if (this.memberMethods == null) {
            this.memberMethods = AccessController.doPrivileged((PrivilegedAction<Method[]>) () -> {
                Method[] methods = AnnotationInvocationHandler.this.type.getDeclaredMethods();
                AnnotationInvocationHandler.this.validateAnnotationMethods(methods);
                AccessibleObject.setAccessible(methods, true);
                return methods;
            });
        }

        return this.memberMethods;
    }


    private void validateAnnotationMethods(Method[] methods) {
        boolean notMalformed = true;
        int methodsLength = methods.length;
        int i = 0;

        while (i < methodsLength) {
            Method method = methods[i];
            if (method.getModifiers() == (Modifier.PUBLIC | Modifier.ABSTRACT) && !method.isDefault() && method.getParameterCount() == 0 && method.getExceptionTypes().length == 0) {
                Class<?> returnType = method.getReturnType();
                if (returnType.isArray()) {
                    returnType = returnType.getComponentType();
                    if (returnType.isArray()) {
                        notMalformed = false;
                        break;
                    }
                }

                //noinspection AlibabaAvoidComplexCondition
                if ((!returnType.isPrimitive() || returnType == Void.TYPE) && returnType != String.class && returnType != Class.class && !returnType.isEnum() && !returnType.isAnnotation()) {
                    notMalformed = false;
                    break;
                }

                String methodName = method.getName();
                //noinspection AlibabaAvoidComplexCondition
                if ((!"toString".equals(methodName) || returnType != String.class) && (!"hashCode".equals(methodName) || returnType != Integer.TYPE) && (!"annotationType".equals(methodName) || returnType != Class.class)) {
                    ++i;
                    continue;
                }

                notMalformed = false;
                break;
            }

            notMalformed = false;
            break;
        }

        if (!notMalformed) {
            throw new AnnotationFormatError("Malformed method on an annotation type");
        }
    }


    private static boolean memberValueEquals(Object o1, Object o2) {
        Class<?> o1Type = o1.getClass();
        if (!o1Type.isArray()) {
            return o1.equals(o2);
        } else if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals(((Object[]) o1), ((Object[]) o2));
        } else if (o2.getClass() != o1Type) {
            return false;
        } else if (o1Type == byte[].class) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        } else if (o1Type == char[].class) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        } else if (o1Type == double[].class) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        } else if (o1Type == float[].class) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        } else if (o1Type == int[].class) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        } else if (o1Type == long[].class) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        } else if (o1Type == short[].class) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        } else {
            assert o1Type == boolean[].class;

            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
    }

    private String toStringImpl() {
        StringBuilder builder = new StringBuilder(128);
        builder.append('@');
        builder.append(this.type.getName());
        builder.append('(');
        boolean first = true;

        final Set<Map.Entry<String, Object>> entries = this.memberValues.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }

            builder.append(entry.getKey());
            builder.append('=');
            builder.append(memberValueToString(entry.getValue()));
        }

        builder.append(')');
        return builder.toString();
    }

    private static String memberValueToString(Object memberValue) {
        if (memberValue == null) {
            return "null";
        }
        Class<?> memberValueType = memberValue.getClass();
        if (!memberValueType.isArray()) {
            if (String.class.isAssignableFrom(memberValueType)) {
                return "\"" + memberValue + "\"";
            }

            return memberValue.toString();
        } else if (memberValueType == byte[].class) {
            return Arrays.toString((byte[]) memberValue);
        } else if (memberValueType == char[].class) {
            return Arrays.toString((char[]) memberValue);
        } else if (memberValueType == double[].class) {
            return Arrays.toString((double[]) memberValue);
        } else if (memberValueType == float[].class) {
            return Arrays.toString((float[]) memberValue);
        } else if (memberValueType == int[].class) {
            return Arrays.toString((int[]) memberValue);
        } else if (memberValueType == long[].class) {
            return Arrays.toString((long[]) memberValue);
        } else if (memberValueType == short[].class) {
            return Arrays.toString((short[]) memberValue);
        } else {
            return memberValueType == boolean[].class ? Arrays.toString((boolean[]) memberValue) : Arrays.toString((Object[]) memberValue);
        }
    }


    private Object cloneArray(Object originalArray) {
        Class<?> arrayClass = originalArray.getClass();
        if (arrayClass == byte[].class) {
            return ((byte[]) originalArray).clone();
        } else if (arrayClass == char[].class) {
            return ((char[]) originalArray).clone();
        } else if (arrayClass == double[].class) {
            return ((double[]) originalArray).clone();
        } else if (arrayClass == float[].class) {
            return ((float[]) originalArray).clone();
        } else if (arrayClass == int[].class) {
            return ((int[]) originalArray).clone();
        } else if (arrayClass == long[].class) {
            return ((long[]) originalArray).clone();
        } else if (arrayClass == short[].class) {
            return ((short[]) originalArray).clone();
        } else if (arrayClass == boolean[].class) {
            return ((boolean[]) originalArray).clone();
        } else {
            return ((Object[]) originalArray).clone();
        }
    }

    private int hashCodeImpl() {
        int hash = 0;

        Map.Entry<String, Object> entries;

        Iterator<Map.Entry<String, Object>> iter = this.memberValues.entrySet().iterator();
        for (; iter.hasNext(); hash += 127 * entries.getKey().hashCode() ^ memberValueHashCode(entries.getValue())) {
            entries = iter.next();
        }

        return hash;
    }

    private static int memberValueHashCode(Object value) {
        Class<?> valueClass = value.getClass();
        if (!valueClass.isArray()) {
            return value.hashCode();
        } else if (valueClass == byte[].class) {
            return Arrays.hashCode((byte[]) value);
        } else if (valueClass == char[].class) {
            return Arrays.hashCode((char[]) value);
        } else if (valueClass == double[].class) {
            return Arrays.hashCode((double[]) value);
        } else if (valueClass == float[].class) {
            return Arrays.hashCode((float[]) value);
        } else if (valueClass == int[].class) {
            return Arrays.hashCode((int[]) value);
        } else if (valueClass == long[].class) {
            return Arrays.hashCode((long[]) value);
        } else if (valueClass == short[].class) {
            return Arrays.hashCode((short[]) value);
        } else {
            return valueClass == boolean[].class ? Arrays.hashCode((boolean[]) value) : Arrays.hashCode((Object[]) value);
        }
    }
}