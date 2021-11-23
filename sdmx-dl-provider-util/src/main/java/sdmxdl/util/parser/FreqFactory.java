/*
 * Copyright 2020 National Bank of Belgium
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
import sdmxdl.DataStructure;
import sdmxdl.Dimension;
import sdmxdl.Frequency;
import sdmxdl.Key;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class FreqFactory implements DefaultObsParserResource<Frequency> {

    @lombok.NonNull
    private final BiFunction<Key.Builder, UnaryOperator<String>, String> extractor;

    @lombok.NonNull
    private final Parser<Frequency> parser;

    @Override
    public Frequency get(Key.Builder k, UnaryOperator<String> a) {
        Frequency freq = parser.parse(extractor.apply(k, a));
        return freq != null ? freq : Frequency.UNDEFINED;
    }

    @NonNull
    public static FreqFactory sdmx20(@NonNull DataStructure dsd) {
        return sdmx20(TIME_FORMAT_CONCEPT);
    }

    @NonNull
    public static FreqFactory sdmx20(@NonNull String attributeName) {
        return builder()
                .extractorByAttribute(attributeName)
                .parser(FreqParsers.onTimeFormatCodeList())
                .build();
    }

    @NonNull
    public static FreqFactory sdmx21(@NonNull DataStructure dsd) {
        return sdmx21(getFrequencyCodeIdIndex(dsd));
    }

    @NonNull
    public static FreqFactory sdmx21(int frequencyCodeIdIndex) {
        return builder()
                .extractorByDimension(frequencyCodeIdIndex)
                .parser(FreqParsers.onFreqCodeList())
                .build();
    }

    public static final class Builder {

        public Builder extractorByAttribute(@NonNull String attributeName) {
            return extractor((k, a) -> a.apply(attributeName));
        }

        public Builder extractorByDimension(int index) {
            return extractor(
                    index != NO_FREQUENCY_CODE_ID_INDEX
                            ? (k, a) -> k.getItem(index)
                            : (k, a) -> null
            );
        }
    }

    public static final String FREQ_CONCEPT = "FREQ";
    public static final String TIME_FORMAT_CONCEPT = "TIME_FORMAT";

    public static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    public static int getFrequencyCodeIdIndex(@NonNull DataStructure dsd) {
        List<Dimension> dimensions = dsd.getDimensionList();
        return IntStream.range(0, dimensions.size())
                .filter(i -> isFrequencyCodeId(dimensions.get(i)))
                .findFirst()
                .orElse(NO_FREQUENCY_CODE_ID_INDEX);
    }

    private static boolean isFrequencyCodeId(Dimension o) {
        switch (o.getId()) {
            case FREQ_CONCEPT:
            case "FREQUENCY":
                return true;
            default:
                return false;
        }
    }
}
