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

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Converters, Only when the types are the same will the conversion be carried out.
 *
 * @author ForteScarlet
 */
public final class NonConverters implements Converters {
    private NonConverters() {
    }

    public static final NonConverters INSTANCE = new NonConverters();
    private static final Map<Class<?>, Class<?>> primitiveToBox;

    static {
        primitiveToBox = new HashMap<>();
        primitiveToBox.put(byte.class, Byte.class);
        primitiveToBox.put(short.class, Short.class);
        primitiveToBox.put(int.class, Integer.class);
        primitiveToBox.put(long.class, Long.class);
        primitiveToBox.put(double.class, Double.class);
        primitiveToBox.put(float.class, Float.class);
        primitiveToBox.put(char.class, Character.class);
        primitiveToBox.put(boolean.class, Boolean.class);
    }


    /**
     * when the types are the same will the conversion be carried out.
     *
     * @throws ConvertException if {@link TO type 'to'} is not assignable from {@link FROM type 'from'}
     */
    @Override
    @SuppressWarnings("unchecked")
    @NotNull
    public <FROM, TO> TO convert(@Nullable Class<FROM> from, @NotNull FROM instance, @NotNull Class<TO> to) {
        Objects.requireNonNull(to, "to type must not be null");
        Objects.requireNonNull(instance, "instance must not be null");
        if (from == null) {
            from = (Class<FROM>) instance.getClass();
        }

        if (from.equals(to) || to.isAssignableFrom(from)) {
            return (TO) instance;
        }

        if (from.equals(Class.class) && to.getName().equals("kotlin.reflect.KClass")) {
            return (TO) JvmClassMappingKt.getKotlinClass((Class<?>) instance);
        }

        if (from.getName().equals("kotlin.reflect.KClass") && to.equals(Class.class)) {
            return (TO) JvmClassMappingKt.getJavaClass((KClass<?>) instance);
        }

        if (to.isPrimitive()) {
            final Class<?> toBoxType = primitiveToBox.get(to);
            if (toBoxType.isAssignableFrom(from)) {
                return (TO) instance;
            }
        }

        if (from.isPrimitive()) {
            final Class<?> fromBoxType = primitiveToBox.get(from);
            if (to.isAssignableFrom(fromBoxType)) {
                return (TO) instance;
            }
        }

        throw new ConvertException("NonConverters only support when the types are the same will the conversion be carried out, But " + from + " is not a subtype of " + to);
    }

}
