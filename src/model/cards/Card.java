package model.cards;

import model.gems.Color;
import model.gems.ReadOnlyGemSet;

/** A representation of a card. */
public interface Card {

    /**
     * @return The number of prestige points the {@link Card} is worth.
     */
    public int points();

    /**
     * @return The {@link Tier} the {@link Card} belongs to.
     */
    public Tier tier();

    /**
     * @return The {@link Color} of the gem the {@link CardImpl} provides.
     */
    public Color color();

    /**
     * @param c The {@link Color} of the cost to get.
     * @return The number of gems of {@link Color} {@code c} needed to buy the
     *             {@link Card}.
     */
    public int cost(Color c);

    /**
     * @return A deep copy of the cost of the {@link Card}.
     */
    public ReadOnlyGemSet cost();

    /**
     * @return {@code true} if the {@link Card} is hidden.
     */
    public boolean isHidden();

}
