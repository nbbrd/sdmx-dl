package sdmxdl;

import lombok.NonNull;

/**
 * Marker interface for comparable references.
 * <p>
 * This interface defines a contract for objects that can be compared and represented
 * as strings. It uses a self-referential generic type bound to ensure type safety
 * when implementing the Comparable interface.
 * </p>
 * <p>
 * The default implementation of {@link #compareTo(Reference)} performs comparison
 * based on the string representation of the objects, obtained via {@link #toString()}.
 * </p>
 * <p>
 * Implementations are expected to provide meaningful string representations that
 * reflect the logical ordering of the reference objects.
 * </p>
 *
 * @param <T> the type of the reference itself; must extend Reference with the same type parameter
 *            to ensure proper type-safe comparisons (self-referential type bound)
 *
 * @author Philippe Charles
 * @see Comparable
 */
public interface Reference<T extends Reference<T>> extends Comparable<T> {

    /**
     * Returns a string representation of this reference.
     * <p>
     * This method is used by the default {@link #compareTo(Reference)} implementation
     * to establish a consistent ordering between references.
     * </p>
     *
     * @return a non-null string representation of this reference
     */
    @Override
    String toString();

    /**
     * Compares this reference with another reference for order.
     * <p>
     * The comparison is performed lexicographically on the string representations
     * of the two references, obtained via {@link #toString()}. This default implementation
     * ensures consistent ordering across all implementations of this interface.
     * </p>
     * <p>
     * Returns:
     * <ul>
     *   <li>a negative integer if this reference's string representation is lexicographically
     *       less than the other reference's string representation</li>
     *   <li>zero if the string representations are equal</li>
     *   <li>a positive integer if this reference's string representation is lexicographically
     *       greater than the other reference's string representation</li>
     * </ul>
     * </p>
     *
     * @param that the reference to compare with; must not be null
     * @return a negative integer, zero, or a positive integer as described above
     * @throws NullPointerException if {@code that} is null
     */
    default int compareTo(@NonNull T that) {
        return this.toString().compareTo(that.toString());
    }
}
