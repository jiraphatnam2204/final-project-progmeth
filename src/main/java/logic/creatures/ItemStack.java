package logic.creatures;

import logic.base.Item;

public class ItemStack {

    private Item item;
    private int amount;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public void reduceAmount(int amount) {
        this.amount -= amount;
    }
}
