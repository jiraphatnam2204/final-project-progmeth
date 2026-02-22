package logic.util;
import logic.base.BaseItem;
import java.util.Objects;
public class ItemCounter {
    private BaseItem item; private int count;
    public ItemCounter(BaseItem item, int count) { this.item = item; this.count = Math.max(1, count); }
    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemCounter that = (ItemCounter) o;
        return this.item.getName().equals(that.item.getName());
    }
    @Override public int hashCode() { return Objects.hashCode(item); }
    public BaseItem getItem() { return item; }
    public void setItem(BaseItem item) { this.item = item; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = Math.max(0, count); }
    public void addCount(int amount) { this.count += amount; }
}
