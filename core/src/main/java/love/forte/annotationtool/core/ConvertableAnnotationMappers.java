package love.forte.annotationtool.core;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convertable mapper.
 *
 * @author ForteScarlet
 */
public class ConvertableAnnotationMappers implements AnnotationMappers /*AnnotationMapper<Annotation, Annotation>*/ {
    private final AnnotationTool tool;
    private final Converters converters;


    ConvertableAnnotationMappers(AnnotationTool tool, Converters converters) {
        this.tool = tool;
        this.converters = converters;
    }

    @SuppressWarnings("unchecked")
    public static <F extends Annotation, T extends Annotation> AnnotationMappers createInstance(AnnotationTool tool, Converters converters) {
        return new ConvertableAnnotationMappers(tool, converters);
    }

    public static <F extends Annotation, T extends Annotation> AnnotationMappers createInstance(Converters converters) {
        return createInstance(AnnotationTools.getAnnotationTool(), converters);
    }

    @Override
    public <F extends Annotation, T extends Annotation> T map(F fromAnnotation, Class<T> toType, @Nullable T base) {
        final Class<? extends Annotation> fromAnnotationType = fromAnnotation.annotationType();
        final Map<String, Object> baseValues = tool.getAnnotationValues(fromAnnotation);

        Map<String, String> nameMap = new HashMap<>();

        final AnnotateMapper typeMapper = tool.getAnnotation(fromAnnotationType, AnnotateMapper.class);
        final boolean singlePropertyTypeMap;
        if (typeMapper == null) {
            singlePropertyTypeMap = false;
        } else {
            singlePropertyTypeMap = typeMapper.value().length == 1;
        }

        AnnotateMapper.Properties properties;

        for (Method method : fromAnnotationType.getMethods()) {
            properties = tool.getAnnotation(method, AnnotateMapper.Properties.class);
            if (properties != null) {
                final AnnotateMapper.Property[] propertiesValue = properties.value();
                for (AnnotateMapper.Property property : propertiesValue) {
                    if (singlePropertyTypeMap || property.target().equals(toType)) {
                        nameMap.put(method.getName(), property.value());
                    }
                }
            }
        }

        if (nameMap.isEmpty()) {
            // Just create.

        }


        final Set<String> fromAnnotationPropertyNames = tool.getPropertyNames(fromAnnotation);


        return null;
    }
}
