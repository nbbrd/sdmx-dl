package internal.sdmxdl.web;

import static org.assertj.core.api.Assertions.*;
import static tests.sdmxdl.web.IconSamples.png;

import java.net.URI;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import sdmxdl.web.SourceIcon;

public class SourceIconsTest {

    @Test
    public void testGetBundledIconNameOrNull() {
        assertThat(SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/ECB")))
                .isEqualTo("ECB");
        assertThat(SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/ec_dg-comp2")))
                .isEqualTo("ec_dg-comp2");
        assertThat(SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:/ECB/EXR")))
                .isNull();
        assertThat(SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:other/ECB")))
                .isNull();
        assertThat(SourceIcons.getBundledIconNameOrNull(URI.create("sdmxdl:icon/ECB")))
                .isNull();
        assertThat(SourceIcons.getBundledIconNameOrNull(URI.create("https://example.com/icon/ECB")))
                .isNull();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/../x")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/a/b")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/ECB.svg")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.getBundledIconNameOrNull(URI.create("sdmx-dl:icon/ECB#x")));
    }

    @Test
    public void testParseDataURI() {
        String base64 = Base64.getEncoder().encodeToString(png(32, 32));

        assertThat(SourceIcons.parseDataURI(URI.create("data:image/png;base64," + base64)))
                .isEqualTo(SourceIcon.ofPng(png(32, 32)));

        assertThat(SourceIcons.parseDataURI(URI.create("DATA:IMAGE/PNG;BASE64," + base64)))
                .isEqualTo(SourceIcon.ofPng(png(32, 32)));

        assertThat(
                        SourceIcons.parseDataURI(
                                URI.create(
                                        "data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%3E%3Crect%20fill='url(%23g)'/%3E%3C/svg%3E")))
                .returns(SourceIcon.SVG_MEDIA_TYPE, SourceIcon::getMediaType);

        assertThat(SourceIcons.parseDataURI(URI.create("data:image/svg+xml;charset=utf-8;base64,"
                        + Base64.getEncoder()
                                .encodeToString("<svg xmlns='http://www.w3.org/2000/svg'><title>é</title></svg>"
                                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)))))
                .returns(SourceIcon.SVG_MEDIA_TYPE, SourceIcon::getMediaType);

        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcons.parseDataURI(URI.create("sdmx-dl:icon/ECB")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.parseDataURI(URI.create("data:image/png;base64")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.parseDataURI(URI.create("data:image/gif;base64,R0lGODlh")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.parseDataURI(URI.create("data:image/png;base64,!!!")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.parseDataURI(URI.create("data:image/svg+xml,%3Csvg%3")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcons.parseDataURI(
                                URI.create(
                                        "data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%3E%3Crect%20fill='url(#g)'/%3E%3C/svg%3E")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.parseDataURI(URI.create(
                        "data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20onload='alert(1)'/%3E")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcons.parseDataURI(URI.create("data:image/png;base64,"
                        + Base64.getEncoder().encodeToString(new byte[SourceIcons.MAX_DATA_LENGTH + 1]))));
    }
}
