package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Convert instance type.
 *
 * @author ForteScarlet
 */
public interface Converters {
    /**
     * Convert an instance type.
     * @throws ConvertException if it cannot be converted.
     */
    @Nullable
    <FROM, TO> TO convert(@Nullable Class<FROM> from, @NotNull Class<TO> to, @NotNull FROM instance);


    /**
     * Convert an instance type.
     * @throws ConvertException if it cannot be converted.
     */
    @Nullable
    default <FROM, TO> TO convert(@NotNull Class<TO> to, @NotNull FROM instance) {
        return convert(null, to, instance);
    }




    ///////// static functions

    /**
     * @see NonConverters
     */
    static Converters nonConverters() {
        return NonConverters.INSTANCE;
    }


}
