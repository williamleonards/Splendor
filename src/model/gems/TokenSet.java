package model.gems;

import java.util.EnumMap;
import java.util.Map;

/** A representation of a set of tokens. */
public final class TokenSet implements ReadOnlyTokenSet {
    
    /** The number of tokens of each {@link TokenColor} in the {@link TokenSet}. */
    private final Map<TokenColor, Integer> tokens = new EnumMap<>(TokenColor.class);

    /**
     * Create an empty {@link TokenSet}.
     */
    public TokenSet() {
        for (TokenColor c : TokenColor.values()) {
            this.tokens.put(c, 0);
        }
    }

    /**
     * Create a (deep) copy of {@code original}.
     *
     * @param original The {@link ReadOnlyTokenSet} to copy.
     */
    public TokenSet(ReadOnlyTokenSet original) {
        this.tokens.putAll(original.tokens());
    }

    @Override
    public int get(TokenColor key) {
        return this.tokens.get(key);
    }

    @Override
    public Map<TokenColor, Integer> tokens() {
        return Map.copyOf(this.tokens);
    }

    @Override
    public TokenSet clone() {
        return new TokenSet(this);
    }

    @Override
    public int size() {
        int total = 0;
        for (TokenColor c : TokenColor.values()) {
            total += get(c);
        }
        return total;
    }

    @Override
    public boolean dominatedBy(ReadOnlyTokenSet other) {
        for (TokenColor c : TokenColor.values()) {
            if (get(c) > other.get(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ReadOnlyTokenSet add(ReadOnlyTokenSet other) {
        TokenSet result = this.clone();
        for (TokenColor c : TokenColor.values()) {
            result.give(c, other.get(c));
        }
        return result;
    }

    @Override
    public ReadOnlyTokenSet sub(ReadOnlyTokenSet other) {
        TokenSet result = this.clone();
        for (TokenColor c : TokenColor.values()) {
            if (!result.take(c, other.get(c))) {
                result.put(c, 0);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TokenColor tc : TokenColor.values()) {
            sb.append("[").append(tc).append(": ").append(get(tc)).append("] ");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Set the number of {@code key} tokens to {@code value}.
     *
     * @param key The {@link TokenColor} to set.
     * @param value The new number of {@code key} tokens. Must be nonnegative.
     * @return {@code true} if the operation was successful.
     */
    public boolean put(TokenColor key, int value) {
        if (value < 0) {
            return false;
        }
        this.tokens.put(key, value);
        return true;
    }

    /**
     * Set the tokens of {@code other} to {@code this}.
     *
     * @param other The tokens to set to {@code this}.
     * @return {@code true} if the operation was successful.
     */
    public boolean put(ReadOnlyTokenSet other) {
        for (TokenColor c : TokenColor.values()) {
            put(c, other.get(c));
        }
        return true;
    }

    /**
     * Add {@code amt} tokens to the {@code key} stack. Does nothing if
     * {@code amt} is negative.
     *
     * @param key The {@link TokenColor} of the tokens to add.
     * @param amt The amount of tokens to add to the stack.
     * @return {@code true} if the addition was successful (i.e. {@code amt} was
     *             nonnegative).
     */
    public boolean give(TokenColor key, int amt) {
        if (amt < 0) {
            return false;
        }
        put(key, get(key) + amt);
        return true;
    }

    /**
     * Add the tokens of {@code other} to {@code this}.
     *
     * @param other The tokens to give to {@code this}.
     * @return {@code true} if the giving was successful.
     */
    public boolean give(ReadOnlyTokenSet other) {
        for (TokenColor c : TokenColor.values()) {
            give(c, other.get(c));
        }
        return true;
    }

    /**
     * Remove {@code amt} tokens from the {@code key} stack. Does nothing if
     * {@code amt} is negative. Does nothing if the removal would result in
     * negative tokens in the stack.
     *
     * @param key The {@link TokenColor} of the tokens to remove.
     * @param amt The amount of tokens to remove from the stack.
     * @return {@code true} if the removal was successful (i.e. {@code amt} was
     *             nonnegative and does not exceed the number of tokens
     *             available).
     */
    public boolean take(TokenColor key, int amt) {
        if (amt < 0 || get(key) - amt < 0) {
            return false;
        }
        put(key, get(key) - amt);
        return true;
    }

    /**
     * Take the tokens of {@code other} from {@code this}. Does nothing if the
     * removal would result in negative tokens in any {@link TokenColor}.
     *
     * @param other The tokens to take from {@code this}.
     * @return {@code true} if the taking was successful (for all
     *             {@link TokenColor}s).
     */
    public boolean take(ReadOnlyTokenSet other) {
        TokenSet temp = this.clone();
        for (TokenColor c : TokenColor.values()) {
            if (!temp.take(c, other.get(c))) {
                return false;
            }
        }
        for (TokenColor c : TokenColor.values()) {
            take(c, other.get(c));
        }
        return true;
    }

}
