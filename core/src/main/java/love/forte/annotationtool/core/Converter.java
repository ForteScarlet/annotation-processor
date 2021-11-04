package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * A type converter. Converter an instance from type A to type B.
 *
 * @author ForteScarlet
 */
public interface Converter<FROM, TO> {

    /**
     * Converter an instance of type {@link FROM} to type {@link TO}.
     * @param instance instance of type {@link FROM}
     * @return converted type. return null if it cannot be converted.
     * @throws ConvertException if it cannot be converted.
     *
     * @see ConvertException
     */
    @Nullable
    TO convert(@NotNull FROM instance);

}
