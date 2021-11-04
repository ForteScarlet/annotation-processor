package love.forte.annotationtool.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Converters, Only when the types are the same will the conversion be carried out.
 *
 * @author ForteScarlet
 */
public final class NonConverters implements Converters {
    private NonConverters(){}
    public static final NonConverters INSTANCE = new NonConverters();

    /**
     * when the types are the same will the conversion be carried out.
     *
     * @throws ConvertException if {@link TO type 'to'} is not assignable from {@link FROM type 'from'}
     */
    @Override
    @SuppressWarnings("unchecked")
    @NotNull
    public <FROM, TO> TO convert(@Nullable Class<FROM> from, @NotNull Class<TO> to, @NotNull FROM instance) {
        Objects.requireNonNull(to, "to type must not be null");
        Objects.requireNonNull(instance, "instance must not be null");
        if (from == null) {
            from = (Class<FROM>) instance.getClass();
        }

        if (from.equals(to) || to.isAssignableFrom(from)) {
            return (TO) instance;
        }

        throw new ConvertException("NonConverters Only when the types are the same will the conversion be carried out, But " + from + " is not a subtype of " + to);
    }

}
