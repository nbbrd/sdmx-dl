package _demo;

import nbbrd.io.curl.CurlHttpURLConnection;
import sdmxdl.*;
import sdmxdl.provider.web.SingleNetworkingSupport;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;

public class Demo {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxWebManager manager = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .networking(SingleNetworkingSupport.builder().id("CURL").urlConnectionFactoryOf(CurlHttpURLConnection::of).build())
                .languages(LanguagePriorityList.ANY)
                .onEvent(Demo::printEvent)
                .build();

        try (Connection ecb = manager.getConnection("ECB")) {
            DataflowRef exr = DataflowRef.parse("EXR");
            System.out.println(ecb.getFlow(exr).getName());

            Key chf = Key.parse("M.CHF.EUR.SP00.A");
            ecb.getData(exr, DataQuery.builder().key(chf).build())
                    .getData()
                    .stream()
                    .map(Series::getKey)
                    .forEach(System.out::println);
        }
    }

    private static void printEvent(SdmxWebSource source, Marker marker, CharSequence message) {
        System.err.println("[" + source.getId() + "] (" + marker + ") " + message);
    }
}
