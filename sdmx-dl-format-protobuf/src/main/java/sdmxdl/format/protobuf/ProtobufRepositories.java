package sdmxdl.format.protobuf;

import sdmxdl.CodelistRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.TimeInterval;

import static sdmxdl.format.protobuf.WellKnownTypes.*;

@lombok.experimental.UtilityClass
public class ProtobufRepositories {

    public static DataRepository fromDataRepository(sdmxdl.DataRepository value) {
        return DataRepository
                .newBuilder()
                .setName(value.getName())
                .addAllStructures(fromCollection(value.getStructures(), ProtobufRepositories::fromDataStructure))
                .addAllFlows(fromCollection(value.getFlows(), ProtobufRepositories::fromDataflow))
                .addAllDataSets(fromCollection(value.getDataSets(), ProtobufRepositories::fromDataSet))
                .setCreationTime(fromInstant(value.getCreationTime()))
                .setExpirationTime(fromInstant(value.getExpirationTime()))
                .build();
    }

    public static sdmxdl.DataRepository toDataRepository(DataRepository value) {
        return sdmxdl.DataRepository
                .builder()
                .name(value.getName())
                .structures(toCollection(value.getStructuresList(), ProtobufRepositories::toDataStructure))
                .flows(toCollection(value.getFlowsList(), ProtobufRepositories::toDataflow))
                .dataSets(toCollection(value.getDataSetsList(), ProtobufRepositories::toDataSet))
                .creationTime(toInstant(value.getCreationTime()))
                .expirationTime(toInstant(value.getExpirationTime()))
                .build();
    }

    public static DataStructure fromDataStructure(sdmxdl.DataStructure value) {
        DataStructure.Builder result = DataStructure
                .newBuilder()
                .setRef(value.getRef().toString())
                .addAllDimensions(fromCollection(value.getDimensions(), ProtobufRepositories::fromDimension))
                .addAllAttributes(fromCollection(value.getAttributes(), ProtobufRepositories::fromAttribute));
        if (value.getTimeDimensionId() != null) {
            result.setTimeDimensionId(value.getTimeDimensionId());
        }
        return result
                .setPrimaryMeasureId(value.getPrimaryMeasureId())
                .setName(value.getName())
                .build();
    }

    public static sdmxdl.DataStructure toDataStructure(DataStructure value) {
        return sdmxdl.DataStructure
                .builder()
                .ref(sdmxdl.DataStructureRef.parse(value.getRef()))
                .dimensions(toCollection(value.getDimensionsList(), ProtobufRepositories::toDimension))
                .attributes(toCollection(value.getAttributesList(), ProtobufRepositories::toAttribute))
                .timeDimensionId(value.hasTimeDimensionId() ? value.getTimeDimensionId() : null)
                .primaryMeasureId(value.getPrimaryMeasureId())
                .name(value.getName())
                .build();
    }

    public static Dimension fromDimension(sdmxdl.Dimension value) {
        return Dimension
                .newBuilder()
                .setId(value.getId())
                .setName(value.getName())
                .setCodelist(fromCodelist(value.getCodelist()))
                .setPosition(value.getPosition())
                .build();
    }

    public static sdmxdl.Dimension toDimension(Dimension value) {
        return sdmxdl.Dimension
                .builder()
                .id(value.getId())
                .name(value.getName())
                .codelist(toCodelist(value.getCodelist()))
                .position(value.getPosition())
                .build();
    }

    public static Codelist fromCodelist(sdmxdl.Codelist value) {
        return Codelist
                .newBuilder()
                .setRef(value.getRef().toString())
                .putAllCodes(value.getCodes())
                .build();
    }

    public sdmxdl.Codelist toCodelist(Codelist value) {
        return sdmxdl.Codelist
                .builder()
                .ref(CodelistRef.parse(value.getRef()))
                .codes(value.getCodesMap())
                .build();
    }

    public static Attribute fromAttribute(sdmxdl.Attribute value) {
        Attribute.Builder result = Attribute
                .newBuilder()
                .setId(value.getId())
                .setName(value.getName());
        if (value.getCodelist() != null)
            result.setCodelist(fromCodelist(value.getCodelist()));
        return result
                .setRelationship(fromAttributeRelationship(value.getRelationship()))
                .build();
    }

    public static sdmxdl.Attribute toAttribute(Attribute value) {
        return sdmxdl.Attribute
                .builder()
                .id(value.getId())
                .name(value.getName())
                .codelist(value.hasCodelist() ? toCodelist(value.getCodelist()) : null)
                .relationship(toAttributeRelationship(value.getRelationship()))
                .build();
    }

    public static AttributeRelationship fromAttributeRelationship(sdmxdl.AttributeRelationship value) {
        return AttributeRelationship.valueOf(value.name());
    }

    public static sdmxdl.AttributeRelationship toAttributeRelationship(AttributeRelationship value) {
        return sdmxdl.AttributeRelationship.valueOf(value.name());
    }

    public static Dataflow fromDataflow(sdmxdl.Dataflow value) {
        Dataflow.Builder result = Dataflow
                .newBuilder()
                .setRef(value.getRef().toString())
                .setStructureRef(value.getStructureRef().toString())
                .setName(value.getName());
        if (value.getDescription() != null) {
            result.setDescription(value.getDescription());
        }
        return result.build();
    }

    public static sdmxdl.Dataflow toDataflow(Dataflow value) {
        return sdmxdl.Dataflow
                .builder()
                .ref(sdmxdl.DataflowRef.parse(value.getRef()))
                .structureRef(sdmxdl.DataStructureRef.parse(value.getStructureRef()))
                .name(value.getName())
                .description(value.hasDescription() ? value.getDescription() : null)
                .build();
    }

    public static DataSet fromDataSet(sdmxdl.DataSet value) {
        return DataSet
                .newBuilder()
                .setRef(value.getRef().toString())
                .setQuery(fromDataQuery(value.getQuery()))
                .addAllData(fromCollection(value.getData(), ProtobufRepositories::fromSeries))
                .build();
    }

    public static sdmxdl.DataSet toDataSet(DataSet value) {
        return sdmxdl.DataSet
                .builder()
                .ref(DataflowRef.parse(value.getRef()))
                .query(toDataQuery(value.getQuery()))
                .data(toCollection(value.getDataList(), ProtobufRepositories::toSeries))
                .build();
    }

    public static DataQuery fromDataQuery(sdmxdl.DataQuery value) {
        return DataQuery
                .newBuilder()
                .setKey(value.getKey().toString())
                .setDetail(fromDataDetail(value.getDetail()))
                .build();
    }

    public static sdmxdl.DataQuery toDataQuery(DataQuery value) {
        return sdmxdl.DataQuery
                .builder()
                .key(Key.parse(value.getKey()))
                .detail(toDataDetail(value.getDetail()))
                .build();
    }

    public static DataDetail fromDataDetail(sdmxdl.DataDetail value) {
        return DataDetail.valueOf(value.name());
    }

    public static sdmxdl.DataDetail toDataDetail(DataDetail value) {
        return sdmxdl.DataDetail.valueOf(value.name());
    }

    public static Series fromSeries(sdmxdl.Series value) {
        return Series
                .newBuilder()
                .setKey(value.getKey().toString())
                .putAllMeta(value.getMeta())
                .addAllObs(fromCollection(value.getObs(), ProtobufRepositories::fromObs))
                .build();
    }

    public static sdmxdl.Series toSeries(Series value) {
        return sdmxdl.Series
                .builder()
                .key(Key.parse(value.getKey()))
                .meta(value.getMetaMap())
                .obs(toCollection(value.getObsList(), ProtobufRepositories::toObs))
                .build();
    }

    public static Obs fromObs(sdmxdl.Obs value) {
        return Obs
                .newBuilder()
                .setPeriod(value.getPeriod().toString())
                .setValue(value.getValue())
                .putAllMeta(value.getMeta())
                .build();
    }

    public static sdmxdl.Obs toObs(Obs value) {
        return sdmxdl.Obs
                .builder()
                .period(TimeInterval.parse(value.getPeriod()))
                .value(value.getValue())
                .meta(value.getMetaMap())
                .build();
    }

    public static Feature fromFeature(sdmxdl.Feature value) {
        switch (value) {
            case DATA_QUERY_ALL_KEYWORD:
                return Feature.DATA_QUERY_KEY;
            case DATA_QUERY_DETAIL:
                return Feature.DATA_QUERY_DETAIL;
            default:
                throw new RuntimeException();
        }
    }

    public static sdmxdl.Feature toFeature(Feature value) {
        switch (value) {
            case DATA_QUERY_KEY:
                return sdmxdl.Feature.DATA_QUERY_ALL_KEYWORD;
            case DATA_QUERY_DETAIL:
                return sdmxdl.Feature.DATA_QUERY_DETAIL;
            default:
                throw new RuntimeException();
        }
    }
}
