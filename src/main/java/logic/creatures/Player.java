package logic.creatures;

import logic.base.BaseCreature;
import logic.base.Item;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class Player extends BaseCreature {

    private int money;
    private ArrayList<ItemCounter> inventory;

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

    public void addItem(Item item)
    {
        inventory.add(item);
    }

    public ArrayList<String> getInventory() {
        return inventory;
    }

    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(this.attack);
    }
}
