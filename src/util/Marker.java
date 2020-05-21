package util;

/** A marker for undo operations. */
public interface Marker {

    /**
     * Undo until just before {@code marker}. If {@code marker} is not in the
     * history, do nothing.
     *
     * @return {@code true} if {@code marker} was found in the history (before
     *             undoing); {@code false} otherwise.
     */
    public boolean undo();

}
