package love.forte.annotationtool.core;

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

}
