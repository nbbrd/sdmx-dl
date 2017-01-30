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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.FlowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.ResourceRef;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.TimeFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
public final class MemSdmxConnection extends SdmxConnection {

    private final Map<ResourceRef, DataStructure> dataStructures;
    private final Map<FlowRef, Dataflow> dataflows;
    private final Map<FlowRef, List<Series>> data;
    private final boolean seriesKeysOnlySupported;

    private MemSdmxConnection(Map<ResourceRef, DataStructure> dataStructures, Map<FlowRef, Dataflow> dataflows, Map<FlowRef, List<Series>> data, boolean seriesKeysOnlySupported) {
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
    public Dataflow getDataflow(FlowRef flowRef) throws IOException {
        Dataflow result = dataflows.get(flowRef);
        if (result != null) {
            return result;
        }
        throw new IOException("Dataflow not found");
    }

    @Override
    public DataStructure getDataStructure(FlowRef flowRef) throws IOException {
        DataStructure result = dataStructures.get(getDataflow(flowRef).getDataStructureRef());
        if (result != null) {
            return result;
        }
        throw new IOException("DataStructure not found");
    }

    @Override
    public DataCursor getData(FlowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
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

    @Nonnull
    public static Builder builder() {
        return new BuilderImpl();
    }

    public interface Builder {

        @Nonnull
        MemSdmxConnection build();

        @Nonnull
        Builder clear();

        @Nonnull
        Builder data(@Nonnull FlowRef flowRef, @Nonnull List<Series> col) throws IllegalArgumentException;

        @Nonnull
        Builder dataStructure(@Nonnull DataStructure dataStructure);

        @Nonnull
        Builder dataStructures(@Nonnull Iterable<? extends DataStructure> dataStructures);

        @Nonnull
        Builder dataflow(@Nonnull Dataflow dataflow) throws IllegalArgumentException;

        @Nonnull
        Builder dataflows(@Nonnull Iterable<? extends Dataflow> dataflows) throws IllegalArgumentException;

        @Nonnull
        Builder seriesKeysOnlySupported(boolean seriesKeysOnlySupported);
    }

    @lombok.Value
    public static final class Series {

        private final Key key;
        private final TimeFormat timeFormat;
        private final List<Obs> obs;

        @Nonnull
        public static Series of(@Nonnull Key key, @Nonnull TimeFormat timeFormat, @Nonnull List<Obs> obs) {
            return new Series(key, timeFormat, Collections.unmodifiableList(new ArrayList<>(obs)));
        }

        private Series(Key key, TimeFormat timeFormat, List<Obs> obs) {
            this.key = key;
            this.timeFormat = timeFormat;
            this.obs = obs;
        }
    }

    @lombok.Value(staticConstructor = "of")
    public static final class Obs {

        private final Date period;
        private final Double value;
    }

    @Nonnull
    public static List<Series> copyOf(@Nonnull DataCursor cursor) throws IOException {
        List<Series> result = new ArrayList<>();
        while (cursor.nextSeries()) {
            List<Obs> data = new ArrayList<>();
            while (cursor.nextObs()) {
                data.add(Obs.of(cursor.getPeriod(), cursor.getValue()));
            }
            result.add(Series.of(cursor.getKey(), cursor.getTimeFormat(), data));
        }
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class BuilderImpl implements Builder {

        private final Map<ResourceRef, DataStructure> dataStructures;
        private final Map<FlowRef, Dataflow> dataflows;
        private final Map<FlowRef, List<Series>> data;
        private boolean seriesKeysOnlySupported;

        public BuilderImpl() {
            this.dataStructures = new HashMap<>();
            this.dataflows = new HashMap<>();
            this.data = new HashMap<>();
            this.seriesKeysOnlySupported = true;
        }

        @Override
        public Builder clear() {
            dataStructures.clear();
            dataflows.clear();
            data.clear();
            seriesKeysOnlySupported = true;
            return this;
        }

        @Override
        public Builder dataStructure(DataStructure dataStructure) {
            dataStructures.put(dataStructure.getRef(), dataStructure);
            return this;
        }

        @Override
        public Builder dataStructures(Iterable<? extends DataStructure> dataStructures) {
            for (DataStructure o : dataStructures) {
                dataStructure(o);
            }
            return this;
        }

        @Override
        public Builder dataflow(Dataflow dataflow) throws IllegalArgumentException {
            if (!dataStructures.containsKey(dataflow.getDataStructureRef())) {
                throw new IllegalArgumentException("Missing data structure: " + dataflow.getDataStructureRef());
            }
            dataflows.put(dataflow.getFlowRef(), dataflow);
            return this;
        }

        @Override
        public Builder dataflows(Iterable<? extends Dataflow> dataflows) throws IllegalArgumentException {
            for (Dataflow o : dataflows) {
                dataflow(o);
            }
            return this;
        }

        @Override
        public Builder data(FlowRef flowRef, List<Series> col) throws IllegalArgumentException {
            if (!dataflows.containsKey(flowRef)) {
                throw new IllegalArgumentException("Missing data flow: " + flowRef);
            }
            data.put(flowRef, Collections.unmodifiableList(new ArrayList<>(col)));
            return this;
        }

        @Override
        public Builder seriesKeysOnlySupported(boolean seriesKeysOnlySupported) {
            this.seriesKeysOnlySupported = seriesKeysOnlySupported;
            return this;
        }

        @Override
        public MemSdmxConnection build() {
            return new MemSdmxConnection(new HashMap<>(dataStructures), new HashMap<>(dataflows), new HashMap<>(data), seriesKeysOnlySupported);
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
    //</editor-fold>
}
