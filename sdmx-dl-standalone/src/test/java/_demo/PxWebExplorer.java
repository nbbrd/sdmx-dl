package _demo;

import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            return Report.of(p.getSource(), Status.CONNECTION_FAILURE, fatal);
        }

        SourceRequest sourceRequest;
        List<Database> databases;
        try {
            sourceRequest = SourceRequest.builder().build();
            databases = p.getDatabases(sourceRequest).stream().sorted(comparing(Objects::toString)).collect(toList());
        } catch (Exception fatal) {
            return Report.of(p.getSource(), Status.DB_FAILURE, fatal);
        }

        if (databases.isEmpty()) {
            return Report.of(p.getSource(), Status.NO_DB);
        }

        DatabaseRequest databaseRequest;
        Iterator<Database> db = databases.iterator();
        List<Flow> flows;
        do {
            try {
                databaseRequest = DatabaseRequest.builderOf(sourceRequest).database(db.next().getRef()).build();
                flows = p.getFlows(databaseRequest).stream().sorted(comparing(Objects::toString)).collect(toList());
            } catch (Exception fatal) {
                return Report.of(p.getSource(), Status.FLOW_FAILURE, fatal);
            }
        } while (db.hasNext() && flows.isEmpty());

        if (flows.isEmpty()) {
            return Report.of(p.getSource(), Status.NO_FLOW);
        }

//        FlowRequest flowRequest;
//        Iterator<Flow> flow = flows.iterator();
//        MetaSet metaSet;
//        do {
//            try {
//                flowRequest = FlowRequest.builderOf(databaseRequest).flow(flow.next().getRef()).build();
//                metaSet = p.getMeta(flowRequest);
//            } catch (Exception fatal) {
//                return Report.of(p.getSource(), Status.META_FAILURE, fatal);
//            }
//        } while (flow.hasNext());
//
//        if (!isValidStructure(metaSet)) {
//            return Report.of(p.getSource(), Status.NO_META, new IllegalStateException("Invalid structure: some dimensions have no codes"));
//        }

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
        SUCCESS
    }

    @lombok.Value
    private static class Report {

        static Report of(WebSource source, Status status, Exception fatal) {
            return new Report(source.getId(), status, fatal.getClass().getSimpleName(), fatal.getMessage());
        }

        static Report of(WebSource source, Status status) {
            return new Report(source.getId(), status, null, null);
        }

        String source;
        Status status;
        String error;
        String message;
    }

    private static boolean isValidStructure(MetaSet metaSet) {
        return metaSet.getStructure().getDimensions().stream()
                .noneMatch(dimension -> dimension.getCodelist().getCodes().isEmpty());
    }
}
