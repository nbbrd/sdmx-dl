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
package sdmxdl.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.ImmutableSerializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.kryo.util.Pool;
import sdmxdl.*;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.ext.ExpiringRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Philippe Charles
 */
public final class KryoSerialization implements sdmxdl.util.ext.Serializer {

    static {
        disableUnsafeIfNotConfigured();
    }

    public static final String KRYO_UNSAFE_PROPERTY = "kryo.unsafe";

    private static void disableUnsafeIfNotConfigured() {
        if (System.getProperty(KRYO_UNSAFE_PROPERTY) == null) {
            System.setProperty(KRYO_UNSAFE_PROPERTY, Boolean.FALSE.toString());
        }
    }

    private final Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        @Override
        protected Kryo create() {
            return newKryo();
        }
    };

    private final Pool<Input> inputPool = new Pool<Input>(true, false, 16) {
        @Override
        protected Input create() {
            return new Input(4096);
        }
    };

    private final Pool<Output> outputPool = new Pool<Output>(true, false, 16) {
        @Override
        protected Output create() {
            return new Output(4096, 4096);
        }
    };

    @Override
    public ExpiringRepository load(InputStream stream) throws IOException {
        Kryo kryo = kryoPool.obtain();
        Input input = inputPool.obtain();
        try {
            input.setInputStream(stream);
            return kryo.readObject(input, ExpiringRepository.class);
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        } finally {
            input.setInputStream(null);
            inputPool.free(input);
            kryoPool.free(kryo);
        }
    }

    @Override
    public void store(OutputStream stream, ExpiringRepository entry) throws IOException {
        Kryo kryo = kryoPool.obtain();
        Output output = outputPool.obtain();
        try {
            output.setOutputStream(stream);
            kryo.writeObject(output, entry);
            output.flush();
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        } finally {
            output.setOutputStream(null);
            outputPool.free(output);
            kryoPool.free(kryo);
        }
    }

    private static Kryo newKryo() {
        Kryo result = new Kryo();
        result.setReferences(false);
        result.setRegistrationRequired(true);

        result.register(ExpiringRepository.class, new FileEntrySerializer());

        result.register(SdmxRepository.class, new SdmxRepositorySerializer());
        result.register(DataStructure.class, new DataStructureSerializer());
        result.register(DataStructureRef.class, new DataStructureRefSerializer());
        result.register(Dataflow.class, new DataflowSerializer());
        result.register(DataflowRef.class, new DataflowRefSerializer());
        result.register(Frequency.class, new FrequencySerializer());
        result.register(Key.class, new KeySerializer());
        result.register(DataSet.class, new DataSetSerializer());
        result.register(Series.class, new SeriesSerializer());
        result.register(Obs.class, new ObsSerializer());
        result.register(Dimension.class, new DimensionSerializer());

        result.register(ArrayList.class);
        result.register(LocalDateTime.class);

        return result;
    }

    private static final class CustomCollectionSerializer<T> extends CollectionSerializer<Collection<T>> {

        CustomCollectionSerializer(Class<T> elementType) {
            setImmutable(true);
            setAcceptsNull(false);
            setElementsCanBeNull(false);
            setElementClass(elementType);
        }
    }

    private static final class CustomMapSerializer<K, V> extends MapSerializer<Map<K, V>> {

        CustomMapSerializer(Class<K> keyType, Class<V> valueType) {
            setImmutable(true);
            setAcceptsNull(false);
            setKeysCanBeNull(false);
            setValuesCanBeNull(false);
            setKeyClass(keyType);
            setValueClass(valueType);
        }
    }

    private static final class FileEntrySerializer extends ImmutableSerializer<ExpiringRepository> {

        @Override
        public void write(Kryo kryo, Output output, ExpiringRepository t) {
            output.writeLong(t.getCreationTimeInMillis(), true);
            output.writeLong(t.getTtlInMillis(), true);
            kryo.writeObject(output, t.getValue());
        }

        @Override
        public ExpiringRepository read(Kryo kryo, Input input, Class<? extends ExpiringRepository> type) {
            return ExpiringRepository.of(
                    input.readLong(true),
                    input.readLong(true),
                    kryo.readObject(input, SdmxRepository.class)
            );
        }
    }

    private static final class SdmxRepositorySerializer extends ImmutableSerializer<SdmxRepository> {

        private final Serializer<Collection<DataStructure>> structures = new CustomCollectionSerializer<>(DataStructure.class);
        private final Serializer<Collection<Dataflow>> flows = new CustomCollectionSerializer<>(Dataflow.class);
        private final Serializer<Collection<DataSet>> dataSets = new CustomCollectionSerializer<>(DataSet.class);

        @Override
        public void write(Kryo kryo, Output output, SdmxRepository t) {
            output.writeString(t.getName());
            kryo.writeObject(output, t.getStructures(), structures);
            kryo.writeObject(output, t.getFlows(), flows);
            kryo.writeObject(output, t.getDataSets(), dataSets);
            output.writeBoolean(t.isSeriesKeysOnlySupported());
        }

        @Override
        public SdmxRepository read(Kryo kryo, Input input, Class<? extends SdmxRepository> type) {
            return SdmxRepository
                    .builder()
                    .name(input.readString())
                    .structures(kryo.readObject(input, ArrayList.class, structures))
                    .flows(kryo.readObject(input, ArrayList.class, flows))
                    .dataSets(kryo.readObject(input, ArrayList.class, dataSets))
                    .seriesKeysOnlySupported(input.readBoolean())
                    .build();
        }
    }

    private static final class DataStructureSerializer extends ImmutableSerializer<DataStructure> {

        private final Serializer<Collection<Dimension>> dimensions = new CustomCollectionSerializer<>(Dimension.class);

        @Override
        public void write(Kryo kryo, Output output, DataStructure t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getDimensions(), dimensions);
            output.writeString(t.getTimeDimensionId());
            output.writeString(t.getPrimaryMeasureId());
            output.writeString(t.getLabel());
        }

        @Override
        public DataStructure read(Kryo kryo, Input input, Class<? extends DataStructure> type) {
            return DataStructure
                    .builder()
                    .ref(kryo.readObject(input, DataStructureRef.class))
                    .dimensions(kryo.readObject(input, ArrayList.class, dimensions))
                    .timeDimensionId(input.readString())
                    .primaryMeasureId(input.readString())
                    .label(input.readString())
                    .build();
        }
    }

    private static final class DataStructureRefSerializer extends ImmutableSerializer<DataStructureRef> {

        @Override
        public void write(Kryo kryo, Output output, DataStructureRef t) {
            output.writeString(t.toString());
        }

        @Override
        public DataStructureRef read(Kryo kryo, Input input, Class<? extends DataStructureRef> type) {
            return DataStructureRef.parse(input.readString());
        }
    }

    private static final class DataflowSerializer extends ImmutableSerializer<Dataflow> {

        @Override
        public void write(Kryo kryo, Output output, Dataflow t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getStructureRef());
            output.writeString(t.getLabel());
        }

        @Override
        public Dataflow read(Kryo kryo, Input input, Class<? extends Dataflow> type) {
            return Dataflow.of(
                    kryo.readObject(input, DataflowRef.class),
                    kryo.readObject(input, DataStructureRef.class),
                    input.readString()
            );
        }
    }

    private static final class DataflowRefSerializer extends ImmutableSerializer<DataflowRef> {

        @Override
        public void write(Kryo kryo, Output output, DataflowRef t) {
            output.writeString(t.toString());
        }

        @Override
        public DataflowRef read(Kryo kryo, Input input, Class<? extends DataflowRef> type) {
            return DataflowRef.parse(input.readString());
        }
    }

    private static final class DataSetSerializer extends ImmutableSerializer<DataSet> {

        private final Serializer<Collection<Series>> data = new CustomCollectionSerializer<>(Series.class);

        @Override
        public void write(Kryo kryo, Output output, DataSet t) {
            kryo.writeObject(output, t.getRef());
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getData(), data);
        }

        @Override
        public DataSet read(Kryo kryo, Input input, Class<? extends DataSet> type) {
            return DataSet
                    .builder()
                    .ref(kryo.readObject(input, DataflowRef.class))
                    .key(kryo.readObject(input, Key.class))
                    .data(kryo.readObject(input, ArrayList.class, data))
                    .build();
        }
    }

    private static final class FrequencySerializer extends ImmutableSerializer<Frequency> {

        private final Frequency[] freqs = Frequency.values();

        @Override
        public void write(Kryo kryo, Output output, Frequency t) {
            output.writeInt(t.ordinal(), true);
        }

        @Override
        public Frequency read(Kryo kryo, Input input, Class<? extends Frequency> type) {
            return freqs[input.readInt(true)];
        }
    }

    private static final class KeySerializer extends ImmutableSerializer<Key> {

        @Override
        public void write(Kryo kryo, Output output, Key t) {
            output.writeString(t.toString());
        }

        @Override
        public Key read(Kryo kryo, Input input, Class<? extends Key> type) {
            return Key.parse(input.readString());
        }
    }

    private static final class SeriesSerializer extends ImmutableSerializer<Series> {

        private final Serializer<Collection<Obs>> obs = new CustomCollectionSerializer<>(Obs.class);
        private final Serializer<Map<String, String>> meta = new CustomMapSerializer<>(String.class, String.class);

        @Override
        public void write(Kryo kryo, Output output, Series t) {
            kryo.writeObject(output, t.getKey());
            kryo.writeObject(output, t.getFreq());
            kryo.writeObject(output, t.getObs(), obs);
            kryo.writeObject(output, t.getMeta(), meta);
        }

        @Override
        public Series read(Kryo kryo, Input input, Class<? extends Series> type) {
            return Series
                    .builder()
                    .key(kryo.readObject(input, Key.class))
                    .freq(kryo.readObject(input, Frequency.class))
                    .obs(kryo.readObject(input, ArrayList.class, obs))
                    .meta(kryo.readObject(input, HashMap.class, meta))
                    .build();
        }
    }

    private static final class ObsSerializer extends ImmutableSerializer<Obs> {

        @Override
        public void write(Kryo kryo, Output output, Obs t) {
            kryo.writeObjectOrNull(output, t.getPeriod(), LocalDateTime.class);
            kryo.writeObjectOrNull(output, t.getValue(), Double.class);
        }

        @Override
        public Obs read(Kryo kryo, Input input, Class<? extends Obs> type) {
            return Obs.of(
                    kryo.readObjectOrNull(input, LocalDateTime.class),
                    kryo.readObjectOrNull(input, Double.class)
            );
        }
    }

    private static final class DimensionSerializer extends ImmutableSerializer<Dimension> {

        private final Serializer<Map<String, String>> codes = new CustomMapSerializer<>(String.class, String.class);

        @Override
        public void write(Kryo kryo, Output output, Dimension t) {
            output.writeString(t.getId());
            output.writeInt(t.getPosition(), true);
            kryo.writeObject(output, t.getCodes(), codes);
            output.writeString(t.getLabel());
        }

        @Override
        public Dimension read(Kryo kryo, Input input, Class<? extends Dimension> type) {
            return Dimension
                    .builder()
                    .id(input.readString())
                    .position(input.readInt(true))
                    .codes(kryo.readObject(input, HashMap.class, codes))
                    .label(input.readString())
                    .build();
        }
    }
}
