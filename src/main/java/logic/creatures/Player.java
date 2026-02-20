package logic.creatures;

import logic.base.BaseCreature;
import logic.base.BaseItem;
import logic.base.BaseArmor;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class Player extends BaseCreature {

    private int gold;
    private ArrayList<ItemCounter> inventory;
    private int speed;
    private int luck;

    private BaseArmor equippedArmor;

    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense);
        this.gold = 0;
        this.inventory = new ArrayList<>();
        this.speed = 0;
        this.luck = 0;
        this.equippedArmor = null;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public ArrayList<ItemCounter> getInventory() {
        return inventory;
    }

    public void addItem(BaseItem item, int amount) {

        for (ItemCounter i : inventory) {
            if (i.getItem().equals(item)) {

                if (i.getItem().isStackable()) {

                    if (i.getCount() + amount <= i.getItem().getMaxStack()) {
                        i.addCount(amount);
                        return;
                    } else {
                        int spaceLeft = i.getItem().getMaxStack() - i.getCount();
                        i.setCount(i.getItem().getMaxStack());
                        amount -= spaceLeft;
                    }

                } else {
                    inventory.add(new ItemCounter(item, 1));
                    return;
                }
            }
        }

        // ถ้ายังไม่มีใน inventory
        inventory.add(new ItemCounter(item, amount));
    }

    public void equipArmor(BaseArmor armor) {

        // ถ้ามีของเก่า ถอดก่อน
        if (equippedArmor != null) {
            equippedArmor.unequip(this);
        }

        equippedArmor = armor;
        armor.equip(this);
    }

    public void unequipArmor() {

        if (equippedArmor != null) {
            equippedArmor.unequip(this);
            equippedArmor = null;
        }
    }

    public BaseArmor getEquippedArmor() {
        return equippedArmor;
    }

    // =========================
    // Stat Bonus (เรียกจาก Armor)
    // =========================

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

    // =========================
    // Getters / Setters
    // =========================

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

    public int getSpeed() {
        return speed;
    }

    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(this.attack);
    }
}