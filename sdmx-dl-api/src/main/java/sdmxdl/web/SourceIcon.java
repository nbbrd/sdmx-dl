package sdmxdl.web;

import internal.sdmxdl.web.SourceIcons;
import java.util.Collection;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

/**
 * An icon of a web source, either scalable (SVG) or raster (PNG).
 * <p>
 * Instances are always valid: the content is checked by the factory methods.
 * PNG icons have a size read from their header while SVG icons are scalable.
 * SVG icons containing scripts, foreign objects or external references are rejected.
 *
 * @see SdmxWebManager#getIcons(WebSource)
 */
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SourceIcon {

    public static final String PNG_MEDIA_TYPE = "image/png";
    public static final String SVG_MEDIA_TYPE = "image/svg+xml";

    @NonNull String mediaType;

    /**
     * The size in pixels of a raster icon or {@code null} if the icon is scalable.
     */
    @Nullable Integer size;

    @lombok.Getter(AccessLevel.NONE)
    @lombok.ToString.Exclude
    byte @NonNull [] content;

    public boolean isScalable() {
        return size == null;
    }

    /**
     * Returns a copy of the content of this icon.
     *
     * @return a non-null array of bytes
     */
    public byte @NonNull [] getContent() {
        return content.clone();
    }

    /**
     * Creates an icon from its media type and content.
     *
     * @param mediaType a non-null media type ({@link #PNG_MEDIA_TYPE} or {@link #SVG_MEDIA_TYPE})
     * @param content   a non-null content
     * @return a non-null icon
     * @throws IllegalArgumentException if the media type is not supported or if the content is invalid
     */
    @StaticFactoryMethod
    public static @NonNull SourceIcon of(@NonNull String mediaType, byte @NonNull [] content)
            throws IllegalArgumentException {
        switch (mediaType) {
            case PNG_MEDIA_TYPE:
                return ofPng(content);
            case SVG_MEDIA_TYPE:
                return ofSvg(content);
            default:
                throw new IllegalArgumentException("Unsupported media type '" + mediaType + "'");
        }
    }

    @StaticFactoryMethod
    public static @NonNull SourceIcon ofPng(byte @NonNull [] content) throws IllegalArgumentException {
        return new SourceIcon(PNG_MEDIA_TYPE, SourceIcons.getPngSize(content), content.clone());
    }

    @StaticFactoryMethod
    public static @NonNull SourceIcon ofSvg(byte @NonNull [] content) throws IllegalArgumentException {
        SourceIcons.checkSvg(content);
        return new SourceIcon(SVG_MEDIA_TYPE, null, content.clone());
    }

    /**
     * Selects the icon that best matches a size.
     * The best match is a raster icon of the exact size, then a scalable icon,
     * then the smallest raster icon larger than the size, then the largest raster icon.
     *
     * @param icons a non-null collection of icons
     * @param size  a positive size in pixels
     * @return a non-null optional icon, empty if there are no icons
     * @throws IllegalArgumentException if the size is not positive
     */
    public static @NonNull Optional<SourceIcon> findBest(@NonNull Collection<SourceIcon> icons, int size)
            throws IllegalArgumentException {
        return SourceIcons.findBest(icons, size);
    }
}
