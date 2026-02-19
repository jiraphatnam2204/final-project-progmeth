package logic.creatures;

import logic.base.BaseCreature;
import logic.base.BaseItem;
import logic.util.ItemCounter;
import java.util.ArrayList;

public class Player extends BaseCreature {

    private int gold;
    private ArrayList<ItemCounter> inventory;
    private int speed;
    private int luck;

    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense);
        this.gold = 0;
        this.inventory = new ArrayList<>();
        this.speed = 0;
        this.luck = 0; // Default luck
    }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = Math.max(0, gold); }

    public ArrayList<ItemCounter> getInventory() { return inventory; }

    public void addItem(ItemCounter item) {
        for (ItemCounter existingItem : inventory) {
            if (existingItem.equals(item)) {
                existingItem.setCount(existingItem.getCount() + item.getCount());
                return;
            }
        }
        inventory.add(item);
    }

    public void addItem(BaseItem item, int count) {
        addItem(new ItemCounter(item, count));
    }

    public void addItem(BaseItem item) {
        addItem(item, 1);
    }

    public void collectDrops(java.util.List<BaseItem> drops) {
        for (BaseItem it : drops) {
            addItem(it, 1);
        }
    }

    public void addBonus(int atk, int def, int hp, int spd) {
        this.attack += atk;
        this.defense += def;
        this.maxHealthPoint += hp;
        this.healthPoint += hp;
        this.speed += spd;
    }

    public void removeBonus(int atk, int def, int hp, int spd) {
        this.attack = Math.max(0, this.attack - atk);
        this.defense = Math.max(0, this.defense - def);
        this.maxHealthPoint = Math.max(1, this.maxHealthPoint - hp);
        if (this.healthPoint > this.maxHealthPoint) {
            this.healthPoint = this.maxHealthPoint;
        }
        this.speed -= spd;
    }

    public int getHealth() {
        return this.healthPoint;
    }

    public int getMaxHealth() {
        return this.maxHealthPoint;
    }

    public void setHealth(int hp) {
        this.healthPoint = Math.max(0, Math.min(maxHealthPoint, hp));
    }

    public int getStrength() {
        return this.attack;
    }

    public void setStrength(int strength) {
        this.attack = Math.max(0, strength);
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = Math.max(0, luck);
    }

    public int getDefense() {
        return this.defense;
    }

    public int getAttack() {
        return this.attack;
    }

    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(this.attack);
    }

}