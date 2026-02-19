package logic.creatures;

import logic.base.BaseCreature;
import logic.base.Item;
import logic.creatures.ItemStack;

import java.util.ArrayList;


public class Player extends BaseCreature {

    private int money;
    private ArrayList<ItemStack> inventory;

    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense);
        this.money = 0;
        this.inventory = new ArrayList<>();
    }

    public void addMoney(int amount) {
        this.money += Math.max(0, amount);
    }

    public int getMoney() {
        return money;
    }

    public void addItem(Item item) {
        for (ItemStack stack : inventory) {
            if (stack.getItem().getName().equalsIgnoreCase(item.getName())) {
                stack.addAmount(1);
                return;
            }
        }
        inventory.add(new ItemStack(item, 1));
    }

    public void removeItem(String itemName) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (stack.getItem().getName().equalsIgnoreCase(itemName)) {
                stack.reduceAmount(1);
                if (stack.getAmount() <= 0) {
                    inventory.remove(i);
                }
                return;
            }
        }
    }

    public int getItemCount(String itemName) {
        for (ItemStack stack : inventory) {
            if (stack.getItem().getName().equalsIgnoreCase(itemName)) {
                return stack.getAmount();
            }
        }
        return 0;
    }

    public ArrayList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(this.attack);
    }
}
