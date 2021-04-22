package sdmxdl.web;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxWebMonitor {

    @lombok.NonNull
    String provider;

    @lombok.NonNull
    String id;
}
