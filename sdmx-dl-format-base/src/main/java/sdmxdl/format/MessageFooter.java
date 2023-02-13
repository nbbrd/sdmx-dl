package sdmxdl.format;

import java.util.List;

@lombok.Value
@lombok.Builder
public class MessageFooter {

    int code;

    String severity;

    @lombok.Singular
    List<String> texts;
}
