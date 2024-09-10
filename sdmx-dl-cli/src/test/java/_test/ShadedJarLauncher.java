package _test;

import nbbrd.io.sys.ProcessReader;
import nbbrd.io.sys.SystemProperties;
import sdmxdl.About;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@lombok.Value
@lombok.Builder
public class ShadedJarLauncher {

    @lombok.Singular
    Map<String, String> envVars;

    public String readString(String... params) throws IOException {
        return ProcessReader.readToString(UTF_8, toProcessBuilder(params).start());
    }

    public List<String> readAllLines(String... params) throws IOException {
        try (BufferedReader reader = ProcessReader.newReader(UTF_8, toProcessBuilder(params).start())) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    private ProcessBuilder toProcessBuilder(String[] params) {
        ProcessBuilder result = new ProcessBuilder(toCommand(params));
        result.environment().clear();
        result.environment().putAll(envVars);
        return result;
    }

    private List<String> toCommand(String[] params) {
        Path java = requireNonNull(SystemProperties.DEFAULT.getJavaHome())
                .resolve("bin")
                .resolve("java");

        String shadedJar = Paths.get("target")
                .resolve("sdmx-dl-cli-" + About.VERSION + "-bin.jar")
                .toString();

        List<String> result = new ArrayList<>();
        result.add(java.toString());
        result.add("-jar");
        result.add(shadedJar);
        result.addAll(asList(params));
        return result;
    }
}
