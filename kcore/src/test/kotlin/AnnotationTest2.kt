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
class Hi

class AnnotationTest2 {

    @Test
    fun test() {
        val tool = KAnnotationTool()

        val tar = tool.getAnnotation(Hi::class, Tar::class)
        println(tar)
        println(tar?.age)
        println(tar?.name)

        val jTar = tool.getAnnotation(Hi::class, JTar::class)
        println(jTar)
        println(jTar?.age)
        println(jTar?.name)

    }
}

