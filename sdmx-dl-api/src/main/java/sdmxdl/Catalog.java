package sdmxdl;

@lombok.Value
public class Catalog implements HasName {

    @lombok.NonNull
    CatalogRef id;

    @lombok.NonNull
    String name;
}
