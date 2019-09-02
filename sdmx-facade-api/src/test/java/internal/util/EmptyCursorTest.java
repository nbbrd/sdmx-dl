/*
 * Copyright 2017 National Bank of Belgium
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
package internal.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataFilter;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class EmptyCursorTest {

    @Test
    @SuppressWarnings("null")
    public void testToStream() throws IOException {
        try (DataCursor c = new EmptyCursor()) {
            assertThat(c.toStream(DataFilter.Detail.FULL)).isEmpty();
        }

        try (DataCursor c = new EmptyCursor()) {
            assertThatNullPointerException().isThrownBy(() -> c.toStream(null));
        }
    }
}
