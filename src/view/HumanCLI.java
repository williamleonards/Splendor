package view;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import controller.Controller;
import model.cards.Card;
import model.cards.ReadOnlyCardDeck;
import model.gems.Color;
import model.gems.ReadOnlyTokenSet;
import model.gems.TokenColor;
import model.gems.TokenSet;
import model.nobles.Noble;
import model.world.ReadOnlyPlayer;

/** CLI for interacting with a person. */
public final class HumanCLI implements User {

    /** The standard input reader. */
    private final Scanner scan = new Scanner(System.in);
    /** The name of the user. */
    private final String name;
    /** The controller of the game the user belongs to. */
    private Controller controller = null;
    /** The player of the user in the game. */
    private ReadOnlyPlayer player = null;

    /**
     * Create a user with the specified name.
     *
     * @param name The name of the user.
     */
    public HumanCLI(String name) {
        this.name = name;
    }

    /**
     * @param str The {@link String} to convert to a {@link Color}.
     * @return The {@link Color} corresponding to {@code str}.
     * @throws IllegalArgumentException If {@code str} is not valid.
     */
    private static Color toColor(String str) throws IllegalArgumentException {
        switch (str) {
            case "brown":
                return Color.BROWN;
            case "red":
                return Color.RED;
            case "green":
                return Color.GREEN;
            case "blue":
                return Color.BLUE;
            case "white":
                return Color.WHITE;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
        this.player = controller.player(this);
    }

    @Override
    public Move move() {
        System.out.println("--------------------------------------------------------------------------------\n");
        List<Card> cards = printCards();
        printTokens();
        System.out.println("You have the following:");
        System.out.println(this.player);
        if (this.controller.finalRound()) {
            System.out.println("This is your last round.");
        }
        while (true) {
            System.out.print("Enter a command or \"help\" for a list of commands.\n> ");
            try {
                String command = this.scan.next();
                switch (command) {
                    case "take":
                        try (Scanner line = new Scanner(this.scan.nextLine())) {
                            boolean dup = false;
                            Color color = null;
                            Set<Color> colors = EnumSet.noneOf(Color.class);
                            while (line.hasNext()) {
                                color = toColor(line.next());
                                if (colors.contains(color)) {
                                    dup = true;
                                } else {
                                    colors.add(color);
                                }
                            }
                            if (dup && colors.size() == 1) {
                                return Move.takeTwo(color);
                            }
                            return Move.takeThree(colors);
                        }
                    case "reserve":
                        return Move.reserve(cards.get(this.scan.nextInt()));
                    case "buy":
                    case "purchase":
                        return Move.purchase(cards.get(this.scan.nextInt()), getTokens());
                    case "tokens":
                        printTokens();
                        break;
                    case "decks":
                        cards = printCards();
                        break;
                    case "nobles":
                        printNobles();
                        break;
                    case "players":
                        printPlayers();
                        break;
                    case "max":
                        System.out.println("You can hold a maximum of " + Controller.MAX_TOKENS + " token(s).\n");
                        break;
                    case "goal":
                        System.out.println("You need " + this.controller.goal + " token(s) to move to the final round.\n");
                        break;
                    case "help":
                        printHelp();
                        break;
                    default:
                        System.out.println(command + " is not a valid command.");
                        break;
                }
            } catch (InputMismatchException e) {
                this.scan.next(); // Discard bad input
                System.out.println("Bad input!");
            } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                System.out.println("Bad input!");
            }
        }
    }

    /**
     * Print the tokens available.
     */
    private void printTokens() {
        System.out.println("Token(s) available:");
        System.out.println(this.controller.tokens());
    }

    /**
     * Print the cards available (on the board and in reserve), along with their
     * indices in the {@link List} returned.
     *
     * @return The available cards.
     */
    private List<Card> printCards() {
        List<Card> cards = new ArrayList<>();
        for (Card card : this.player.reserved()) {
            cards.add(this.controller.unhide(this, card));
        }
        int sep = cards.size() - 1;
        for (ReadOnlyCardDeck deck : this.controller.decks().values()) {
            for (Card card : deck.display()) {
                cards.add(card);
            }
            if (!deck.isDeckEmpty()) {
                cards.add(deck.peek());
            }
        }
        for (int i = cards.size() - 1; i >= 0; --i) {
            if (i == sep) {
                System.out.println("Reserved:\n");
            }
            System.out.println("[" + i + "]");
            System.out.println(cards.get(i));
        }
        return cards;
    }

    /**
     * Print the nobles available.
     */
    private void printNobles() {
        System.out.println("Noble(s) available:\n");
        System.out.println(this.controller.nobles());
    }

    /**
     * Print information on all the players.
     */
    private void printPlayers() {
        List<ReadOnlyPlayer> players = this.controller.players();
        for (int i = 0; i < players.size(); ++i) {
            ReadOnlyPlayer p = players.get(i);
            System.out.println("Player " + i);
            if (p.equals(this.player)) {
                System.out.println("(This is you!)");
            }
            System.out.println(p);
        }
    }

    /**
     * Print a list of available commands.
     */
    private static void printHelp() {
        System.out.println("take <color> <color> [color]: take tokens");
        System.out.println("reserve <card number>: reserve a card");
        System.out.println("buy <card number>: purchase a card");
        System.out.println("purchase <card number>: purchase a card");
        System.out.println("tokens: display all the available tokens");
        System.out.println("decks: display all the available cards");
        System.out.println("nobles: display all the available nobles");
        System.out.println("players: display information about all the players in the game");
        System.out.println("max: print the maximum number of tokens a player can hold at any given time");
        System.out.println("goal: print the number of points needed to move to the final round");
        System.out.println("help: display this help screen");
        System.out.println();
    }

    @Override
    public Move movePrevIllegal(String reason) {
        System.out.println(reason);
        System.out.println();
        return move();
    }

    @Override
    public ReadOnlyTokenSet discard(int count) {
        while (true) {
            System.out.println("You have too many tokens and must discard exactly " + count + " token(s).\n");
            try {
                return getTokens();
            } catch (InputMismatchException e) {
                this.scan.next(); // Discard bad input
                System.out.println("Bad input!");
            } catch (IllegalArgumentException e) {
                System.out.println("Bad input!");
            }
        }
    }

    @Override
    public ReadOnlyTokenSet discardPrevIllegal(int count, String reason) {
        System.out.println(reason);
        System.out.println();
        return discard(count);
    }

    @Override
    public Noble chooseNoble(Set<? extends Noble> nobles) {
        Noble[] nobleArray = (Noble[])nobles.toArray();
        System.out.println("Please choose one of the following nobles:\n");
        for (int i = 0; i < nobleArray.length; ++i) {
            System.out.println("[" + i + "]");
            System.out.println(nobleArray[i]);
            System.out.println();
        }
        String prompt = "Please enter an integer between 0 and " + (nobleArray.length - 1) + " (inclusive).\n> ";
        while (true) {
            System.out.print(prompt);
            try {
                Noble noble = nobleArray[this.scan.nextInt()];
                System.out.println();
                return noble;
            } catch (InputMismatchException e) {
                this.scan.next(); // Discard bad input
                System.out.println("That is not an integer.");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("That is outside the valid range.");
            }
        }
    }

    @Override
    public Noble chooseNoblePrevIllegal(Set<? extends Noble> nobles, String reason) {
        System.out.println(reason);
        System.out.println();
        return chooseNoble(nobles);
    }

    @Override
    public void notifyNobleVisit(Noble n) {
        System.out.println("You have been visited by the following noble:");
        System.out.println(n);
        System.out.println();
    }

    /**
     * Prompt the user to select tokens.
     *
     * @return The tokens selected by the user.
     * @throws InputMismatchException If one of the inputs is not an integer.
     * @throws IllegalArgumentException If one of the inputs is negative.
     */
    private ReadOnlyTokenSet getTokens() throws InputMismatchException, IllegalArgumentException {
        System.out.println("You currently have the following token(s):");
        System.out.println(this.player.tokens());
        List<String> colorNames = new ArrayList<>();
        for (TokenColor tc : TokenColor.values()) {
            colorNames.add("[" + tc + "]");
        }
        String prompt = "Please enter the token(s) to select. Format:\n" + String.join(" ", colorNames) + "\n> ";
        System.out.print(prompt);
        try {
            TokenSet tokens = new TokenSet();
            for (TokenColor tc : TokenColor.values()) {
                if (!tokens.put(tc, this.scan.nextInt())) {
                    throw new IllegalArgumentException();
                }
            }
            System.out.println();
            return tokens;
        } catch (InputMismatchException e) {
            System.out.println("That is not an integer.");
            throw e; // Exit token selection screen
        } catch (IllegalArgumentException e) {
            System.out.println("You cannot select a negative number of tokens!");
            throw e; // Exit token selection screen
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

}
