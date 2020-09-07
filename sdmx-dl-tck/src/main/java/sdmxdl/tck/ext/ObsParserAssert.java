package sdmxdl.tck.ext;

import internal.sdmxdl.tck.TckUtil;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;

import java.util.function.UnaryOperator;

@lombok.experimental.UtilityClass
public class ObsParserAssert {

    @lombok.Value
    @lombok.Builder
    public static class Sample {
        Key.Builder validKey;
        UnaryOperator<String> validAttributes;
        String validValue;
        String invalidValue;
        String validPeriod;
        String invalidPeriod;
    }

    public void assertCompliance(ObsParser parser, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, parser, sample));
    }

    public void assertCompliance(SoftAssertions s, ObsParser parser, Sample sample) {
        checkClear(s, parser);
        checkFrequency(s, parser, sample);
        checkPeriod(s, parser, sample);
        checkValue(s, parser, sample);
    }

    private static void checkValue(SoftAssertions s, ObsParser parser, Sample sample) {
        parser.frequency(sample.validKey, sample.validAttributes);

        s.assertThat(parser.value(null)).isEqualTo(parser);
        s.assertThat(parser.getValue()).isNull();
        s.assertThat(parser.parseValue()).isNull();

        s.assertThat(parser.value(sample.validValue)).isEqualTo(parser);
        s.assertThat(parser.getValue()).isEqualTo(sample.validValue);
        s.assertThat(parser.parseValue()).isNotNull();

        s.assertThat(parser.value(sample.invalidValue)).isEqualTo(parser);
        s.assertThat(parser.getValue()).isEqualTo(sample.invalidValue);
        s.assertThat(parser.parseValue()).isNull();
    }

    private static void checkPeriod(SoftAssertions s, ObsParser parser, Sample sample) {
        parser.frequency(sample.validKey, sample.validAttributes);

        s.assertThat(parser.period(null)).isEqualTo(parser);
        s.assertThat(parser.getPeriod()).isNull();
        s.assertThat(parser.parsePeriod()).isNull();

        s.assertThat(parser.period(sample.validPeriod)).isEqualTo(parser);
        s.assertThat(parser.getPeriod()).isEqualTo(sample.validPeriod);
        s.assertThat(parser.parsePeriod()).isNotNull();

        s.assertThat(parser.period(sample.invalidPeriod)).isEqualTo(parser);
        s.assertThat(parser.getPeriod()).isEqualTo(sample.invalidPeriod);
        s.assertThat(parser.parsePeriod()).isNull();
    }

    private static void checkFrequency(SoftAssertions s, ObsParser parser, Sample sample) {
        s.assertThatThrownBy(() -> parser.frequency(null, sample.validAttributes))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> parser.frequency(sample.validKey, null))
                .isInstanceOf(NullPointerException.class);

        s.assertThat(parser.frequency(sample.validKey, sample.validAttributes))
                .isEqualTo(parser);
    }

    private static void checkClear(SoftAssertions s, ObsParser parser) {
        s.assertThat(parser.clear())
                .isEqualTo(parser);
    }
}
