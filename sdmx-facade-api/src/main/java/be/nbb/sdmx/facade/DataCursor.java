/*
 * Copyright 2015 National Bank of Belgium
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
package be.nbb.sdmx.facade;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
public interface DataCursor extends Closeable {

    boolean nextSeries() throws IOException;

    @Nonnull
    Key getSeriesKey() throws IOException;

    @Nonnull
    TimeFormat getSeriesTimeFormat() throws IOException;

    @Nullable
    String getSeriesAttribute(@Nonnull String key) throws IOException;

    @Nonnull
    Map<String, String> getSeriesAttributes() throws IOException;

    boolean nextObs() throws IOException;

    @Nullable
    Date getObsPeriod() throws IOException;

    @Nullable
    Double getObsValue() throws IOException;
}
