package sdmxdl;

import lombok.NonNull;

/**
 * Defines the ability to have a reference that identifies a resource.
 */
public interface HasReference<T> {

    /**
     * Gets a reference.
     *
     * @return a non-null reference
     */
    @NonNull T getRef();
}
