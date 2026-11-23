package sdmxdl.provider.web;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static tests.sdmxdl.web.IconSamples.png;
import static tests.sdmxdl.web.IconSamples.svg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sdmxdl.web.SourceIcon;
import tests.sdmxdl.web.spi.IconProviderAssert;

public class IconProviderSupportTest {

    private static final byte[] SVG = svg("<svg xmlns='http://www.w3.org/2000/svg'/>");

    @Test
    public void testCompliance() throws IOException {
        IconProviderAssert.assertCompliance(of(resources(
                "# comment\nfile,origin\n\nABC.svg,x\nABC-16.png,y\nX_Y-Z-32.png,z\n",
                "ABC.svg",
                SVG,
                "ABC-16.png",
                png(16, 16),
                "X_Y-Z-32.png",
                png(32, 32))));
    }

    @Test
    public void testGetIcons() throws IOException {
        IconProviderSupport x = of(resources(
                "file\nABC.svg\nABC-16.png\nABC-64.png\n",
                "ABC.svg",
                SVG,
                "ABC-16.png",
                png(16, 16),
                "ABC-64.png",
                png(64, 64)));

        assertThat(x.getIconNames()).containsExactly("ABC");
        assertThat(x.getIcons("ABC"))
                .extracting(SourceIcon::getMediaType, SourceIcon::getSize)
                .containsExactly(
                        tuple(SourceIcon.SVG_MEDIA_TYPE, null),
                        tuple(SourceIcon.PNG_MEDIA_TYPE, 16),
                        tuple(SourceIcon.PNG_MEDIA_TYPE, 64));
        assertThat(x.getIcons("XYZ")).isEmpty();
    }

    @Test
    public void testInvalidResources() {
        assertThatIOException()
                .isThrownBy(() -> of(resources("file\nABC-16.png\n")).getIcons("ABC"))
                .withMessageContaining("Missing resource 'ABC-16.png'");

        assertThatIOException()
                .isThrownBy(() -> of(resources("file\nABC-16.png\n", "ABC-16.png", png(32, 32)))
                        .getIcons("ABC"))
                .withMessageContaining("expected 16, found 32");

        assertThatIOException()
                .isThrownBy(() -> of(resources(
                                "file\nABC.svg\n",
                                "ABC.svg",
                                svg("<svg xmlns='http://www.w3.org/2000/svg'><script/></svg>")))
                        .getIcons("ABC"))
                .withMessageContaining("Invalid icon 'ABC.svg'");

        assertThatIOException()
                .isThrownBy(() -> of(resources("file\n../ABC.svg\n")).getIcons("ABC"))
                .withMessageContaining("Invalid icon file name");

        assertThatIOException()
                .isThrownBy(() -> of(resources("file\nABC.png\n")).getIcons("ABC"))
                .withMessageContaining("Invalid icon file name");

        assertThatIOException()
                .isThrownBy(() -> of(name -> null).getIcons("ABC"))
                .withMessageContaining("Missing resource 'icons.csv'");
    }

    private static IconProviderSupport of(nbbrd.io.function.IOFunction<String, java.io.InputStream> resources) {
        return IconProviderSupport.builder().id("TEST").resources(resources).build();
    }

    private static nbbrd.io.function.IOFunction<String, java.io.InputStream> resources(
            String manifest, Object... files) {
        Map<String, byte[]> map = new HashMap<>();
        map.put(IconProviderSupport.MANIFEST, manifest.getBytes(UTF_8));
        for (int i = 0; i < files.length; i += 2) {
            map.put((String) files[i], (byte[]) files[i + 1]);
        }
        return name -> map.containsKey(name) ? new ByteArrayInputStream(map.get(name)) : null;
    }
}
