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
package love.forte.annotationtool.core

/**
 *
 * A type converter. Converter an instance from type A to type B.
 *
 * @author ForteScarlet
 */
public interface Converter<FROM : Any, TO : Any> {

    /**
     * Converter an instance of type [FROM] to type [TO].
     * @param instance instance of type [FROM]
     * @return converted type. return null if it cannot be converted.
     * @throws ConvertException if it cannot be converted.
     *
     * @see ConvertException
     */
    public fun convert(instance: FROM): TO?
}