package love.forte.annotationtool.core;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author ForteScarlet
 */
public abstract class BaseAnnotationMapper<FROM extends Annotation, TO extends Annotation> implements AnnotationMapper<FROM, TO> {

    protected abstract Class<TO> getToAnnotationType();
    protected abstract Map<String, Object> getAnnotationProperties(FROM annotation);

    /**
     * Get annotation tool instance. Override it, if you want.
     * @return AnnotationTool instance.
     */
    protected AnnotationTool getTool() {
        return AnnotationTools.getAnnotationTool();
    }


    @Override
    public TO map(FROM annotation) {
        final AnnotationTool tool = getTool();
        final Map<String, Object> properties = tool.getAnnotationValues(annotation);

        final Map<String, Object> annotationProperties = getAnnotationProperties(annotation);
        properties.putAll(annotationProperties);

        return tool.createAnnotationInstance(getToAnnotationType(), properties);
    }
}
