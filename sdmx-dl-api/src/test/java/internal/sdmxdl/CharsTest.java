package internal.sdmxdl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharsTest {

    @Test
    void testEmptyToDefault() {
        assertThat(Chars.emptyToDefault("", "default")).isEqualTo("default");
        assertThat(Chars.emptyToDefault("value", "default")).isEqualTo("value");
        assertThat(Chars.emptyToDefault(" ", "default")).isEqualTo(" ");
    }

    @Test
    void testNullOrEmptyToDefault() {
        assertThat(Chars.nullOrEmptyToDefault(null, "default")).isEqualTo("default");
        assertThat(Chars.nullOrEmptyToDefault("", "default")).isEqualTo("default");
        assertThat(Chars.nullOrEmptyToDefault("value", "default")).isEqualTo("value");
        assertThat(Chars.nullOrEmptyToDefault(" ", "default")).isEqualTo(" ");
    }

    @Test
    void testContains() {
        assertThat(Chars.contains("hello", 'e')).isTrue();
        assertThat(Chars.contains("hello", 'z')).isFalse();
        assertThat(Chars.contains("", 'a')).isFalse();
        assertThat(Chars.contains(".", '.')).isTrue();
        assertThat(Chars.contains("A+B", '+')).isTrue();
    }

    @Test
    void testSplitToArrayWithPlainDelimiter() {
        assertThat(Chars.splitToArray("a,b,c", ',')).containsExactly("a", "b", "c");
        assertThat(Chars.splitToArray("a", ',')).containsExactly("a");
        assertThat(Chars.splitToArray("", ',')).containsExactly("");
        assertThat(Chars.splitToArray(",", ',')).containsExactly("", "");
        assertThat(Chars.splitToArray("a,,c", ',')).containsExactly("a", "", "c");
    }

    @Test
    void testSplitToArrayWithRegexMetaChar() {
        // '.' is a regex metacharacter — must be handled literally
        assertThat(Chars.splitToArray("a.b.c", '.')).containsExactly("a", "b", "c");
        assertThat(Chars.splitToArray("4.AUS.M", '.')).containsExactly("4", "AUS", "M");

        // '+' is a regex metacharacter — must be handled literally
        assertThat(Chars.splitToArray("A+B+C", '+')).containsExactly("A", "B", "C");
        assertThat(Chars.splitToArray("A+B", '+')).containsExactly("A", "B");

        // '|' is a regex metacharacter — must be handled literally
        assertThat(Chars.splitToArray("x|y", '|')).containsExactly("x", "y");
    }

    @Test
    void testJoin() {
        assertThat(Chars.join('.', new String[]{"a", "b", "c"})).isEqualTo("a.b.c");
        assertThat(Chars.join('.', new String[]{"a"})).isEqualTo("a");
        assertThat(Chars.join('.', new String[]{})).isEqualTo("");
        assertThat(Chars.join('.', new String[]{"", ""})).isEqualTo(".");
        assertThat(Chars.join(',', new String[]{"4", "AUS", "M"})).isEqualTo("4,AUS,M");
    }
}

