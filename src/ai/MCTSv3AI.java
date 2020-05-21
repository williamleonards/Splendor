package ai;

import java.util.HashSet;
import java.util.Set;

import view.Move;
import view.Move.Type;
import view.User;

/**
 * An AI employing Monte Carlo tree search. The set of children is generated
 * without {@link view.Move.Type#RESERVE}.
 */
public final class MCTSv3AI extends MCTSv2AI {

    /**
     * Create a user with the specified name.
     *
     * @param debug Whether the AI prints output.
     * @param name The name of the user.
     * @param timeout Maximum time per turn (in seconds)
     */
    public MCTSv3AI(boolean debug, String name, int timeout) {
        super(debug, name, timeout);
    }

    @Override
    protected Set<Move> getMovesToConsider(User user) {
        Set<Move> allMoves = super.getMovesToConsider(user);
        Set<Move> goodMoves = new HashSet<>();
        for (Move move : allMoves) {
            if (move.type() != Type.RESERVE) {
                goodMoves.add(move);
            }
        }
        return goodMoves.isEmpty() ? allMoves : goodMoves;
    }

}
