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

plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    testImplementation(V.Jupiter.Api.NOTATION)
    testRuntimeOnly(V.Jupiter.Engine.NOTATION)
    compileOnly(V.Jetbrains.Annotations.NOTATION)
    testCompileOnly(V.Jetbrains.Annotations.NOTATION)
    testImplementation("cn.hutool:hutool-core:5.7.20")
}