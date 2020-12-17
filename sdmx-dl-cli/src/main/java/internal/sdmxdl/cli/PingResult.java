/*
 * Copyright 2020 National Bank of Belgium
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
package internal.sdmxdl.cli;

import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.Value
public class PingResult {

    public static PingResult of(SdmxWebManager manager, String source) {
        try (final SdmxWebConnection conn = manager.getConnection(source)) {
            return success(source, conn.ping());
        } catch (IOException ex) {
            return failure(source, ex);
        }
    }

    public static PingResult success(String source, Duration duration) {
        return new PingResult(source, duration, null);
    }

    public static PingResult failure(String source, IOException cause) {
        Logger.getLogger(PingResult.class.getName()).log(Level.INFO, "Ping failed on '" + source + "'", cause);
        return new PingResult(source, null, cause.getMessage());
    }

    @lombok.NonNull
    String source;

    Duration duration;

    String cause;

    public boolean isSuccess() {
        return cause == null;
    }
}
