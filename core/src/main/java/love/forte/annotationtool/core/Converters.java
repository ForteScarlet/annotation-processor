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
