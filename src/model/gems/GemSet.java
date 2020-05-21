package model.gems;

import java.util.EnumMap;
import java.util.Map;

/** A representation of a set of gems (e.g. for a card or noble). */
public final class GemSet implements ReadOnlyGemSet {

    /** The number of gems of each {@link Color} in the {@link GemSet}. */
    private final Map<Color, Integer> gems = new EnumMap<>(Color.class);

    /**
     * Create an empty {@link GemSet}.
     */
    public GemSet() {
        for (Color c : Color.values()) {
            this.gems.put(c, 0);
        }
    }

    /**
     * Create a (deep) copy of {@code original}.
     *
     * @param original The {@link ReadOnlyGemSet} to copy.
     */
    public GemSet(ReadOnlyGemSet original) {
        this.gems.putAll(original.gems());
    }

    @Override
    public int get(Color key) {
        return this.gems.get(key);
    }

    @Override
    public Map<Color, Integer> gems() {
        return Map.copyOf(this.gems);
    }

    @Override
    public GemSet clone() {
        return new GemSet(this);
    }

    @Override
    public int size() {
        int total = 0;
        for (Color c : Color.values()) {
            total += get(c);
        }
        return total;
    }

    @Override
    public boolean dominatedBy(ReadOnlyGemSet other) {
        for (Color c : Color.values()) {
            if (get(c) > other.get(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean dominatedBy(ReadOnlyTokenSet other) {
        int minGold = 0;
        for (Color c : Color.values()) {
            int rem = get(c) - other.get(c.toTokenColor());
            if (rem > 0) {
                minGold += rem;
            }
        }
        return other.get(TokenColor.GOLD) >= minGold;
    }

    @Override
    public ReadOnlyGemSet add(ReadOnlyGemSet other) {
        GemSet result = this.clone();
        for (Color c : Color.values()) {
            result.give(c, other.get(c));
        }
        return result;
    }

    @Override
    public ReadOnlyGemSet sub(ReadOnlyGemSet other) {
        GemSet result = this.clone();
        for (Color c : Color.values()) {
            if (!result.take(c, other.get(c))) {
                result.put(c, 0);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Color c : Color.values()) {
            sb.append("[").append(c).append(": ").append(get(c)).append("] ");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Set the number of {@code key} gems to {@code value}.
     *
     * @param key The {@link Color} to set.
     * @param value The new number of {@code key} gems. Must be nonnegative.
     * @return {@code true} if the operation was successful.
     */
    public boolean put(Color key, int value) {
        if (value < 0) {
            return false;
        }
        this.gems.put(key, value);
        return true;
    }

    /**
     * Set the gems of {@code other} to {@code this}.
     *
     * @param other The gems to set to {@code this}.
     * @return {@code true} if the operation was successful.
     */
    public boolean put(ReadOnlyGemSet other) {
        for (Color c : Color.values()) {
            put(c, other.get(c));
        }
        return true;
    }

    /**
     * Add one to the number of {@code key} gems.
     *
     * @param key The {@link Color} to increment.
     */
    public void incr(Color key) {
        give(key, 1);
    }

    /**
     * Remove one from the number of {@code key} gems (if possible).
     *
     * @param key The {@link Color} to decrement.
     */
    public void decr(Color key) {
        take(key, 1);
    }

    /**
     * Add {@code amt} gems to the {@code key} stack. Does nothing if
     * {@code amt} is negative.
     *
     * @param key The {@link Color} of the gems to add.
     * @param amt The amount of gems to add to the stack.
     * @return {@code true} if the addition was successful (i.e. {@code amt} was
     *             nonnegative).
     */
    public boolean give(Color key, int amt) {
        if (amt < 0) {
            return false;
        }
        put(key, get(key) + amt);
        return true;
    }

    /**
     * Add the gems of {@code other} to {@code this}.
     *
     * @param other The gems to give to {@code this}.
     * @return {@code true} if the giving was successful.
     */
    public boolean give(ReadOnlyGemSet other) {
        for (Color c : Color.values()) {
            give(c, other.get(c));
        }
        return true;
    }

    /**
     * Remove {@code amt} gems from the {@code key} stack. Does nothing if
     * {@code amt} is negative. Does nothing if the removal would result in
     * negative gems in the stack.
     *
     * @param key The {@link Color} of the gems to remove.
     * @param amt The amount of gems to remove from the stack.
     * @return {@code true} if the removal was successful (i.e. {@code amt} was
     *             nonnegative and does not exceed the number of gems
     *             available).
     */
    public boolean take(Color key, int amt) {
        if (amt < 0 || get(key) - amt < 0) {
            return false;
        }
        put(key, get(key) - amt);
        return true;
    }

    /**
     * Take the gems of {@code other} from {@code this}. Does nothing if the
     * removal would result in negative gems in any {@link Color}.
     *
     * @param other The gems to take from {@code this}.
     * @return {@code true} if the taking was successful (for all
     *             {@link Color}s).
     */
    public boolean take(ReadOnlyGemSet other) {
        GemSet temp = this.clone();
        for (Color c : Color.values()) {
            if (!temp.take(c, other.get(c))) {
                return false;
            }
        }
        for (Color c : Color.values()) {
            take(c, other.get(c));
        }
        return true;
    }

    /**
     * Convert an array of gem counts to a {@link ReadOnlyGemSet}. The mapping
     * of colors to quantities is in accordance with the ordering of colors in
     * the declaration of {@link Color}.
     *
     * @param gems The array of gem counts. Must have the same length as
     *            {@link Color#values}.
     * @return {@code gems} in {@link ReadOnlyGemSet} form.
     * @throws IllegalArgumentException If {@code gems} is invalid.
     */
    public static ReadOnlyGemSet toGemSet(int[] gems) {
        Color[] values = Color.values();
        if (gems.length != values.length) {
            throw new IllegalArgumentException(
                    "costArray does not have the right length.");
        }
        GemSet cost = new GemSet();
        for (int i = 0; i < values.length; ++i) {
            cost.put(values[i], gems[i]);
        }
        return cost;
    }

}
