package view;

import java.util.Set;

import controller.Controller;
import model.gems.ReadOnlyTokenSet;
import model.nobles.Noble;

/** Interface for interaction with a user. */
public interface User {

    /**
     * Set the {@link User}'s {@link Controller}. Must be called before any
     * other methods; not doing so may lead to {@link NullPointerException}s.
     *
     * @param controller The {@link Controller} to use.
     */
    public void setController(Controller controller);

    /**
     * Prompt the {@link User} for a {@link Move}.
     *
     * @return The {@link User}'s next {@link Move}.
     */
    public Move move();

    /**
     * Re-prompt for a {@link Move} because the previous one was illegal.
     *
     * @param reason The reason the previous {@link Move} was illegal.
     * @return The {@link User}'s next {@link Move}.
     */
    public Move movePrevIllegal(String reason);

    /**
     * Ask the {@link User} to discard {@code count} tokens.
     *
     * @param count The number of tokens to discard.
     * @return The tokens to discard.
     */
    public ReadOnlyTokenSet discard(int count);

    /**
     * Re-prompt to discard tokens because the previous one was illegal.
     *
     * @param count The number of tokens to discard.
     * @param reason The reason the previous discard was illegal.
     * @return The tokens to discard.
     */
    public ReadOnlyTokenSet discardPrevIllegal(int count, String reason);

    /**
     * Ask the {@link User} to choose between the visiting {@link Noble}s.
     *
     * @param nobles The visiting {@link Noble}s.
     * @return The chosen {@link Noble}.
     */
    public Noble chooseNoble(Set<? extends Noble> nobles);

    /**
     * Re-prompt to choose a noble because the previous one was illegal.
     *
     * @param nobles The visiting {@link Noble}s.
     * @param reason The reason the previous discard was illegal.
     * @return The chosen {@link Noble}.
     */
    public Noble chooseNoblePrevIllegal(Set<? extends Noble> nobles, String reason);

    /**
     * Notify the {@link User} that a {@link Noble} visited.
     *
     * @param n The {@link Noble} that visited.
     */
    public void notifyNobleVisit(Noble n);

}
