package logic.util;

import logic.base.BaseItem;
import java.util.Objects;

/**
 * Represents an item paired with a quantity in the player's inventory.
 * Two {@code ItemCounter} objects are considered equal if they hold the same item name.
 */
public class ItemCounter {

    private BaseItem item;
    private int count;

    /**
     * Creates a new ItemCounter with the given item and count.
     * Count is enforced to be at least 1.
     *
     * @param item  the item being tracked
     * @param count the quantity; minimum value is 1
     */
    public ItemCounter(BaseItem item, int count) { this.item = item; this.count = Math.max(1, count); }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemCounter that = (ItemCounter) o;
        return this.item.getName().equals(that.item.getName());
    }

    @Override
    public int hashCode() { return Objects.hashCode(item); }

    /**
     * Returns the item being tracked.
     *
     * @return the item
     */
    public BaseItem getItem() { return item; }

    /**
     * Sets the item being tracked.
     *
     * @param item the new item
     */
    public void setItem(BaseItem item) { this.item = item; }

    /**
     * Returns the current quantity of this item.
     *
     * @return item count
     */
    public int getCount() { return count; }

    /**
     * Sets the quantity. Minimum value is 0.
     *
     * @param count the new quantity
     */
    public void setCount(int count) { this.count = Math.max(0, count); }

    /**
     * Increases the quantity by the given amount.
     *
     * @param amount the amount to add
     */
    public void addCount(int amount) { this.count += amount; }
}
