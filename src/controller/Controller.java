package controller;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.cards.Card;
import model.cards.CardDeck;
import model.cards.ReadOnlyCardDeck;
import model.cards.Tier;
import model.gems.Color;
import model.gems.ReadOnlyTokenSet;
import model.gems.TokenColor;
import model.gems.TokenSet;
import model.nobles.Noble;
import model.nobles.NobleDeck;
import model.nobles.ReadOnlyNobleDeck;
import model.world.Player;
import model.world.ReadOnlyPlayer;
import view.Move;
import view.User;

/** The game controller. All mutations to the model should go through here. */
public class Controller {

    /** The maximum number of tokens a player can hold at any given time. */
    public static final int MAX_TOKENS = 10;

    /** The different possible phases of a turn. */
    public enum Phase {
        /** The user is to make a normal move. */
        MOVE,
        /** The user is to discard token(s). */
        DISCARD,
        /** The user is to be visited by a noble. */
        VISIT
    }

    /** The number of points needed to move to the final round. */
    public final int goal;

    /** The {@link User}s in the game. */
    private final List<User> users = new ArrayList<>();
    /** The {@link Player}s of the {@link User}s in the game. */
    protected final Map<User, Player> players = new HashMap<>();
    /** The {@link CardDeck}s (of each {@link Tier}) in the game. */
    protected final Map<Tier, CardDeck> decks = new EnumMap<>(Tier.class);
    /** The tokens that are currently available. */
    protected final TokenSet tokens = new TokenSet();
    /** The nobles that are still available. */
    protected final NobleDeck nobles;

    /** The number of rounds that have passed. */
    protected int rounds = 0;
    /** The index of the current user in {@link #users}. */
    protected int currUserIdx = 0;
    /** The current {@link Phase} of the current turn. */
    protected Phase phase = Phase.MOVE;

    /**
     * Create a new instance of the game. Play begins from the first
     * {@link User} in {@code users} and proceeds sequentially.
     *
     * @param goal The number of points needed to win.
     * @param users The {@link User}s in the game.
     */
    public Controller(int goal, List<? extends User> users) {
        int no_of_users = users.size();
        this.goal = goal;
        this.users.addAll(users);
        for (User u : users) {
            this.players.put(u, new Player());
        }
        for (Tier t : Tier.values()) {
            this.decks.put(t, new CardDeck(t));
        }
        for (TokenColor c : TokenColor.values()) {
            this.tokens.put(c, numTokens(c, no_of_users));
        }
        this.nobles = new NobleDeck(no_of_users + 1);
        for (User u : users) {
            u.setController(this);
        }
    }

    /**
     * Create a copy of {@code controller} but with {@link #users} replaced by
     * {@code users} and the decks shuffled.
     *
     * @param controller The {@link Controller} to copy.
     * @param users The {@link User}s in the game.
     * @throws IllegalArgumentException If {@code users} does not have the same
     *             size as {@code controller.users}.
     */
    public Controller(Controller controller, List<? extends User> users) {
        if (controller.numberOfUsers() != users.size()) {
            throw new IllegalArgumentException(
                    "users does not have the same size as controller.users.");
        }
        this.goal = controller.goal;
        this.users.addAll(users);
        for (int i = 0; i < users.size(); ++i) {
            Player player = controller.players.get(controller.users.get(i));
            Player clone = player.clone();
            this.players.put(users.get(i), clone);
        }
        for (Tier t : Tier.values()) {
            this.decks.put(t, controller.decks.get(t).clone());
        }
        this.tokens.put(controller.tokens());
        this.nobles = controller.nobles.clone();
        this.rounds = controller.rounds;
        this.currUserIdx = controller.currUserIdx;
        this.phase = controller.phase;
        for (User u : users) {
            u.setController(this);
        }
        for (CardDeck deck : this.decks.values()) {
            deck.shuffle();
        }
    }

    /**
     * @param color The {@link TokenColor} of the token(s).
     * @param users The number of {@link User}s in the game.
     * @return The starting number of tokens for this {@code color} and this
     *             {@code players}.
     */
    private static int numTokens(TokenColor color, int users) {
        if (color == TokenColor.GOLD) {
            return 5;
        }
        switch (users) {
            case 2:
                return 4;
            case 3:
                return 5;
            case 4:
                return 7;
            default:
                System.err.println("Invalid number of users!");
                return 0;
        }
    }

    /**
     * @return The number of users in the game.
     */
    public int numberOfUsers() {
        return this.users.size();
    }

    /**
     * @return A read-only view of the players in the game (in turn order).
     */
    public List<ReadOnlyPlayer> players() {
        List<ReadOnlyPlayer> p = new ArrayList<>();
        for (User u : this.users) {
            p.add(player(u));
        }
        return p;
    }

    /**
     * @return A read-only view of the current player, or {@code null} if no
     *             such player exists.
     */
    public ReadOnlyPlayer player() {
        return this.players.get(this.users.get(this.currUserIdx));
    }

    /**
     * @param user The {@link User} requesting the player.
     * @return A read-only view of the player belonging to {@code user}, or
     *             {@code null} if no such player exists.
     */
    public ReadOnlyPlayer player(User user) {
        return this.players.get(user);
    }

    /**
     * @return A read-only view of the decks (of each {@link Tier}) in the game.
     */
    public Map<Tier, ReadOnlyCardDeck> decks() {
        Map<Tier, ReadOnlyCardDeck> d = new EnumMap<>(Tier.class);
        for (Tier t : Tier.values()) {
            d.put(t, deck(t));
        }
        return d;
    }

    /**
     * @param tier The {@link Tier} of the deck to return.
     * @return A read-only view of the {@code tier} deck in the game.
     */
    public ReadOnlyCardDeck deck(Tier tier) {
        return this.decks.get(tier);
    }

    /**
     * @return A read-only view of the tokens that are currently available.
     */
    public ReadOnlyTokenSet tokens() {
        return this.tokens;
    }

    /**
     * @param tc The {@link TokenColor} of the pile to query.
     * @return The number of {@code tc} tokens available.
     */
    public int tokens(TokenColor tc) {
        return this.tokens.get(tc);
    }

    /**
     * @return A read-only view of the nobles that are still available.
     */
    public ReadOnlyNobleDeck nobles() {
        return this.nobles;
    }

    /**
     * @return Whether it is currently the final round.
     */
    public boolean finalRound() {
        for (ReadOnlyPlayer p : this.players.values()) {
            if (p.points() >= this.goal) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the game is in a deadlock.
     *
     * @return {@code true} if nobody can make a move; {@code false} otherwise.
     */
    private boolean deadlock() {
        for (Color c : Color.values()) {
            if (tokens(c.toTokenColor()) > 0) {
                return false;
            }
        }
        for (ReadOnlyPlayer p : this.players.values()) {
            for (Noble n : nobles().contents()) {
                if (n.satisfiedBy(p.cardGems())) {
                    return false;
                }
            }
            for (Card c : p.reserved()) {
                if (p.canPurchase(unhide(c))) {
                    return false;
                }
            }
            for (ReadOnlyCardDeck d : this.decks.values()) {
                if (!d.isDeckEmpty() && p.canReserve(d.peek())) {
                    return false;
                }
                for (Card c : d.display()) {
                    if (p.canReserve(c) || p.canPurchase(c)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * @return {@code true} if the game has ended; {@code false} otherwise.
     */
    public boolean gameOver() {
        return this.currUserIdx == 0 && this.phase == Phase.MOVE
                && (finalRound() || deadlock());
    }

    /**
     * Return {@code card} but unhidden, provided {@code card} is currently
     * reserved by {@code user}.
     *
     * @param user The {@link User} requesting the unhiding operation.
     * @param card The {@link Card} to unhide.
     * @return The unhidden version of {@code card} if it is currently reserved
     *             by {@code user} (which may be {@code card} itself if it is
     *             already unhidden).
     */
    public Card unhide(User user, Card card) {
        if (player(user).reserved().contains(card)) {
            return this.decks.get(card.tier()).unhide(card);
        }
        return card;
    }

    /**
     * Return {@code card} but unhidden, provided {@code card} was hidden by one
     * of the decks present.
     *
     * @param card The {@link Card} to unhide.
     * @return The unhidden version of {@code card} if it was hidden by one of
     *             the decks present (which may be {@code card} itself if it is
     *             already unhidden).
     */
    private Card unhide(Card card) {
        if (!card.isHidden()) {
            return card;
        }
        for (CardDeck deck : this.decks.values()) {
            Card trueCard = deck.unhide(card);
            if (!trueCard.isHidden()) {
                return trueCard;
            }
        }
        return card;
    }

    /**
     * Play the game.
     *
     * @return The winning {@link User}(s).
     */
    Set<User> play() {
        while (next()) {/**/}
        return getWinners();
    }

    /**
     * Advance the game by a single phase.
     *
     * @return {@code true} if the game was advanced; {@code false} otherwise.
     */
    protected boolean next() {
        if (gameOver()) {
            return false;
        }
        if (this.currUserIdx == 0 && this.phase == Phase.MOVE) {
            ++this.rounds;
        }
        User u = this.users.get(this.currUserIdx);
        Player p = this.players.get(u);
        switch (this.phase) {
            case MOVE: {
                // Normal moves
                String error = move(p, u.move());
                while (error != null) {
                    error = move(p, u.movePrevIllegal(error));
                }
                this.phase = Phase.DISCARD;
                break;
            }
            case DISCARD: {
                // Discard tokens
                int extra = p.tokenCount() - MAX_TOKENS;
                if (extra > 0) {
                    String error = discard(p, extra, u.discard(extra));
                    while (error != null) {
                        error = discard(p, extra,
                                u.discardPrevIllegal(extra, error));
                    }
                }
                this.phase = Phase.VISIT;
                break;
            }
            case VISIT: {
                // Noble visits
                Set<Noble> visits = this.nobles.satisfied(p.cardGems());
                if (!visits.isEmpty()) {
                    Noble noble;
                    if (visits.size() == 1) {
                        noble = visits.iterator().next();
                    } else {
                        noble = u.chooseNoble(visits);
                    }
                    String error = nobleVisit(p, visits, noble);
                    // One-noble visits must never error out!
                    while (error != null) {
                        noble = u.chooseNoblePrevIllegal(visits, error);
                        error = nobleVisit(p, visits, u.chooseNoble(visits));
                    }
                    u.notifyNobleVisit(noble);
                }
                this.phase = Phase.MOVE;
                ++this.currUserIdx;
                this.currUserIdx %= numberOfUsers();
                break;
            }
            default:
                throw new InternalError("This is impossible!");
        }
        return true;
    }

    /**
     * @return The winning {@link User}(s) of the game, or {@code null} if the
     *             game is not over yet.
     */
    private Set<User> getWinners() {
        if (!gameOver()) {
            return null;
        }
        // Get winners
        int maxPoints = this.goal;
        Set<User> prewinners = new HashSet<>();
        for (User u : this.users) {
            ReadOnlyPlayer p = player(u);
            int pts = p.points();
            if (pts > maxPoints) {
                maxPoints = pts;
                prewinners.clear();
                prewinners.add(u);
            } else if (pts == maxPoints) {
                prewinners.add(u);
            }
        }
        int minCards = Integer.MAX_VALUE;
        Set<User> winners = new HashSet<>();
        for (User u : prewinners) {
            ReadOnlyPlayer p = player(u);
            int size = p.owned().size();
            if (size < minCards) {
                minCards = size;
                winners.clear();
                winners.add(u);
            } else if (size == minCards) {
                winners.add(u);
            }
        }
        return winners;
    }

    /**
     * Perform the given {@code move}.
     *
     * @param p The {@link Player} performing the {@link Move}.
     * @param move The {@link Move} of {@code p}.
     * @return {@code null} if the move was legal and successful; an error
     *             message otherwise.
     */
    private String move(Player p, Move move) {
        if (move == null) {
            return "You are not allowed to return a null move.";
        }
        switch (move.type()) {
            case TAKE_THREE:
                return takeThree(p, move.colors());
            case TAKE_TWO:
                return takeTwo(p, move.color());
            case RESERVE:
                return reserve(p, move.card());
            case PURCHASE:
                return purchase(p, move.card(), move.payment());
            default:
                throw new InternalError("This is impossible!");
        }
    }

    /**
     * Take (up to) three tokens of different colors and give them to {@code p}.
     *
     * @param p The {@link Player} in question.
     * @param colors The colors of the tokens in question.
     * @return {@code null} if the move was legal and successful; an error
     *             message otherwise.
     */
    private String takeThree(Player p, Set<Color> colors) {
        if (colors == null) {
            return "You are not allowed to return a null color set.";
        }
        for (Color c : colors) {
            if (c == null) {
                return "You are not allowed to return a null color.";
            }
        }
        Set<Color> avails = availableColors();
        if (avails.size() < 3) {
            if (!avails.equals(colors)) {
                return "There are less than 3 colors available, so you must take exactly those colors.";
            }
        } else if (colors.size() != 3) {
            return "You must take exactly three colors.";
        }
        for (Color c : colors) {
            if (!avails.contains(c)) {
                return "There are no more " + c.toString() + " tokens.";
            }
        }
        for (Color c : colors) {
            this.tokens.take(c.toTokenColor(), 1);
            p.give(c.toTokenColor(), 1);
        }
        return null;
    }

    /**
     * @return A {@link Set} of the token {@link Color}s that are available.
     */
    private Set<Color> availableColors() {
        Set<Color> avails = EnumSet.noneOf(Color.class);
        for (Color c : Color.values()) {
            if (this.tokens.get(c.toTokenColor()) > 0) {
                avails.add(c);
            }
        }
        return avails;
    }

    /**
     * Take (up to) two tokens of the same color and give them to {@code p}.
     *
     * @param p The {@link Player} in question.
     * @param color The colors of the tokens in question.
     * @return {@code null} if the move was legal and successful; an error
     *             message otherwise.
     */
    private String takeTwo(Player p, Color color) {
        if (color == null) {
            return "You are not allowed to return a null color.";
        }
        TokenColor tc = color.toTokenColor();
        if (this.tokens.get(tc) < 4) {
            return "There are not enough tokens of that color.";
        }
        this.tokens.take(tc, 2);
        p.give(tc, 2);
        return null;
    }

    /**
     * Reserve {@code card} for {@code p}.
     *
     * @param p The {@link Player} in question.
     * @param card The {@link Card} to reserve.
     * @return {@code null} if the move was legal and successful; an error
     *             message otherwise.
     */
    private String reserve(Player p, Card card) {
        if (card == null) {
            return "You are not allowed to return a null card.";
        }
        if (!p.canReserve(card)) {
            return "You cannot reserve this card.";
        }
        CardDeck deck = this.decks.get(card.tier());
        if (deck.onDeck(card)) {
            deck.draw();
        } else if (deck.inDisplay(card)) {
            deck.take(card);
        } else {
            return "This card is not available.";
        }
        p.reserve(card);
        if (this.tokens.take(TokenColor.GOLD, 1)) {
            p.give(TokenColor.GOLD, 1);
        }
        return null;
    }

    /**
     * Purchase {@code card} for {@code p}.
     *
     * @param p The {@link Player} in question.
     * @param card The {@link Card} to purchase.
     * @param payment The tokens to buy {@code card} with.
     * @return {@code null} if the move was legal and successful; an error
     *             message otherwise.
     */
    private String purchase(Player p, Card card, ReadOnlyTokenSet payment) {
        if (card == null) {
            return "You are not allowed to return a null card.";
        }
        if (payment == null) {
            return "You are not allowed to return a null payment.";
        }
        CardDeck deck = this.decks.get(card.tier());
        if (!p.reserved().contains(card)) {
            if (card.isHidden()) {
                return "You must reserve a hidden card before purchasing it.";
            }
            if (!deck.inDisplay(card)) {
                return "This card is not available.";
            }
        }
        Card trueCard = unhide(card);
        if (!p.canPurchase(trueCard)) {
            return "You cannot purchase this card.";
        }
        if (!p.canPurchase(trueCard, payment)) {
            return "Your payment does not match the card's cost.";
        }
        deck.take(trueCard);
        p.purchase(trueCard, payment);
        this.tokens.give(payment);
        return null;
    }

    /**
     * Attempt to have {@code p} discard {@code tokens}. Must discard exactly
     * {@code extra} tokens.
     *
     * @param p The {@link Player} that needs to discard tokens.
     * @param extra The number of tokens to discard.
     * @param discards The tokens to be discarded.
     * @return {@code null} if the discard was legal and successful; an error
     *             message otherwise.
     */
    private String discard(Player p, int extra, ReadOnlyTokenSet discards) {
        if (discards == null) {
            return "You are not allowed to discard a null set.";
        }
        if (discards.size() != extra) {
            return "Incorrect number of tokens discarded.";
        }
        if (!discards.dominatedBy(p.tokens())) {
            return "You do not have enough tokens to discard.";
        }
        for (TokenColor c : TokenColor.values()) {
            int amt = discards.get(c);
            if (amt > 0) {
                p.take(c, amt);
                this.tokens.give(c, amt);
            }
        }
        return null;
    }

    /**
     * Attempt to have {@code n} visit {@code p}.
     *
     * @param p The {@link Player} to be visited.
     * @param visits The {@link Noble}s that wish to visit {@code p}.
     * @param n The visiting {@link Noble}.
     * @return {@code null} if the visit was legal and successful; an error
     *             message otherwise.
     */
    private String nobleVisit(Player p, Set<Noble> visits, Noble n) {
        if (n == null) {
            return "You are not allowed to choose a null noble.";
        }
        if (!visits.contains(n)) {
            return "The noble you have chosen does not want you.";
        }
        this.nobles.take(n);
        p.visitedBy(n);
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Players:\n\n");
        for (User u : this.users) {
            sb.append(u).append("\n").append(player(u)).append("\n");
        }
        sb.append("Decks:\n\n");
        for (Tier t : Tier.values()) {
            sb.append(t).append("\n").append(deck(t)).append("\n");
        }
        sb.append("Tokens: ").append(tokens()).append("\n");
        sb.append("Nobles:\n\n").append(nobles());
        sb.append("Number of rounds elapsed: ").append(this.rounds).append("\n");
        return sb.toString();
    }

    /**
     * @return Data associated with the {@link Controller}, but compressed.
     */
    public String compressedData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Players\n");
        for (User u : this.users) {
            sb.append(u).append(" ").append(player(u).points()).append("\n");
        }
        sb.append("Decks\n");
        for (Tier t : Tier.values()) {
            ReadOnlyCardDeck d = deck(t);
            sb.append(t).append(" ").append(d.deckSize() + d.display().size()).append("\n");
        }
        sb.append("Rounds\n").append(this.rounds);
        return sb.toString();
    }

}
