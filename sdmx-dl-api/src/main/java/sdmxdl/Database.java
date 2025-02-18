package sdmxdl;

@lombok.Value
public class Database implements HasName {

    @lombok.NonNull
    DatabaseRef id;

    @lombok.NonNull
    String name;
}
