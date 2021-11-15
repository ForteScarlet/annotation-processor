import java.net.URI

@Suppress("PropertyName")
abstract class Dep(val groupId: String, val id: String, val version: String?) {
    inline val NOTATION: String get() = version?.let { v -> "$groupId:$id:$v" } ?: NOTATION_NOV
    inline val NOTATION_NOV: String get() = "$groupId:$id"
}



object P {
    const val GROUP = "love.forte.annotation-tool"
    const val VERSION = "0.5.0"
    const val DESCRIPTION = "An exquisite annotation tool."
}

@Suppress("ClassName")
object Sonatype {
    object oss {
        const val NAME = "oss"
        const val URL = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    }
    object `snapshot-oss` {
        const val NAME = "snapshot-oss"
        const val URL = "https://oss.sonatype.org/content/repositories/snapshots/"

    }
}


/**
 * Versions.
 */
object V {
    sealed class Jetbrains(id: String, version: String?) : Dep("org.jetbrains", id, version) {
        object Annotations : Jetbrains("annotations", "22.0.0")
    }

    sealed class Jupiter(id: String, version: String?) : Dep("org.junit.jupiter", id, version) {
        object Api : Jupiter("junit-jupiter-api", "5.8.1")
        object Engine : Jupiter("junit-jupiter-engine", "5.8.1")
    }

}



