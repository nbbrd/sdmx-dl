package sdmxdl.csv;

import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;
import org.junit.jupiter.api.Test;
import sdmxdl.DataStructure;
import sdmxdl.DataflowRef;
import sdmxdl.Obs;
import sdmxdl.Series;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.csv.SdmxCsvFieldWriter.*;
import static sdmxdl.samples.RepoSamples.*;

public class SdmxCsvFieldWriterTest {

    private String head(SdmxCsvFieldWriter x) {
        StringWriter result = new StringWriter();
        try (Csv.Writer csv = Csv.Writer.of(Csv.Format.DEFAULT, Csv.WriterOptions.DEFAULT, result, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            x.writeHead(csv::writeField);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result.toString();
    }

    private String body(SdmxCsvFieldWriter x, Series series, Obs obs) {
        StringWriter result = new StringWriter();
        try (Csv.Writer csv = Csv.Writer.of(Csv.Format.DEFAULT, Csv.WriterOptions.DEFAULT, result, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            x.writeBody(series, obs, csv::writeField);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result.toString();
    }

    @Test
    public void testOnDataflow() {
        assertThatNullPointerException().isThrownBy(() -> onDataflow(null, DataflowRef.parse("abc")));
        assertThatNullPointerException().isThrownBy(() -> onDataflow("xyz", null));

        assertThat(onDataflow("xyz", DataflowRef.parse("abc")))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("xyz");
                    assertThat(body(x, S1, OBS1)).isEqualTo("all:abc(latest)");
                });
    }

    @Test
    public void testOnKeyDimensions() {
        assertThatNullPointerException().isThrownBy(() -> onKeyDimensions((DataStructure) null));

        assertThat(onKeyDimensions(STRUCT))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("FREQ,REGION,SECTOR");
                    assertThat(body(x, S1, OBS1)).isEqualTo("M,BE,INDUSTRY");
                });

        assertThatNullPointerException().isThrownBy(() -> onKeyDimensions((List<String>) null));

        assertThat(onKeyDimensions(asList("FREQ", "REGION", "SECTOR")))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("FREQ,REGION,SECTOR");
                    assertThat(body(x, S1, OBS1)).isEqualTo("M,BE,INDUSTRY");
                });
    }

    @Test
    public void testOnTimeDimension() {
        Formatter<LocalDateTime> formatter = Formatter.onDateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assertThatNullPointerException().isThrownBy(() -> onTimeDimension((DataStructure) null, formatter));
        assertThatNullPointerException().isThrownBy(() -> onTimeDimension(STRUCT, null));

        assertThat(onTimeDimension(STRUCT, formatter))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("TIME");
                    assertThat(body(x, S1, OBS1)).isEqualTo("2010-01-01T00:00:00");
                });

        assertThatNullPointerException().isThrownBy(() -> onTimeDimension((String) null, formatter));
        assertThatNullPointerException().isThrownBy(() -> onTimeDimension("xyz", null));

        assertThat(onTimeDimension("xyz", formatter))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("xyz");
                    assertThat(body(x, S1, OBS1)).isEqualTo("2010-01-01T00:00:00");
                });
    }

    @Test
    public void testOnObsValue() {
        Formatter<Number> formatter = Formatter.onDouble().compose(Number::doubleValue);

        assertThatNullPointerException().isThrownBy(() -> onObsValue(null, formatter));
        assertThatNullPointerException().isThrownBy(() -> onObsValue("xyz", null));

        assertThat(onObsValue("xyz", formatter))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("xyz");
                    assertThat(body(x, S1, OBS1)).isEqualTo(String.valueOf(Math.PI));
                });
    }

    @Test
    public void testOnAttributes() {
        assertThatNullPointerException().isThrownBy(() -> onAttributes((DataStructure) null));

        assertThat(onAttributes(STRUCT))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("TITLE");
                    assertThat(body(x, S1, OBS1)).isEqualTo("hello world");
                });

        assertThatNullPointerException().isThrownBy(() -> onAttributes((List<String>) null));

        assertThat(onAttributes(singletonList("TITLE")))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("TITLE");
                    assertThat(body(x, S1, OBS1)).isEqualTo("hello world");
                });
    }

    @Test
    public void testOnCompactObsAttributes() {
        Formatter<Map<String, String>> formatter = o -> String.valueOf(o != null ? o.size() : -1);

        assertThatNullPointerException().isThrownBy(() -> onCompactObsAttributes(null, formatter));
        assertThatNullPointerException().isThrownBy(() -> onCompactObsAttributes("xyz", null));

        assertThat(onCompactObsAttributes("xyz", formatter))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("xyz");
                    assertThat(body(x, S1, OBS1)).isEqualTo("0");
                });
    }

    @Test
    public void testOnSeriesKey() {
        assertThatNullPointerException().isThrownBy(() -> onSeriesKey(null));

        assertThat(onSeriesKey("xyz"))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("xyz");
                    assertThat(body(x, S1, OBS1)).isEqualTo("M.BE.INDUSTRY");
                });
    }

    @Test
    public void testOnConstant() {
        assertThatNullPointerException().isThrownBy(() -> onConstant(null, "abc"));
        assertThatNullPointerException().isThrownBy(() -> onConstant("xyz", null));

        assertThat(onConstant("xyz", "abc"))
                .satisfies(x -> {
                    assertThat(head(x)).isEqualTo("xyz");
                    assertThat(body(x, S1, OBS1)).isEqualTo("abc");
                });
    }
}
