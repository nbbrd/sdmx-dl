package spreadsheet4j.standalone;

import _test.DependencyResolver;
import nbbrd.io.text.TextParser;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeDependenciesTest {

    @Test
    public void testRuntimeDependencies() throws IOException {
        assertThat(getRuntimeDependencies())
                .describedAs("Check runtime dependencies")
                .satisfies(RuntimeDependenciesTest::checkJavaIoUtil)
                .satisfies(RuntimeDependenciesTest::checkSdmxdl)
                .satisfies(RuntimeDependenciesTest::checkPicocsv)
                .satisfies(RuntimeDependenciesTest::checkPowershell)
                .satisfies(RuntimeDependenciesTest::checkGson)
                .satisfies(RuntimeDependenciesTest::checkSllContextKickstart)
                .satisfies(RuntimeDependenciesTest::checkJavaNetProxy)
                .satisfies(RuntimeDependenciesTest::checkKryo5)
                .satisfies(RuntimeDependenciesTest::checkMsal)
                .hasSize(25);
    }

    private static void checkJavaIoUtil(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.java-io-util")
                .has(sameVersion())
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder(
                        "java-io-picocsv",
                        "java-io-xml",
                        "java-io-base",
                        "java-io-curl",
                        "java-io-http"
                );
    }

    private static void checkSdmxdl(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.sdmx-dl")
                .has(sameVersion())
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder(
                        "sdmx-dl-api",
                        "sdmx-dl-format-base",
                        "sdmx-dl-format-csv",
                        "sdmx-dl-format-kryo",
                        "sdmx-dl-format-xml",
                        "sdmx-dl-provider-base",
                        "sdmx-dl-provider-dialects",
                        "sdmx-dl-provider-px",
                        "sdmx-dl-provider-ri"
                );
    }

    private static void checkPicocsv(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.picocsv")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("picocsv");
    }

    private static void checkPowershell(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.tuupertunut")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("powershell-lib-java");
    }

    private static void checkKryo5(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.esotericsoftware.kryo")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("kryo5");
    }

    private static void checkGson(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.google.code.gson")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("gson");
    }

    private static void checkSllContextKickstart(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "io.github.hakky54")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("ayza", "sude");
    }

    private static void checkJavaNetProxy(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.java-net-proxy")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("java-net-proxy");
    }

    private static void checkMsal(List<? extends DependencyResolver.GAV> coordinates) {
        assertThatGroupId(coordinates, "com.microsoft.azure")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("msal4j");

        assertThatGroupId(coordinates, "com.azure")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("azure-json");

        assertThatGroupId(coordinates, "org.slf4j")
                .extracting(DependencyResolver.GAV::getArtifactId)
                .containsExactlyInAnyOrder("slf4j-api", "slf4j-jdk14");
    }

    private static ListAssert<? extends DependencyResolver.GAV> assertThatGroupId(List<? extends DependencyResolver.GAV> coordinates, String groupId) {
        return assertThat(coordinates)
                .describedAs("Check " + groupId)
                .filteredOn(DependencyResolver.GAV::getGroupId, groupId);
    }

    private static Condition<List<? extends DependencyResolver.GAV>> sameVersion() {
        return new Condition<>(DependencyResolver.GAV::haveSameVersion, "same version");
    }

    private static List<DependencyResolver.GAV> getRuntimeDependencies() throws IOException {
        return TextParser.onParsingReader(reader -> DependencyResolver.parse(asBufferedReader(reader).lines()))
                .parseResource(RuntimeDependenciesTest.class, "/runtime-dependencies.txt", UTF_8);
    }

    private static BufferedReader asBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }
}
