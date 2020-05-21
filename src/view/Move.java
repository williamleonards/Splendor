package view;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.cards.Card;
import model.gems.Color;
import model.gems.ReadOnlyTokenSet;

/** The move of a {@link User}. */
public final class Move {

    /**
     * All the {@link Move}(s) of {@link Type#TAKE_THREE} that have been created
     * by the class. Used for interning.
     */
    private static final Map<Color, Move> TAKE_2 = new EnumMap<>(Color.class);
    /**
     * All the {@link Move}(s) of {@link Type#TAKE_TWO} that have been created
     * by the class. Used for interning.
     */
    private static final Map<Set<Color>, Move> TAKE_3 = new HashMap<>();

    static {
        Set<Color> emptySet = EnumSet.noneOf(Color.class);
        TAKE_3.put(emptySet, new Move(emptySet));
    }

    /** The {@link Type} of the {@link Move}. */
    private final Type type;
    /**
     * The chosen {@link Color}s for {@link Type#TAKE_THREE}. Not {@code null}
     * iff {@link #type} is {@link Type#TAKE_THREE}.
     */
    private final Set<Color> colors;
    /**
     * The chosen {@link Color} for {@link Type#TAKE_TWO}. Not {@code null} iff
     * {@link #type} is {@link Type#TAKE_TWO}.
     */
    private final Color color;
    /**
     * The chosen {@link Card} for {@link Type#RESERVE} and
     * {@link Type#PURCHASE}. Not {@code null} iff {@link #type} is
     * {@link Type#RESERVE} or {@link Type#PURCHASE}.
     */
    private final Card card;
    /**
     * The tokens for purchasing {@code card}. Not {@code null} iff
     * {@link #type} is {@link Type#PURCHASE}.
     */
    private final ReadOnlyTokenSet payment;

    /**
     * Create a {@link Move} of type {@link Type#TAKE_THREE}.
     *
     * @param colors The colors of the tokens to take.
     */
    private Move(Set<Color> colors) {
        this.type = Type.TAKE_THREE;
        this.colors = colors;
        this.color = null;
        this.card = null;
        this.payment = null;
    }

    /**
     * @param colors The colors of the tokens to take.
     * @return A {@link Move} of type {@link Type#TAKE_THREE}.
     */
    public static Move takeThree(Set<Color> colors) {
        if (!TAKE_3.containsKey(colors)) {
            Set<Color> copy = EnumSet.copyOf(colors);
            TAKE_3.put(copy, new Move(copy));
        }
        return TAKE_3.get(colors);
    }

    /**
     * Create a {@link Move} of type {@link Type#TAKE_TWO}.
     *
     * @param color The color of the token to take.
     */
    private Move(Color color) {
        this.type = Type.TAKE_TWO;
        this.colors = null;
        this.color = color;
        this.card = null;
        this.payment = null;
    }

    /**
     * @param color The color of the token to take.
     * @return A {@link Move} of type {@link Type#TAKE_TWO}.
     */
    public static Move takeTwo(Color color) {
        if (!TAKE_2.containsKey(color)) {
            TAKE_2.put(color, new Move(color));
        }
        return TAKE_2.get(color);
    }

    /**
     * Create a {@link Move} of type {@link Type#RESERVE}.
     *
     * @param card The card to reserve.
     */
    private Move(Card card) {
        this.type = Type.RESERVE;
        this.colors = null;
        this.color = null;
        this.card = card;
        this.payment = null;
    }

    /**
     * @param card The card to reserve.
     * @return A {@link Move} of type {@link Type#RESERVE}.
     */
    public static Move reserve(Card card) {
        return new Move(card);
    }

    /**
     * Create a {@link Move} of type {@link Type#PURCHASE}.
     *
     * @param card The card to purchase.
     * @param payment The tokens used to pay the card.
     */
    private Move(Card card, ReadOnlyTokenSet payment) {
        this.type = Type.PURCHASE;
        this.colors = null;
        this.color = null;
        this.card = card;
        this.payment = payment;
    }

    /**
     * @param card The card to purchase.
     * @param payment The tokens used to pay the card.
     * @return A {@link Move} of type {@link Type#PURCHASE}.
     */
    public static Move purchase(Card card, ReadOnlyTokenSet payment) {
        return new Move(card, payment);
    }

    /**
     * @return The {@link Type} of the {@link Move}.
     */
    public Type type() {
        return this.type;
    }

    /**
     * @return The chosen {@link Color}s for {@link Type#TAKE_THREE}. Not
     *             {@code null} iff {@link #type} is {@link Type#TAKE_THREE}.
     */
    public Set<Color> colors() {
        return Set.copyOf(this.colors);
    }

    /**
     * @return The chosen {@link Color} for {@link Type#TAKE_TWO}. Not
     *             {@code null} iff {@link #type} is {@link Type#TAKE_TWO}.
     */
    public Color color() {
        return this.color;
    }

    /**
     * @return The chosen {@link Card} for {@link Type#RESERVE} and
     *             {@link Type#PURCHASE}. Not {@code null} iff {@link #type} is
     *             {@link Type#RESERVE} or {@link Type#PURCHASE}.
     */
    public Card card() {
        return this.card;
    }

    /**
     * @return The tokens for purchasing {@code card}. Not {@code null} iff
     *             {@link #type} is {@link Type#PURCHASE}.
     */
    public ReadOnlyTokenSet payment() {
        return this.payment;
    }

    /** The different possible moves of a {@link User}. */
    public enum Type {
        /** Take three tokens of different colors. */
        TAKE_THREE,
        /** Take two tokens of the same color. */
        TAKE_TWO,
        /** Reserve a card. */
        RESERVE,
        /** Purchase a card. */
        PURCHASE
    }

}
