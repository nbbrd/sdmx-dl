package sdmxdl;

import lombok.Getter;
import lombok.NonNull;
import sdmxdl.web.WebSource;

/**
 * Labels for ECB Confidentiality Regime.
 * <p>
 * See <a href="https://www.ecb.europa.eu/ecb/access_to_documents/document/pa_document/shared/data/ecb.dr.par2023_0056_presentation_confidentiality_regime20230504.en.pdf">Presentation</a><br>
 * See <a href="https://www.ecb.europa.eu/ecb/access_to_documents/document/pa_document/shared/data/ecb.dr.par2022_0052ECB_confidentiality_regime.en.pdf?6fd7696f3957f71d4d3d3713e38634f4">How to classify</a>
 */
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum Confidentiality {

    PUBLIC("none", "green"),
    UNRESTRICTED("low", "blue"),
    RESTRICTED("medium", "yellow"),
    CONFIDENTIAL("high", "orange"),
    SECRET("very high", "red");

    @Getter
    private final @NonNull String impact;

    @Getter
    private final @NonNull String color;

    public boolean isAllowedIn(@NonNull WebSource source) {
        return this.ordinal() >= source.getConfidentiality().ordinal();
    }
}
