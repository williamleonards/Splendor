package model.nobles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import model.gems.ReadOnlyGemSet;
import util.Marker;
import util.Undoable;

/** A representation of a deck of {@link Noble}s. */
public final class NobleDeck implements ReadOnlyNobleDeck, Undoable {

    /** All the available {@link Noble}s. */
    private static final List<Noble> deck = Arrays.asList(new Noble[] {
            new Noble(3, new int[] { 4, 4, 0, 0, 0 }),
            new Noble(3, new int[] { 0, 4, 4, 0, 0 }),
            new Noble(3, new int[] { 0, 0, 4, 4, 0 }),
            new Noble(3, new int[] { 0, 0, 0, 4, 4 }),
            new Noble(3, new int[] { 4, 0, 0, 0, 4 }),
            new Noble(3, new int[] { 3, 3, 3, 0, 0 }),
            new Noble(3, new int[] { 0, 3, 3, 3, 0 }),
            new Noble(3, new int[] { 0, 0, 3, 3, 3 }),
            new Noble(3, new int[] { 3, 0, 0, 3, 3 }),
            new Noble(3, new int[] { 3, 3, 0, 0, 3 }) });

    /** The {@link Noble}s currently on display. */
    private final Set<Noble> display = new HashSet<>();
    /** The history of the {@link NobleDeck}, from most to least recent. */
    private final Deque<Action> actions = new LinkedList<>();
    /**
     * The {@link Marker}s that have been returned by {@link #mark()} and are
     * still in {@link #actions}.
     */
    private final Set<Action> markers = new HashSet<>();

    /**
     * Create a random {@link NobleDeck} with {@code n} {@link Noble}s.
     *
     * @param n The number of {@link Noble}s to start with.
     * @throws IllegalArgumentException If {@code n} is greater than the number
     *             of {@link Noble}s in the {@link #deck}.
     */
    public NobleDeck(int n) {
        if (n > deck.size()) {
            throw new IllegalArgumentException(
                    "Not enough nobles in the deck.");
        }
        Collections.shuffle(deck);
        this.display.addAll(deck.subList(0, n));
    }

    /**
     * Create a {@link NobleDeck} using the {@link Noble}s in {@code nobles}.
     *
     * @param nobles The {@link Noble}s to populate the {@link NobleDeck} with.
     */
    public NobleDeck(Collection<? extends Noble> nobles) {
        this.display.addAll(nobles);
    }

    @Override
    public boolean isEmpty() {
        return this.display.isEmpty();
    }

    @Override
    public Set<Noble> contents() {
        return Set.copyOf(this.display);
    }

    @Override
    public Set<Noble> satisfied(ReadOnlyGemSet gems) {
        Set<Noble> nobles = new HashSet<>();
        for (Noble n : this.display) {
            if (n.satisfiedBy(gems)) {
                nobles.add(n);
            }
        }
        return nobles;
    }

    @Override
    public NobleDeck clone() {
        NobleDeck clone = new NobleDeck(this.display);
        clone.actions.addAll(this.actions);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Noble n : this.display) {
            sb.append(n).append("\n");
        }
        return sb.toString();
    }

    /**
     * Remove {@code noble} from the {@link NobleDeck} if it exists.
     *
     * @param noble The {@link Noble} to remove.
     * @return {@code true} if {@code noble} was successfully removed.
     */
    public boolean take(Noble noble) {
        if (!this.display.contains(noble)) {
            return false;
        }
        this.display.remove(noble);
        this.actions.push(new Action(noble));
        return true;
    }

    @Override
    public Action mark() {
        Action mark = new Action();
        this.actions.push(mark);
        this.markers.add(mark);
        return mark;
    }

    /** The action of a {@link NobleDeck}. */
    private final class Action implements Marker {

        /** The {@link Type} of the {@link Action}. */
        private final Type type;
        /** The {@link Noble} involved in the {@link Action}. */
        private final Noble noble;

        /**
         * Create an {@link Action}.
         *
         * @param noble The {@link Noble} involved in the {@link Action}.
         */
        private Action(Noble noble) {
            this.type = Type.TAKE;
            this.noble = noble;
        }

        /**
         * Create a marker {@link Action}.
         */
        private Action() {
            this.type = Type.MARK;
            this.noble = null;
        }

        @Override
        public boolean undo() {
            if (!NobleDeck.this.markers.contains(this)) {
                return false;
            }
            Action action;
            do {
                action = NobleDeck.this.actions.pop();
                switch (action.type) {
                    case TAKE:
                        NobleDeck.this.display.add(action.noble);
                        break;
                    case MARK:
                        NobleDeck.this.markers.remove(action);
                        break;
                    default:
                        throw new InternalError("This is impossible!");
                }
            } while (!action.equals(this));
            return true;
        }

    }

    /** The different possible actions of a {@link NobleDeck}. */
    private enum Type {
        /** The action is a {@link NobleDeck#take(Noble)}. */
        TAKE,
        /** The action is a mark for undoing. */
        MARK
    }

}
