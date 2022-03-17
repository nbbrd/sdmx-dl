package sdmxdl.csv;

import nbbrd.io.function.IOFunction;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.picocsv.Csv;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.DataSet;

import java.util.Locale;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.csv.SdmxCsvFields.*;
import static tests.sdmxdl.api.RepoSamples.DATA_SET;
import static tests.sdmxdl.api.RepoSamples.STRUCT;

public class SdmxPicocsvFormatterTest {

    @Test
    public void testFormatToString() {
        Function<Picocsv.Formatter<DataSet>, String> extractor = IOFunction.unchecked(o -> o.formatToString(DATA_SET));
        SdmxPicocsvFormatter x = SdmxPicocsvFormatter.builder().build();

        assertThat(x.getFormatter(STRUCT))
                .as("Default options")
                .extracting(extractor, Assertions.STRING)
                .contains("DATAFLOW,FREQ,REGION,SECTOR,TIME,OBS_VALUE,OBS_STATUS,TITLE,SERIESKEY")
                .contains("NBB:XYZ(v2.0),M,BE,INDUSTRY,2010-01-01T00:00:00,3.141592653589793,,hello world,M.BE.INDUSTRY")
                .contains("NBB:XYZ(v2.0),M,BE,INDUSTRY,2010-02-01T00:00:00,2.718281828459045,,hello world,M.BE.INDUSTRY");

        assertThat(x.toBuilder().fields(asList(SERIESKEY, TIME_DIMENSION, OBS_VALUE)).build().getFormatter(STRUCT))
                .as("Custom fields")
                .extracting(extractor, Assertions.STRING)
                .contains("SERIESKEY,TIME,OBS_VALUE")
                .contains("M.BE.INDUSTRY,2010-01-01T00:00:00,3.141592653589793")
                .contains("M.BE.INDUSTRY,2010-02-01T00:00:00,2.718281828459045");

        assertThat(x.getFormatter(STRUCT).toBuilder().format(Csv.Format.builder().delimiter(';').build()).build())
                .as("Custom format")
                .extracting(extractor, Assertions.STRING)
                .contains("DATAFLOW;FREQ;REGION;SECTOR;TIME;OBS_VALUE;OBS_STATUS;TITLE;SERIESKEY")
                .contains("NBB:XYZ(v2.0);M;BE;INDUSTRY;2010-01-01T00:00:00;3.141592653589793;;hello world;M.BE.INDUSTRY")
                .contains("NBB:XYZ(v2.0);M;BE;INDUSTRY;2010-02-01T00:00:00;2.718281828459045;;hello world;M.BE.INDUSTRY");

        assertThat(x.toBuilder().locale(Locale.FRENCH).build().getFormatter(STRUCT))
                .as("Custom locale")
                .extracting(extractor, Assertions.STRING)
                .contains("DATAFLOW,FREQ,REGION,SECTOR,TIME,OBS_VALUE,OBS_STATUS,TITLE,SERIESKEY")
                .contains("NBB:XYZ(v2.0),M,BE,INDUSTRY,2010-01-01T00:00:00,\"3,141592653589793\",,hello world,M.BE.INDUSTRY")
                .contains("NBB:XYZ(v2.0),M,BE,INDUSTRY,2010-02-01T00:00:00,\"2,718281828459045\",,hello world,M.BE.INDUSTRY");

        assertThat(x.toBuilder().ignoreHeader(true).build().getFormatter(STRUCT))
                .as("Ignore header")
                .extracting(extractor, Assertions.STRING)
                .doesNotContain("DATAFLOW,FREQ,REGION,SECTOR,TIME,OBS_VALUE,OBS_STATUS,TITLE,SERIESKEY")
                .contains("NBB:XYZ(v2.0),M,BE,INDUSTRY,2010-01-01T00:00:00,3.141592653589793,,hello world,M.BE.INDUSTRY")
                .contains("NBB:XYZ(v2.0),M,BE,INDUSTRY,2010-02-01T00:00:00,2.718281828459045,,hello world,M.BE.INDUSTRY");

        assertThat(x.toBuilder().customFactory(DATAFLOW, dataSet -> SdmxCsvFieldWriter.onConstant("FLOW", dataSet.getRef().getId())).build().getFormatter(STRUCT))
                .as("Custom factory")
                .extracting(extractor, Assertions.STRING)
                .contains("FLOW,FREQ,REGION,SECTOR,TIME,OBS_VALUE,OBS_STATUS,TITLE,SERIESKEY")
                .contains("XYZ,M,BE,INDUSTRY,2010-01-01T00:00:00,3.141592653589793,,hello world,M.BE.INDUSTRY")
                .contains("XYZ,M,BE,INDUSTRY,2010-02-01T00:00:00,2.718281828459045,,hello world,M.BE.INDUSTRY");
    }
}
