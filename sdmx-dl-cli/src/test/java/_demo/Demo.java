package _demo;

import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.IOException;

import static sdmxdl.DatabaseRef.NO_DATABASE;
import static sdmxdl.Detail.DATA_ONLY;
import static sdmxdl.Languages.ANY;

public class Demo {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxWebManager manager = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(Demo::printEvent)
                .build();

        try (Connection ecb = manager.getConnection("ECB", ANY)) {
            FlowRef exr = FlowRef.parse("EXR");
            printFlow(ecb.getFlow(NO_DATABASE, exr));

            Key chf = Key.parse("M.CHF.EUR.SP00.A");
            Query chfData = Query.builder().key(chf).detail(DATA_ONLY).build();
            ecb.getData(NO_DATABASE, exr, chfData)
                    .getData()
                    .forEach(Demo::printSeries);
        }
    }

    private static void printFlow(Flow flow) {
        System.out.println(flow.getName());
    }

    private static void printSeries(Series series) {
        System.out.println(series.getKey() + ": " + series.getObs().size() + " observations");
    }

    private static void printEvent(WebSource source, String marker, CharSequence message) {
        System.err.println("[" + source.getId() + "] (" + marker + ") " + message);
    }
}
