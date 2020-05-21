package model.gems;

import java.util.Map;

/** A read-only view of a set of tokens. */
public interface ReadOnlyTokenSet {

    /**
     * @param key The {@link TokenColor} to query.
     * @return The number of {@code key} tokens.
     */
    public int get(TokenColor key);

    /**
     * @return A read-only view of the underlying tokens.
     */
    public Map<TokenColor, Integer> tokens();

    /**
     * @return A deep copy of the {@link ReadOnlyTokenSet}.
     */
    public ReadOnlyTokenSet clone();

    /**
     * @return The number of tokens in the {@link ReadOnlyTokenSet}.
     */
    public int size();

    /**
     * @param other The {@link ReadOnlyTokenSet} to compare against.
     * @return {@code true} if each entry of {@code other} is >= the
     *             corresponding entry of {@code this}.
     */
    public boolean dominatedBy(ReadOnlyTokenSet other);

    /**
     * This method must not mutate the {@link ReadOnlyTokenSet}.
     *
     * @param other The {@link ReadOnlyTokenSet} to add.
     * @return The sum of {@code this} and {@code other}.
     */
    public ReadOnlyTokenSet add(ReadOnlyTokenSet other);

    /**
     * This method must not mutate the {@link ReadOnlyTokenSet}.
     *
     * @param other The {@link ReadOnlyTokenSet} to subtract.
     * @return The difference of {@code this} and {@code other}. If a
     *             subtraction would result in a negative number, the result is
     *             set to 0 instead.
     */
    public ReadOnlyTokenSet sub(ReadOnlyTokenSet other);

}
