package ai;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import model.cards.Card;
import model.cards.ReadOnlyCardDeck;
import model.cards.Tier;
import model.gems.Color;
import model.gems.GemSet;
import model.gems.TokenColor;
import model.nobles.Noble;
import model.world.ReadOnlyPlayer;
import view.Move;

/** William's AI. */
public final class WillAI extends DefaultAI {

    /**
     * Create a user with the specified name.
     *
     * @param debug Whether the AI prints output.
     * @param name The name of the user.
     */
    public WillAI(boolean debug, String name) {
        super(debug, name);
    }

    @Override
    public Move move() {
        List<Card> cards = new ArrayList<>();
        for (Card c : this.player.reserved()) {
            cards.add(this.controller.unhide(this, c));
        }
        Tier[] tiers = Tier.values();
        for (Tier t : tiers) {
            for (Card c : this.controller.deck(t).display()) {
                cards.add(c);
            }
        }
        cards.sort((a, b) -> { // Sort by descending utility
            double A = cardUtility(a, this.player);
            double B = cardUtility(b, this.player);
            return A > B ? -1 : (A == B ? 0 : 1);
        });
        for (int i = tiers.length - 1; i >= 0; --i) {
            ReadOnlyCardDeck d = this.controller.deck(tiers[i]);
            if (!d.isDeckEmpty()) {
                cards.add(d.peek());
            }
        }

        // Try buying the best card
        Card bestCard = cards.get(0);
        if (this.player.canPurchase(bestCard)) {
            return purchase(bestCard);
        }

        // Else, (try to) take tokens towards that card
        return affordCard(cards);
    }

    @SuppressWarnings("javadoc")
    private Move affordCard(List<Card> considered) {
        Card bestCard = considered.get(0);
        List<Color> colors = new ArrayList<>();
        GemSet remCost = (GemSet)bestCard.cost().sub(this.player.cardGems());
        for (Color c : Color.values()) {
            colors.add(c);
            remCost.take(c, this.player.tokens(c.toTokenColor()));
        }
        colors.sort((a, b) -> {
            int diff = remCost.get(b) - remCost.get(a);
            return diff != 0 ? diff
                    : (this.controller.tokens(a.toTokenColor())
                            - this.controller.tokens(b.toTokenColor()));
        });

        // Take two tokens where appropriate
        Color color1 = colors.get(0);
        Color color2 = colors.get(1);
        if (remCost.get(color1) > 1 && remCost.get(color2) <= 0) {
            if (this.controller.tokens(color1.toTokenColor()) > 3) {
                println(this.name + " takes two of color " + color1 + "\n");
                return Move.takeTwo(color1);
            }
            // If have > 8 tokens, reserve a card to get gold token
            if (this.player.tokenCount() > 8
                    && this.controller.tokens(TokenColor.GOLD) > 0) {
                for (Card c : considered) {
                    if (this.player.canReserve(c)) {
                        println(this.name + " reserves the following card:");
                        println(c);
                        return Move.reserve(c);
                    }
                }
            }
        }

        // If no tokens are left, attempt to purchase/reserve *anything*
        boolean empty = true;
        for (Color c : Color.values()) {
            if (this.controller.tokens(c.toTokenColor()) > 0) {
                empty = false;
            }
        }
        if (empty) {
            for (ReadOnlyCardDeck d : this.controller.decks().values()) {
                for (Card card : d.display()) {
                    if (this.player.canPurchase(card)) {
                        return purchase(card);
                    }
                }
            }
            for (Card card : this.player.reserved()) {
                if (this.player.canPurchase(card)) {
                    return purchase(card);
                }
            }
            for (Card c : considered) {
                if (this.player.canReserve(c)) {
                    println(this.name + " reserves the following card:");
                    println(c);
                    return Move.reserve(c);
                }
            }
        }

        // Else, take three tokens, or whatever's left
        Set<Color> taken = EnumSet.noneOf(Color.class);
        int i = 0;
        print(this.name + " takes (up to) three tokens:");
        for (Color color : colors) {
            if (this.controller.tokens(color.toTokenColor()) > 0) {
                print(" " + color);
                taken.add(color);
                ++i;
            }
            if (i >= 3) {
                break;
            }
        }
        println("\n");
        return Move.takeThree(taken);
    }

    @SuppressWarnings("javadoc")
    private class Pair<U, V> {
        private U first;
        private V second;

        private Pair(U x, V y) {
            this.first = x;
            this.second = y;
        }
    }

    @SuppressWarnings("javadoc")
    private static double turnUtility(double turns) {
        return turns;
    }

    @SuppressWarnings("javadoc")
    private static double prevGemUtility(double prevGems) {
        return prevGems;
    }

    @SuppressWarnings("javadoc")
    private static double take2Penalty(int x) {
        return x * (x - 1) / 4.0;
    }

    @SuppressWarnings("javadoc")
    private Pair<Double, Integer> turnsRequired(Card c, ReadOnlyPlayer p) {
        List<Pair<Color, Integer>> requirements = new ArrayList<>();
        int turns = 0;
        int take2 = 0;
        for (Color color : Color.values()) {
            if (this.controller.tokens(color.toTokenColor()) == 0) {
                // have to reserve gold token at the moment
                turns += c.cost(color);
                requirements.add(new Pair<>(color, 0));
            } else {
                int required = c.cost(color);
                required = Math.max(required - p.tokens(color.toTokenColor()) - p.cardGems(color), 0);
                requirements.add(new Pair<>(color, required));
            }
        }

        // card is unreachable; set high number of turns; 3 is max number of gold tokens
        if (turns > 3) {
            return new Pair<>(100.0, 0);
        }
        requirements.sort((a, b) -> b.second - a.second);
        while (requirements.get(0).second > 0) {
            Pair<Color, Integer> color1 = requirements.get(0);
            Pair<Color, Integer> color2 = requirements.get(1);
            Pair<Color, Integer> color3 = requirements.get(2);
            // take two
            if (color2.second <= 0 && color1.second > 1 && this.controller.tokens(color1.first.toTokenColor()) > 3) {
                color1.second -= 2;
                take2++;
                turns++;
            } else { // take three
                color1.second -= 1;
                color2.second -= 1;
                color3.second -= 1;
                turns++;
                requirements.sort((a, b) -> b.second - a.second);
            }
        }
        int surplus = 0;
        for (Pair<Color, Integer> color : requirements) {
            surplus -= color.second;
        }
        return new Pair<>(turns + take2Penalty(take2), surplus);
    }

    @SuppressWarnings("javadoc")
    private double cardUtility(Card c, ReadOnlyPlayer p) {
        int points = c.points();
        Pair<Double, Integer> pair = turnsRequired(c, p);
        double turns = pair.first;
        double noblePts = 0;
        Color rewardGem = c.color();
        for (Noble n : this.controller.nobles().contents()) {
            if (n.cost(rewardGem) > p.cardGems(rewardGem)) {
                int left = 0;
                for (Color color : Color.values()) {
                    left += Math.max(0, n.cost(color) - p.cardGems(color));
                }
                noblePts += (double)n.points / left;
            }
        }
        double tokenSurplus = (double)pair.second / 3;
        int prevGems = p.cardGems(rewardGem);
        return -turnUtility(turns) + points + noblePts + tokenSurplus - prevGemUtility(prevGems);
    }

}
