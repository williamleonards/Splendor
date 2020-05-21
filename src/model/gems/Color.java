package model.gems;

/** The different possible colors of a gem. */
public enum Color {

    /** The gem is brown. */
    BROWN,
    /** The gem is red. */
    RED,
    /** The gem is green. */
    GREEN,
    /** The gem is blue. */
    BLUE,
    /** The gem is white. */
    WHITE;

    /**
     * @return The {@link TokenColor} corresponding to the {@link Color}.
     */
    public final TokenColor toTokenColor() {
        switch (this) {
            case BROWN:
                return TokenColor.BROWN;
            case RED:
                return TokenColor.RED;
            case GREEN:
                return TokenColor.GREEN;
            case BLUE:
                return TokenColor.BLUE;
            case WHITE:
                return TokenColor.WHITE;
            default:
                throw new InternalError("This is impossible!");
        }
    }

}
