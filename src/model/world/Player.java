package model.world;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import model.cards.Card;
import model.gems.Color;
import model.gems.GemSet;
import model.gems.ReadOnlyGemSet;
import model.gems.ReadOnlyTokenSet;
import model.gems.TokenColor;
import model.gems.TokenSet;
import model.nobles.Noble;
import util.Marker;
import util.Undoable;

/** A representation of a player. */
public final class Player implements ReadOnlyPlayer, Undoable {

    /** The maximum number of {@link Card}s that can be reserved at once. */
    public static final int RESERVE_CAP = 3;

    /** The gems (from {@link Card}s) the {@link Player} has. */
    private final GemSet cardGems = new GemSet();
    /** The tokens the {@link Player} has. */
    private final TokenSet tokens = new TokenSet();

    /** The {@link Card}s that the {@link Player} has reserved. */
    private final Set<Card> reserved = new HashSet<>();
    /** The {@link Card}s that the {@link Player} owns. */
    private final Set<Card> owned = new HashSet<>();
    /** The {@link Noble}s that the {@link Player} has. */
    private final Set<Noble> nobles = new HashSet<>();
    /** The history of the {@link Player}, from most to least recent. */
    private final Deque<Action> actions = new LinkedList<>();
    /**
     * The {@link Marker}s that have been returned by {@link #mark()} and are
     * still in {@link #actions}.
     */
    private final Set<Action> markers = new HashSet<>();

    /** The number of prestige points the {@link Player} has. */
    private int points = 0;

    @Override
    public ReadOnlyGemSet cardGems() {
        return this.cardGems.clone();
    }

    @Override
    public int cardGems(Color c) {
        return this.cardGems.get(c);
    }

    @Override
    public ReadOnlyTokenSet tokens() {
        return this.tokens.clone();
    }

    @Override
    public int tokens(TokenColor c) {
        return this.tokens.get(c);
    }

    @Override
    public int tokenCount() {
        return this.tokens.size();
    }

    @Override
    public Set<Card> reserved() {
        return Set.copyOf(this.reserved);
    }

    @Override
    public Set<Card> owned() {
        return Set.copyOf(this.owned);
    }

    @Override
    public Set<Noble> nobles() {
        return Set.copyOf(this.nobles);
    }

    @Override
    public int points() {
        return this.points;
    }

    /**
     * Give {@code amt} tokens of {@link TokenColor} {@code color} to the
     * {@link Player}. Does nothing if {@code amt} is negative.
     *
     * @param color The {@link TokenColor} of the tokens to give.
     * @param amt The amount of tokens to give to the {@link Player}.
     * @return {@code true} if the addition was successful (i.e. {@code amt} was
     *             nonnegative).
     */
    public boolean give(TokenColor color, int amt) {
        return this.tokens.give(color, amt);
    }

    /**
     * Take away {@code amt} tokens of {@link TokenColor} {@code color} from the
     * {@link Player}. Does nothing if {@code amt} is not positive. Does nothing
     * if there are not enough tokens to remove.
     *
     * @param color The {@link TokenColor} of the tokens to take away.
     * @param amt The amount of tokens to take away from the {@link Player}.
     * @return {@code true} if the removal was successful (i.e. {@code amt} was
     *             positive and does not exceed the number of available tokens).
     */
    public boolean take(TokenColor color, int amt) {
        return this.tokens.take(color, amt);
    }

    @Override
    public boolean canReserve(Card card) {
        return this.reserved.size() < RESERVE_CAP
                && !this.reserved.contains(card) && !this.owned.contains(card);
    }

    /**
     * Add {@code card} to the set of {@link Card}s the {@link Player} has in
     * reserve. Does nothing if the {@link Player} has no space to reserve
     * {@code card}.
     *
     * @param card The {@link Card} to reserve.
     * @return {@code true} if {@code card} was successfully reserved.
     */
    public boolean reserve(Card card) {
        boolean success = canReserve(card) && this.reserved.add(card);
        if (success) {
            this.actions.push(new Action(card));
        }
        return success;
    }

    @Override
    public boolean canPurchase(Card card) {
        return !this.owned.contains(card)
                && card.cost().sub(this.cardGems).dominatedBy(this.tokens);
    }

    @Override
    public boolean canPurchase(Card card, ReadOnlyTokenSet payment) {
        if (this.owned.contains(card) || !payment.dominatedBy(this.tokens)) {
            return false;
        }
        int minGold = 0;
        int maxGold = 0;
        for (Color c : Color.values()) {
            // Use tokens of that exact color first
            int rem = card.cost(c) - payment.get(c.toTokenColor());
            // Reject if over-paid
            if (rem < 0) {
                return false;
            }
            // Measure gold spread
            int min = rem - this.cardGems(c);
            minGold += min > 0 ? min : 0;
            maxGold += rem;
        }
        // Reject if not enough or too much gold
        int gold = payment.get(TokenColor.GOLD);
        return minGold <= gold && gold <= maxGold;
    }

    /**
     * Take {@code payment} from {@link #tokens} and add {@code card} to the set
     * of {@link Card}s the {@link Player} owns. Does nothing if the
     * {@link Player} cannot afford {@code payment} or {@code payment} is not
     * EXACTLY enough to pay for {@code card}.
     *
     * @param card The {@link Card} to purchase.
     * @param payment The tokens to pay with. Must be EXACTLY enough to pay for
     *            {@code card}.
     * @return {@code true} if {@code card} was successfully purchased.
     */
    public boolean purchase(Card card, ReadOnlyTokenSet payment) {
        boolean success = canPurchase(card, payment)
                && this.tokens.take(payment) && this.owned.add(card);
        if (success) {
            this.cardGems.incr(card.color());
            this.points += card.points();
            this.actions.push(new Action(this.reserved.remove(card), card, payment));
        }
        return success;
    }

    /**
     * Add {@code noble} to the set of {@link Noble}s the {@link Player} has won
     * over. Does nothing if the {@link Player} does not satisfy the
     * requirements of {@code noble}.
     *
     * @param noble The {@link Noble} to add.
     * @return {@code true} if {@code noble} was successfully added.
     */
    public boolean visitedBy(Noble noble) {
        if (!noble.satisfiedBy(this.cardGems)) {
            return false;
        }
        this.nobles.add(noble);
        this.points += noble.points;
        this.actions.push(new Action(noble));
        return true;
    }

    @Override
    public Player clone() {
        Player clone = new Player();
        clone.cardGems.put(this.cardGems);
        clone.tokens.put(this.tokens);
        clone.reserved.addAll(this.reserved);
        clone.owned.addAll(this.owned);
        clone.nobles.addAll(this.nobles);
        clone.actions.addAll(this.actions);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Gems from cards:\n").append(this.cardGems);
        sb.append("Tokens:\n").append(this.tokens);
        if (!this.reserved.isEmpty()) {
            sb.append("Reserved cards:\n");
            for (Card card : this.reserved) {
                sb.append(card);
            }
        }
        sb.append("Points: ").append(this.points).append("\n");
        return sb.toString();
    }

    @Override
    public Action mark() {
        Action mark = new Action();
        this.actions.push(mark);
        this.markers.add(mark);
        return mark;
    }

    /** The action of a {@link Player}. */
    private final class Action implements Marker {

        /** The {@link Type} of the {@link Action}. */
        private final Type type;
        /** {@code true} if the purchased card came from reserve. */
        private final boolean fromReserved;
        /** The {@link Card} involved in the {@link Action}. */
        private final Card card;
        /** The {@link Noble} involved in the {@link Action}. */
        private final Noble noble;
        /** The {@link ReadOnlyTokenSet} involved in the {@link Action}. */
        private final ReadOnlyTokenSet tokenSet;

        /**
         * Create an {@link Action} of {@link Type#RESERVE}.
         *
         * @param card The {@link Card} involved in the {@link Action}.
         */
        private Action(Card card) {
            this.type = Type.RESERVE;
            this.fromReserved = false;
            this.card = card;
            this.noble = null;
            this.tokenSet = null;
        }

        /**
         * Create an {@link Action} of {@link Type#PURCHASE}.
         *
         * @param fromReserved Whether {@code card} came from reserve.
         * @param card The {@link Card} involved in the {@link Action}.
         * @param tokens The {@link ReadOnlyTokenSet} involved in the
         *            {@link Action}.
         */
        private Action(boolean fromReserved, Card card,
                             ReadOnlyTokenSet tokens) {
            this.type = Type.PURCHASE;
            this.fromReserved = fromReserved;
            this.card = card;
            this.noble = null;
            this.tokenSet = tokens;
        }

        /**
         * Create an {@link Action} of {@link Type#VISIT}.
         *
         * @param noble The {@link Noble} involved in the {@link Action}.
         */
        private Action(Noble noble) {
            this.type = Type.VISIT;
            this.fromReserved = false;
            this.card = null;
            this.noble = noble;
            this.tokenSet = null;
        }

        /**
         * Create a marker {@link Action}.
         */
        private Action() {
            this.type = Type.MARK;
            this.fromReserved = false;
            this.card = null;
            this.noble = null;
            this.tokenSet = Player.this.tokens.clone();
        }

        @Override
        public boolean undo() {
            if (!Player.this.markers.contains(this)) {
                return false;
            }
            Action action;
            do {
                action = Player.this.actions.pop();
                switch (action.type) {
                    case RESERVE:
                        Player.this.reserved.remove(action.card);
                        break;
                    case PURCHASE:
                        Card c = action.card;
                        Player.this.tokens.give(action.tokenSet);
                        Player.this.owned.remove(c);
                        if (action.fromReserved) {
                            Player.this.reserved.add(c);
                        }
                        Player.this.cardGems.decr(c.color());
                        Player.this.points -= c.points();
                        break;
                    case VISIT:
                        Noble n = action.noble;
                        Player.this.nobles.remove(n);
                        Player.this.points -= n.points;
                        break;
                    case MARK:
                        Player.this.markers.remove(action);
                        break;
                    default:
                        throw new InternalError("This is impossible!");
                }
            } while (!action.equals(this));
            Player.this.tokens.put(this.tokenSet);
            return true;
        }

    }

    /** The different possible actions of a {@link Player}. */
    private enum Type {
        /** The action is a {@link Player#reserve(Card)}. */
        RESERVE,
        /** The action is a {@link Player#purchase(Card, ReadOnlyTokenSet)}. */
        PURCHASE,
        /** The action is a {@link Player#visitedBy(Noble)}. */
        VISIT,
        /** The action is a mark for undoing. */
        MARK
    }

}
