package model.gems;

import java.util.Map;

/** A read-only view of a set of gems (e.g. for a card or noble). */
public interface ReadOnlyGemSet {

    /**
     * @param key The {@link Color} to query.
     * @return The number of {@code key} gems.
     */
    public int get(Color key);

    /**
     * @return A read-only view of the underlying gems.
     */
    public Map<Color, Integer> gems();

    /**
     * @return A deep copy of the {@link ReadOnlyGemSet}.
     */
    public ReadOnlyGemSet clone();

    /**
     * @return The number of gems in the {@link ReadOnlyGemSet}.
     */
    public int size();

    /**
     * @param other The {@link ReadOnlyGemSet} to compare against.
     * @return {@code true} if each entry of {@code other} is >= the
     *             corresponding entry of {@code this}.
     */
    public boolean dominatedBy(ReadOnlyGemSet other);

    /**
     * @param other The {@link ReadOnlyTokenSet} to compare against.
     * @return {@code true} if {@code other} can buy {@code this}.
     */
    public boolean dominatedBy(ReadOnlyTokenSet other);

    /**
     * This method must not mutate the {@link ReadOnlyGemSet}.
     *
     * @param other The {@link ReadOnlyGemSet} to add.
     * @return The sum of {@code this} and {@code other}.
     */
    public ReadOnlyGemSet add(ReadOnlyGemSet other);

    /**
     * This method must not mutate the {@link ReadOnlyGemSet}.
     *
     * @param other The {@link ReadOnlyGemSet} to subtract.
     * @return The difference of {@code this} and {@code other}. If a
     *             subtraction would result in a negative number, the result is
     *             set to 0 instead.
     */
    public ReadOnlyGemSet sub(ReadOnlyGemSet other);

}
