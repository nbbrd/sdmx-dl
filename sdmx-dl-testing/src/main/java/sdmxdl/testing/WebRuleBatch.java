package sdmxdl.testing;

import java.util.stream.Stream;

public interface WebRuleBatch {

    Stream<WebRule> getProviders();
}
