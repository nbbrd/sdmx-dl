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
package be.nbb.sdmx.facade.tck;

import internal.io.ConsumerWithIO;
import be.nbb.sdmx.facade.DataCursor;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.assertj.core.api.SoftAssertions;

public final class DataCursorAssert {

    public static void assertCompliance(Callable<DataCursor> supplier) {
        SoftAssertions s = new SoftAssertions();
        try {
            assertCompliance(s, supplier);
        } catch (Exception ex) {
            s.fail("Unexpected exception", ex);
        }
        s.assertAll();
    }

    public static void assertCompliance(SoftAssertions s, Callable<DataCursor> supplier) throws Exception {
        try (DataCursor c = supplier.call()) {
            while (c.nextSeries()) {
                assertNonnull(s, c);
                s.assertThat(c.getSeriesAttributes()).isNotNull().isEqualTo(c.getSeriesAttributes());
                s.assertThat(c.getSeriesKey()).isNotNull().isEqualTo(c.getSeriesKey());
                s.assertThat(c.getSeriesTimeFormat()).isNotNull().isEqualTo(c.getSeriesTimeFormat());
                s.assertThat(c.getSeriesAttribute("hello")).isEqualTo(c.getSeriesAttribute("hello"));
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

        try (DataCursor c = supplier.call()) {
            c.close();
        } catch (Exception ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }

        try (DataCursor c = supplier.call()) {
            nextSeriesToEnd().andThen(DataCursor::nextSeries).accept(c);
        } catch (Exception ex) {
            s.fail("Subsequent calls to #nextSeries must not raise exception", ex);
        }

        assertState(s, supplier, DataCursor::nextSeries, "#nextSeries");

        assertSeriesState(s, supplier, DataCursor::getSeriesKey, "#getSeriesKey");
        assertSeriesState(s, supplier, DataCursor::getSeriesAttributes, "#getSeriesAttributes");
        assertSeriesState(s, supplier, DataCursor::getSeriesTimeFormat, "#getSeriesTimeFormat");
        assertSeriesState(s, supplier, o -> o.getSeriesAttribute(""), "#getSeriesAttribute");
        assertSeriesState(s, supplier, o -> o.nextObs(), "#nextObs");

        assertObsState(s, supplier, DataCursor::getObsPeriod, "#getObsPeriod");
        assertObsState(s, supplier, DataCursor::getObsValue, "#getObsValue");
    }

    @SuppressWarnings("null")
    private static void assertNonnull(SoftAssertions s, DataCursor c) {
        s.assertThatThrownBy(() -> c.getSeriesAttribute(null)).isInstanceOf(NullPointerException.class);
    }

    private static void assertState(SoftAssertions s, Callable<DataCursor> supplier, ConsumerWithIO<DataCursor> consumer, String method) {
        s.assertThatThrownBy(() -> with(supplier, close().andThen(consumer)))
                .as("Calling %s after close must throw IOException", method)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("closed");
    }

    private static void assertSeriesState(SoftAssertions s, Callable<DataCursor> supplier, ConsumerWithIO<DataCursor> consumer, String method) {
        assertState(s, supplier, consumer, method);
        s.assertThatThrownBy(() -> with(supplier, consumer))
                .as("Calling %s before first series must throw IllegalStateException", method)
                .isInstanceOf(IllegalStateException.class);
        s.assertThatThrownBy(() -> with(supplier, nextSeriesToEnd().andThen(consumer)))
                .as("Calling %s after last series must throw IllegalStateException", method)
                .isInstanceOf(IllegalStateException.class);
    }

    private static void assertObsState(SoftAssertions s, Callable<DataCursor> supplier, ConsumerWithIO<DataCursor> consumer, String method) throws Exception {
        assertSeriesState(s, supplier, consumer, method);
        try (DataCursor c = supplier.call()) {
            while (c.nextSeries()) {
                s.assertThatThrownBy(() -> c.getObsPeriod())
                        .as("Calling #getObsPeriod before first obs must throw IllegalStateException")
                        .isInstanceOf(IllegalStateException.class);
                while (c.nextObs()) {
                }
                s.assertThatThrownBy(() -> c.getObsPeriod())
                        .as("Calling #getObsPeriod after last must throw IllegalStateException")
                        .isInstanceOf(IllegalStateException.class);
            }
        }
    }

    private static void with(Callable<DataCursor> supplier, ConsumerWithIO consumer) throws Exception {
        try (DataCursor c = supplier.call()) {
            consumer.accept(c);
        }
    }

    static ConsumerWithIO<DataCursor> nextSeriesToEnd() {
        return c -> {
            while (c.nextSeries()) {
            }
        };
    }

    static ConsumerWithIO<DataCursor> close() {
        return DataCursor::close;
    }
}
