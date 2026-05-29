package internal.sdmxdl.format.search;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TokenizerTest {

    @Test
    void tokenizeShouldReturnEmptyListForEmptyString() {
        assertThat(Tokenizer.tokenize("")).isEmpty();
    }

    @Test
    void tokenizeShouldSplitOnNonAlphanumericCharacters() {
        assertThat(Tokenizer.tokenize("hello world")).containsExactly("hello", "world");
        assertThat(Tokenizer.tokenize("foo-bar_baz")).containsExactly("foo", "bar", "baz");
        assertThat(Tokenizer.tokenize("one.two/three")).containsExactly("one", "two", "three");
    }

    @Test
    void tokenizeShouldLowercaseTokens() {
        assertThat(Tokenizer.tokenize("Hello WORLD")).containsExactly("hello", "world");
        assertThat(Tokenizer.tokenize("EXR")).containsExactly("exr");
    }

    @Test
    void tokenizeShouldStripAccents() {
        assertThat(Tokenizer.tokenize("café")).containsExactly("cafe");
        assertThat(Tokenizer.tokenize("résumé")).containsExactly("resume");
        assertThat(Tokenizer.tokenize("Ångström")).containsExactly("angstrom");
    }

    @Test
    void tokenizeShouldIgnoreLeadingAndTrailingSeparators() {
        assertThat(Tokenizer.tokenize("  hello  ")).containsExactly("hello");
        assertThat(Tokenizer.tokenize("--foo--")).containsExactly("foo");
    }

    @Test
    void tokenizeShouldPreserveDigits() {
        assertThat(Tokenizer.tokenize("abc123")).containsExactly("abc123");
        assertThat(Tokenizer.tokenize("v2 release")).containsExactly("v2", "release");
    }

    @Test
    void tokenizeShouldHandleOnlyNonAlphanumericInput() {
        assertThat(Tokenizer.tokenize("---")).isEmpty();
        assertThat(Tokenizer.tokenize("   ")).isEmpty();
    }

    @Test
    void tokenizeShouldRejectNull() {
        assertThatNullPointerException().isThrownBy(() -> Tokenizer.tokenize(null));
    }
}

