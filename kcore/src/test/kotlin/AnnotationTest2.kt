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

annotation class Foo(val name: String)

annotation class Bar(val age: Int)


@AnnotationMapper(
    Bar::class, Foo::class
)
annotation class Tar(
    @get:AnnotationMapper.Property(target = Bar::class, value = "age")
    val age: Int = 17,
    @get:AnnotationMapper.Property(target = Foo::class, value = "name")
    val name: String = "forli"
)

@Tar
@JTar
private class Hi

class AnnotationTest2 {

    @Test
    fun test() {
        val tool = KAnnotationTool()

        val tar = tool.getAnnotation(Hi::class, Tar::class)
        assert(tar?.age == 17)
        assert(tar?.name == "forli")

        val jTar = tool.getAnnotation(Hi::class, JTar::class)
        assert(jTar?.age == 17)
        assert(jTar?.name == "forli")

        val bar = tool.getAnnotation(Hi::class, Bar::class)
        assert(bar?.age == 17)

        val foo = tool.getAnnotation(Hi::class, Foo::class)
        assert(foo?.name == "forli")

    }
}

