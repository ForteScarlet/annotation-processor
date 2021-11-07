package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link AnnotationTool}'s tool.
 *
 * @author ForteScarlet
 */
public final class AnnotationTools {
    /**
     * Default annotation tool.
     */
    private static final AnnotationTool DEFAULT = new SimpleAnnotationTool(
            new LinkedHashMap<>(),
            new LinkedHashMap<>(),
            Converters.nonConverters());

    /**
     * Get Default annotation tool.
     * <p>
     * Not thread safe.
     */
    public @NotNull
    static AnnotationTool getAnnotationTool() {
        return DEFAULT;
    }

    /**
     * Get an {@link AnnotationTool} instance by config.
     * <p>
     * Not thread safe.
     *
     * @param configuration config
     * @return An {@link AnnotationTool} instance.
     */
    public @NotNull
    static AnnotationTool getAnnotationTool(@Nullable AnnotationToolConfiguration configuration) {
        if (configuration == null) {
            return getAnnotationTool();
        }

        final Map<AnnotatedElement, Map<Class<? extends Annotation>, Annotation>> cacheMap = configuration.getCacheMap();
        final Map<AnnotatedElement, Set<Class<? extends Annotation>>> nullCacheMap = configuration.getNullCacheMap();
        final Converters converters = configuration.getConverters();

        return new SimpleAnnotationTool(
                or(cacheMap, LinkedHashMap::new),
                or(nullCacheMap, LinkedHashMap::new),
                or(converters, Converters::nonConverters)
        );

    }


    @NotNull
    private static <T> T or(@Nullable T value, @NotNull T def) {
        return value == null ? def : value;
    }

    @NotNull
    private static <T> T or(@Nullable T value, @NotNull Supplier<T> supplier) {
        return value == null ? supplier.get() : value;
    }
}
