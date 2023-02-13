package sdmxdl.format.time;

import _test.PeriodSource;
import nbbrd.io.function.IOUnaryOperator;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static _test.PeriodSource.*;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_MONTH;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_YEAR;
import static sdmxdl.format.time.TimeFormats.*;

@SuppressWarnings("DataFlowIssue")
public class TimeFormatsTest {

    @Test
    public void testGetReportingYearStartDay() throws IOException {
        IOUnaryOperator<String> none = o -> null;
        IOUnaryOperator<String> id1 = singletonMap("REPORTING_YEAR_START_DAY", "--07-01")::get;
        IOUnaryOperator<String> id2 = singletonMap("REPYEARSTART", "--07-01")::get;

        assertThat(getReportingYearStartDay(none)).isNull();
        assertThat(getReportingYearStartDay(id1)).isEqualTo(MonthDay.of(7, 1));
        assertThat(getReportingYearStartDay(id2)).isEqualTo(MonthDay.of(7, 1));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testOnReportingFormat() {
        assertThatNullPointerException().isThrownBy(() -> onReportingFormat(null, IGNORE_ERROR));
        assertThatNullPointerException().isThrownBy(() -> onReportingFormat(REPORTING_MONTH, null));

        assertThat(onReportingFormat(REPORTING_MONTH, IGNORE_ERROR).parse(null)).isNull();
        assertThat(onReportingFormat(REPORTING_MONTH, IGNORE_ERROR).parse(_2000_A1)).isNull();
        assertThat(onReportingFormat(REPORTING_MONTH, IGNORE_ERROR).parse(_2000_M12)).isInstanceOf(ReportingTimePeriod.class);

        assertThat(onReportingFormat(REPORTING_YEAR, IGNORE_ERROR).parse(null)).isNull();
        assertThat(onReportingFormat(REPORTING_YEAR, IGNORE_ERROR).parse(_2000_A1)).isInstanceOf(ReportingTimePeriod.class);
        assertThat(onReportingFormat(REPORTING_YEAR, IGNORE_ERROR).parse(_2000_M12)).isNull();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testOnParser() {
        assertThatNullPointerException().isThrownBy(() -> onParser(null, TimeRange.DateTimeRange::parse, IGNORE_ERROR));
        assertThatNullPointerException().isThrownBy(() -> onParser(IGNORE_FILTER, null, IGNORE_ERROR));
        assertThatNullPointerException().isThrownBy(() -> onParser(IGNORE_FILTER, TimeRange.DateTimeRange::parse, null));

        assertThat(onParser(IGNORE_FILTER, TimeRange.DateRange::parse, IGNORE_ERROR).parse(null)).isNull();
        assertThat(onParser(IGNORE_FILTER, TimeRange.DateRange::parse, IGNORE_ERROR).parse(_2001_02_03_P2D)).isInstanceOf(TimeRange.DateRange.class);
        assertThat(onParser(IGNORE_FILTER, TimeRange.DateRange::parse, IGNORE_ERROR).parse(_2001_02_03T04_05_06_P2D)).isNull();

        assertThat(onParser(IGNORE_FILTER, TimeRange.DateTimeRange::parse, IGNORE_ERROR).parse(null)).isNull();
        assertThat(onParser(IGNORE_FILTER, TimeRange.DateTimeRange::parse, IGNORE_ERROR).parse(_2001_02_03_P2D)).isNull();
        assertThat(onParser(IGNORE_FILTER, TimeRange.DateTimeRange::parse, IGNORE_ERROR).parse(_2001_02_03T04_05_06_P2D)).isInstanceOf(TimeRange.DateTimeRange.class);
    }

    @ParameterizedTest
    @MethodSource("_test.PeriodSource#getAll")
    public void testValidInputNeverRaiseException(PeriodSource source) {
        List<Throwable> list = new ArrayList<>();

        assertThat(TimeFormats.getObservationalTimePeriod(list::add).parse(source.getText()))
                .isEqualTo(source.getPeriod())
                .extracting(period -> period.toStartTime(null), Assertions.LOCAL_DATE_TIME)
                .isEqualTo(source.getStartTime());

        assertThat(list).hasSize(0);
    }

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return t -> !predicate.test(t);
    }

    public static Condition<String> throwingDateTimeParseExceptionOn(Consumer<? super String> consumer) {
        return new Condition<>(text -> {
            try {
                consumer.accept(text);
                return false;
            } catch (RuntimeException ex) {
                return ex instanceof DateTimeParseException;
            }
        }, "throwing DateTimeParseException");
    }

    public static List<String> generateInvalids(String source) {
        char invalid = 'X';
        List<String> result = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            char[] chars = source.toCharArray();
            chars[i] = invalid;
            result.add(String.valueOf(chars));
        }
        result.add(invalid + source);
        result.add(source + invalid);
        result.add(source.substring(1));
        result.add(source.substring(0, source.length() - 1));
        return result;
    }
}
