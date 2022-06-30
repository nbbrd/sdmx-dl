package sdmxdl.format.time;

import nbbrd.io.text.Parser;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sdmxdl.format.time.ObsTimeParser.ofAll;
import static sdmxdl.format.time.StandardReportingFormat.*;

/**
 * Time formats as described in the <a href="https://sdmx.org/wp-content/uploads/SDMX_2-1_SECTION_6_TechnicalNotes_2020-07.pdf">SDMX21 technical notes</a>
 *
 * <pre>
 * 536 4.2 Time and Time Format
 * 537 4.2.1 Introduction
 * 538 First, it is important to recognize that most observation times are a period. SDMX
 * 539 specifies precisely how Time is handled.
 * 540
 * 541 The representation of time is broken into a hierarchical collection of representations.
 * 542 A data structure definition can use of any of the representations in the hierarchy as
 * 543 the representation of time. This allows for the time dimension of a particular data
 * 544 structure definition allow for only a subset of the default representation.
 * 545
 * 546 The hierarchy of time formats is as follows (bold indicates a category which is made
 * 547 up of multiple formats, italic indicates a distinct format):
 * 548
 * 549    Observational Time Period
 * 550     o Standard Time Period
 * 551        Basic Time Period
 * 552          Gregorian Time Period
 * 553          Date Time
 * 554        Reporting Time Period
 * 555     o Time Range
 * 556
 * 557 The details of these time period categories and of the distinct formats which make
 * 558 them up are detailed in the sections to follow.
 * </pre>
 */
@lombok.experimental.UtilityClass
class TimeFormats {

    /**
     * <pre>
     * 751 4.2.7 Distinct Range
     * 752 In the case that the reporting period does not fit into one of the prescribe periods
     * 753 above, a distinct time range can be used. The value of these ranges is based on the
     * 754 ISO 8601 time interval format of start/duration. Start can be expressed as either an
     * 755 ISO 8601 date or a date-time, and duration is expressed as an ISO 8601 duration.
     * 756 However, the duration can only be postive.
     * </pre>
     */
    static final ObsTimeParser TIME_RANGE =
            ObsTimeParser.onTimeRange(TimeRange.DateRange::parse)
                    .orElse(ObsTimeParser.onTimeRange(TimeRange.DateTimeRange::parse));

    /**
     * <pre>
     * 589 4.2.6 Standard Reporting Period
     * 590 Standard reporting periods are periods of time in relation to a reporting year. Each of
     * 591 these standard reporting periods has a duration (based on the ISO 8601 definition)
     * 592 associated with it. The general format of a reporting period is as follows:
     * 593
     * 594   [REPORTING_YEAR]-[PERIOD_INDICATOR][PERIOD_VALUE]
     * 595
     * 596   Where:
     * 597     REPORTING_YEAR represents the reporting year as four digits (YYYY)
     * 598     PERIOD_INDICATOR identifies the type of period which determines the
     * 599     duration of the period
     * 600     PERIOD_VALUE indicates the actual period within the year
     * </pre>
     */
    static final ObsTimeParser REPORTING_TIME_PERIOD = ofAll(
            Stream.of(REPORTING_YEAR, REPORTING_SEMESTER, REPORTING_TRIMESTER, REPORTING_QUARTER, REPORTING_MONTH, REPORTING_WEEK, REPORTING_DAY)
                    .map(ObsTimeParser::onStandardReporting)
                    .collect(Collectors.toList())
    );

    /**
     * <pre>
     * 583 4.2.5 Date Time
     * 584 This is used to unambiguously state that a date-time represents an observation at a
     * 585 single point in time. Therefore, if one wants to use SDMX for data which is measured
     * 586 at a distinct point in time rather than being reported over a period, the date-time
     * 587 representation can be used.
     * 588   Representation: xs:dateTime (YYYY-MM-DDThh:mm:ss)
     * </pre>
     * <pre>
     *     The seconds can be reported fractionally
     * </pre>
     */
    static final ObsTimeParser DATE_TIME = ObsTimeParser.of(Parser.of(LocalDateTime::parse));

    /**
     * <pre>
     * 568 4.2.4 Gregorian Time Period
     * 569 A Gregorian time period is always represented by a Gregorian year, year-month, or
     * 570 day. These are all based on ISO 8601 dates. The representation in SDMX-ML
     * 571 messages and the period covered by each of the Gregorian time periods are as
     * 572 follows:
     * 573
     * 574   Gregorian Year:
     * 575     Representation: xs:gYear (YYYY)
     * 576     Period: the start of January 1 to the end of December 31
     * 577   Gregorian Year Month:
     * 578     Representation: xs:gYearMonth (YYYY-MM)
     * 579     Period: the start of the first day of the month to end of the last day of the month
     * 580   Gregorian Day:
     * 581     Representation: xs:date (YYYY-MM-DD)
     * 582     Period: the start of the day (00:00:00) to the end of the day (23:59:59)
     * </pre>
     */
    static final ObsTimeParser GREGORIAN_TIME_PERIOD =
            GregorianTimePeriods.GREGORIAN_YEAR
                    .orElse(GregorianTimePeriods.GREGORIAN_YEAR_MONTH)
                    .orElse(GregorianTimePeriods.GREGORIAN_DAY);

    /**
     * <pre>
     * </pre>
     */
    static final ObsTimeParser BASIC_TIME_PERIOD = GREGORIAN_TIME_PERIOD.orElse(DATE_TIME);

    /**
     * <pre>
     * 562 4.2.3 Standard Time Period
     * 563 This is the superset of any predefined time period or a distinct point in time. A time
     * 564 period consists of a distinct start and end point. If the start and end of a period are
     * 565 expressed as date instead of a complete date time, then it is implied that the start of
     * 566 the period is the beginning of the start day (i.e. 00:00:00) and the end of the period is
     * 567 the end of the end day (i.e. 23:59:59).
     * </pre>
     */
    static final ObsTimeParser STANDARD_TIME_PERIOD = BASIC_TIME_PERIOD.orElse(REPORTING_TIME_PERIOD);

    /**
     * <pre>
     * 559 4.2.2 Observational Time Period
     * 560 This is the superset of all time representations in SDMX. This allows for time to be
     * 561 expressed as any of the allowable formats
     * </pre>
     */
    static final ObsTimeParser OBSERVATIONAL_TIME_PERIOD = STANDARD_TIME_PERIOD.orElse(TIME_RANGE);
}
