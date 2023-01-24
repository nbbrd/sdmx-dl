/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package tests.sdmxdl.format;

import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOSupplier;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.DataDetail;
import sdmxdl.Key;
import sdmxdl.format.DataCursor;
import tests.sdmxdl.api.TckUtil;

import java.io.IOException;

@SuppressWarnings("CatchMayIgnoreException")
@lombok.experimental.UtilityClass
public class DataCursorAssert {

    public void assertCompliance(IOSupplier<DataCursor> supplier, Key key, DataDetail detail) {
        TckUtil.run(s -> assertCompliance(s, supplier, key, detail));
    }

    public void assertCompliance(SoftAssertions s, IOSupplier<DataCursor> supplier, Key key, DataDetail detail) throws Exception {
        try (DataCursor c = supplier.getWithIO()) {
            while (c.nextSeries()) {
                assertNonnull(s, c);
                // header
                s.assertThat(c.getSeriesKey()).isNotNull().isEqualTo(c.getSeriesKey()).matches(key::contains);
                // attributes
                if (!detail.isMetaRequested()) {
                    s.assertThat(c.getSeriesAttributes()).isEmpty();
                } else {
                    s.assertThat(c.getSeriesAttributes()).isNotNull().isEqualTo(c.getSeriesAttributes());
                    s.assertThat(c.getSeriesAttribute("hello")).isEqualTo(c.getSeriesAttribute("hello"));
                }
                // values
                if (!detail.isDataRequested()) {
                    s.assertThat(c.nextObs()).isFalse();
                } else {
                    while (c.nextObs()) {
                        s.assertThat(c.getObsPeriod()).isEqualTo(c.getObsPeriod());
                        s.assertThat(c.getObsValue()).isEqualTo(c.getObsValue());
                        if (c.getObsPeriod() == null) {
                            // FIXME: problem with scrictDatePattern
//                        s.assertThat(c.getObsValue())
//                                .as("Non-null value must have non-null period")
//                                .isNull();
                        }
                    }
                }
            }
        }

        try (DataCursor c = supplier.getWithIO()) {
            //noinspection RedundantExplicitClose
            c.close();
        } catch (Exception ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }

        try (DataCursor c = supplier.getWithIO()) {
            nextSeriesToEnd().andThen(DataCursor::nextSeries).acceptWithIO(c);
        } catch (Exception ex) {
            s.fail("Subsequent calls to #nextSeries must not raise exception", ex);
        }

        assertState(s, supplier, DataCursor::nextSeries, "#nextSeries");

        assertSeriesState(s, supplier, DataCursor::getSeriesKey, "#getSeriesKey");
        assertSeriesState(s, supplier, DataCursor::getSeriesAttributes, "#getSeriesAttributes");
        assertSeriesState(s, supplier, o -> o.getSeriesAttribute(""), "#getSeriesAttribute");
        assertSeriesState(s, supplier, DataCursor::nextObs, "#nextObs");

        assertObsState(s, supplier, DataCursor::getObsPeriod, "#getObsPeriod");
        assertObsState(s, supplier, DataCursor::getObsValue, "#getObsValue");
    }

    @SuppressWarnings({"null", "ConstantConditions"})
    private static void assertNonnull(SoftAssertions s, DataCursor c) {
        s.assertThatThrownBy(() -> c.getSeriesAttribute(null)).isInstanceOf(NullPointerException.class);
    }

    private void assertState(SoftAssertions s, IOSupplier<DataCursor> supplier, IOConsumer<DataCursor> consumer, String method) {
        s.assertThatThrownBy(() -> with(supplier, close().andThen(consumer)))
                .as("Calling %s after close must throw IOException", method)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("closed");
    }

    private void assertSeriesState(SoftAssertions s, IOSupplier<DataCursor> supplier, IOConsumer<DataCursor> consumer, String method) {
        assertState(s, supplier, consumer, method);
        s.assertThatThrownBy(() -> with(supplier, consumer))
                .as("Calling %s before first series must throw IllegalStateException", method)
                .isInstanceOf(IllegalStateException.class);
        s.assertThatThrownBy(() -> with(supplier, nextSeriesToEnd().andThen(consumer)))
                .as("Calling %s after last series must throw IllegalStateException", method)
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertObsState(SoftAssertions s, IOSupplier<DataCursor> supplier, IOConsumer<DataCursor> consumer, String method) throws Exception {
        assertSeriesState(s, supplier, consumer, method);
        try (DataCursor c = supplier.getWithIO()) {
            while (c.nextSeries()) {
                s.assertThatThrownBy(c::getObsPeriod)
                        .as("Calling #getObsPeriod before first obs must throw IllegalStateException")
                        .isInstanceOf(IllegalStateException.class);
                while (c.nextObs()) {
                }
                s.assertThatThrownBy(c::getObsPeriod)
                        .as("Calling #getObsPeriod after last must throw IllegalStateException")
                        .isInstanceOf(IllegalStateException.class);
            }
        }
    }

    private void with(IOSupplier<DataCursor> supplier, IOConsumer<DataCursor> consumer) throws Exception {
        try (DataCursor c = supplier.getWithIO()) {
            consumer.acceptWithIO(c);
        }
    }

    private IOConsumer<DataCursor> nextSeriesToEnd() {
        return c -> {
            while (c.nextSeries()) {
            }
        };
    }

    private IOConsumer<DataCursor> close() {
        return DataCursor::close;
    }
}
