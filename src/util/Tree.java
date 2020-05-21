package util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A tree whose nodes contain data of type {@code T}.
 *
 * @param <T> The type of data stored in the nodes.
 */
public final class Tree<T> {

    private final T data;
    private Tree<T> parent = null;
    private final Set<Tree<T>> children = new LinkedHashSet<>();

    /**
     * Create a {@link Tree} with one node containing {@code data}.
     *
     * @param data The data stored at the root.
     */
    public Tree(T data) {
        this.data = data;
        this.parent = null;
    }

    public T data() {
        return this.data;
    }

    public Tree<T> parent() {
        return this.parent;
    }

    public List<Tree<T>> children() {
        return new ArrayList<>(this.children);
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public boolean addLeaf(T leaf) {
        return addChild(new Tree<>(leaf));
    }

    public boolean addChild(Tree<T> child) {
        if (this.children.add(child)) {
            child.parent = this;
            return true;
        }
        return false;
    }

}
