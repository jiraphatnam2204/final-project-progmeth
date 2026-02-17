package Logic.Util;

import Logic.Base.Item;

import java.util.Objects;

public class ItemCounter {
    private Item item;
    private int count;
    public ItemCounter(Item item,int count){
        setItem(item);
        setCount(count);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemCounter that = (ItemCounter) o;
        return Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(item);
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = Math.max(0,count);
    }
}
