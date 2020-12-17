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
import sdmxdl.Dimension;
import sdmxdl.Series;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public final class WebReport {

    @lombok.NonNull
    WebResponse response;

    @lombok.Singular
    List<String> problems;

    @NonNull
    public static WebReport of(@NonNull WebResponse response) {
        WebReport.Builder result = WebReport.builder().response(response);
        Stream.of(Check.values())
                .map(check -> check.apply(response))
                .filter(Objects::nonNull)
                .forEach(result::problem);
        return result.build();
    }

    private static boolean isBlank(String o) {
        return o.trim().isEmpty();
    }

    private static String checkDimension(String checkName, Dimension dimension) {
        return isBlank(dimension.getId())
                || isBlank(dimension.getLabel())
                || dimension.getPosition() <= 0
                || dimension.getCodes().isEmpty()
                ? checkName
                : checkMap(checkName, dimension.getCodes());
    }

    private static String checkMap(String checkName, Map<String, String> map) {
        return map.entrySet()
                .stream()
                .map(entry -> checkEntry(checkName, entry))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    private static String checkEntry(String checkName, Map.Entry<String, String> entry) {
        return isBlank(entry.getKey()) || isBlank(entry.getValue())
                ? checkName
                : null;
    }

    private enum Check implements Function<WebResponse, String> {
        FLOWS_MIN_COUNT {
            @Override
            public String apply(WebResponse r) {
                return r.hasFlows() && r.getFlows().size() < r.getRequest().getMinFlowCount()
                        ? name()
                        : null;
            }
        },
        FLOWS_LABEL_NOT_BLANK {
            @Override
            public String apply(WebResponse r) {
                return r.hasFlows()
                        ? r.getFlows()
                        .stream()
                        .filter(flow -> isBlank(flow.getLabel()))
                        .findAny()
                        .map(flow -> name())
                        .orElse(null)
                        : null;
            }
        },
        FLOW_INVALID_REF {
            @Override
            public String apply(WebResponse r) {
                return r.hasFlow() && !r.getRequest().getFlow().containsRef(r.getFlow())
                        ? name()
                        : null;
            }
        },
        FLOW_LABEL_NOT_BLANK {
            @Override
            public String apply(WebResponse r) {
                return r.hasFlow() && isBlank(r.getFlow().getLabel())
                        ? name()
                        : null;
            }
        },
        STRUCT_LABEL_NOT_BLANK {
            @Override
            public String apply(WebResponse r) {
                return r.hasStructure() && isBlank(r.getStructure().getLabel())
                        ? name()
                        : null;
            }
        },
        STRUCT_INVALID_REF {
            @Override
            public String apply(WebResponse r) {
                return r.hasStructure() && r.hasFlow() && !r.getStructure().getRef().contains(r.getFlow().getStructureRef())
                        ? name()
                        : null;
            }
        },
        STRUCT_DIMENSION_COUNT {
            @Override
            public String apply(WebResponse r) {
                return r.hasStructure() && r.getStructure().getDimensions().size() != r.getRequest().getDimensionCount()
                        ? name()
                        : null;
            }
        },
        STRUCT_INVALID_DIMENSION {
            @Override
            public String apply(WebResponse r) {
                return r.hasStructure()
                        ? r.getStructure()
                        .getDimensions()
                        .stream()
                        .map(dimension -> checkDimension(name(), dimension))
                        .filter(Objects::nonNull)
                        .findAny()
                        .orElse(null)
                        : null;
            }
        },
        DATA_MIN_SERIES_COUNT {
            @Override
            public String apply(WebResponse r) {
                return r.hasData() && r.getData().size() < r.getRequest().getMinSeriesCount()
                        ? name()
                        : null;
            }
        },
        DATA_MIN_OBS_COUNT {
            @Override
            public String apply(WebResponse r) {
                return r.hasData() && r.getData()
                        .stream()
                        .map(Series::getObs)
                        .mapToInt(Collection::size)
                        .sum() < r.getRequest().getMinObsCount()
                        ? name()
                        : null;
            }
        },
        DATA_META_NOT_BLANK {
            @Override
            public String apply(WebResponse r) {
                return r.hasData()
                        ? r.getData()
                        .stream()
                        .map(Series::getMeta)
                        .map(meta -> checkMap(name(), meta))
                        .filter(Objects::nonNull)
                        .findAny()
                        .orElse(null)
                        : null;
            }
        };
    }
}
