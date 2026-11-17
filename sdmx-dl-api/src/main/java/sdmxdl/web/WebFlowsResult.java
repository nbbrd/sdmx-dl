package sdmxdl.web;

import java.util.List;
import lombok.NonNull;
import nbbrd.design.NonNegative;

/**
 * Result of a {@link WebFlowsRequest}: a page of the matching flows along with the total number
 * of matches before paging and the failures of the sources that could not be searched.
 *
 * @see SdmxWebManager#searchFlows(WebFlowsRequest)
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebFlowsResult {

    /**
     * Matching flows, after skipping {@link WebFlowsRequest#getOffset()} items and limiting to
     * {@link WebFlowsRequest#getEffectiveMaxResults()} items.
     */
    @lombok.Singular
    @NonNull List<FlowEntry> items;

    /**
     * Total number of matching flows, before paging.
     */
    @NonNegative int total;

    /**
     * Relevance scores of the {@link #getItems() items}, in the same order, as defined by
     * {@link Search.Result#getScore()}; empty when the request has no query.
     */
    @lombok.Singular
    @NonNull List<Double> scores;

    /**
     * Sources (or databases) whose flows could not be loaded, sorted by source id.
     */
    @lombok.Singular
    @NonNull List<SourceFailure> failures;
}
