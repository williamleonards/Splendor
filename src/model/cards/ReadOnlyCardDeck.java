package model.cards;

import java.util.Set;

/** A read-only view of a deck of {@link Card}s. */
public interface ReadOnlyCardDeck {

    /**
     * @return {@code true} if the deck (not including the {@link Card}s on
     *             display) contains no {@link Card}s.
     */
    public boolean isDeckEmpty();

    /**
     * @return {@code true} if the display contains no {@link Card}s.
     */
    public boolean isDisplayEmpty();

    /**
     * @return The number of {@link Card}s left in the deck (not including the
     *             {@link Card}s on display).
     */
    public int deckSize();

    /**
     * @return An unmodifiable copy of the {@link Card}s on display.
     */
    public Set<Card> display();

    /**
     * @param card The {@code Card} to check.
     * @return {@code true} if the top card of deck is {@code card}. Always
     *             {@code false} if the deck is empty.
     */
    public boolean onDeck(Card card);

    /**
     * @param card The {@code Card} to check.
     * @return {@code true} if the display contains {@code card}. Always
     *             {@code false} if the display is empty.
     */
    public boolean inDisplay(Card card);

    /**
     * @return The (hidden) top {@link Card} of the {@link ReadOnlyCardDeck}, or
     *             {@code null} if the deck (not the display) is empty.
     */
    public Card peek();

    /**
     * @return A "deep" copy of the {@link ReadOnlyCardDeck}.
     */
    public ReadOnlyCardDeck clone();

}
