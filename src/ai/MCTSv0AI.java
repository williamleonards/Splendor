package ai;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import controller.Controller;
import controller.UndoableController;
import model.cards.Card;
import model.cards.ReadOnlyCardDeck;
import model.gems.Color;
import model.gems.ReadOnlyTokenSet;
import model.gems.TokenColor;
import model.gems.TokenSet;
import model.nobles.Noble;
import model.world.ReadOnlyPlayer;
import util.Marker;
import util.Tree;
import view.Move;
import view.User;

/**
 * An AI employing Monte Carlo tree search. The best child is chosen randomly.
 */
public class MCTSv0AI extends DefaultAI {

    protected static final Random RANDOM = new Random();
    private static final Map<Set<Color>, Set<Move>> legalTakes = new HashMap<>();

    private final long timeout_nanos;
    private final List<DummyAI> users = new ArrayList<>();
    private final Map<ReadOnlyPlayer, DummyAI> owner = new HashMap<>();
    private final Map<Tree<Data>, Marker> markers = new HashMap<>();

    private UndoableController simulator = null;
    private Tree<Data> root = null;
    protected Tree<Data> current = null;
    private boolean stop = false;

    /**
     * Create a user with the specified name.
     *
     * @param debug Whether the AI prints output.
     * @param name The name of the user.
     * @param timeout Maximum time per turn (in seconds)
     */
    public MCTSv0AI(boolean debug, String name, int timeout) {
        super(debug, name + timeout);
        this.timeout_nanos = 1000000000L * timeout;
    }

    @Override
    public void setController(Controller controller) {
        super.setController(controller);
        this.users.clear();
        for (int i = 0; i < this.controller.numberOfUsers(); ++i) {
            this.users.add(new DummyAI());
        }
    }

    @Override
    public Move move() {
        this.simulator = new UndoableController(this.controller, this.users);
        for (DummyAI user : this.users) {
            this.owner.put(this.simulator.player(user), user);
        }
        this.root = new Tree<>(Data.NULL);
        this.current = this.root;
        this.stop = false;
        long startTime = System.nanoTime();
        while (!this.stop) {
            mark();
            // Selection
            while (!this.current.isLeaf()) {
                advance(getBestChild());
            }
            // Expansion
            DummyAI user = this.owner.get(this.simulator.player());
            Set<Move> moves = getMovesToConsider(user);
            for (Move move : moves) {
                this.current.addLeaf(new Data(user, move));
            }
            // Deviation: run every child at least once
            for (Tree<Data> child : this.current.children()) {
                Tree<Data> marker = mark();
                // Simulation
                advance(child);
                Set<User> winners = simulate();
                // Backpropagation
                for (Tree<Data> n = this.current; n != this.root; n = n.parent()) {
                    Data data = n.data();
                    if (winners.contains(data.user)) {
                        ++data.wins;
                    }
                    ++data.sims;
                }
                ++this.root.data().sims;
                undo(marker);
            }
            undo(this.root);
            if (System.nanoTime() - startTime >= this.timeout_nanos) {
                this.stop = true;
            }
        }
        double maxWinRate = -1;
        Move best = null;
        for (Tree<Data> child : this.root.children()) {
            double winRate = child.data().winRate();
            if (winRate > maxWinRate) {
                maxWinRate = winRate;
                best = child.data().move;
            }
        }
        if (best != null) {
            switch (best.type()) {
                case TAKE_THREE:
                    print(this.name + " takes 3 tokens:");
                    for (Color color : best.colors()) {
                        print(" " + color);
                    }
                    println("\n");
                    break;
                case TAKE_TWO:
                    println(this.name + " takes two of color " + best.color() + "\n");
                    break;
                case RESERVE:
                    // If best.card() is from the top of a deck,
                    // replace it with the actual card (before shuffling)
                    if (best.card().isHidden()) {
                        best = Move.reserve(this.controller
                                .deck(best.card().tier()).peek());
                    }
                    println(this.name + " reserves the following card:");
                    println(best.card());
                    break;
                case PURCHASE:
                    println(this.name + " purchases the following card:");
                    print(best.card());
                    println("Using the following tokens:");
                    println(best.payment());
                    break;
                default:
                    throw new InternalError("This is impossible!");
            }
            return best;
        }
        return super.move();
    }

    private Tree<Data> mark() {
        this.markers.put(this.current, this.simulator.mark());
        return this.current;
    }

    private boolean undo(Tree<Data> node) {
        if (!this.markers.containsKey(node)) {
            return false;
        }
        this.markers.get(node).undo();
        this.current = node;
        return true;
    }

    private boolean advance(Tree<Data> next) {
        if (next.parent() != this.current) {
            return false;
        }
        this.current = next;
        Data data = this.current.data();
        for (int i = 0; i < 3; ++i) {
            data.user.next = data;
            this.simulator.next();
        }
        return true;
    }

    protected Tree<Data> getBestChild() {
        List<Tree<Data>> children = this.current.children();
        return children.isEmpty()
                ? null
                : children.get(RANDOM.nextInt(children.size()));
    }

    private Set<User> simulate() {
        List<User> dummies = new ArrayList<>();
        Map<User, User> original = new HashMap<>();
        for (User user : this.users) {
            User dummy = new DummyAI();
            dummies.add(dummy);
            original.put(dummy, user);
        }
        UndoableController uc = new UndoableController(this.simulator, dummies);
        Set<User> winners = new HashSet<>();
        for (User dummy : uc.play()) {
            winners.add(original.get(dummy));
        }
        return winners;
    }

    protected Set<Move> getMovesToConsider(User user) {
        return getLegalMoves(this.simulator, user);
    }

    private static Set<Move> getLegalMoves(Controller ctrl, User user) {
        Set<Move> legalMoves = new HashSet<>();
        if (ctrl.gameOver()) {
            return legalMoves;
        }
        ReadOnlyPlayer p = ctrl.player(user);
        // Decks
        for (ReadOnlyCardDeck d : ctrl.decks().values()) {
            for (Card card : d.display()) {
                if (p.canPurchase(card)) {
                    legalMoves.add(Move.purchase(card, getPayment(p, card)));
                }
                if (p.canReserve(card)) {
                    legalMoves.add(Move.reserve(card));
                }
            }
            if (!d.isDeckEmpty()) {
                Card card = d.peek();
                if (p.canReserve(card)) {
                    legalMoves.add(Move.reserve(card));
                }
            }
        }
        // Purchase from reserved pile
        for (Card card : p.reserved()) {
            Card trueCard = ctrl.unhide(user, card);
            if (p.canPurchase(trueCard)) {
                legalMoves.add(Move.purchase(trueCard, getPayment(p, trueCard)));
            }
        }
        // Tokens
        List<Color> colorsLeft = new ArrayList<>();
        for (Color color : Color.values()) {
            int numTokens = ctrl.tokens(color.toTokenColor());
            if (numTokens > 0) {
                colorsLeft.add(color);
            }
            if (numTokens > 3) {
                // Take two
                legalMoves.add(Move.takeTwo(color));
            }
        }
        // Take three
        Set<Color> colorSet = colorsLeft.isEmpty()
                ? EnumSet.noneOf(Color.class)
                : EnumSet.copyOf(colorsLeft);
        if (!legalTakes.containsKey(colorSet)) {
            Set<Move> moves = new HashSet<>();
            if (colorsLeft.size() <= 3) {
                moves.add(Move.takeThree(colorSet));
            } else {
                for (int i = 0; i < colorsLeft.size(); ++i) {
                    for (int j = 0; j < i; ++j) {
                        for (int k = 0; k < j; ++k) {
                            moves.add(Move.takeThree(Set.of(colorsLeft.get(i),
                                    colorsLeft.get(j), colorsLeft.get(k))));
                        }
                    }
                }
            }
            legalTakes.put(colorSet, moves);
        }
        legalMoves.addAll(legalTakes.get(colorSet));
        return legalMoves;
    }

    private static ReadOnlyTokenSet getPayment(ReadOnlyPlayer p, Card card) {
        TokenSet payment = new TokenSet();
        for (Color c : Color.values()) {
            int rem = card.cost(c) - p.cardGems(c);
            if (rem > 0) {
                int tokens = p.tokens(c.toTokenColor());
                if (tokens >= rem) {
                    payment.give(c.toTokenColor(), rem);
                } else {
                    payment.give(c.toTokenColor(), tokens);
                    payment.give(TokenColor.GOLD, rem - tokens);
                }
            }
        }
        return payment;
    }

    protected static final class Data {

        private static final Data NULL = new Data();

        private final DummyAI user;
        private Move move;
        private ReadOnlyTokenSet tokens;
        private Noble noble;
        protected int wins = 0;
        protected int sims = 0;

        private Data() {
            this.user = null;
            this.move = null;
            this.tokens = null;
            this.noble = null;
        }

        private Data(DummyAI user, Move move) {
            this.user = user;
            this.move = move;
            this.tokens = null;
            this.noble = null;
        }

        private Data(DummyAI user, ReadOnlyTokenSet tokens) {
            this.user = user;
            this.move = null;
            this.tokens = tokens;
            this.noble = null;
        }

        private Data(DummyAI user, Noble noble) {
            this.user = user;
            this.move = null;
            this.tokens = null;
            this.noble = noble;
        }

        protected double winRate() {
            return (double)this.wins / this.sims;
        }

    }

    private static final class DummyAI extends DefaultAI {

        private Data next = Data.NULL;

        private DummyAI() {
            super(false, "");
        }

        @Override
        public Move move() {
            if (this.next != Data.NULL) {
                if (this.next.move == null) {
                    this.next.move = super.move();
                }
                Move move = this.next.move;
                this.next = Data.NULL;
                return move;
            }
            return super.move();
        }

        @Override
        public ReadOnlyTokenSet discard(int count) {
            if (this.next != Data.NULL) {
                if (this.next.tokens == null) {
                    this.next.tokens = super.discard(count);
                }
                ReadOnlyTokenSet tokens = this.next.tokens;
                this.next = Data.NULL;
                return tokens;
            }
            return super.discard(count);
        }

        @Override
        public Noble chooseNoble(Set<? extends Noble> nobles) {
            if (this.next != Data.NULL) {
                if (this.next.noble == null) {
                    this.next.noble = super.chooseNoble(nobles);
                }
                Noble noble = this.next.noble;
                this.next = Data.NULL;
                return noble;
            }
            return super.chooseNoble(nobles);
        }

    }

}
