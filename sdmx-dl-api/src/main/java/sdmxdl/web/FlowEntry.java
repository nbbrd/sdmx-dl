package sdmxdl.web;

import lombok.NonNull;
import sdmxdl.DatabaseRef;
import sdmxdl.Flow;

/**
 * A loaded flow together with its originating source and database.
 * <p>
 * Used as the unit of indexing in {@link Search#ofFlowEntries(java.util.Collection, sdmxdl.Languages)},
 * which ranks results across all three dimensions simultaneously.
 * </p>
 */
@lombok.Value
public class FlowEntry {

    @NonNull
    WebSource source;

    @NonNull
    DatabaseRef database;

    @NonNull
    Flow flow;
}

