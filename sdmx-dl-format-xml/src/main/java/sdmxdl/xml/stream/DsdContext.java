package sdmxdl.xml.stream;


import java.util.HashMap;
import java.util.Map;

@lombok.Getter
final class DsdContext {

    private final Map<String, Map<String, String>> codelists = new HashMap<>();

    private final Map<String, String> concepts = new HashMap<>();

}
