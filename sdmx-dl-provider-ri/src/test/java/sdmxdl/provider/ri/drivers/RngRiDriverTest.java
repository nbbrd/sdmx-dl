package sdmxdl.provider.ri.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.Connection;
import sdmxdl.Query;
import sdmxdl.Flow;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;

import static sdmxdl.Languages.ANY;

public class RngRiDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new RngRiDriver());
    }

    public static void main(String[] args) throws IOException {
        RngRiDriver x = new RngRiDriver();

        for (WebSource source : x.getDefaultSources()) {
            System.out.println(source);
            try (Connection conn = x.connect(source, ANY, DriverAssert.noOpWebContext())) {
                for (Flow flow : conn.getFlows()) {
                    System.out.println(flow);
                    System.out.println(conn.getStructure(flow.getRef()));
                    conn.getDataStream(flow.getRef(), Query.ALL)
                            .forEach(series -> System.out.println(series.getKey() + " " + series.getObs().size()));
                }
            }
        }
    }
}
