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
package sdmxdl.testing;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.extern.java.Log
public class WebResponse {

    @lombok.NonNull
    WebRequest request;

    @lombok.NonNull
    SdmxWebSource source;

    @Nullable
    String error;

    @Nullable
    Collection<Dataflow> flows;

    @Nullable
    Dataflow flow;

    @Nullable
    DataStructure structure;

    @Nullable
    Collection<Series> data;

    public boolean hasError() {
        return error != null;
    }

    public boolean hasFlows() {
        return flows != null;
    }

    public boolean hasFlow() {
        return flow != null;
    }

    public boolean hasStructure() {
        return structure != null;
    }

    public boolean hasData() {
        return data != null;
    }

    @NonNull
    public static WebResponse of(@NonNull WebRequest request, @NonNull SdmxWebManager manager) {
        WebResponse.Builder result = WebResponse
                .builder()
                .request(request)
                .source(manager.getSources().get(request.getSource()));

        try (SdmxWebConnection conn = manager.getConnection(request.getSource())) {
            result
                    .flows(conn.getFlows())
                    .flow(conn.getFlow(request.getDataRef().getFlowRef()))
                    .structure(conn.getStructure(request.getDataRef().getFlowRef()))
                    .data(conn.getData(DataRef.of(request.getDataRef().getFlowRef(), request.getDataRef().getKey(), DataFilter.FULL)));
        } catch (IOException ex) {
            log.log(Level.WARNING, "While getting response", ex);
            result.error(toError(ex));
        }

        return result.build();
    }

    private static String toError(IOException ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        Throwable cause = ex.getCause();
        while (cause != null) {
            sb.append(System.lineSeparator()).append(cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
        }
        return sb.toString();
    }
}
