package logic.base;

import java.util.Objects;

/**
 * The base class for all items in the game.
 * Stores the item's name, stackability, and maximum stack size.
 */
public class BaseItem {

    /** The name of this item. */
    private final String name;

    /** Whether this item can be stacked in the inventory. */
    private final boolean stackable;

    /** The maximum number of this item allowed per inventory slot. */
    private final int maxStack;

    /**
     * Creates a new item with the specified name, stackability, and max stack size.
     *
     * @param name      the name of the item; must not be null or blank
     * @param stackable whether this item can be stacked in the inventory
     * @param maxStack  the maximum number of this item per inventory slot
     * @throws IllegalArgumentException if name is null or blank
     */
    public BaseItem(String name, boolean stackable, int maxStack) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Item name cannot be empty");
        this.name = name;
        this.stackable = stackable;
        this.maxStack = stackable ? Math.max(1, maxStack) : 1;
    }

    /**
     * Creates a stackable item with a default max stack of 64.
     *
     * @param name the name of the item
     */
    public BaseItem(String name) { this(name, true, 64); }

    /**
     * Returns the name of this item.
     *
     * @return item name
     */
    public String getName() { return name; }

    /**
     * Returns whether this item can be stacked in the inventory.
     *
     * @return {@code true} if stackable
     */
    public boolean isStackable() { return stackable; }

    /**
     * Returns the maximum number of this item allowed per inventory slot.
     *
     * @return max stack size
     */
    public int getMaxStack() { return maxStack; }

    @Override
    public String toString() { return String.format("%s (Max: %d)", name, maxStack); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseItem baseItem)) return false;
        return stackable == baseItem.stackable && maxStack == baseItem.maxStack && Objects.equals(name, baseItem.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name, stackable, maxStack); }
}
