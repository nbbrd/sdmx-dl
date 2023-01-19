package sdmxdl.format.time;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdmxdl.format.time.GregorianTimePeriod.Day;
import sdmxdl.format.time.GregorianTimePeriod.Year;
import sdmxdl.format.time.GregorianTimePeriod.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.ObsTimeParserTest.*;

public class GregorianTimePeriodTest {

    @Nested
    class YearTest {

        @Test
        public void testOf() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Year.of(null));

            assertThat(Year.of(JT_2001))
                    .isEqualTo(Year.of(JT_2001))
                    .extracting(Year::getDate)
                    .isEqualTo(JT_2001);
        }

        @Test
        public void testParse() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Year.parse(null));

            assertThat(Year.parse("2001"))
                    .isEqualTo(Year.of(JT_2001));

            assertThat(generateInvalids("2001"))
                    .are(throwingDateTimeParseExceptionOn(Year::parse));
        }

        @Test
        public void testIsParsable() {
            assertThat(Year.isParsable(null)).isFalse();

            assertThat(Year.isParsable("2001")).isTrue();

//            assertThat(generateInvalids("2001"))
//                    .areNot(parsableUsing(Year::isParsable));
        }

        @Test
        public void testToString() {
            assertThat(Year.of(JT_2001))
                    .hasToString("2001");
        }

        @Test
        public void testToStartTime() {
            assertThat(Year.of(JT_2001).toStartTime(null))
                    .isEqualTo(JT_2001.atDay(1).atStartOfDay());
        }
    }

    @Nested
    class YearMonthTest {

        @Test
        public void testOf() {
            assertThatNullPointerException()
                    .isThrownBy(() -> YearMonth.of(null));

            assertThat(YearMonth.of(JT_2001_02))
                    .isEqualTo(YearMonth.of(JT_2001_02))
                    .extracting(YearMonth::getDate)
                    .isEqualTo(JT_2001_02);
        }

        @Test
        public void testParse() {
            assertThatNullPointerException()
                    .isThrownBy(() -> YearMonth.parse(null));

            assertThat(YearMonth.parse("2001-02"))
                    .isEqualTo(YearMonth.of(JT_2001_02));

            assertThat(generateInvalids("2001-02"))
                    .are(throwingDateTimeParseExceptionOn(YearMonth::parse));
        }

        @Test
        public void testIsParsable() {
            assertThat(YearMonth.isParsable(null)).isFalse();

            assertThat(YearMonth.isParsable("2001-02")).isTrue();

//            assertThat(generateInvalids("2001-02"))
//                    .areNot(parsableUsing(YearMonth::isParsable));
        }

        @Test
        public void testToString() {
            assertThat(YearMonth.of(JT_2001_02))
                    .hasToString("2001-02");
        }

        @Test
        public void testToStartTime() {
            assertThat(YearMonth.of(JT_2001_02).toStartTime(null))
                    .isEqualTo(JT_2001_02.atDay(1).atStartOfDay());
        }
    }

    @Nested
    class DayTest {

        @Test
        public void testOf() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Day.of(null));

            assertThat(Day.of(JT_2001_02_03))
                    .isEqualTo(Day.of(JT_2001_02_03))
                    .extracting(Day::getDate)
                    .isEqualTo(JT_2001_02_03);
        }

        @Test
        public void testParse() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Day.parse(null));

            assertThat(Day.parse("2001-02-03"))
                    .isEqualTo(Day.of(JT_2001_02_03));

            assertThat(generateInvalids("2001-02-03"))
                    .are(throwingDateTimeParseExceptionOn(Day::parse));
        }

        @Test
        public void testIsParsable() {
            assertThat(Day.isParsable(null)).isFalse();

            assertThat(Day.isParsable("2001-02-03")).isTrue();

//            assertThat(generateInvalids("2001-02-03"))
//                    .areNot(parsableUsing(Day::isParsable));
        }

        @Test
        public void testToString() {
            assertThat(Day.of(JT_2001_02_03))
                    .hasToString("2001-02-03");
        }

        @Test
        public void testToStartTime() {
            assertThat(Day.of(JT_2001_02_03).toStartTime(null))
                    .isEqualTo(JT_2001_02_03.atStartOfDay());
        }
    }

    private static final java.time.Year JT_2001 = java.time.Year.parse("2001");
    private static final java.time.YearMonth JT_2001_02 = java.time.YearMonth.parse("2001-02");
    private static final java.time.LocalDate JT_2001_02_03 = java.time.LocalDate.parse("2001-02-03");
}
