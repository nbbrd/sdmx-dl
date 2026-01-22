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
        } catch (Exception e) {
            return new Report(p.getSource().getId(), Status.CONNECTION_FAILURE, e.getClass().getSimpleName(), e.getMessage());
        }

        List<Database> databases;
        try {
            databases = p.getDatabases(SourceRequest.builder().build())
                    .stream().sorted(comparing(Objects::toString)).collect(toList());
        } catch (Exception e) {
            return new Report(p.getSource().getId(), Status.DB_FAILURE, e.getClass().getSimpleName(), e.getMessage());
        }

        if (databases.isEmpty()) {
            return new Report(p.getSource().getId(), Status.NO_DB, null, null);
        }

        List<Flow> flows;
        Iterator<Database> db = databases.iterator();
        do {
            try {
                flows = p.getFlows(DatabaseRequest.builder().database(db.next().getRef()).build())
                        .stream().sorted(comparing(Objects::toString)).collect(toList());
            } catch (Exception e) {
                return new Report(p.getSource().getId(), Status.FLOW_FAILURE, e.getClass().getSimpleName(), e.getMessage());
            }
        } while (flows.isEmpty() && db.hasNext());

        if (flows.isEmpty()) {
            return new Report(p.getSource().getId(), Status.NO_FLOW, null, null);
        }

        return new Report(p.getSource().getId(), Status.SUCCESS, null, null);

    }

    private static void print(Map.Entry<Status, List<Report>> entry) {
        System.out.println("==== " + entry.getKey() + " ====");
        entry.getValue().forEach(System.out::println);
        System.out.println();
    }

    enum Status {
        SUCCESS,
        CONNECTION_FAILURE,
        DB_FAILURE,
        NO_DB,
        FLOW_FAILURE,
        NO_FLOW
    }

    @lombok.Value
    private static class Report {
        String source;
        Status status;
        String error;
        String message;
    }
}
