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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Converters, Only when the types are the same will the conversion be carried out.
 *
 * @author ForteScarlet
 */
public final class NonConverters implements Converters {
    private NonConverters(){}
    public static final NonConverters INSTANCE = new NonConverters();

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

        throw new ConvertException("NonConverters Only when the types are the same will the conversion be carried out, But " + from + " is not a subtype of " + to);
    }

}
