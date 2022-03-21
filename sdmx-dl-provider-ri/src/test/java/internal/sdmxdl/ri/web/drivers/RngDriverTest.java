package internal.sdmxdl.ri.web.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.Connection;
import sdmxdl.DataQuery;
import sdmxdl.Dataflow;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.WebDriverAssert;

import java.io.IOException;

public class RngDriverTest {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new RngDriver());
    }

    public static void main(String[] args) throws IOException {
        RngDriver x = new RngDriver();
        WebContext context = WebContext.builder().build();

        for (SdmxWebSource source : x.getDefaultSources()) {
            System.out.println(source);
            try (Connection conn = x.connect(source, context)) {
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
