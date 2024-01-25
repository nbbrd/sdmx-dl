package sdmxdl.desktop;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

public interface HasModel<MODEL> {

    String MODEL_PROPERTY = "model";

    @Nullable MODEL getModel();

    void setModel(@Nullable MODEL model);

    static <M, C extends HasModel<M>> C create(Supplier<C> factory, M model) {
        C result = factory.get();
        result.setModel(model);
        return result;
    }
}
