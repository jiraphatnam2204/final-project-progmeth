package logic.item;

public class BaseItem {

    private final String name;
    private final boolean stackable;
    private final int maxStack;

    public BaseItem(String name, boolean stackable, int maxStack) {
        this.name = name;
        this.stackable = stackable;
        this.maxStack = stackable ? maxStack : 1;
    }

    public BaseItem(String name) {
        this.name = name;
        this.stackable = true;
        this.maxStack = 64;
    }

    public String getName() {
        return name;
    }

    public boolean isStackable() {
        return stackable;
    }

    public int getMaxStack() {
        return maxStack;
    }

    @Override
    public String toString() {
        return name;
    }
}
