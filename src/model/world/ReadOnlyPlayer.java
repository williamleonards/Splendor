package model.world;

import java.util.Set;

import model.cards.Card;
import model.gems.Color;
import model.gems.ReadOnlyGemSet;
import model.gems.ReadOnlyTokenSet;
import model.gems.TokenColor;
import model.nobles.Noble;

/** A read-only view of a player. */
public interface ReadOnlyPlayer {

    /**
     * @return A deep copy of the gems (from {@link Card}s) the
     *             {@link ReadOnlyPlayer} has.
     */
    public ReadOnlyGemSet cardGems();

    /**
     * @param c The {@link Color} of the number of card gems to get.
     * @return The number of card gems of {@link Color} {@code c} the
     *             {@link ReadOnlyPlayer} has.
     */
    public int cardGems(Color c);

    /**
     * @return A deep copy of the tokens the {@link ReadOnlyPlayer} has.
     */
    public ReadOnlyTokenSet tokens();

    /**
     * @param c The {@link TokenColor} of the number of tokens to get.
     * @return The number of tokens of {@link TokenColor} {@code c} the
     *             {@link ReadOnlyPlayer} has.
     */
    public int tokens(TokenColor c);

    /**
     * @return The total number of tokens the {@link ReadOnlyPlayer} has.
     */
    public int tokenCount();

    /**
     * @return An unmodifiable copy of the {@link Card}s that the
     *             {@link ReadOnlyPlayer} has reserved.
     */
    public Set<Card> reserved();

    /**
     * @return An unmodifiable copy of the {@link Card}s that the
     *             {@link ReadOnlyPlayer} owns.
     */
    public Set<Card> owned();

    /**
     * @return An unmodifiable copy of the {@link Noble}s that the
     *             {@link ReadOnlyPlayer} has.
     */
    public Set<Noble> nobles();

    /**
     * @return The number of prestige points the {@link ReadOnlyPlayer} has.
     */
    public int points();

    /**
     * @param card The {@link Card} to check.
     * @return {@code true} if {@code card} can be reserved.
     */
    public boolean canReserve(Card card);

    /**
     * @param card The {@link Card} to check.
     * @return {@code true} if {@code card} can be bought and the
     *             {@link ReadOnlyPlayer} can afford it.
     */
    public boolean canPurchase(Card card);

    /**
     * @param card The {@link Card} to check.
     * @param tokens The {@link ReadOnlyTokenSet} to buy {@code card} with.
     * @return {@code true} if {@code card} can be bought with EXACTLY
     *             {@code tokens} and the {@link ReadOnlyPlayer} can afford it.
     */
    public boolean canPurchase(Card card, ReadOnlyTokenSet tokens);

    /**
     * @return A "deep" copy of the {@link ReadOnlyPlayer}.
     */
    public ReadOnlyPlayer clone();

}
