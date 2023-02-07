package _test;

import lombok.NonNull;
import sdmxdl.Duration;
import sdmxdl.format.time.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static java.util.Arrays.asList;
import static sdmxdl.format.time.StandardReportingFormat.*;

@lombok.Value
public class PeriodSource implements CharSequence {

    @lombok.experimental.Delegate(types = CharSequence.class)
    @NonNull String text;

    @NonNull ObservationalTimePeriod period;

    @NonNull LocalDateTime startTime;

    public static final String T_2001 = "2001";
    public static final GregorianTimePeriod.Year P_2001 = GregorianTimePeriod.Year.of(Year.of(2001));
    public static final PeriodSource _2001 = new PeriodSource(T_2001, P_2001, LocalDateTime.of(2001, 1, 1, 0, 0));

    public static final String T_2001_02 = "2001-02";
    public static final GregorianTimePeriod.YearMonth P_2001_02 = GregorianTimePeriod.YearMonth.of(YearMonth.of(2001, 2));
    public static final PeriodSource _2001_02 = new PeriodSource(T_2001_02, P_2001_02, LocalDateTime.of(2001, 2, 1, 0, 0));

    public static final String T_2001_02_03 = "2001-02-03";
    public static final GregorianTimePeriod.Day P_2001_02_03 = GregorianTimePeriod.Day.of(LocalDate.of(2001, 2, 3));
    public static final PeriodSource _2001_02_03 = new PeriodSource(T_2001_02_03, P_2001_02_03, LocalDateTime.of(2001, 2, 3, 0, 0));

    public static final String T_2001_02_03T04_05 = "2001-02-03T04:05";
    public static final DateTime P_2001_02_03T04_05 = DateTime.of(LocalDateTime.of(2001, 2, 3, 4, 5));
    public static final PeriodSource _2001_02_03T04_05 = new PeriodSource(T_2001_02_03T04_05, P_2001_02_03T04_05, LocalDateTime.of(2001, 2, 3, 4, 5));

    public static final String T_2001_02_03T04_05_06 = "2001-02-03T04:05:06";
    public static final DateTime P_2001_02_03T04_05_06 = DateTime.of(LocalDateTime.of(2001, 2, 3, 4, 5, 6));
    public static final PeriodSource _2001_02_03T04_05_06 = new PeriodSource(T_2001_02_03T04_05_06, P_2001_02_03T04_05_06, LocalDateTime.of(2001, 2, 3, 4, 5, 6));

    public static final String T_2000_A1 = "2000-A1";
    public static final ReportingTimePeriod P_2000_A1 = ReportingTimePeriod.of(REPORTING_YEAR, StandardReportingPeriod.parse(T_2000_A1));
    public static final PeriodSource _2000_A1 = new PeriodSource(T_2000_A1, P_2000_A1, LocalDateTime.of(2000, 1, 1, 0, 0));

    public static final String T_2000_S2 = "2000-S2";
    public static final ReportingTimePeriod P_2000_S2 = ReportingTimePeriod.of(REPORTING_SEMESTER, StandardReportingPeriod.parse(T_2000_S2));
    public static final PeriodSource _2000_S2 = new PeriodSource(T_2000_S2, P_2000_S2, LocalDateTime.of(2000, 7, 1, 0, 0));

    public static final String T_2000_T3 = "2000-T3";
    public static final ReportingTimePeriod P_2000_T3 = ReportingTimePeriod.of(REPORTING_TRIMESTER, StandardReportingPeriod.parse(T_2000_T3));
    public static final PeriodSource _2000_T3 = new PeriodSource(T_2000_T3, P_2000_T3, LocalDateTime.of(2000, 9, 1, 0, 0));

    public static final String T_2000_Q4 = "2000-Q4";
    public static final ReportingTimePeriod P_2000_Q4 = ReportingTimePeriod.of(REPORTING_QUARTER, StandardReportingPeriod.parse(T_2000_Q4));
    public static final PeriodSource _2000_Q4 = new PeriodSource(T_2000_Q4, P_2000_Q4, LocalDateTime.of(2000, 10, 1, 0, 0));

    public static final String T_2000_M12 = "2000-M12";
    public static final ReportingTimePeriod P_2000_M12 = ReportingTimePeriod.of(REPORTING_MONTH, StandardReportingPeriod.parse(T_2000_M12));
    public static final PeriodSource _2000_M12 = new PeriodSource(T_2000_M12, P_2000_M12, LocalDateTime.of(2000, 12, 1, 0, 0));

    public static final String T_2000_W53 = "2000-W53";
    public static final ReportingTimePeriod P_2000_W53 = ReportingTimePeriod.of(REPORTING_WEEK, StandardReportingPeriod.parse(T_2000_W53));
    public static final PeriodSource _2000_W53 = new PeriodSource(T_2000_W53, P_2000_W53, LocalDateTime.of(2001, 1, 1, 0, 0));

    public static final String T_2000_D366 = "2000-D366";
    public static final ReportingTimePeriod P_2000_D366 = ReportingTimePeriod.of(REPORTING_DAY, StandardReportingPeriod.parse(T_2000_D366));
    public static final PeriodSource _2000_D366 = new PeriodSource(T_2000_D366, P_2000_D366, LocalDateTime.of(2000, 12, 31, 0, 0));

    public static final String T_2001_02_03_P2D = "2001-02-03/P2D";
    public static final TimeRange.DateRange P_2001_02_03_P2D = TimeRange.DateRange.of(LocalDate.of(2001, 2, 3), Duration.parse("P2D"));
    public static final PeriodSource _2001_02_03_P2D = new PeriodSource(T_2001_02_03_P2D, P_2001_02_03_P2D, LocalDateTime.of(2001, 2, 3, 0, 0));

    public static final String T_2001_02_03T04_05_P2D = "2001-02-03T04:05/P2D";
    public static final TimeRange.DateTimeRange P_2001_02_03T04_05_P2D = TimeRange.DateTimeRange.of(LocalDateTime.of(2001, 2, 3, 4, 5), Duration.parse("P2D"));
    public static final PeriodSource _2001_02_03T04_05_P2D = new PeriodSource(T_2001_02_03T04_05_P2D, P_2001_02_03T04_05_P2D, LocalDateTime.of(2001, 2, 3, 4, 5));

    public static final String T_2001_02_03T04_05_06_P2D = "2001-02-03T04:05:06/P2D";
    public static final TimeRange.DateTimeRange P_2001_02_03T04_05_06_P2D = TimeRange.DateTimeRange.of(LocalDateTime.of(2001, 2, 3, 4, 5, 6), Duration.parse("P2D"));
    public static final PeriodSource _2001_02_03T04_05_06_P2D = new PeriodSource(T_2001_02_03T04_05_06_P2D, P_2001_02_03T04_05_06_P2D, LocalDateTime.of(2001, 2, 3, 4, 5, 6));

    public static List<PeriodSource> getAll() {
        return asList(
                _2001, _2001_02, _2001_02_03,
                _2001_02_03T04_05, _2001_02_03T04_05_06,
                _2000_A1, _2000_S2, _2000_T3, _2000_Q4, _2000_M12, _2000_W53, _2000_D366,
                _2001_02_03_P2D, _2001_02_03T04_05_P2D, _2001_02_03T04_05_06_P2D
        );
    }
}
