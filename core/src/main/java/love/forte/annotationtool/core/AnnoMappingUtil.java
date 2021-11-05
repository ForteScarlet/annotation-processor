package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

/**
 * @author ForteScarlet
 */
final class AnnoMappingUtil {

    @NotNull
    public static <FROM extends Annotation, TO extends Annotation> AnnotationMapper<FROM, TO> getAnnotationMapper(AnnoMapping.Mapping mapping) {
        // TODO
        return null;
    }


}
