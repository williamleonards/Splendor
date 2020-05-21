package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ai.ArthurAI;
import ai.DefaultAI;
import ai.MCTSv0AI;
import ai.MCTSv1AI;
import ai.MCTSv2AI;
import ai.MCTSv3AI;
import ai.WillAI;
import view.HumanCLI;
import view.User;

/**
 * The {@link Main} class.
 */
public final class Main {

    /**
     * The entry point of the program.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        Controller controller = null;
        try {
            boolean debug = false;
            int goal = 15;
            int usersCount = 0;
            List<User> users = new ArrayList<>();
            for (int i = 0; i < args.length; ++i) {
                switch (args[i]) {
                    case "-d":
                    case "-debug":
                    case "--debug":
                        debug = true;
                        break;
                    case "-D":
                    case "-Debug":
                    case "--Debug":
                        debug = false;
                        break;
                    case "-g":
                    case "-goal":
                    case "--goal":
                        goal = Integer.parseInt(args[++i]);
                        break;
                    case "Human":
                        users.add(new HumanCLI("Player " + ++usersCount + ": Human"));
                        break;
                    case "ArthurAI":
                        users.add(new ArthurAI(debug, "Player " + ++usersCount + ": ArthurAI"));
                        break;
                    case "DefaultAI":
                        users.add(new DefaultAI(debug, "Player " + ++usersCount + ": DefaultAI"));
                        break;
                    case "MCTSv0AI":
                        users.add(new MCTSv0AI(debug,
                                "Player " + ++usersCount + ": MCTSv0AI",
                                Integer.parseInt(args[++i])));
                        break;
                    case "MCTSv1AI":
                        users.add(new MCTSv1AI(debug,
                                "Player " + ++usersCount + ": MCTSv1AI",
                                Integer.parseInt(args[++i])));
                        break;
                    case "MCTSv2AI":
                        users.add(new MCTSv2AI(debug,
                                "Player " + ++usersCount + ": MCTSv2AI",
                                Integer.parseInt(args[++i])));
                        break;
                    case "MCTSv3AI":
                        users.add(new MCTSv3AI(debug,
                                "Player " + ++usersCount + ": MCTSv3AI",
                                Integer.parseInt(args[++i])));
                        break;
                    case "WillAI":
                        users.add(new WillAI(debug, "Player " + ++usersCount + ": WillAI"));
                        break;
                    default:
                        throw new IllegalArgumentException("'" + args[i] + "' is not a valid option.");
                }
            }
            if (goal <= 0) {
                throw new IllegalArgumentException("The goal must be positive.");
            }
            if (usersCount < 2 || usersCount > 4) {
                throw new IllegalArgumentException("Splendor is for 2-4 players only.");
            }
            controller = new Controller(goal, users);
            Set<User> winners = controller.play();
            if (debug) {
                System.out.println("End state:\n");
                System.out.println(controller);
                System.out.println("Winner(s): " + winners);
            } else {
                System.out.println(controller.compressedData());
                System.out.println("Winner(s)");
                for (User u : winners) {
                    System.out.println(u);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
            return;
        } catch (Exception e) {
            System.out.println(controller);
            throw e;
        }
    }

    /** Prints details on how to use this program. */
    private static void printUsage() {
        System.out.println("Usage:\n"
                         + "    java -jar <THIS_JAR> [-dD] [-g <goal>] <type> <type> [type] [type]\n"
                         + "Where:\n"
                         + "    (-d) is to enable debug output\n"
                         + "    (-D) is to disable debug output\n"
                         + "    (goal) is the number of points needed to win\n"
                         + "    (type) is one of:\n"
                         + "        Human | ArthurAI | DefaultAI | MCTSv[0-3]AI <timeout (seconds)> | WillAI");
    }

}
