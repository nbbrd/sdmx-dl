package sdmxdl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.Confidentiality.*;
import static tests.sdmxdl.api.RepoSamples.BASIC_SOURCE;

class ConfidentialityTest {

    @Test
    void isAllowedIn() {
        assertThat(BASIC_SOURCE.toBuilder().confidentiality(PUBLIC).build())
                .matches(PUBLIC::isAllowedIn)
                .matches(UNRESTRICTED::isAllowedIn)
                .matches(RESTRICTED::isAllowedIn);

        assertThat(BASIC_SOURCE.toBuilder().confidentiality(UNRESTRICTED).build())
                .doesNotMatch(PUBLIC::isAllowedIn)
                .matches(UNRESTRICTED::isAllowedIn)
                .matches(RESTRICTED::isAllowedIn);

        assertThat(BASIC_SOURCE.toBuilder().confidentiality(RESTRICTED).build())
                .doesNotMatch(PUBLIC::isAllowedIn)
                .doesNotMatch(UNRESTRICTED::isAllowedIn)
                .matches(RESTRICTED::isAllowedIn);
    }
}