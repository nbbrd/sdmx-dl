package _demo;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class PxWebExplorer {

    @nbbrd.design.Demo
    public static void main(String[] args) {

        System.setProperty("enablePxWebDriver", "true");

        SdmxWebManager manager = SdmxWebManager.ofServiceLoader();
        manager.getSources()
                .values()
                .parallelStream()
                .filter(source -> source.getDriver().equals("PX_PXWEB"))
                .filter(source -> !source.isAlias())
                .map(manager::using)
                .map(PxWebExplorer::explore)
                .collect(groupingBy(Report::getStatus))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(PxWebExplorer::print);
    }

    private static Report explore(Provider<WebSource> p) {
        try {
            p.testConnection(SourceRequest.builder().build());
        } catch (Exception fatal) {
            return Report.of(p.getSource(), Status.CONNECTION_FAILURE, fatal, SourceRequest.builder().build());
        }

        SourceRequest sourceRequest;
        List<Database> databases;
        {
            sourceRequest = SourceRequest.builder().build();
            try {
                databases = p.getDatabases(sourceRequest).stream().sorted(comparing(Objects::toString)).collect(toList());
            } catch (Exception fatal) {
                return Report.of(p.getSource(), Status.DB_FAILURE, fatal, sourceRequest);
            }
        }

        if (databases.isEmpty()) {
            return Report.of(p.getSource(), Status.NO_DB);
        }

        DatabaseRequest databaseRequest;
        List<Flow> flows;
        {
            Iterator<Database> db = databases.iterator();
            do {
                databaseRequest = DatabaseRequest.builderOf(sourceRequest).database(db.next().getRef()).build();
                try {
                    flows = p.getFlows(databaseRequest).stream().sorted(comparing(Objects::toString)).collect(toList());
                } catch (Exception fatal) {
                    return Report.of(p.getSource(), Status.FLOW_FAILURE, fatal, databaseRequest);
                }
            } while (db.hasNext() && flows.isEmpty());
        }

        if (flows.isEmpty()) {
            return Report.of(p.getSource(), Status.NO_FLOW);
        }

        FlowRequest flowRequest;
        MetaSet metaSet;
        {
            Iterator<Flow> flow = flows.iterator();
            do {
                flowRequest = FlowRequest.builderOf(databaseRequest).flow(flow.next().getRef()).build();
                try {
                    metaSet = p.getMeta(flowRequest);
                } catch (Exception fatal) {
                    return Report.of(p.getSource(), Status.META_FAILURE, fatal, flowRequest);
                }
            } while (flow.hasNext() && isInvalidStructure(metaSet));
        }

        if (isInvalidStructure(metaSet)) {
            return Report.of(p.getSource(), Status.NO_META);
        }

        KeyRequest keyRequest;
        DataSet dataSet;
        {
            Iterator<Key> key = keyIterator(metaSet.getStructure());
            do {
                keyRequest = KeyRequest.builderOf(flowRequest).key(key.next()).build();
                try {
                    dataSet = p.getData(keyRequest);
                } catch (Exception fatal) {
                    return Report.of(p.getSource(), Status.DATA_FAILURE, fatal, keyRequest);
                }
            } while (key.hasNext() && dataSet.getData().isEmpty());
        }

        if (dataSet.getData().isEmpty()) {
            return Report.of(p.getSource(), Status.NO_DATA);
        }

        return Report.of(p.getSource(), Status.SUCCESS);
    }

    private static void print(Map.Entry<Status, List<Report>> entry) {
        System.out.println("==== " + entry.getKey() + " ====");
        entry.getValue().forEach(System.out::println);
        System.out.println();
    }

    enum Status {
        CONNECTION_FAILURE,
        DB_FAILURE,
        NO_DB,
        FLOW_FAILURE,
        NO_FLOW,
        META_FAILURE,
        NO_META,
        DATA_FAILURE,
        NO_DATA,
        SUCCESS
    }

    @lombok.Value
    private static class Report {

        static Report of(WebSource source, Status status, Exception fatal, Object request) {
            return new Report(source.getId(), status, fatal.getClass().getSimpleName(), fatal.getMessage(), request);
        }

        static Report of(WebSource source, Status status) {
            return new Report(source.getId(), status, null, null, null);
        }

        @NonNull
        String source;

        @NonNull
        Status status;

        @Nullable
        String error;

        @Nullable
        String message;

        @Nullable
        Object request;
    }

    private static boolean isInvalidStructure(MetaSet metaSet) {
        return metaSet.getStructure().getDimensions().stream()
                .anyMatch(dimension -> dimension.getCodelist().getCodes().isEmpty());
    }

    private static Iterator<Key> keyIterator(Structure structure) {
        Key.Builder key = Key.builder(structure);
        for (Dimension dimension : structure.getDimensions()) {
            key.put(dimension.getId(), dimension.getCodelist().getCodes().keySet().stream().findFirst().orElseThrow(() -> new RuntimeException("boom")));
        }
        return Collections.singletonList(key.build()).iterator();
    }
}
