/*
 *  Copyright (c) 2021 ForteScarlet <https://github.com/ForteScarlet>
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

import java.lang.reflect.Array;
import java.util.List;

/**
 * @author ForteScarlet
 */
final class ArrayUtil {

    public static Object cloneArray(Object originalArray) {
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

    public static Object toArray(Class<?> type, @NotNull List<?> list) {
        final Object array = Array.newInstance(type, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }

}
