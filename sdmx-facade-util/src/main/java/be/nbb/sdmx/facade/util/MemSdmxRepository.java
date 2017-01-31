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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                toMap(dataStructures, ToDataStructureKey.INSTANCE),
                toMap(dataflows, ToDataflowKey.INSTANCE),
                toMultimap(data, ToDataflowKey2.INSTANCE),
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
    }

    @lombok.Value(staticConstructor = "of")
    public static final class Obs {

        Date period;
        Double value;
    }

    public static final class Builder {

        @Nonnull
        public Builder copyOf(@Nonnull DataflowRef flowRef, @Nonnull DataCursor cursor) throws IOException {
            while (cursor.nextSeries()) {
                List<Obs> obs = new ArrayList<>();
                while (cursor.nextObs()) {
                    obs.add(Obs.of(cursor.getPeriod(), cursor.getValue()));
                }
                series(Series.of(flowRef, cursor.getKey(), cursor.getTimeFormat(), obs));
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
                return asDataCursor(col, key);
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

    private static DataCursor asDataCursor(final List<Series> col, final Key key) {
        return new DataCursor() {
            private int i = -1;
            private int j = -1;

            @Override
            public boolean nextSeries() throws IOException {
                do {
                    i++;
                    j = -1;
                } while (i < col.size() && !key.contains(getKey()));
                return i < col.size();
            }

            @Override
            public boolean nextObs() throws IOException {
                j++;
                return j < col.get(i).getObs().size();
            }

            @Override
            public Key getKey() throws IOException {
                return col.get(i).getKey();
            }

            @Override
            public TimeFormat getTimeFormat() throws IOException {
                return col.get(i).getTimeFormat();
            }

            @Override
            public Date getPeriod() throws IOException {
                return col.get(i).getObs().get(j).getPeriod();
            }

            @Override
            public Double getValue() throws IOException {
                return col.get(i).getObs().get(j).getValue();
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    private interface Function<X, Y> {

        Y apply(X x);
    }

    private enum ToDataStructureKey implements Function<DataStructure, DataStructureRef> {
        INSTANCE;

        @Override
        public DataStructureRef apply(DataStructure x) {
            return x.getRef();
        }
    }

    private enum ToDataflowKey implements Function<Dataflow, DataflowRef> {
        INSTANCE;

        @Override
        public DataflowRef apply(Dataflow x) {
            return x.getFlowRef();
        }
    }

    private enum ToDataflowKey2 implements Function<Series, DataflowRef> {
        INSTANCE;

        @Override
        public DataflowRef apply(Series x) {
            return x.getFlowRef();
        }
    }

    private static <K, V> Map<K, V> toMap(List<V> list, Function<V, K> toKey) {
        Map<K, V> result = new HashMap<>();
        for (V value : list) {
            result.put(toKey.apply(value), value);
        }
        return result;
    }

    private static <K, V> Map<K, List<V>> toMultimap(List<V> list, Function<V, K> toKey) {
        Map<K, List<V>> result = new HashMap<>();
        for (V value : list) {
            K key = toKey.apply(value);
            List<V> tmp = result.get(key);
            if (tmp == null) {
                tmp = new ArrayList<>();
                result.put(key, tmp);
            }
            tmp.add(value);
        }
        return result;
    }
    //</editor-fold>
}
