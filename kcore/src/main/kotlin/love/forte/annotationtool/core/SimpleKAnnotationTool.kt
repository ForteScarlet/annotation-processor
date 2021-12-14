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

import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

/**
 * This is the default implementation of the library for the [KAnnotationTool] and is based on the kotlin reflect which implements the functionality required by the [KAnnotationTool].
 *
 *
 *
 * This implementation internally caches the final [Annotation] instance by default (except for the result of [createAnnotationInstance(...) ][createAnnotationInstance] ). In [KAnnotationTools], two [WeakHashMap] are used as caches by default.
 * You can change this default by providing another Map, for example using [HashMap].
 *
 *
 *
 * [SimpleKAnnotationTool] is **not thread-safe**.
 *
 * @author ForteScarlet
 */
internal class SimpleKAnnotationTool(
    private val cacheMap: MutableMap<KAnnotatedElement, MutableMap<KClass<out Annotation>, Annotation>>,
    private val nullCacheMap: MutableMap<KAnnotatedElement, MutableSet<KClass<out Annotation>>>,
    private val converters: Converters
) : KAnnotationTool {

    /**
     * 获取注解实例. 深度获取
     */
    override fun <A : Annotation> getAnnotation(
        fromElement: KAnnotatedElement,
        annotationType: KClass<A>,
        excludes: Set<String>
    ): A? {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun <A : Annotation> getAnnotations(
        element: KAnnotatedElement,
        annotationType: KClass<A>,
        excludes: Set<String>
    ): List<A> {
        element.findAnnotations(annotationType)
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> getAnnotationValues(annotation: A): Map<String, Any> {
        return annotation.annotationClass.metadata().getProperties(annotation)
    }

    override fun getProperties(annotation: Annotation): Set<String> {
        return annotation.annotationClass.metadata().propertyNames
    }

    override fun getAnnotationPropertyTypes(annotationType: KClass<out Annotation>): Map<String, KType> {
        return annotationType.metadata().propertyTypes
    }

    override fun <A : Annotation> createAnnotationInstance(
        annotationType: KClass<A>,
        properties: Map<String, Any>,
        base: A?
    ): A {
        val primaryConstructor = annotationType.primaryConstructor!!
        val args = primaryConstructor.valueParameters.mapNotNull {
            var value = properties[it.name] ?: return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            val valueType: KClass<Any> = value::class as KClass<Any>
            val parameterType = it.type.classifier as KClass<*>
            if (!valueType.isSubclassOf(parameterType)) {
                value = converters.convert(valueType, valueType.cast(value), parameterType)
            }
            it to value
        }.toMap()

        return annotationType.newInstance(primaryConstructor, args)
    }

    override fun clearCache() {
        cacheMap.clear()
        nullCacheMap.clear()
    }
}


/**
 * Create An annotation instance by primaryConstructor.
 */
internal fun <A : Annotation> KClass<A>.newInstance(constructor: KFunction<A> = primaryConstructor!!, args: Map<KParameter, Any>): A {
    return constructor.callBy(args)
}