package sdmxdl;

@lombok.Value
public class Database implements HasReference<DatabaseRef>, HasName {

    @lombok.NonNull
    DatabaseRef ref;

    @lombok.NonNull
    String name;
}
