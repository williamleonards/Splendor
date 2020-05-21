package model.nobles;

import java.util.Set;

import model.gems.ReadOnlyGemSet;

/** A read-only view of a deck of {@link Noble}s. */
public interface ReadOnlyNobleDeck {

    /**
     * @return {@code true} if the {@link ReadOnlyNobleDeck} has no
     *             {@link Noble}s.
     */
    public boolean isEmpty();

    /**
     * @return An unmodifiable copy of the {@link Set} of {@link Noble}s left in
     *             this {@link ReadOnlyNobleDeck}.
     */
    public Set<Noble> contents();

    /**
     * @param gems The {@link ReadOnlyGemSet} to compare against.
     * @return The {@link Noble}s that can be won over by {@code gems}.
     */
    public Set<Noble> satisfied(ReadOnlyGemSet gems);

    /**
     * @return A "deep" copy of the {@link ReadOnlyNobleDeck}.
     */
    public ReadOnlyNobleDeck clone();

}
