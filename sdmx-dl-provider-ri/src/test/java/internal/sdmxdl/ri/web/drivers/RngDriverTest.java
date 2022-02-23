package internal.sdmxdl.ri.web.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.DataQuery;
import sdmxdl.Dataflow;
import sdmxdl.SdmxConnection;
import tests.sdmxdl.web.SdmxWebDriverAssert;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.IOException;

public class RngDriverTest {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new RngDriver());
    }

    public static void main(String[] args) throws IOException {
        RngDriver x = new RngDriver();
        SdmxWebContext context = SdmxWebContext.builder().build();

        for (SdmxWebSource source : x.getDefaultSources()) {
            System.out.println(source);
            try (SdmxConnection conn = x.connect(source, context)) {
                for (Dataflow dataflow : conn.getFlows()) {
                    System.out.println(dataflow);
                    System.out.println(conn.getStructure(dataflow.getRef()));
                    conn.getDataStream(dataflow.getRef(), DataQuery.ALL)
                            .forEach(series -> System.out.println(series.getKey() + " " + series.getObs().size()));
                }
            }
        }
    }
}
