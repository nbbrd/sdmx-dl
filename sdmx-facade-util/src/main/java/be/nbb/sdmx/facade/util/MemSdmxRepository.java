/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import be.nbb.sdmx.facade.TimeFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class MemSdmxRepository {

    @lombok.NonNull
    String name;

    @lombok.NonNull
    @lombok.Singular
    List<DataStructure> dataStructures;

    @lombok.NonNull
    @lombok.Singular
    List<Dataflow> dataflows;

    @lombok.NonNull
    @lombok.Singular(value = "series")
    List<Series> data;

    boolean seriesKeysOnlySupported;

    @Nonnull
    public SdmxConnection asConnection() {
        return new MemSdmxConnection(
                toMap(dataStructures, DataStructure::getRef),
                toMap(dataflows, Dataflow::getFlowRef),
                toMultimap(data, Series::getFlowRef),
                seriesKeysOnlySupported);
    }

    @Nonnull
    public SdmxConnectionSupplier asConnectionSupplier() {
        return MemSdmxConnectionSupplier.builder().repository(this).build();
    }

    @lombok.Value(staticConstructor = "of")
    public static final class Series {

        @lombok.NonNull
        DataflowRef flowRef;

        @lombok.NonNull
        Key key;

        @lombok.NonNull
        TimeFormat timeFormat;

        @lombok.NonNull
        List<Obs> obs;

        @lombok.NonNull
        Map<String, String> meta;

        @Nonnull
        public static Series of(@Nonnull DataflowRef flowRef, @Nonnull Key key, @Nonnull TimeFormat timeFormat, @Nonnull List<Obs> obs) {
            return of(flowRef, key, timeFormat, obs, Collections.emptyMap());
        }

        @Nonnull
        public static List<Series> copyOf(@Nonnull DataflowRef flowRef, @Nonnull DataCursor cursor) throws IOException {
            List<Series> result = new ArrayList<>();
            while (cursor.nextSeries()) {
                List<Obs> obs = new ArrayList<>();
                while (cursor.nextObs()) {
                    obs.add(Obs.of(cursor.getObsPeriod(), cursor.getObsValue()));
                }
                result.add(Series.of(flowRef, cursor.getSeriesKey(), cursor.getSeriesTimeFormat(), obs, cursor.getSeriesAttributes()));
            }
            return result;
        }
    }

    @lombok.Value(staticConstructor = "of")
    public static final class Obs {

        Date period;
        Double value;
    }

    public static final class Builder {

        private boolean seriesKeysOnlySupported = true;

        @Nonnull
        public Builder copyOf(@Nonnull DataflowRef flowRef, @Nonnull DataCursor cursor) throws IOException {
            while (cursor.nextSeries()) {
                List<Obs> obs = new ArrayList<>();
                while (cursor.nextObs()) {
                    obs.add(Obs.of(cursor.getObsPeriod(), cursor.getObsValue()));
                }
                series(Series.of(flowRef, cursor.getSeriesKey(), cursor.getSeriesTimeFormat(), obs, cursor.getSeriesAttributes()));
            }
            return this;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class MemSdmxConnection implements SdmxConnection {

        private final Map<DataStructureRef, DataStructure> dataStructures;
        private final Map<DataflowRef, Dataflow> dataflows;
        private final Map<DataflowRef, List<Series>> data;
        private final boolean seriesKeysOnlySupported;

        private MemSdmxConnection(Map<DataStructureRef, DataStructure> dataStructures, Map<DataflowRef, Dataflow> dataflows, Map<DataflowRef, List<Series>> data, boolean seriesKeysOnlySupported) {
            this.dataStructures = dataStructures;
            this.dataflows = dataflows;
            this.data = data;
            this.seriesKeysOnlySupported = seriesKeysOnlySupported;
        }

        @Override
        public Set<Dataflow> getDataflows() throws IOException {
            return new HashSet<>(dataflows.values());
        }

        @Override
        public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
            Dataflow result = dataflows.get(flowRef);
            if (result != null) {
                return result;
            }
            throw new IOException("Dataflow not found");
        }

        @Override
        public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
            DataStructure result = dataStructures.get(getDataflow(flowRef).getDataStructureRef());
            if (result != null) {
                return result;
            }
            throw new IOException("DataStructure not found");
        }

        @Override
        public DataCursor getData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
            List<Series> col = data.get(flowRef);
            if (col != null) {
                return new MemDataCursor(col, key);
            }
            throw new IOException("Data not found");
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return seriesKeysOnlySupported;
        }

        @Override
        public void close() throws IOException {
            // nothing to do
        }
    }

    private static final class MemDataCursor implements DataCursor {

        private final List<Series> col;
        private final Key key;
        private int i;
        private int j;

        MemDataCursor(List<Series> col, Key key) {
            this.col = col;
            this.key = key;
            this.i = -1;
            this.j = -1;
        }

        @Override
        public boolean nextSeries() throws IOException {
            do {
                i++;
                j = -1;
            } while (i < col.size() && !key.contains(getSeriesKey()));
            return i < col.size();
        }

        @Override
        public boolean nextObs() throws IOException {
            j++;
            return j < col.get(i).getObs().size();
        }

        @Override
        public Key getSeriesKey() throws IOException {
            return col.get(i).getKey();
        }

        @Override
        public TimeFormat getSeriesTimeFormat() throws IOException {
            return col.get(i).getTimeFormat();
        }

        @Override
        public String getSeriesAttribute(String key) throws IOException {
            return col.get(i).getMeta().get(key);
        }

        @Override
        public Map<String, String> getSeriesAttributes() throws IOException {
            return col.get(i).getMeta();
        }

        @Override
        public Date getObsPeriod() throws IOException {
            return col.get(i).getObs().get(j).getPeriod();
        }

        @Override
        public Double getObsValue() throws IOException {
            return col.get(i).getObs().get(j).getValue();
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static <K, V> Map<K, V> toMap(List<V> list, Function<V, K> toKey) {
        return list.stream().collect(Collectors.toMap(toKey, Function.identity()));
    }

    private static <K, V> Map<K, List<V>> toMultimap(List<V> list, Function<V, K> toKey) {
        return list.stream().collect(Collectors.groupingBy(toKey));
    }
    //</editor-fold>
}
