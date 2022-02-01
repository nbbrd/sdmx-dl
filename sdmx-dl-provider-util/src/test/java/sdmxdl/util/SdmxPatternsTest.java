package sdmxdl.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.util.SdmxPatterns.*;

/**
 * https://github.com/sdmx-twg/sdmx-ml-v2_1/blob/master/schemas/SDMXCommonReferences.xsd
 * https://github.com/sosna/sdmx-rest4js/blob/master/src/utils/sdmx-patterns.coffee
 */
public class SdmxPatternsTest {

    @Test
    public void testAgencyIdPattern() {
        assertThat("abc").matches(AGENCY_ID_PATTERN);
        assertThat("AB1").matches(AGENCY_ID_PATTERN);
        assertThat("1AB").doesNotMatch(AGENCY_ID_PATTERN);
        assertThat("AB1").matches(AGENCY_ID_PATTERN);
        assertThat("AB1 ").doesNotMatch(AGENCY_ID_PATTERN);
        assertThat("A B1").doesNotMatch(AGENCY_ID_PATTERN);
        assertThat("").doesNotMatch(AGENCY_ID_PATTERN);
    }

    @Test
    public void testResourceIdPattern() {
        assertThat("abc").matches(RESOURCE_ID_PATTERN);
        assertThat("AB1").matches(RESOURCE_ID_PATTERN);
        assertThat("1AB").matches(RESOURCE_ID_PATTERN);
        assertThat(" AB1").doesNotMatch(RESOURCE_ID_PATTERN);
        assertThat("AB1 ").doesNotMatch(RESOURCE_ID_PATTERN);
        assertThat("A B1").doesNotMatch(RESOURCE_ID_PATTERN);
        assertThat("").doesNotMatch(RESOURCE_ID_PATTERN);
    }

    @Test
    public void testVersionPattern() {
        assertThat("1.0").matches(VERSION_PATTERN);
        assertThat("all").matches(VERSION_PATTERN);
        assertThat("latest").matches(VERSION_PATTERN);
        assertThat("v1.0").doesNotMatch(VERSION_PATTERN);
        assertThat("all1.0").doesNotMatch(VERSION_PATTERN);
        assertThat("").doesNotMatch(VERSION_PATTERN);
    }

    @Test
    public void testFlowRefPattern() {
        assertThat("abc").matches(FLOW_REF_PATTERN);
        assertThat("AB1").matches(FLOW_REF_PATTERN);
        assertThat("1AB").matches(FLOW_REF_PATTERN);
        assertThat(" AB1").doesNotMatch(FLOW_REF_PATTERN);
        assertThat("AB1 ").doesNotMatch(FLOW_REF_PATTERN);
        assertThat("A B1").doesNotMatch(FLOW_REF_PATTERN);
        assertThat("").doesNotMatch(FLOW_REF_PATTERN);

        assertThat("A,B,1.0").matches(FLOW_REF_PATTERN);
        assertThat("A,B,latest").matches(FLOW_REF_PATTERN);
        assertThat(",B,1.0").doesNotMatch(FLOW_REF_PATTERN);
        assertThat("A,B,").doesNotMatch(FLOW_REF_PATTERN);
        assertThat("A,,1.0").doesNotMatch(FLOW_REF_PATTERN);
        assertThat("A,B C,1.0").doesNotMatch(FLOW_REF_PATTERN);
    }
}
