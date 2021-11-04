package love.forte.annotationtool.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Set;

/**
 *
 * Config for {@link SimpleAnnotationTool}.
 *
 * @author ForteScarlet
 */
public class AnnotationToolConfiguration {
    private Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap;
    private Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap;
    private boolean mixAllRepeatable;
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

    public boolean isMixAllRepeatable() {
        return mixAllRepeatable;
    }

    public void setMixAllRepeatable(boolean mixAllRepeatable) {
        this.mixAllRepeatable = mixAllRepeatable;
    }

    public Converters getConverters() {
        return converters;
    }

    public void setConverters(Converters converters) {
        this.converters = converters;
    }
}
