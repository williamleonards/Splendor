package controller;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import model.cards.CardDeck;
import model.gems.ReadOnlyTokenSet;
import model.world.Player;
import util.Marker;
import util.Undoable;
import view.User;

/** A {@link Controller} that supports undo operations. */
public final class UndoableController extends Controller implements Undoable {

    /**
     * The history of the {@link UndoableController}, from most to least recent.
     */
    private final Deque<Action> actions = new LinkedList<>();
    /**
     * The {@link Marker}s that have been returned by {@link #mark()} and are
     * still in {@link #actions}.
     */
    private final Set<Action> markers = new HashSet<>();

    /**
     * Create a new instance of the game. Play begins from the first
     * {@link User} in {@code users} and proceeds sequentially.
     *
     * @param goal The number of points needed to win.
     * @param users The {@link User}s in the game.
     */
    public UndoableController(int goal, List<? extends User> users) {
        super(goal, users);
    }

    /**
     * Create a copy of {@code controller} but with the internal list of users
     * replaced by {@code users} and the decks shuffled. If {@code controller}
     * is an {@link UndoableController}, its history is not copied.
     *
     * @param controller The {@link Controller} to copy.
     * @param users The {@link User}s in the game.
     * @throws IllegalArgumentException if {@code users} does not have the same
     *             size as {@code controller.users}.
     */
    public UndoableController(Controller controller, List<? extends User> users) {
        super(controller, users);
    }

    @Override
    public Set<User> play() {
        return super.play();
    }

    @Override
    public boolean next() {
        return super.next();
    }

    @Override
    public Action mark() {
        Action mark = new Action();
        this.actions.push(mark);
        this.markers.add(mark);
        return mark;
    }

    /** The action of an {@link UndoableController}. */
    private final class Action implements Marker {

        /** The {@link Marker}(s) involved in the {@link Action}. */
        private final Set<Marker> marks = new HashSet<>();
        /** {@link UndoableController#tokens} at creation of {@link Action}. */
        private final ReadOnlyTokenSet tokensMark;
        /** {@link UndoableController#rounds} at creation of {@link Action}. */
        private final int roundsMark;
        /** {@link UndoableController#currUserIdx} at creation of {@link Action}. */
        private final int currUserIdxMark;
        /** {@link UndoableController#phase} at creation of {@link Action}. */
        private final Phase phaseMark;

        /**
         * Create a marker {@link Action}.
         */
        private Action() {
            UndoableController uc = UndoableController.this;
            for (Player p : uc.players.values()) {
                this.marks.add(p.mark());
            }
            for (CardDeck deck : uc.decks.values()) {
                this.marks.add(deck.mark());
            }
            this.marks.add(uc.nobles.mark());
            this.tokensMark = uc.tokens.clone();
            this.roundsMark = uc.rounds;
            this.currUserIdxMark = uc.currUserIdx;
            this.phaseMark = uc.phase;
        }

        @Override
        public boolean undo() {
            UndoableController uc = UndoableController.this;
            if (!uc.markers.contains(this)) {
                return false;
            }
            while (uc.markers.contains(this)) {
                uc.markers.remove(uc.actions.pop());
            }
            for (Marker m : this.marks) {
                m.undo();
            }
            uc.tokens.put(this.tokensMark);
            uc.rounds = this.roundsMark;
            uc.currUserIdx = this.currUserIdxMark;
            uc.phase = this.phaseMark;
            return true;
        }

    }

}
