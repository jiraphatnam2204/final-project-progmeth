package logic.base;

import java.util.Objects;

public class BaseItem {

    private final String name;
    private final boolean stackable;
    private final int maxStack;

    public BaseItem(String name, boolean stackable, int maxStack) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        this.name = name;
        this.stackable = stackable;
        this.maxStack = stackable ? Math.max(1, maxStack) : 1;
    }

    public BaseItem(String name) {
        this(name, true, 64);
    }

    public String getName() { return name; }
    public boolean isStackable() { return stackable; }
    public int getMaxStack() { return maxStack; }

    @Override
    public String toString() { return String.format("%s (Max: %d)", name, maxStack); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseItem baseItem)) return false;
        return stackable == baseItem.stackable &&
                maxStack == baseItem.maxStack &&
                Objects.equals(name, baseItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stackable, maxStack);
    }

}
