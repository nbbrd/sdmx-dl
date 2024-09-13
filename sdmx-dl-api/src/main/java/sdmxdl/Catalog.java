package sdmxdl;

@lombok.Value
public class Catalog implements HasName {

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String name;
}
