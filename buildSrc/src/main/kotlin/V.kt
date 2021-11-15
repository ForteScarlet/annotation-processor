/*
 *  Copyright (c) 2021 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   除非适用法律要求或书面同意，根据许可证分发的软件
 *   许可证下分发的软件是以 "原样" 为基础的。
 *   没有任何形式的保证或条件，无论是明示还是暗示。
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 *   许可证下的权限和限制。
 */

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



