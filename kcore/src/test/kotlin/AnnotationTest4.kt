/*
 *  Copyright (c) 2022 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 */

import love.forte.annotationtool.core.KAnnotationTool
import org.junit.jupiter.api.Test
import kotlin.reflect.full.findAnnotations

/*
 *  Copyright (c) 2022 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 */


@JvmRepeatable(Tar2::class)
annotation class Foo2

annotation class Tar2(vararg val value: Foo2)

@Foo2
@Foo2
class Foo2Test

/**
 *
 * @author ForteScarlet
 */
class AnnotationTest4 {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun test() {
        val tool = KAnnotationTool()

        val found1 = Foo2Test::class.findAnnotations(Foo2::class)
        assert(found1.size == 2)

        val list = tool.getAnnotations(Foo2Test::class, Foo2::class)
        assert(list.size == 2)
    }

}