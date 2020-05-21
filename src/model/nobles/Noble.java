package model.nobles;

import model.gems.Color;
import model.gems.GemSet;
import model.gems.ReadOnlyGemSet;

/** A representation of a noble. */
public final class Noble {

    /** The number of prestige points the {@link Noble} is worth. */
    public final int points;
    /** The number of gems needed to earn a visit from the {@link Noble}. */
    private final ReadOnlyGemSet cost;

    /**
     * Create a {@link Noble}.
     *
     * @param points The number of prestige points the noble is worth.
     * @param cost The number of gems needed to earn a visit from the noble. Any
     *            {@code null} mappings are treated as zero.
     */
    public Noble(int points, ReadOnlyGemSet cost) {
        this.points = points;
        this.cost = cost.clone();
    }

    /**
     * Create a {@link Noble}.
     *
     * @param points The number of prestige points the noble is worth.
     * @param cost The number of gems needed to earn a visit from the noble. Any
     *            {@code null} mappings are treated as zero. Must obey the
     *            preconditions of {@link GemSet#toGemSet}.
     * @throws IllegalArgumentException If {@code cost} is invalid.
     */
    public Noble(int points, int[] cost) {
        this(points, GemSet.toGemSet(cost));
    }

    /**
     * @param c The {@link Color} of the cost to get.
     * @return The number of gems of {@link Color} {@code c} needed to earn a
     *             visit from the noble.
     */
    public int cost(Color c) {
        return this.cost.get(c);
    }

    /**
     * @param gems The {@link ReadOnlyGemSet} to compare against.
     * @return {@code true} if {@code gems} is enough to win the noble.
     */
    public boolean satisfiedBy(ReadOnlyGemSet gems) {
        return this.cost.dominatedBy(gems);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Points: ").append(this.points).append("\n");
        sb.append("Cost:");
        for (Color c : Color.values()) {
            sb.append(" [").append(c).append(": ").append(cost(c)).append("]");
        }
        sb.append("\n");
        return sb.toString();
    }

}
