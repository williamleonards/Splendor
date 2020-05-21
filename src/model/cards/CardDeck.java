package model.cards;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.gems.Color;
import util.Marker;
import util.Undoable;

/** A representation of a deck of {@link Card}s. */
public final class CardDeck implements ReadOnlyCardDeck, Undoable {

    /** The maximum number of {@link Card}s on {@link #display}. */
    public static final int DISPLAY_CAP = 4;

    /** Map from {@link Tier}s to default card lists of that tier. */
    private static final Map<Tier, List<CardImpl>> defaultDeck = new EnumMap<>(Tier.class);

    /** Default {@link Tier#LOW} {@link CardDeck}. */
    private static final List<CardImpl> defaultLow = Arrays.asList(new CardImpl[] {
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 0, 0, 3, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 0, 0, 0, 0, 3 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 0, 3, 0, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 3, 0, 0, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 0, 0, 0, 3, 0 }),
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 0, 1, 2, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 0, 0, 1, 2, 0 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 0, 0, 0, 1, 2 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 2, 0, 0, 0, 1 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 1, 2, 0, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 0, 0, 2, 0, 2 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 0, 2, 0, 0, 2 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 0, 2, 0, 2, 0 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 2, 0, 2, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 2, 0, 0, 2, 0 }),
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 0, 1, 1, 1, 1 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 1, 0, 1, 1, 1 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 1, 1, 0, 1, 1 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 1, 1, 1, 0, 1 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 1, 1, 1, 1, 0 }),
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 0, 1, 0, 2, 2 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 2, 0, 1, 0, 2 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 2, 2, 0, 1, 0 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 0, 2, 2, 0, 1 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 1, 0, 2, 2, 0 }),
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 1, 3, 1, 0, 0 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 3, 1, 0, 0, 1 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 0, 0, 1, 3, 1 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 0, 1, 3, 1, 0 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 1, 0, 0, 1, 3 }),
            new CardImpl(0, Tier.LOW , Color.BROWN, new int[] { 0, 1, 1, 2, 1 }),
            new CardImpl(0, Tier.LOW , Color.RED  , new int[] { 1, 0, 1, 1, 2 }),
            new CardImpl(0, Tier.LOW , Color.GREEN, new int[] { 2, 1, 0, 1, 1 }),
            new CardImpl(0, Tier.LOW , Color.BLUE , new int[] { 1, 2, 1, 0, 1 }),
            new CardImpl(0, Tier.LOW , Color.WHITE, new int[] { 1, 1, 2, 1, 0 }),
            new CardImpl(1, Tier.LOW , Color.BROWN, new int[] { 0, 0, 0, 4, 0 }),
            new CardImpl(1, Tier.LOW , Color.RED  , new int[] { 0, 0, 0, 0, 4 }),
            new CardImpl(1, Tier.LOW , Color.GREEN, new int[] { 4, 0, 0, 0, 0 }),
            new CardImpl(1, Tier.LOW , Color.BLUE , new int[] { 0, 4, 0, 0, 0 }),
            new CardImpl(1, Tier.LOW , Color.WHITE, new int[] { 0, 0, 4, 0, 0 }) });
    /** Default {@link Tier#MID} {@link CardDeck}. */
    private static final List<CardImpl> defaultMid = Arrays.asList(new CardImpl[] {
            new CardImpl(1, Tier.MID , Color.BROWN, new int[] { 0, 0, 2, 2, 3 }),
            new CardImpl(1, Tier.MID , Color.RED  , new int[] { 3, 2, 0, 0, 2 }),
            new CardImpl(1, Tier.MID , Color.GREEN, new int[] { 2, 0, 0, 3, 2 }),
            new CardImpl(1, Tier.MID , Color.BLUE , new int[] { 0, 3, 2, 2, 0 }),
            new CardImpl(1, Tier.MID , Color.WHITE, new int[] { 2, 2, 3, 0, 0 }),
            new CardImpl(1, Tier.MID , Color.BROWN, new int[] { 2, 0, 3, 0, 3 }),
            new CardImpl(1, Tier.MID , Color.RED  , new int[] { 3, 2, 0, 3, 0 }),
            new CardImpl(1, Tier.MID , Color.GREEN, new int[] { 0, 3, 2, 0, 3 }),
            new CardImpl(1, Tier.MID , Color.BLUE , new int[] { 3, 0, 3, 2, 0 }),
            new CardImpl(1, Tier.MID , Color.WHITE, new int[] { 0, 3, 0, 3, 2 }),
            new CardImpl(2, Tier.MID , Color.BROWN, new int[] { 0, 0, 0, 0, 5 }),
            new CardImpl(2, Tier.MID , Color.RED  , new int[] { 5, 0, 0, 0, 0 }),
            new CardImpl(2, Tier.MID , Color.GREEN, new int[] { 0, 0, 5, 0, 0 }),
            new CardImpl(2, Tier.MID , Color.BLUE , new int[] { 0, 0, 0, 5, 0 }),
            new CardImpl(2, Tier.MID , Color.WHITE, new int[] { 0, 5, 0, 0, 0 }),
            new CardImpl(2, Tier.MID , Color.BROWN, new int[] { 0, 2, 4, 1, 0 }),
            new CardImpl(2, Tier.MID , Color.RED  , new int[] { 0, 0, 2, 4, 1 }),
            new CardImpl(2, Tier.MID , Color.GREEN, new int[] { 1, 0, 0, 2, 4 }),
            new CardImpl(2, Tier.MID , Color.BLUE , new int[] { 4, 1, 0, 0, 2 }),
            new CardImpl(2, Tier.MID , Color.WHITE, new int[] { 2, 4, 1, 0, 0 }),
            new CardImpl(2, Tier.MID , Color.BROWN, new int[] { 0, 3, 5, 0, 0 }),
            new CardImpl(2, Tier.MID , Color.RED  , new int[] { 5, 0, 0, 0, 3 }),
            new CardImpl(2, Tier.MID , Color.GREEN, new int[] { 0, 0, 3, 5, 0 }),
            new CardImpl(2, Tier.MID , Color.BLUE , new int[] { 0, 0, 0, 3, 5 }),
            new CardImpl(2, Tier.MID , Color.WHITE, new int[] { 3, 5, 0, 0, 0 }),
            new CardImpl(3, Tier.MID , Color.BROWN, new int[] { 6, 0, 0, 0, 0 }),
            new CardImpl(3, Tier.MID , Color.RED  , new int[] { 0, 6, 0, 0, 0 }),
            new CardImpl(3, Tier.MID , Color.GREEN, new int[] { 0, 0, 6, 0, 0 }),
            new CardImpl(3, Tier.MID , Color.BLUE , new int[] { 0, 0, 0, 6, 0 }),
            new CardImpl(3, Tier.MID , Color.WHITE, new int[] { 0, 0, 0, 0, 6 }) });
    /** Default {@link Tier#HIGH} {@link CardDeck}. */
    private static final List<CardImpl> defaultHigh = Arrays.asList(new CardImpl[] {
            new CardImpl(3, Tier.HIGH, Color.BROWN, new int[] { 0, 3, 5, 3, 3 }),
            new CardImpl(3, Tier.HIGH, Color.RED  , new int[] { 3, 0, 3, 5, 3 }),
            new CardImpl(3, Tier.HIGH, Color.GREEN, new int[] { 3, 3, 0, 3, 5 }),
            new CardImpl(3, Tier.HIGH, Color.BLUE , new int[] { 5, 3, 3, 0, 3 }),
            new CardImpl(3, Tier.HIGH, Color.WHITE, new int[] { 3, 5, 3, 3, 0 }),
            new CardImpl(4, Tier.HIGH, Color.BROWN, new int[] { 0, 7, 0, 0, 0 }),
            new CardImpl(4, Tier.HIGH, Color.RED  , new int[] { 0, 0, 7, 0, 0 }),
            new CardImpl(4, Tier.HIGH, Color.GREEN, new int[] { 0, 0, 0, 7, 0 }),
            new CardImpl(4, Tier.HIGH, Color.BLUE , new int[] { 0, 0, 0, 0, 7 }),
            new CardImpl(4, Tier.HIGH, Color.WHITE, new int[] { 7, 0, 0, 0, 0 }),
            new CardImpl(4, Tier.HIGH, Color.BROWN, new int[] { 3, 6, 3, 0, 0 }),
            new CardImpl(4, Tier.HIGH, Color.RED  , new int[] { 0, 3, 6, 3, 0 }),
            new CardImpl(4, Tier.HIGH, Color.GREEN, new int[] { 0, 0, 3, 6, 3 }),
            new CardImpl(4, Tier.HIGH, Color.BLUE , new int[] { 3, 0, 0, 3, 6 }),
            new CardImpl(4, Tier.HIGH, Color.WHITE, new int[] { 6, 3, 0, 0, 3 }),
            new CardImpl(5, Tier.HIGH, Color.BROWN, new int[] { 3, 7, 0, 0, 0 }),
            new CardImpl(5, Tier.HIGH, Color.RED  , new int[] { 0, 3, 7, 0, 0 }),
            new CardImpl(5, Tier.HIGH, Color.GREEN, new int[] { 0, 0, 3, 7, 0 }),
            new CardImpl(5, Tier.HIGH, Color.BLUE , new int[] { 0, 0, 0, 3, 7 }),
            new CardImpl(5, Tier.HIGH, Color.WHITE, new int[] { 7, 0, 0, 0, 3 }) });

    static {
        defaultDeck.put(Tier.LOW, defaultLow);
        defaultDeck.put(Tier.MID, defaultMid);
        defaultDeck.put(Tier.HIGH, defaultHigh);
    }

    /** The {@link Tier} of the {@link Card}s in the {@link CardDeck}. */
    public final Tier tier;
    /** The {@link Card}s in the {@link CardDeck} (in order). */
    private final LinkedList<CardImpl> deck = new LinkedList<>();
    /** The {@link Card}s currently on display. */
    private final Set<Card> display = new HashSet<>();
    /** The {@link Card}s that have ever been issued as hidden. */
    private final Set<Card> hidden = new HashSet<>();
    /** The history of the {@link CardDeck}, from most to least recent. */
    private final Deque<Action> actions = new LinkedList<>();
    /**
     * The {@link Marker}s that have been returned by {@link #mark()} and are
     * still in {@link #actions}.
     */
    private final Set<Action> markers = new HashSet<>();

    /**
     * Create a default {@link CardDeck} of the specified {@link Tier}.
     *
     * @param tier The {@link Tier} of the {@link CardDeck}.
     */
    public CardDeck(Tier tier) {
        this(tier, defaultDeck.get(tier));
    }

    /**
     * Create a {@link CardDeck} of the specified {@link Tier} using the
     * {@link CardImpl}s in {@code cards}.
     *
     * @param tier The {@link Tier} of the {@link CardDeck}.
     * @param cards A valid {@link Collection} of {@link CardImpl}s. Each
     *            {@link CardImpl} in {@code cards} must be in {@code tier}.
     * @throws IllegalArgumentException If {@code cards} is invalid.
     */
    public CardDeck(Tier tier, Collection<? extends CardImpl> cards) {
        this.tier = tier;
        for (CardImpl c : cards) {
            if (!c.tier().equals(tier)) {
                throw new IllegalArgumentException(
                        "cards has a card of the wrong tier.");
            }
            this.deck.add(c.clone());
        }
        Collections.shuffle(this.deck);
        while (this.display.size() < DISPLAY_CAP && !isDeckEmpty()) {
            this.display.add(this.deck.poll());
        }
    }

    /**
     * Create a copy of {@code other}, i.e. this is a copy constructor.
     *
     * @param other The {@link CardDeck} to copy.
     */
    private CardDeck(CardDeck other) {
        this.tier = other.tier;
        this.deck.addAll(other.deck);
        this.display.addAll(other.display);
        this.hidden.addAll(other.hidden);
        this.actions.addAll(other.actions);
    }

    @Override
    public boolean isDeckEmpty() {
        return this.deck.isEmpty();
    }

    @Override
    public boolean isDisplayEmpty() {
        return this.display.isEmpty();
    }

    @Override
    public int deckSize() {
        return this.deck.size();
    }

    @Override
    public Set<Card> display() {
        return Set.copyOf(this.display);
    }

    @Override
    public boolean onDeck(Card card) {
        Card top = peek();
        return top != null && top.equals(card);
    }

    @Override
    public boolean inDisplay(Card card) {
        return this.display.contains(card);
    }

    @Override
    public Card peek() {
        CardImpl card = this.deck.peek();
        if (card != null) {
            HiddenCard hc = new HiddenCard(card);
            this.hidden.add(hc);
            return hc;
        }
        return null;
    }

    @Override
    public CardDeck clone() {
        return new CardDeck(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : this.display) {
            sb.append(card);
        }
        sb.append("The deck has ").append(deckSize()).append(" card(s) left\n");
        return sb.toString();
    }

    /**
     * Return {@code card} but unhidden, provided {@code card} was hidden by the
     * {@link CardDeck}.
     *
     * @param card The {@link Card} to unhide.
     * @return The unhidden version of {@code card} if it was hidden by the
     *             {@link CardDeck} (which may be {@code card} itself if it is
     *             already unhidden).
     */
    public Card unhide(Card card) {
        if (this.hidden.contains(card) && card instanceof HiddenCard) {
            return ((HiddenCard)card).card();
        }
        return card;
    }

    /**
     * Pop and return the top {@link Card} of {@link #deck} (NOT
     * {@link #display}), or {@code null} if there is none.
     *
     * @return The top {@link Card} of {@link #deck}, or {@code null} if
     *             {@link #deck} is empty.
     */
    public Card draw() {
        Card card = this.deck.poll();
        if (card != null) {
            this.actions.push(new Action(Type.DRAW, card));
        }
        return card;
    }

    /**
     * Remove {@code card} from {@link #display} if it exists.
     *
     * @param card The {@link Card} to remove.
     * @return {@code true} if {@code card} was successfully removed.
     */
    public boolean take(Card card) {
        if (!this.display.contains(card)) {
            return false;
        }
        this.display.remove(card);
        this.actions.push(new Action(Type.TAKE, card));
        refillDisplay();
        return true;
    }

    /**
     * Refill {@link #display} with {@link Card}s from {@link #deck}.
     *
     * @return {@code true} if the {@link CardDeck} was mutated.
     */
    private boolean refillDisplay() {
        boolean changed = false;
        while (this.display.size() < DISPLAY_CAP && !isDeckEmpty()) {
            Card card = this.deck.poll();
            this.actions.push(new Action(Type.REFILL, card));
            this.display.add(card);
            changed = true;
        }
        return changed;
    }

    /**
     * Shuffle the {@link #deck} (not including the {@link #display}).
     */
    public void shuffle() {
        Collections.shuffle(this.deck);
    }

    @Override
    public Action mark() {
        Action mark = new Action();
        this.actions.push(mark);
        this.markers.add(mark);
        return mark;
    }

    /** The action of a {@link CardDeck}. */
    private final class Action implements Marker {

        /** The {@link Type} of the {@link Action}. */
        private final Type type;
        /** The {@link Card} involved in the {@link Action}. */
        private final Card card;

        /**
         * Create an {@link Action}.
         *
         * @param type The {@link Type} of the {@link Action}.
         * @param card The {@link Card} involved in the {@link Action}.
         */
        private Action(Type type, Card card) {
            this.type = type;
            this.card = card;
        }

        /**
         * Create a marker {@link Action}.
         */
        private Action() {
            this.type = Type.MARK;
            this.card = null;
        }

        @Override
        public boolean undo() {
            if (!CardDeck.this.markers.contains(this)) {
                return false;
            }
            Action action;
            do {
                action = CardDeck.this.actions.pop();
                switch (action.type) {
                    case DRAW:
                        CardDeck.this.deck.addFirst((CardImpl)action.card);
                        break;
                    case TAKE:
                        CardDeck.this.display.add(action.card);
                        break;
                    case REFILL:
                        CardDeck.this.display.remove(action.card);
                        CardDeck.this.deck.addFirst((CardImpl)action.card);
                        break;
                    case MARK:
                        CardDeck.this.markers.remove(action);
                        break;
                    default:
                        throw new InternalError("This is impossible!");
                }
            } while (!action.equals(this));
            return true;
        }

    }

    /** The different possible actions of a {@link CardDeck}. */
    private enum Type {
        /** The action is a {@link CardDeck#draw()}. */
        DRAW,
        /** The action is a {@link CardDeck#take(Card)}. */
        TAKE,
        /** The action is a {@link CardDeck#refillDisplay()}. */
        REFILL,
        /** The action is a mark for undoing. */
        MARK
    }

}
