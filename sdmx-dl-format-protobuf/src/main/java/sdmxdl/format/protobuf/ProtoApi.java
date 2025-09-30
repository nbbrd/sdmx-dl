package sdmxdl.format.protobuf;

import sdmxdl.*;

import static sdmxdl.format.protobuf.WellKnownTypes.*;

@lombok.experimental.UtilityClass
public class ProtoApi {

    public static AboutDto fromAbout() {
        return AboutDto
                .newBuilder()
                .setName(About.NAME)
                .setVersion(About.VERSION)
                .build();
    }

    public static DataRepositoryDto fromDataRepository(DataRepository value) {
        return DataRepositoryDto
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

    public static DataRepository toDataRepository(DataRepositoryDto value) {
        return DataRepository
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

    public static DatabaseDto fromDatabase(Database value) {
        return DatabaseDto
                .newBuilder()
                .setRef(value.getRef().getId())
                .setName(value.getName())
                .build();
    }

    public static Database toDatabase(DatabaseDto value) {
        return new Database(DatabaseRef.parse(value.getRef()), value.getName());
    }

    public static StructureDto fromDataStructure(Structure value) {
        StructureDto.Builder result = StructureDto
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

    public static Structure toDataStructure(StructureDto value) {
        return Structure
                .builder()
                .ref(StructureRef.parse(value.getRef()))
                .dimensions(toCollection(value.getDimensionsList(), ProtoApi::toDimension))
                .attributes(toCollection(value.getAttributesList(), ProtoApi::toAttribute))
                .timeDimensionId(value.hasTimeDimensionId() ? value.getTimeDimensionId() : null)
                .primaryMeasureId(value.getPrimaryMeasureId())
                .name(value.getName())
                .build();
    }

    public static DimensionDto fromDimension(Dimension value) {
        return DimensionDto
                .newBuilder()
                .setId(value.getId())
                .setName(value.getName())
                .setCodelist(fromCodelist(value.getCodelist()))
                .setPosition(value.getPosition())
                .build();
    }

    public static Dimension toDimension(DimensionDto value) {
        return Dimension
                .builder()
                .id(value.getId())
                .name(value.getName())
                .codelist(toCodelist(value.getCodelist()))
                .position(value.getPosition())
                .build();
    }

    public static CodelistDto fromCodelist(Codelist value) {
        return CodelistDto
                .newBuilder()
                .setRef(value.getRef().toString())
                .putAllCodes(value.getCodes())
                .build();
    }

    public Codelist toCodelist(CodelistDto value) {
        return Codelist
                .builder()
                .ref(CodelistRef.parse(value.getRef()))
                .codes(value.getCodesMap())
                .build();
    }

    public static AttributeDto fromAttribute(Attribute value) {
        AttributeDto.Builder result = AttributeDto
                .newBuilder()
                .setId(value.getId())
                .setName(value.getName());
        if (value.getCodelist() != null)
            result.setCodelist(fromCodelist(value.getCodelist()));
        return result
                .setRelationship(fromAttributeRelationship(value.getRelationship()))
                .build();
    }

    public static Attribute toAttribute(AttributeDto value) {
        return Attribute
                .builder()
                .id(value.getId())
                .name(value.getName())
                .codelist(value.hasCodelist() ? toCodelist(value.getCodelist()) : null)
                .relationship(toAttributeRelationship(value.getRelationship()))
                .build();
    }

    public static AttributeRelationshipDto fromAttributeRelationship(AttributeRelationship value) {
        return AttributeRelationshipDto.valueOf(value.name());
    }

    public static AttributeRelationship toAttributeRelationship(AttributeRelationshipDto value) {
        return AttributeRelationship.valueOf(value.name());
    }

    public static FlowDto fromDataflow(Flow value) {
        FlowDto.Builder result = FlowDto
                .newBuilder()
                .setRef(value.getRef().toString())
                .setStructureRef(value.getStructureRef().toString())
                .setName(value.getName());
        if (value.getDescription() != null) {
            result.setDescription(value.getDescription());
        }
        return result.build();
    }

    public static Flow toDataflow(FlowDto value) {
        return Flow
                .builder()
                .ref(FlowRef.parse(value.getRef()))
                .structureRef(StructureRef.parse(value.getStructureRef()))
                .name(value.getName())
                .description(value.hasDescription() ? value.getDescription() : null)
                .build();
    }

    public static DataSetDto fromDataSet(DataSet value) {
        return DataSetDto
                .newBuilder()
                .setRef(value.getRef().toString())
                .setQuery(fromDataQuery(value.getQuery()))
                .addAllData(fromCollection(value.getData(), ProtoApi::fromSeries))
                .build();
    }

    public static DataSet toDataSet(DataSetDto value) {
        return DataSet
                .builder()
                .ref(FlowRef.parse(value.getRef()))
                .query(toDataQuery(value.getQuery()))
                .data(toCollection(value.getDataList(), ProtoApi::toSeries))
                .build();
    }

    public static MetaSetDto fromMetaSet(MetaSet value) {
        return MetaSetDto
                .newBuilder()
                .setFlow(fromDataflow(value.getFlow()))
                .setStructure(fromDataStructure(value.getStructure()))
                .build();
    }

    public static MetaSet toMetaSet(MetaSetDto value) {
        return MetaSet
                .builder()
                .flow(toDataflow(value.getFlow()))
                .structure(toDataStructure(value.getStructure()))
                .build();
    }

    public static QueryDto fromDataQuery(Query value) {
        return QueryDto
                .newBuilder()
                .setKey(value.getKey().toString())
                .setDetail(fromDataDetail(value.getDetail()))
                .build();
    }

    public static Query toDataQuery(QueryDto value) {
        return Query
                .builder()
                .key(Key.parse(value.getKey()))
                .detail(toDataDetail(value.getDetail()))
                .build();
    }

    public static DetailDto fromDataDetail(Detail value) {
        return DetailDto.valueOf(value.name());
    }

    public static Detail toDataDetail(DetailDto value) {
        return Detail.valueOf(value.name());
    }

    public static SeriesDto fromSeries(Series value) {
        return SeriesDto
                .newBuilder()
                .setKey(value.getKey().toString())
                .putAllMeta(value.getMeta())
                .addAllObs(fromCollection(value.getObs(), ProtoApi::fromObs))
                .build();
    }

    public static Series toSeries(SeriesDto value) {
        return Series
                .builder()
                .key(Key.parse(value.getKey()))
                .meta(value.getMetaMap())
                .obs(toCollection(value.getObsList(), ProtoApi::toObs))
                .build();
    }

    public static ObsDto fromObs(Obs value) {
        return ObsDto
                .newBuilder()
                .setPeriod(value.getPeriod().toString())
                .setValue(value.getValue())
                .putAllMeta(value.getMeta())
                .build();
    }

    public static Obs toObs(ObsDto value) {
        return Obs
                .builder()
                .period(TimeInterval.parse(value.getPeriod()))
                .value(value.getValue())
                .meta(value.getMetaMap())
                .build();
    }

    public static FeatureDto fromFeature(Feature value) {
        switch (value) {
            case DATA_QUERY_ALL_KEYWORD:
                return FeatureDto.DATA_QUERY_ALL_KEYWORD;
            case DATA_QUERY_DETAIL:
                return FeatureDto.DATA_QUERY_DETAIL;
            default:
                throw new RuntimeException();
        }
    }

    public static Feature toFeature(FeatureDto value) {
        switch (value) {
            case DATA_QUERY_ALL_KEYWORD:
                return Feature.DATA_QUERY_ALL_KEYWORD;
            case DATA_QUERY_DETAIL:
                return Feature.DATA_QUERY_DETAIL;
            default:
                throw new RuntimeException();
        }
    }

    public static ConfidentialityDto fromConfidentiality(Confidentiality value) {
        return ConfidentialityDto.valueOf(value.name());
    }

    public static Confidentiality toConfidentiality(ConfidentialityDto value) {
        return Confidentiality.valueOf(value.name());
    }
}
