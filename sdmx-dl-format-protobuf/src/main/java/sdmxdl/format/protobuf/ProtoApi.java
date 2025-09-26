package sdmxdl.format.protobuf;

import static sdmxdl.format.protobuf.WellKnownTypes.*;

@lombok.experimental.UtilityClass
public class ProtoApi {

    public static About fromAbout() {
        return About
                .newBuilder()
                .setName(sdmxdl.About.NAME)
                .setVersion(sdmxdl.About.VERSION)
                .build();
    }

    public static DataRepository fromDataRepository(sdmxdl.DataRepository value) {
        return DataRepository
                .newBuilder()
                .setName(value.getName())
                .addAllDatabases(fromCollection(value.getDatabases(), ProtoApi::fromDatabase))
                .addAllStructures(fromCollection(value.getStructures(), ProtoApi::fromDataStructure))
                .addAllFlows(fromCollection(value.getFlows(), ProtoApi::fromDataflow))
                .addAllDataSets(fromCollection(value.getDataSets(), ProtoApi::fromDataSet))
                .setCreationTime(fromInstant(value.getCreationTime()))
                .setExpirationTime(fromInstant(value.getExpirationTime()))
                .build();
    }

    public static sdmxdl.DataRepository toDataRepository(DataRepository value) {
        return sdmxdl.DataRepository
                .builder()
                .name(value.getName())
                .databases(toCollection(value.getDatabasesList(), ProtoApi::toDatabase))
                .structures(toCollection(value.getStructuresList(), ProtoApi::toDataStructure))
                .flows(toCollection(value.getFlowsList(), ProtoApi::toDataflow))
                .dataSets(toCollection(value.getDataSetsList(), ProtoApi::toDataSet))
                .creationTime(toInstant(value.getCreationTime()))
                .expirationTime(toInstant(value.getExpirationTime()))
                .build();
    }

    public static Database fromDatabase(sdmxdl.Database value) {
        return Database
                .newBuilder()
                .setRef(value.getRef().getId())
                .setName(value.getName())
                .build();
    }

    public static sdmxdl.Database toDatabase(Database value) {
        return new sdmxdl.Database(sdmxdl.DatabaseRef.parse(value.getRef()), value.getName());
    }

    public static Structure fromDataStructure(sdmxdl.Structure value) {
        Structure.Builder result = Structure
                .newBuilder()
                .setRef(value.getRef().toString())
                .addAllDimensions(fromCollection(value.getDimensions(), ProtoApi::fromDimension))
                .addAllAttributes(fromCollection(value.getAttributes(), ProtoApi::fromAttribute));
        if (value.getTimeDimensionId() != null) {
            result.setTimeDimensionId(value.getTimeDimensionId());
        }
        return result
                .setPrimaryMeasureId(value.getPrimaryMeasureId())
                .setName(value.getName())
                .build();
    }

    public static sdmxdl.Structure toDataStructure(Structure value) {
        return sdmxdl.Structure
                .builder()
                .ref(sdmxdl.StructureRef.parse(value.getRef()))
                .dimensions(toCollection(value.getDimensionsList(), ProtoApi::toDimension))
                .attributes(toCollection(value.getAttributesList(), ProtoApi::toAttribute))
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
                .ref(sdmxdl.CodelistRef.parse(value.getRef()))
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

    public static Flow fromDataflow(sdmxdl.Flow value) {
        Flow.Builder result = Flow
                .newBuilder()
                .setRef(value.getRef().toString())
                .setStructureRef(value.getStructureRef().toString())
                .setName(value.getName());
        if (value.getDescription() != null) {
            result.setDescription(value.getDescription());
        }
        return result.build();
    }

    public static sdmxdl.Flow toDataflow(Flow value) {
        return sdmxdl.Flow
                .builder()
                .ref(sdmxdl.FlowRef.parse(value.getRef()))
                .structureRef(sdmxdl.StructureRef.parse(value.getStructureRef()))
                .name(value.getName())
                .description(value.hasDescription() ? value.getDescription() : null)
                .build();
    }

    public static DataSet fromDataSet(sdmxdl.DataSet value) {
        return DataSet
                .newBuilder()
                .setRef(value.getRef().toString())
                .setQuery(fromDataQuery(value.getQuery()))
                .addAllData(fromCollection(value.getData(), ProtoApi::fromSeries))
                .build();
    }

    public static sdmxdl.DataSet toDataSet(DataSet value) {
        return sdmxdl.DataSet
                .builder()
                .ref(sdmxdl.FlowRef.parse(value.getRef()))
                .query(toDataQuery(value.getQuery()))
                .data(toCollection(value.getDataList(), ProtoApi::toSeries))
                .build();
    }

    public static MetaSet fromMetaSet(sdmxdl.MetaSet value) {
        return MetaSet
                .newBuilder()
                .setFlow(fromDataflow(value.getFlow()))
                .setStructure(fromDataStructure(value.getStructure()))
                .build();
    }

    public static sdmxdl.MetaSet toMetaSet(MetaSet value) {
        return sdmxdl.MetaSet
                .builder()
                .flow(toDataflow(value.getFlow()))
                .structure(toDataStructure(value.getStructure()))
                .build();
    }

    public static Query fromDataQuery(sdmxdl.Query value) {
        return Query
                .newBuilder()
                .setKey(value.getKey().toString())
                .setDetail(fromDataDetail(value.getDetail()))
                .build();
    }

    public static sdmxdl.Query toDataQuery(Query value) {
        return sdmxdl.Query
                .builder()
                .key(sdmxdl.Key.parse(value.getKey()))
                .detail(toDataDetail(value.getDetail()))
                .build();
    }

    public static Detail fromDataDetail(sdmxdl.Detail value) {
        return Detail.valueOf(value.name());
    }

    public static sdmxdl.Detail toDataDetail(Detail value) {
        return sdmxdl.Detail.valueOf(value.name());
    }

    public static Series fromSeries(sdmxdl.Series value) {
        return Series
                .newBuilder()
                .setKey(value.getKey().toString())
                .putAllMeta(value.getMeta())
                .addAllObs(fromCollection(value.getObs(), ProtoApi::fromObs))
                .build();
    }

    public static sdmxdl.Series toSeries(Series value) {
        return sdmxdl.Series
                .builder()
                .key(sdmxdl.Key.parse(value.getKey()))
                .meta(value.getMetaMap())
                .obs(toCollection(value.getObsList(), ProtoApi::toObs))
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
                .period(sdmxdl.TimeInterval.parse(value.getPeriod()))
                .value(value.getValue())
                .meta(value.getMetaMap())
                .build();
    }

    public static Feature fromFeature(sdmxdl.Feature value) {
        switch (value) {
            case DATA_QUERY_ALL_KEYWORD:
                return Feature.DATA_QUERY_ALL_KEYWORD;
            case DATA_QUERY_DETAIL:
                return Feature.DATA_QUERY_DETAIL;
            default:
                throw new RuntimeException();
        }
    }

    public static sdmxdl.Feature toFeature(Feature value) {
        switch (value) {
            case DATA_QUERY_ALL_KEYWORD:
                return sdmxdl.Feature.DATA_QUERY_ALL_KEYWORD;
            case DATA_QUERY_DETAIL:
                return sdmxdl.Feature.DATA_QUERY_DETAIL;
            default:
                throw new RuntimeException();
        }
    }

    public static Confidentiality fromConfidentiality(sdmxdl.Confidentiality value) {
        return Confidentiality.valueOf(value.name());
    }

    public static sdmxdl.Confidentiality toConfidentiality(Confidentiality value) {
        return sdmxdl.Confidentiality.valueOf(value.name());
    }
}
