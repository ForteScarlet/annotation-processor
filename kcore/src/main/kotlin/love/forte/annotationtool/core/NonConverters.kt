/*
 *  Copyright (c) 2021-2022 ForteScarlet <https://github.com/ForteScarlet>
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

import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.safeCast

/**
 * Converters, Only when the types are the same will the conversion be carried out.
 *
 * @author ForteScarlet
 */
internal object NonConverters : Converters {
    private val primitives: Set<KClass<*>> = setOf(
        Byte::class, Short::class, Int::class, Long::class,
        Double::class, Float::class, Char::class, Boolean::class,
        String::class, CharSequence::class
    )

    /**
     * when the types are the same will the conversion be carried out.
     *
     * @throws ConvertException if [type &#39;to&#39;][TO] is not assignable from [type &#39;from&#39;][FROM]
     */
    override fun <FROM : Any, TO : Any> convert(from: KClass<FROM>?, instance: FROM, to: KClass<TO>): TO {
        val fromType: KClass<out FROM> = from ?: instance::class

        val safeCast: TO? = to.safeCast(instance)
        if (safeCast != null) return safeCast

        @Suppress("UNCHECKED_CAST")
        if (from == Class::class && to == KClass::class) {
            // class to kClass
            return (instance as Class<*>).kotlin as TO
        }
        @Suppress("UNCHECKED_CAST")
        if (from == KClass::class && to == Class::class) {
            // class to kClass
            return (instance as KClass<*>).java as TO
        }

        if (fromType in primitives && to in primitives) {
            when {
                fromType.isSubclassOf(Number::class) -> {
                    @Suppress("UNCHECKED_CAST")
                    fromType as KClass<out Number>
                    val converted = when (to) {
                        Byte::class -> to.cast(fromType.cast(instance).toByte())
                        Short::class -> to.cast(fromType.cast(instance).toShort())
                        Int::class -> to.cast(fromType.cast(instance).toInt())
                        Char::class -> to.cast(fromType.cast(instance).toChar())
                        Long::class -> to.cast(fromType.cast(instance).toLong())
                        Float::class -> to.cast(fromType.cast(instance).toFloat())
                        Double::class -> to.cast(fromType.cast(instance).toDouble())
                        String::class -> to.cast(fromType.cast(instance).toString())
                        CharSequence::class -> to.cast(fromType.cast(instance).toString())
                        else -> null
                    }
                    if (converted != null) return converted
                }
                fromType == String::class -> {
                    @Suppress("UNCHECKED_CAST")
                    fromType as KClass<out String>
                    val converted = when (to) {
                        Byte::class -> to.cast(fromType.cast(instance).toByte())
                        Short::class -> to.cast(fromType.cast(instance).toShort())
                        Int::class -> to.cast(fromType.cast(instance).toInt())
                        Char::class -> {
                            val str = fromType.cast(instance)
                            if (str.length == 1) to.cast(str[0]) else throw ConvertException(fromType, to, instance)
                        }
                        Long::class -> to.cast(fromType.cast(instance).toLong())
                        Float::class -> to.cast(fromType.cast(instance).toFloat())
                        Double::class -> to.cast(fromType.cast(instance).toDouble())
                        String::class -> to.cast(fromType.cast(instance))
                        CharSequence::class -> to.cast(fromType.cast(instance))
                        else -> null
                    }
                    if (converted != null) return converted
                }
            }

        }

        throw ConvertException(fromType, to, instance)
        // throw ConvertException("NonConverters only support when the types are the same will the conversion be carried out, But $from is not a subtype of $to")
    }

}
