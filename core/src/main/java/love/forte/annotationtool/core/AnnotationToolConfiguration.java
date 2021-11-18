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

package love.forte.annotationtool.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Set;

/**
 * Config for {@link SimpleAnnotationTool}.
 *
 * @author ForteScarlet
 */
public class AnnotationToolConfiguration {
    private Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap;
    private Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap;
    private Converters converters;


    public Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> getCacheMap() {
        return cacheMap;
    }

    public void setCacheMap(Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public Map<AnnotatedElement, Set<Class<? extends Annotation>>> getNullCacheMap() {
        return nullCacheMap;
    }

    public void setNullCacheMap(Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap) {
        this.nullCacheMap = nullCacheMap;
    }

    public Converters getConverters() {
        return converters;
    }

    public void setConverters(Converters converters) {
        this.converters = converters;
    }
}
