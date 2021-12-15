import love.forte.annotationtool.AnnotationMapper
import love.forte.annotationtool.core.SimpleKAnnotationTool
import love.forte.annotationtool.core.nonConverters

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
class AnnotationTest3


fun main() {
    val tool = SimpleKAnnotationTool(
        mutableMapOf(), mutableMapOf(), nonConverters()
    )
    val anList = tool.getAnnotations(AnnotationTest3::class, An::class)
    for (an in anList) {
        println(an)
    }
}