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

import kotlin.reflect.KClass


/**
 * @author ForteScarlet
 */
public class ConvertException : TypeCastException {
    public constructor()
    public constructor(s: String?) : super(s)
    public constructor(from: KClass<*>, to: KClass<*>, instance: Any) : super("Cannot convert instance $instance type of $from to type of $to")
}