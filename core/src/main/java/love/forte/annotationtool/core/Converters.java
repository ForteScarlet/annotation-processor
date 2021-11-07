package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Convert instance type.
 *
 * @author ForteScarlet
 */
public interface Converters {
    /**
     * Convert an instance type.
     *
     * @throws ConvertException if it cannot be converted.
     */
    @Nullable <FROM, TO> TO convert(@Nullable Class<FROM> from, @NotNull FROM instance, @NotNull Class<TO> to);


    /**
     * Convert an instance type.
     *
     * @throws ConvertException if it cannot be converted.
     */
    @Nullable
    default <FROM, TO> TO convert(@NotNull FROM instance, @NotNull Class<TO> to) {
        return convert(null, instance, to);
    }


    ///////// static functions

    /**
     * @see NonConverters
     */
    static Converters nonConverters() {
        return NonConverters.INSTANCE;
    }


}
