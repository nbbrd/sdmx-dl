package sdmxdl.provider.ri.web.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.Connection;
import sdmxdl.DataQuery;
import sdmxdl.Dataflow;
import sdmxdl.web.SdmxWebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;

import static sdmxdl.Languages.ANY;

public class RngDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new RngDriver());
    }

    public static void main(String[] args) throws IOException {
        RngDriver x = new RngDriver();

        for (SdmxWebSource source : x.getDefaultSources()) {
            System.out.println(source);
            try (Connection conn = x.connect(source, ANY, DriverAssert.noOpWebContext())) {
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
