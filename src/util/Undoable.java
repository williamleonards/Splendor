package util;

/** An interface for classes that support undo operations. */
public interface Undoable {

    /**
     * Add a {@link Marker} to the history.
     *
     * @return The added {@link Marker}.
     */
    public Marker mark();

}
