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

package love.forte.annotationtool.core;

import java.lang.annotation.*;

/**
 * Annotated on annotation type.
 * Just like... An annotation extends other annotation?
 *
 * @author ForteScarlet
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationMapper {
    Class<? extends Annotation>[] value();



    /**
     * Annotation's property's mapper.
     * Should use on annotation's property method.
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Properties.class)
    @interface Property {

        /**
         * Target annotation type.
         * <p>
         * if {@link AnnotationMapper#value()}'s length <= 1, this can be ignored.
         */
        Class<? extends Annotation> target() default Annotation.class;

        /**
         * Target annotation's property name.
         */
        String value();
    }



    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Properties {
        Property[] value();
    }

}
