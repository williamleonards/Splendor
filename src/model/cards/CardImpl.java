package model.cards;

import model.gems.Color;
import model.gems.GemSet;
import model.gems.ReadOnlyGemSet;

/** A real {@link Card}. */
final class CardImpl implements Card {

    /** The number of prestige points the {@link CardImpl} is worth. */
    private final int points;
    /** The {@link Tier} the {@link CardImpl} belongs to. */
    private final Tier tier;
    /** The {@link Color} of the gem the {@link CardImpl} provides. */
    private final Color color;
    /** The number of gems needed to buy the {@link CardImpl}. */
    private final ReadOnlyGemSet cost;

    /**
     * Create a {@link CardImpl}.
     *
     * @param points The number of prestige points the card is worth.
     * @param tier The {@link Tier} the card belongs to.
     * @param color The {@link Color} of the gem the card provides.
     * @param cost The number of gems needed to buy the card. Any {@code null}
     *            mappings are treated as zero.
     */
    public CardImpl(int points, Tier tier, Color color, ReadOnlyGemSet cost) {
        this.points = points;
        this.tier = tier;
        this.color = color;
        this.cost = cost.clone();
    }

    /**
     * Create a {@link CardImpl}.
     *
     * @param points The number of prestige points the card is worth.
     * @param tier The {@link Tier} the card belongs to.
     * @param color The {@link Color} of the gem the card provides.
     * @param cost The number of gems needed to buy the card. Must obey the
     *            preconditions of {@link GemSet#toGemSet}.
     * @throws IllegalArgumentException If {@code cost} is invalid.
     */
    public CardImpl(int points, Tier tier, Color color, int[] cost) {
        this(points, tier, color, GemSet.toGemSet(cost));
    }

    @Override
    public CardImpl clone() {
        return new CardImpl(points(), tier(), color(), cost());
    }

    @Override
    public int points() {
        return this.points;
    }

    @Override
    public Tier tier() {
        return this.tier;
    }

    @Override
    public Color color() {
        return this.color;
    }

    @Override
    public int cost(Color c) {
        return this.cost.get(c);
    }

    @Override
    public ReadOnlyGemSet cost() {
        return this.cost.clone();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HiddenCard) {
            return equals(((HiddenCard)obj).card());
        }
        return this == obj;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Points: ").append(this.points).append(", ");
        sb.append("Tier: ").append(this.tier).append(", ");
        sb.append("Color: ").append(this.color).append("\n");
        sb.append("Cost:");
        for (Color c : Color.values()) {
            sb.append(" [").append(c).append(": ").append(cost(c)).append("]");
        }
        sb.append("\n");
        return sb.toString();
    }

}
