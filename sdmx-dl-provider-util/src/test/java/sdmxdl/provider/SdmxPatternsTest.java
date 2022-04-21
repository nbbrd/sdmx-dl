package sdmxdl.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * https://github.com/sdmx-twg/sdmx-ml-v2_1/blob/master/schemas/SDMXCommonReferences.xsd
 * https://github.com/sosna/sdmx-rest4js/blob/master/src/utils/sdmx-patterns.coffee
 */
public class SdmxPatternsTest {

    @Test
    public void testAgencyIdPattern() {
        assertThat("abc").matches(SdmxPatterns.AGENCY_ID_PATTERN);
        assertThat("AB1").matches(SdmxPatterns.AGENCY_ID_PATTERN);
        assertThat("1AB").doesNotMatch(SdmxPatterns.AGENCY_ID_PATTERN);
        assertThat("AB1").matches(SdmxPatterns.AGENCY_ID_PATTERN);
        assertThat("AB1 ").doesNotMatch(SdmxPatterns.AGENCY_ID_PATTERN);
        assertThat("A B1").doesNotMatch(SdmxPatterns.AGENCY_ID_PATTERN);
        assertThat("").doesNotMatch(SdmxPatterns.AGENCY_ID_PATTERN);
    }

    @Test
    public void testResourceIdPattern() {
        assertThat("abc").matches(SdmxPatterns.RESOURCE_ID_PATTERN);
        assertThat("AB1").matches(SdmxPatterns.RESOURCE_ID_PATTERN);
        assertThat("1AB").matches(SdmxPatterns.RESOURCE_ID_PATTERN);
        assertThat(" AB1").doesNotMatch(SdmxPatterns.RESOURCE_ID_PATTERN);
        assertThat("AB1 ").doesNotMatch(SdmxPatterns.RESOURCE_ID_PATTERN);
        assertThat("A B1").doesNotMatch(SdmxPatterns.RESOURCE_ID_PATTERN);
        assertThat("").doesNotMatch(SdmxPatterns.RESOURCE_ID_PATTERN);
    }

    @Test
    public void testVersionPattern() {
        assertThat("1.0").matches(SdmxPatterns.VERSION_PATTERN);
        assertThat("all").matches(SdmxPatterns.VERSION_PATTERN);
        assertThat("latest").matches(SdmxPatterns.VERSION_PATTERN);
        assertThat("v1.0").doesNotMatch(SdmxPatterns.VERSION_PATTERN);
        assertThat("all1.0").doesNotMatch(SdmxPatterns.VERSION_PATTERN);
        assertThat("").doesNotMatch(SdmxPatterns.VERSION_PATTERN);
    }

    @Test
    public void testFlowRefPattern() {
        assertThat("abc").matches(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("AB1").matches(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("1AB").matches(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat(" AB1").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("AB1 ").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("A B1").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);

        assertThat("A,B,1.0").matches(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("A,B,latest").matches(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat(",B,1.0").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("A,B,").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("A,,1.0").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
        assertThat("A,B C,1.0").doesNotMatch(SdmxPatterns.FLOW_REF_PATTERN);
    }
}
