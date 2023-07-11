package _demo;

import nbbrd.io.curl.CurlHttpURLConnection;
import sdmxdl.*;
import sdmxdl.provider.web.SingleNetworkingSupport;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.stream.Stream;

import static sdmxdl.DataDetail.DATA_ONLY;
import static sdmxdl.Languages.ANY;

public class Demo {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxWebManager manager = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .networking(SingleNetworkingSupport.builder().id("CURL").urlConnectionFactoryOf(CurlHttpURLConnection::of).build())
                .onEvent(Demo::printEvent)
                .build();

        try (Connection ecb = manager.getConnection("ECB", ANY)) {
            DataflowRef exr = DataflowRef.parse("EXR");
            printFlow(ecb.getFlow(exr));

            Key chf = Key.parse("M.CHF.EUR.SP00.A");
            DataQuery chfData = DataQuery.builder().key(chf).detail(DATA_ONLY).build();
            try (Stream<Series> dataStream = ecb.getDataStream(exr, chfData)) {
                dataStream.forEach(Demo::printSeries);
            }
        }
    }

    private static void printFlow(Dataflow flow) {
        System.out.println(flow.getName());
    }

    private static void printSeries(Series series) {
        System.out.println(series.getKey() + ": " + series.getObs().size() + " observations");
    }

    private static void printEvent(SdmxWebSource source, String marker, CharSequence message) {
        System.err.println("[" + source.getId() + "] (" + marker + ") " + message);
    }
}
