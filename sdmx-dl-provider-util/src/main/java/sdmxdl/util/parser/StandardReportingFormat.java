package sdmxdl.util.parser;

import lombok.AccessLevel;
import nbbrd.design.MightBePromoted;
import org.checkerframework.checker.index.qual.NonNegative;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@lombok.Value
@lombok.Builder
public class StandardReportingFormat {

    public static final StandardReportingFormat YEAR = builder()
            .indicator('A')
            .durationOf("P1Y")
            .limitPerYear(1)
            .build();

    public static final StandardReportingFormat SEMESTER = builder()
            .indicator('S')
            .durationOf("P6M")
            .limitPerYear(2)
            .build();

    public static final StandardReportingFormat TRIMESTER = builder()
            .indicator('T')
            .durationOf("P4M")
            .limitPerYear(3)
            .build();

    public static final StandardReportingFormat QUARTER = builder()
            .indicator('Q')
            .durationOf("P3M")
            .limitPerYear(4)
            .build();

    public static final StandardReportingFormat MONTH = builder()
            .indicator('M')
            .durationOf("P1M")
            .limitPerYear(12)
            .build();

    public static final StandardReportingFormat WEEK = builder()
            .indicator('W')
            .durationOf("P7D")
            .limitPerYear(53)
            .yearBaseFunction(StandardReportingFormat::getReportingWeekYearBase)
            .build();

    public static final StandardReportingFormat DAY = builder()
            .indicator('D')
            .durationOf("P1D")
            .limitPerYear(366)
            .build();

    char indicator;

    @lombok.NonNull
    Period duration;

    @NonNegative
    int limitPerYear;

    @lombok.NonNull
    @lombok.Builder.Default
    UnaryOperator<LocalDate> yearBaseFunction = UnaryOperator.identity();

    @lombok.Getter(lazy = true, value = AccessLevel.PACKAGE)
    int periodValueDigits = initPeriodValueDigits();

    @lombok.Getter(lazy = true, value = AccessLevel.PACKAGE)
    List<Period> amounts = initAmounts();

    private int initPeriodValueDigits() {
        return getNumberOfDigits(limitPerYear);
    }

    private List<Period> initAmounts() {
        return IntStream.range(0, limitPerYear).mapToObj(duration::multipliedBy).collect(Collectors.toList());
    }

    public static final class Builder {

        public Builder durationOf(CharSequence duration) {
            return duration(Period.parse(duration));
        }
    }

    private static final int[] DAY_OF_WEEK_LAGS = {0, -1, -2, -3, 3, 2, 1};

    private static LocalDate getReportingWeekYearBase(LocalDate reportingYearStartDate) {
        return reportingYearStartDate.plusDays(DAY_OF_WEEK_LAGS[reportingYearStartDate.getDayOfWeek().ordinal()]);
    }

    @MightBePromoted
    static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }
}
