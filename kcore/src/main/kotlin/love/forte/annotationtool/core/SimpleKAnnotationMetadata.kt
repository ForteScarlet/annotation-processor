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

import love.forte.annotationtool.AnnotationMapper
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

/**
 * Simple implement for [KAnnotationMetadata]
 *
 * @author ForteScarlet
 */
internal class SimpleKAnnotationMetadata<A : Annotation>(override val annotationType: KClass<A>) :
    KAnnotationMetadata<A>, java.io.Serializable {

    @Transient
    override val retention: AnnotationRetention

    @Transient
    override val targets: Set<AnnotationTarget>

    // repeatable | deprecated | mustDocumented
    @Transient
    private val marks: Byte

    @Transient
    override val deprecatedMessage: String?

    @Transient
    override val deprecatedReplaceWithExpression: String?

    @Transient
    override val deprecatedReplaceWithImports: Set<String>?

    @Transient
    override val deprecatedLevel: DeprecationLevel?

    @Transient
    override val propertyDefaultValues: Map<String, Any>

    @Transient
    private val propertiesMap: Map<String, KProperty1<A, Any>>

    @Transient
    override val propertyTypes: Map<String, KType> // get() = propertiesMap.mapValues { e -> e.value.returnType }

    @Transient
    private val namingMaps: MutableMap<KClass<out Annotation>, MutableMap<String, String>>

    init {

        // repeatable
        val repeatable = annotationType.hasAnnotation<Repeatable>()

        val deprecated = getDeprecated(annotationType)
        deprecatedMessage = deprecated?.message
        deprecatedLevel = deprecated?.level
        with(deprecated?.replaceWith) {
            deprecatedReplaceWithExpression = this?.expression
            deprecatedReplaceWithImports = this?.imports?.toSet()
        }

        marks = getMarks0(annotationType, deprecated != null, repeatable)
        retention = annotationType.retentionPolicy
        targets = annotationType.targets

        @Suppress("UNCHECKED_CAST")
        val properties = annotationType.memberProperties.map { it as KProperty1<A, Any> }
        propertiesMap = properties.associateBy { it.name }
        propertyTypes = propertiesMap.mapValues { e -> e.value.returnType }
        propertyDefaultValues = propertiesMap.mapNotNull { entry ->
            kotlin.runCatching {
                val def = entry.value.javaGetter?.defaultValue
                if (def != null) entry.key to def else null
            }.getOrNull()
        }.toMap()

        val namingMaps: MutableMap<KClass<out Annotation>, MutableMap<String, String>> = WeakHashMap()

        for (property in properties) {
            val mapper: AnnotationMapper? = annotationType.findAnnotation()
            val defaultMapType: KClass<out Annotation>? = mapper?.value?.takeIf { it.size == 1 }?.first()

            // namingMap
            resolveNamingMaps(property, defaultMapType, namingMaps)
        }

        this.namingMaps = namingMaps
    }


    //endregion


    // repeatable | deprecated | mustDocumented
    override val isDeprecated: Boolean
        get() = marks and deprecatedByte != ZERO_BYTE

    override val isMustBeDocumented: Boolean
        get() = marks and mustDocumentedByte != ZERO_BYTE

    override val isRepeatable: Boolean
        get() = marks and repeatableByte != ZERO_BYTE

    override val propertyNames: Set<String>
        get() = propertyTypes.keys

    override fun getPropertyType(property: String): KType? = propertyTypes[property]

    override fun getPropertyDefaultValue(property: String): Any? {
        val def = propertyDefaultValues[property] ?: return null
        return if (def is Array<*>) def.copyOf() else def
    }

    override fun getAnnotationValue(property: String, annotation: A): Any? {
        return propertiesMap[property]?.get(annotation)
    }

    override fun getProperties(annotation: A): Map<String, Any> {
        return propertiesMap.mapValues { (_, value) -> value.get(annotation) }
    }

    // contains annotation target
    override fun contains(type: AnnotationTarget): Boolean = type in targets

    // contains property name
    override fun contains(name: String): Boolean = name in propertyNames

    override fun getPropertyNamingMaps(targetType: KClass<out Annotation>): Map<String, String> {
        val namingMap = namingMaps[targetType] ?: namingMaps[Annotation::class] ?: emptyMap()

        val targetMetadata: KAnnotationMetadata<out Annotation> = KAnnotationMetadata.resolve(targetType)
        val targetNames: Set<String> = targetMetadata.propertyNames

        val map: MutableMap<String, String> = namingMap.toMutableMap()
        for (targetName in targetNames) {
            if (targetName !in map && targetName in propertyTypes) {
                map[targetName] = targetName
            }
        }
        return map
    }

    override fun getPropertyNamingMap(targetType: KClass<out Annotation>, targetPropertyName: String): String? {
        val targetMapping = namingMaps[targetType]?.get(targetPropertyName)
        if (targetMapping != null) {
            return targetMapping
        }

        return if (containsProperty(targetType, targetPropertyName) && targetPropertyName in this) {
            targetPropertyName
        } else null

    }

    private fun containsProperty(targetType: KClass<out Annotation>, property: String): Boolean {
        val targetMetadata: KAnnotationMetadata<out Annotation> = KAnnotationMetadata.resolve(targetType)
        return property in targetMetadata
    }

    override fun toString(): String {
        return "KAnnotationMetadata(annotationType=${annotationType.qualifiedName})"
    }

    companion object {
        // repeatable | deprecated | mustDocumented
        private const val ZERO_BYTE: Byte = 0
        private const val mustDocumentedByte: Byte = 1
        private const val deprecatedByte: Byte = 2
        private const val repeatableByte: Byte = 4

        @OptIn(ExperimentalStdlibApi::class)
        private fun <A : Annotation> resolveNamingMaps(
            property: KProperty1<A, *>,
            defaultMapType: KClass<out Annotation>?,
            namingMaps: MutableMap<KClass<out Annotation>, MutableMap<String, String>>
        ) {
            val name = property.name
            val properties: List<AnnotationMapper.Property> = property.findAnnotations()

            if (properties.isNotEmpty()) {
                for (mapperProperty in properties) {
                    var target: KClass<out Annotation> = mapperProperty.target
                    if (target == Annotation::class) {
                        target = defaultMapType ?: // 无法确定属性的默认映射目标
                                throw IllegalStateException("Unable to determine the default mapping target of the mapperProperty.")
                    }
                    val targetName: String = mapperProperty.value
                    namingMaps.computeIfAbsent(target) { mutableMapOf() }
                        .merge(targetName, name) { v1: String, v2: String ->
                            throw IllegalStateException(
                                "Duplicate mapping target: $v1 -> $targetName vs $v2 -> $targetName"
                            )
                        }
                }
            }
        }

        // repeatable | deprecated | mustDocumented
        private fun getMarks0(
            annotationType: KClass<out Annotation>,
            deprecated: Boolean,
            repeatable: Boolean
        ): Byte {
            var marks: Byte = 0
            if (annotationType.hasAnnotation<MustBeDocumented>()) {
                marks = marks or mustDocumentedByte
            }
            if (deprecated) {
                marks = marks or deprecatedByte
            }
            if (repeatable) {
                marks = marks or repeatableByte
            }
            return marks
        }
    }
}


private val KClass<out Annotation>.retentionPolicy: AnnotationRetention
    get() = findAnnotation<Retention>()?.value ?: AnnotationRetention.RUNTIME

private val KClass<out Annotation>.targets: Set<AnnotationTarget>
    get() = findAnnotation<Target>()?.allowedTargets?.toSet() ?: emptySet()

private fun getDeprecated(annotationType: KClass<out Annotation>): Deprecated? = annotationType.findAnnotation()

