import love.forte.annotationtool.AnnotationMapper
import love.forte.annotationtool.core.SimpleKAnnotationTool
import love.forte.annotationtool.core.nonConverters

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

fun main() {
    val tool = SimpleKAnnotationTool(
        mutableMapOf(), mutableMapOf(), nonConverters()
    )

    val tar = tool.getAnnotation(Hi::class, Tar::class)
    println(tar)
    println(tar?.age)
    println(tar?.name)

    val jTar = tool.getAnnotation(Hi::class, JTar::class)
    println(jTar)
    println(jTar?.age)
    println(jTar?.name)

}