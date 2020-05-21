package model.cards;

import model.gems.Color;
import model.gems.ReadOnlyGemSet;

/** A {@link Card} whose details are hidden (because it is face down). */
final class HiddenCard implements Card {

    /** The hidden {@link CardImpl}. */
    private final CardImpl card;

    /**
     * Create a {@link HiddenCard}.
     *
     * @param card The {@link CardImpl} to hide.
     */
    public HiddenCard(CardImpl card) {
        this.card = card;
    }

    /**
     * @return The hidden {@link CardImpl}.
     */
    public CardImpl card() {
        return this.card;
    }

    @Override
    public int points() {
        return 0;
    }

    @Override
    public Tier tier() {
        return this.card.tier();
    }

    @Override
    public Color color() {
        return null;
    }

    @Override
    public int cost(@SuppressWarnings("unused") Color c) {
        return 0;
    }

    @Override
    public ReadOnlyGemSet cost() {
        return null;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this.card.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.card.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Points: Hidden, ");
        sb.append("Tier: ").append(this.card.tier()).append(", ");
        sb.append("Color: Hidden\n");
        sb.append("Cost:");
        for (Color c : Color.values()) {
            sb.append(" [").append(c).append(": Hidden]");
        }
        sb.append("\n");
        return sb.toString();
    }

}
