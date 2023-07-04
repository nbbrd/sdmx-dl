package _demo;

import nbbrd.io.curl.CurlHttpURLConnection;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

public class Demo {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxWebManager manager = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .network(EasyNetwork.DEFAULT
                        .toBuilder()
                        .urlConnectionFactory(() -> CurlHttpURLConnection::of)
                        .build())
                .languages(LanguagePriorityList.ANY)
                .eventListener((source, message) -> System.err.println("[" + source.getId() + "] " + message))
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
}
