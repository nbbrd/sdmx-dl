package sdmxdl.format.csv;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.io.text.Parser;
import nbbrd.picocsv.Csv;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.ObservationalTimePeriod;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static sdmxdl.DataSet.toDataSet;

@lombok.Builder(toBuilder = true)
public final class SdmxPicocsvParser {

    @lombok.NonNull
    private final Supplier<ObsParser> factory;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Locale locale = Locale.ROOT;

    public Picocsv.@NonNull Parser<DataSet> getParser(DataStructure dsd) {
        return Picocsv.Parser.builder(reader -> parseCsv(dsd, reader)).build();
    }

    private DataSet parseCsv(DataStructure dsd, Csv.Reader reader) throws IOException {
        List<String> header = readHeader(reader);

        int minHeaderSize = 3 + dsd.getDimensions().size();
        if (header.size() < minHeaderSize) {
            throw new IOException("Invalid header size");
        }

        if (!SdmxCsvFields.DATAFLOW.equals(header.get(0))) {
            throw new IOException("Invalid dataflow header");
        }

        if (!dsd.getTimeDimensionId().equals(header.get(1 + dsd.getDimensions().size()))) {
            throw new IOException("Invalid time dimension header");
        }

        if (!SdmxCsvFields.OBS_VALUE.equals(header.get(1 + dsd.getDimensions().size() + 1))) {
            throw new IOException("Invalid obs value header");
        }

        int seriesKeyIndex = header.subList(minHeaderSize, header.size()).indexOf(SdmxCsvFields.SERIESKEY);
        if (seriesKeyIndex != -1) {
            Map<String, Attribute> attributes = dsd.getAttributes().stream().collect(Collectors.toMap(Attribute::getId, Function.identity()));
            for (int i = minHeaderSize; i < seriesKeyIndex; i++) {
                String attributeId = header.get(i);
                if (!attributes.containsKey(attributeId)) {
                    throw new IOException("Unknown attribute header");
                }
            }
        }

        ObsParser obsParser = factory.get();
        Parser<DataflowRef> refParser = SdmxCsvFields.getDataflowRefParser();

        DataflowRef dataflowRef = DataflowRef.of(null, "", null);

        Map<Key, Series.Builder> data = new HashMap<>();
        Key.Builder keyBuilder = Key.builder(dsd);
        Obs.Builder obs = Obs.builder();
        while (skipComments(reader)) {
            if (!reader.readField()) {
                throw new IOException("Missing dataflow field");
            }
            dataflowRef = refParser.parse(reader);

            keyBuilder.clear();
            for (int i = 0; i < keyBuilder.size(); i++) {
                if (!reader.readField()) {
                    throw new IOException("Missing dimension field");
                }
                keyBuilder.put(header.get(1 + i), reader.toString());
            }

            if (!reader.readField()) {
                throw new IOException("Missing time dimension field");
            }
            obsParser.period(reader.toString());

            if (!reader.readField()) {
                throw new IOException("Missing obs value field");
            }
            obsParser.value(reader.toString());

            Series.Builder series = data.computeIfAbsent(keyBuilder.build(), z -> Series.builder().key(z));
            ObservationalTimePeriod observationalTimePeriod = obsParser.parsePeriod();
            LocalDateTime nullablePeriod = observationalTimePeriod != null ? observationalTimePeriod.toStartTime(null) : null;
            if (nullablePeriod == null) {
                continue;
            }
            Double nullableValue = obsParser.parseValue();
            if (nullableValue == null) {
                continue;
            }
            series.obs(obs
                    .clearMeta()
                    .period(nullablePeriod)
                    .value(nullableValue)
                    .build()
            );
        }

        return data.values()
                .stream()
                .map(Series.Builder::build)
                .collect(toDataSet(dataflowRef, DataQuery.ALL));
    }

    private List<String> readHeader(Csv.Reader reader) throws IOException {
        List<String> result = new ArrayList<>();
        if (!skipComments(reader)) {
            throw new IOException("Missing header");
        }
        while (reader.readField()) {
            result.add(reader.toString());
        }
        return result;
    }

    @MightBePromoted
    private static boolean skipComments(Csv.Reader reader) throws IOException {
        while (reader.readLine()) {
            if (!reader.isComment()) {
                return true;
            }
        }
        return false;
    }
}
