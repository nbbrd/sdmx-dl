package sdmxdl.format.protobuf;

import sdmxdl.*;

import static sdmxdl.format.protobuf.WellKnownTypes.*;

@lombok.experimental.UtilityClass
public class ProtobufRepositories {

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
                .addAllCatalogs(fromCollection(value.getCatalogs(), ProtobufRepositories::fromCatalog))
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
                .catalogs(toCollection(value.getCatalogsList(), ProtobufRepositories::toCatalog))
                .structures(toCollection(value.getStructuresList(), ProtobufRepositories::toDataStructure))
                .flows(toCollection(value.getFlowsList(), ProtobufRepositories::toDataflow))
                .dataSets(toCollection(value.getDataSetsList(), ProtobufRepositories::toDataSet))
                .creationTime(toInstant(value.getCreationTime()))
                .expirationTime(toInstant(value.getExpirationTime()))
                .build();
    }

    public static Catalog fromCatalog(sdmxdl.Catalog value) {
        return Catalog
                .newBuilder()
                .setId(value.getId().getId())
                .setName(value.getName())
                .build();
    }

    public static sdmxdl.Catalog toCatalog(Catalog value) {
        return new sdmxdl.Catalog(sdmxdl.CatalogRef.parse(value.getId()), value.getName());
    }

    public static DataStructure fromDataStructure(Structure value) {
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

    public static Structure toDataStructure(DataStructure value) {
        return Structure
                .builder()
                .ref(StructureRef.parse(value.getRef()))
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

    public static Dataflow fromDataflow(Flow value) {
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

    public static Flow toDataflow(Dataflow value) {
        return Flow
                .builder()
                .ref(FlowRef.parse(value.getRef()))
                .structureRef(StructureRef.parse(value.getStructureRef()))
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
                .ref(FlowRef.parse(value.getRef()))
                .query(toDataQuery(value.getQuery()))
                .data(toCollection(value.getDataList(), ProtobufRepositories::toSeries))
                .build();
    }

    public static DataQuery fromDataQuery(Query value) {
        return DataQuery
                .newBuilder()
                .setKey(value.getKey().toString())
                .setDetail(fromDataDetail(value.getDetail()))
                .build();
    }

    public static Query toDataQuery(DataQuery value) {
        return Query
                .builder()
                .key(Key.parse(value.getKey()))
                .detail(toDataDetail(value.getDetail()))
                .build();
    }

    public static DataDetail fromDataDetail(Detail value) {
        return DataDetail.valueOf(value.name());
    }

    public static Detail toDataDetail(DataDetail value) {
        return Detail.valueOf(value.name());
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
}
