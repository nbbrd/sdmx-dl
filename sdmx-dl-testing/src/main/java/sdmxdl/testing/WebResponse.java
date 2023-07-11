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

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Connection;
import sdmxdl.DataStructure;
import sdmxdl.Dataflow;
import sdmxdl.Series;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

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

    @NonNull
    public static WebResponse of(@NonNull WebRequest request, @NonNull SdmxWebManager manager) {
        WebResponse.Builder result = WebResponse
                .builder()
                .request(request)
                .source(manager.getSources().get(request.getSource()));

        try (Connection conn = manager.getConnection(request.getSource(), request.getLanguages())) {
            result
                    .flows(conn.getFlows())
                    .flow(conn.getFlow(request.getFlowRef()))
                    .structure(conn.getStructure(request.getFlowRef()))
                    .data(conn.getData(request.getFlowRef(), request.getQuery()).getData());
        } catch (Exception ex) {
            log.log(Level.WARNING, "While getting response", ex);
            result.error(toError(ex));
        }

        return result.build();
    }

    private static String toError(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        Throwable cause = ex.getCause();
        while (cause != null) {
            sb.append(System.lineSeparator()).append(cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
        }
        return sb.toString();
    }

    public static int getObsCount(Collection<Series> data) {
        return data.stream().mapToInt(series -> series.getObs().size()).sum();
    }
}
