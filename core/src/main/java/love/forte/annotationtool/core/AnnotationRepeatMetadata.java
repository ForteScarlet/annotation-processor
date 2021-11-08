package love.forte.annotationtool.core;

/**
 *
 *  'Repeatable' metadata of annotation.
 *
 * @author ForteScarlet
 */
public abstract class AnnotationRepeatMetadata {
    AnnotationRepeatMetadata(){}

    /**
     * Whether it can be repeated.
     * @return true if repeatable.
     */
    public abstract boolean isRepeatable();


    public abstract boolean isRepeatRootType();


    public abstract boolean isRepeatSubtype();


}
