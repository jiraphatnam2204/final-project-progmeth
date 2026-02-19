package logic.util;

import logic.base.BaseItem;

public class ItemCounter {
    private BaseItem item;
    private int count;

<<<<<<< HEAD
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemCounter that = (ItemCounter) o;
        return this.item.getName().equals(that.item.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(item);
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
||||||| 0bdb9de
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
=======
    public ItemCounter(BaseItem item, int count) {
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
        this.item = item;
        this.count = Math.max(1, count);
    }

    public BaseItem getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = Math.max(0, count);
    }
<<<<<<< HEAD

    public void addCount(int amount){this.count += amount;}
}
||||||| 0bdb9de
}
=======

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemCounter other = (ItemCounter) obj;
        return this.item.getName().equals(other.item.getName());
    }
}
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
