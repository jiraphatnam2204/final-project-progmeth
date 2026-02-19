package logic.util;

import logic.base.BaseItem;

public class ItemCounter {
    private BaseItem item;
    private int count;

    public ItemCounter(BaseItem item, int count) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemCounter other = (ItemCounter) obj;
        return this.item.getName().equals(other.item.getName());
    }
}