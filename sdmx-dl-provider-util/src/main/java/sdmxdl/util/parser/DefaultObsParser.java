/*
 * Copyright 2015 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
public final class DefaultObsParser implements ObsParser {

    private final DefaultObsParserResource<Frequency> freqFactory;
    private final DefaultObsParserResource<TimeFormatParser> periodFactory;
    private final DefaultObsParserResource<Parser<Double>> valueFactory;

    private TimeFormatParser periodParser;
    private Parser<Double> valueParser;
    private Frequency freq;
    private String period;
    private String value;

    @lombok.Builder
    private DefaultObsParser(DefaultObsParserResource<Frequency> freqFactory,
                             DefaultObsParserResource<TimeFormatParser> periodFactory,
                             DefaultObsParserResource<Parser<Double>> valueFactory) {
        this.freqFactory = freqFactory != null ? freqFactory : (key, attributes) -> Frequency.UNDEFINED;
        this.periodFactory = periodFactory != null ? periodFactory : (key, attributes) -> TimeFormatParser.onObservationalTimePeriod();
        this.valueFactory = valueFactory != null ? valueFactory : (key, attributes) -> Parser.onDouble();
        this.periodParser = TimeFormatParser.onNull();
        this.valueParser = Parser.onNull();
        this.freq = Frequency.UNDEFINED;
        this.period = null;
        this.value = null;
    }

    @Override
    @NonNull
    public ObsParser clear() {
        this.period = null;
        this.value = null;
        return this;
    }

    @Override
    @NonNull
    public ObsParser head(Key.@NonNull Builder seriesKey, @NonNull UnaryOperator<String> seriesAttributes) {
        Objects.requireNonNull(seriesKey);
        Objects.requireNonNull(seriesAttributes);
        this.freq = freqFactory.get(seriesKey, seriesAttributes);
        this.periodParser = periodFactory.get(seriesKey, seriesAttributes);
        this.valueParser = valueFactory.get(seriesKey, seriesAttributes);
        return this;
    }

    @Override
    @NonNull
    public ObsParser period(@Nullable String period) {
        this.period = period;
        return this;
    }

    @Override
    @NonNull
    public ObsParser value(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Override
    @NonNull
    public Frequency getFrequency() {
        return freq;
    }

    @Override
    @Nullable
    public LocalDateTime parsePeriod(@NonNull UnaryOperator<String> obsAttributes) {
        Objects.requireNonNull(obsAttributes);
        return periodParser.parse(period, getReportingYearStartDay(obsAttributes));
    }

    @Override
    @Nullable
    public Double parseValue() {
        return valueParser.parse(value);
    }

    // https://sis-cc.gitlab.io/dotstatsuite-documentation/using-api/typical-use-cases/#non-calendar-reporting-periods
    private static MonthDay getReportingYearStartDay(UnaryOperator<String> obsAttributes) {
        String reportingYearStartDay = obsAttributes.apply("REPORTING_YEAR_START_DAY");
        if (reportingYearStartDay == null) {
            reportingYearStartDay = obsAttributes.apply("REPYEARSTART");
        }
        if (reportingYearStartDay == null) {
            return null;
        }
        return MONTH_DAY_PARSER.parse(reportingYearStartDay);
    }

    private static final Parser<MonthDay> MONTH_DAY_PARSER = Parser.of(MonthDay::parse);
}
