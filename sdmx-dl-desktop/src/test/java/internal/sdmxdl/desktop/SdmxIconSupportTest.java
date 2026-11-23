package internal.sdmxdl.desktop;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.MultiResolutionImageSupport;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import sdmxdl.swing.SdmxLogo;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.IconSamples;

class SdmxIconSupportTest {

    @Test
    void testToIcon() throws IOException {
        assertThat(SdmxIconSupport.toIcon(emptyList(), 16))
                .isInstanceOf(SdmxLogo.class)
                .extracting(Icon::getIconWidth)
                .isEqualTo(16);

        assertThat(SdmxIconSupport.toIcon(singletonList(SourceIcon.ofPng(IconSamples.png(16, 16))), 16))
                .describedAs("Undecodable PNG falls back to the logo")
                .isInstanceOf(SdmxLogo.class);

        assertThat(SdmxIconSupport.toIcon(singletonList(SourceIcon.ofPng(png(64))), 16))
                .isInstanceOf(ImageIcon.class)
                .extracting(Icon::getIconWidth, Icon::getIconHeight)
                .containsExactly(16, 16);

        assertThat(SdmxIconSupport.toIcon(singletonList(SourceIcon.ofSvg(IconSamples.svg(SVG))), 32))
                .isInstanceOf(FlatSVGIcon.class)
                .extracting(Icon::getIconWidth, Icon::getIconHeight)
                .containsExactly(32, 32);

        Icon hiDpi = SdmxIconSupport.toIcon(Arrays.asList(SourceIcon.ofPng(png(16)), SourceIcon.ofPng(png(32))), 16);
        assertThat(hiDpi.getIconWidth()).isEqualTo(16);
        assertThat(MultiResolutionImageSupport.isMultiResolutionImage(((ImageIcon) hiDpi).getImage()))
                .isEqualTo(MultiResolutionImageSupport.isAvailable());
    }

    @Test
    void testGetIcon() throws IOException {
        WebSource source = WebSource.builder()
                .id("X")
                .driver("Y")
                .endpointOf("https://example.com")
                .iconOf("data:image/png;base64," + Base64.getEncoder().encodeToString(png(32)))
                .build();

        SdmxIconSupport x = SdmxIconSupport.of(SdmxWebManager.builder().build());

        assertThat(x.getIcon(source, 32))
                .isInstanceOf(ImageIcon.class)
                .isSameAs(x.getIcon(source, 32))
                .isNotSameAs(x.getIcon(source, 16));

        assertThat(x.getIcon(source.toBuilder().icon(null).build(), 32)).isInstanceOf(SdmxLogo.class);
    }

    private static final String SVG =
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 10 10\"><rect width=\"10\" height=\"10\"/></svg>";

    private static byte[] png(int size) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB), "png", result);
        return result.toByteArray();
    }
}
