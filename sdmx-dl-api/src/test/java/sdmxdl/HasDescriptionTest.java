package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Philippe Charles
 */
public class HasDescriptionTest {

    private static HasDescription of(String description) {
        return () -> description;
    }

    @Test
    public void testGetDescription() {
        assertThat(of("hello").getDescription()).isEqualTo("hello");
        assertThat(of(null).getDescription()).isNull();
    }

    @Test
    public void testGetDescriptionRawByDefault() {
        HasDescription x = of("<b>hello</b>");
        assertThat(x.getDescription(false, HasDescription.NO_DESCRIPTION_LIMIT)).isEqualTo("<b>hello</b>");
    }

    @Test
    public void testGetDescriptionWithPlainTextOnly() {
        HasDescription x = of("<b>hello</b> world");
        assertThat(x.getDescription(true, HasDescription.NO_DESCRIPTION_LIMIT)).isEqualTo("hello world");
    }

    @Test
    public void testGetDescriptionWithMaxLengthOnly() {
        HasDescription x = of("hello world");
        assertThat(x.getDescription(false, 5)).isEqualTo("hell…");
    }

    @Test
    public void testGetDescriptionWithPlainTextAndMaxLength() {
        HasDescription x = of("<b>hello</b> world");
        assertThat(x.getDescription(true, 5)).isEqualTo("hell…");
    }

    @Test
    public void testGetDescriptionWithMaxLengthNotExceeded() {
        HasDescription x = of("hello");
        assertThat(x.getDescription(false, 10)).isEqualTo("hello");
    }

    @Test
    public void testGetDescriptionWithNull() {
        HasDescription x = of(null);
        assertThat(x.getDescription(true, 10)).isNull();
    }
}
