package ai;

import util.Tree;

/**
 * An AI employing Monte Carlo tree search. The best child is chosen according
 * to the formula on Wikipedia.
 */
public class MCTSv2AI extends MCTSv0AI {

    /**
     * Create a user with the specified name.
     *
     * @param debug Whether the AI prints output.
     * @param name The name of the user.
     * @param timeout Maximum time per turn (in seconds)
     */
    public MCTSv2AI(boolean debug, String name, int timeout) {
        super(debug, name, timeout);
    }

    @Override
    protected Tree<Data> getBestChild() {
        double maxValue = -1;
        Tree<Data> best = null;
        double lnNi = Math.log(this.current.data().sims);
        for (Tree<Data> child : this.current.children()) {
            Data data = child.data();
            double value = data.winRate() + Math.sqrt(2 * lnNi / data.sims);
            if (value > maxValue) {
                maxValue = value;
                best = child;
            }
        }
        return best;
    }

}
