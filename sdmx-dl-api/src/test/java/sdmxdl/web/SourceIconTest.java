package sdmxdl.web;

import static org.assertj.core.api.Assertions.*;
import static tests.sdmxdl.web.IconSamples.png;
import static tests.sdmxdl.web.IconSamples.svg;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class SourceIconTest {

    @Test
    public void testOfPng() {
        assertThat(SourceIcon.ofPng(png(32, 32)))
                .returns(SourceIcon.PNG_MEDIA_TYPE, SourceIcon::getMediaType)
                .returns(32, SourceIcon::getSize)
                .returns(false, SourceIcon::isScalable);

        assertThat(SourceIcon.ofPng(png(64, 48)).getSize()).isEqualTo(64);

        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.ofPng(new byte[0]));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcon.ofPng(svg("<svg xmlns='http://www.w3.org/2000/svg'/>")));
        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.ofPng(png(0, 16)));
    }

    @Test
    public void testOfSvg() {
        assertThat(
                        SourceIcon.ofSvg(
                                svg(
                                        "<svg xmlns='http://www.w3.org/2000/svg'><defs><linearGradient id='g'/></defs><rect fill='url(#g)' style='fill:url(\"#g\")'/><use href='#g'/></svg>")))
                .returns(SourceIcon.SVG_MEDIA_TYPE, SourceIcon::getMediaType)
                .returns(null, SourceIcon::getSize)
                .returns(true, SourceIcon::isScalable);

        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.ofSvg(svg("")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcon.ofSvg(svg("<svg xmlns='http://www.w3.org/2000/svg'>")));
        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.ofSvg(svg("<html/>")));
        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.ofSvg(svg("<svg/>")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcon.ofSvg(
                        svg("<svg xmlns='http://www.w3.org/2000/svg'><script>alert(1)</script></svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcon.ofSvg(svg("<svg xmlns='http://www.w3.org/2000/svg'><foreignObject/></svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcon.ofSvg(svg("<svg xmlns='http://www.w3.org/2000/svg' onload='alert(1)'/>")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcon.ofSvg(
                                svg(
                                        "<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'><image xlink:href='https://example.com/x.png'/></svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SourceIcon.ofSvg(
                        svg("<svg xmlns='http://www.w3.org/2000/svg'><a href='javascript:alert(1)'/></svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcon.ofSvg(
                                svg(
                                        "<svg xmlns='http://www.w3.org/2000/svg'><rect style='fill:url(https://example.com/x)'/></svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcon.ofSvg(
                                svg(
                                        "<svg xmlns='http://www.w3.org/2000/svg'><style>@import 'https://example.com/x.css';</style></svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcon.ofSvg(
                                svg(
                                        "<!DOCTYPE svg [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><svg xmlns='http://www.w3.org/2000/svg'>&x;</svg>")));
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> SourceIcon.ofSvg(
                                svg(
                                        "<?xml-stylesheet href='https://example.com/x.css'?><svg xmlns='http://www.w3.org/2000/svg'/>")));
    }

    @Test
    public void testOf() {
        assertThat(SourceIcon.of(SourceIcon.PNG_MEDIA_TYPE, png(16, 16)).getSize())
                .isEqualTo(16);
        assertThat(SourceIcon.of(SourceIcon.SVG_MEDIA_TYPE, svg("<svg xmlns='http://www.w3.org/2000/svg'/>"))
                        .isScalable())
                .isTrue();
        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.of("image/gif", new byte[0]));
    }

    @Test
    public void testContentIsCopied() {
        byte[] content = png(16, 16);
        SourceIcon icon = SourceIcon.ofPng(content);
        content[0] = 0;
        icon.getContent()[1] = 0;
        assertThat(icon.getContent()).isEqualTo(png(16, 16));
        assertThat(icon).isEqualTo(SourceIcon.ofPng(png(16, 16)));
    }

    @Test
    public void testFindBest() {
        SourceIcon svg = SourceIcon.ofSvg(svg("<svg xmlns='http://www.w3.org/2000/svg'/>"));
        SourceIcon png16 = SourceIcon.ofPng(png(16, 16));
        SourceIcon png32 = SourceIcon.ofPng(png(32, 32));
        SourceIcon png64 = SourceIcon.ofPng(png(64, 64));

        assertThat(SourceIcon.findBest(Arrays.asList(png64, svg, png16, png32), 32))
                .contains(png32);
        assertThat(SourceIcon.findBest(Arrays.asList(png64, svg, png16), 32)).contains(svg);
        assertThat(SourceIcon.findBest(Arrays.asList(png64, png16, png32), 24)).contains(png32);
        assertThat(SourceIcon.findBest(Arrays.asList(png16, png32), 128)).contains(png32);
        assertThat(SourceIcon.findBest(Collections.emptyList(), 16)).isEmpty();
        assertThatIllegalArgumentException().isThrownBy(() -> SourceIcon.findBest(Collections.emptyList(), 0));
    }
}
