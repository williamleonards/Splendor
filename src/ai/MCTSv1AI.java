package ai;

import java.util.ArrayList;
import java.util.List;

import util.Tree;

/**
 * An AI employing Monte Carlo tree search. The best child is chosen randomly
 * from those with the most wins.
 */
public final class MCTSv1AI extends MCTSv0AI {

    /**
     * Create a user with the specified name.
     *
     * @param debug Whether the AI prints output.
     * @param name The name of the user.
     * @param timeout Maximum time per turn (in seconds)
     */
    public MCTSv1AI(boolean debug, String name, int timeout) {
        super(debug, name, timeout);
    }

    @Override
    protected Tree<Data> getBestChild() {
        int maxWins = -1;
        List<Tree<Data>> best = new ArrayList<>();
        for (Tree<Data> child : this.current.children()) {
            int wins = child.data().wins;
            if (wins > maxWins) {
                maxWins = wins;
                best.clear();
                best.add(child);
            } else if (wins == maxWins) {
                best.add(child);
            }
        }
        return best.isEmpty() ? null : best.get(RANDOM.nextInt(best.size()));
    }

}
