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

import love.forte.annotationtool.AnnotationMapper
import love.forte.annotationtool.core.KAnnotationTool
import org.junit.jupiter.api.Test

@Repeatable
annotation class An(val age: Int, val name: String)

@Repeatable
@AnnotationMapper(An::class)
@An(age = 15, name = "")
annotation class An2(
    @get:AnnotationMapper.Property("name", target = An::class)
    val value: String
)

@An(age = 1, name = "forte1")
@An(age = 2, name = "forte2")
@An(age = 3, name = "forte3")
@An(age = 4, name = "forte4")
@An2("forli1")
@An2("forli2")
@An2("forli3")
@An2("forli4")
class AnnotationTest3 {
    @Test
    fun test() {
        val tool = KAnnotationTool()

        val anList = tool.getAnnotations(AnnotationTest3::class, An::class)
        assert(anList.size == 8)
        // An2
        assert(anList.filter { it.age == 15 }.size == 4)
    }
}
