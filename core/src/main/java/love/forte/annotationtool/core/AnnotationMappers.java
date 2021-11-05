package love.forte.annotationtool.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Annotation Mappers.
 *
 * @author ForteScarlet
 */
public final class AnnotationMappers {
    private static final Map<String, AnnotationMapper<?, ?>> mappers = new ConcurrentHashMap<>();


    public static AnnotationMapper<?, ?> get(String name) {
        return mappers.get(name);
    }

    public static AnnotationMapper<?, ?> put(String name, AnnotationMapper<?, ?> mapper) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        return mappers.put(name, mapper);
    }

    public static AnnotationMapper<?, ?> merge(String name, AnnotationMapper<?, ?> value,
                                               BiFunction<? super AnnotationMapper<?, ?>, ? super AnnotationMapper<?, ?>, ? extends AnnotationMapper<?, ?>> remappingFunctions) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        return mappers.merge(name, value, remappingFunctions);
    }

}
