package ai;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import controller.Controller;
import model.cards.Card;
import model.cards.ReadOnlyCardDeck;
import model.cards.Tier;
import model.gems.Color;
import model.gems.ReadOnlyTokenSet;
import model.gems.TokenColor;
import model.gems.TokenSet;
import model.nobles.Noble;
import model.world.ReadOnlyPlayer;
import view.Move;
import view.User;

/** The default AI that (most) AIs should extend. */
public class DefaultAI implements User {

    /** Whether the AI prints output. */
    protected final boolean debug;
    /** The name of the user. */
    protected final String name;
    /** The controller of the game the user belongs to. */
    protected Controller controller = null;
    /** The player of the user in the game. */
    protected ReadOnlyPlayer player = null;

    /**
     * Create a user with the specified name.
     *
     * @param debug Whether the AI prints output.
     * @param name The name of the user.
     */
    public DefaultAI(boolean debug, String name) {
        this.debug = debug;
        this.name = name;
    }

    /**
     * If {@link #debug}, {@code System.out.print(x)}; else nothing.
     *
     * @param x The object to print.
     */
    protected void print(Object x) {
        if (this.debug) {
            System.out.print(x);
        }
    }

    /**
     * If {@link #debug}, {@code System.out.println(x)}; else nothing.
     *
     * @param x The object to print.
     */
    protected void println(Object x) {
        if (this.debug) {
            System.out.println(x);
        }
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
        this.player = controller.player(this);
    }

    @Override
    public Move move() {
        // Sort decks by descending tier
        ReadOnlyCardDeck[] decks = {
                this.controller.deck(Tier.HIGH),
                this.controller.deck(Tier.MID),
                this.controller.deck(Tier.LOW)
        };

        // Purchase
        for (ReadOnlyCardDeck d : decks) {
            for (Card card : d.display()) {
                if (this.player.canPurchase(card)) {
                    return purchase(card);
                }
            }
        }
        for (Card card : this.player.reserved()) {
            Card trueCard = this.controller.unhide(this, card);
            if (this.player.canPurchase(trueCard)) {
                return purchase(trueCard);
            }
        }

        // Gather color info
        List<Color> colorsLeft = new ArrayList<>();
        Color canTakeTwo = null;
        for (Color color : Color.values()) {
            int numTokens = this.controller.tokens(color.toTokenColor());
            if (numTokens > 0) {
                colorsLeft.add(color);
            }
            if (numTokens > 3) {
                canTakeTwo = color;
            }
        }

        // Take three
        if (colorsLeft.size() >= 3) {
            Set<Color> taken = EnumSet.noneOf(Color.class);
            print(this.name + " takes 3 tokens:");
            for (int i = 0; i < 3; ++i) {
                Color color = colorsLeft.get(i);
                print(" " + color);
                taken.add(color);
            }
            println("\n");
            return Move.takeThree(taken);
        }

        // Take two
        if (canTakeTwo != null) {
            println(this.name + " takes two of color " + canTakeTwo + "\n");
            return Move.takeTwo(canTakeTwo);
        }

        // Reserve
        for (ReadOnlyCardDeck d : decks) {
            for (Card card : d.display()) {
                if (this.player.canReserve(card)) {
                    return reserve(card);
                }
            }
            if (!d.isDeckEmpty()) {
                Card card = d.peek();
                if (this.player.canReserve(card)) {
                    return reserve(card);
                }
            }
        }

        // Take whatever's left
        Set<Color> taken = EnumSet.noneOf(Color.class);
        print(this.name + " takes " + colorsLeft.size() + " tokens:");
        for (Color color : colorsLeft) {
            print(" " + color);
            taken.add(color);
        }
        println("\n");
        return Move.takeThree(taken);
    }

    /**
     * Reserve {@code card} if possible.
     *
     * @param card The {@link Card} to reserve.
     * @return A {@link Move} of type {@link view.Move.Type#RESERVE} reserving
     *             {@code card}, or {@code null} if the player cannot reserve
     *             {@code card}.
     */
    protected Move reserve(Card card) {
        if (!this.player.canReserve(card)) {
            return null;
        }
        println(this.name + " reserves the following card:");
        println(card);
        return Move.reserve(card);
    }

    /**
     * Purchase {@code card} with as little tokens as possible, and as little
     * gold tokens as possible.
     *
     * @param card The {@link Card} to purchase.
     * @return A {@link Move} of type {@link view.Move.Type#PURCHASE} buying
     *             {@code card} with as little tokens as possible, and as little
     *             gold tokens as possible, or {@code null} if the player cannot
     *             purchase {@code card}.
     */
    protected Move purchase(Card card) {
        if (!this.player.canPurchase(card)) {
            return null;
        }
        TokenSet payment = new TokenSet();
        for (Color c : Color.values()) {
            int rem = card.cost(c) - this.player.cardGems(c);
            if (rem > 0) {
                int tokens = this.player.tokens(c.toTokenColor());
                if (tokens >= rem) {
                    payment.give(c.toTokenColor(), rem);
                } else {
                    payment.give(c.toTokenColor(), tokens);
                    payment.give(TokenColor.GOLD, rem - tokens);
                }
            }
        }
        println(this.name + " purchases the following card:");
        print(card);
        println("Using the following tokens:");
        println(payment);
        return Move.purchase(card, payment);
    }

    @Override
    public Move movePrevIllegal(String reason) {
        System.err.println(reason);
        throw new InternalError("An AI should not make illegal moves!");
    }

    @Override
    public ReadOnlyTokenSet discard(int count) {
        Random r = new Random();
        TokenColor[] colors = TokenColor.values();
        TokenSet tokens = new TokenSet();
        while (tokens.size() < count) {
            TokenColor c = colors[r.nextInt(colors.length)];
            if (tokens.get(c) < this.player.tokens(c)) {
                tokens.give(c, 1);
            }
        }
        return tokens;
    }

    @Override
    public ReadOnlyTokenSet discardPrevIllegal(
            @SuppressWarnings("unused") int count, String reason) {
        System.err.println(reason);
        throw new InternalError("An AI should not make illegal moves!");
    }

    @Override
    public Noble chooseNoble(Set<? extends Noble> nobles) {
        int maxPoints = 0;
        Noble bestNoble = null;
        for (Noble n : nobles) {
            int pts = n.points;
            if (pts > maxPoints) {
                maxPoints = pts;
                bestNoble = n;
            }
        }
        return bestNoble;
    }

    @Override
    public Noble chooseNoblePrevIllegal(
            @SuppressWarnings("unused") Set<? extends Noble> nobles,
            String reason) {
        System.err.println(reason);
        throw new InternalError("An AI should not make illegal moves!");
    }

    @Override
    public void notifyNobleVisit(Noble n) {
        println(this.name + " has been visited by a noble:");
        println(n);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
