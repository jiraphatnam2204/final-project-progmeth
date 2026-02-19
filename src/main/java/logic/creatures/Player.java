package logic.creatures;

import interfaces.Stackable;
import logic.base.BaseCreature;
<<<<<<< HEAD
import logic.base.Item;
import logic.util.ItemCounter;

||||||| 0bdb9de
=======
import logic.util.ItemCounter;
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
import java.util.ArrayList;

public class Player extends BaseCreature {

    private int money;
<<<<<<< HEAD
    private ArrayList<ItemCounter> inventory;
||||||| 0bdb9de
    private ArrayList<String> inventory;

=======
    private ArrayList<ItemCounter> inventory;
    private int speed;
    private int luck;

>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense);
        this.money = 0;
        this.inventory = new ArrayList<>();
        this.speed = 0;
        this.luck = 0; // Default luck
    }

    public int getGold() { return money; }
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = Math.max(0, money); }
    public void setGold(int money) { setMoney(money); }

    public ArrayList<ItemCounter> getInventory() { return inventory; }

<<<<<<< HEAD
    public void addItem(Item item,int amount)
    {
        for(ItemCounter i : inventory){
            if(i.getItem().equals(item)) {
                if (i instanceof Stackable) {
                    if (i.getCount() + amount <= Stackable.maxStack) {
                        i.addCount(amount);
                    } else {
                        i.setCount(Stackable.maxStack);
                        int remain = amount - (Stackable.maxStack - i.getCount());
                        inventory.add(new ItemCounter(item,remain));
                    }
                }
                else{
                    inventory.add(new ItemCounter(item,1));
                }
            }

        }
||||||| 0bdb9de
    public void addItem(String item) {
        inventory.add(item);
=======
    public void addItem(ItemCounter item) {
        for (ItemCounter existingItem : inventory) {
            if (existingItem.equals(item)) {
                existingItem.setCount(existingItem.getCount() + item.getCount());
                return;
            }
        }
        inventory.add(item);
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
    }

<<<<<<< HEAD
    public ArrayList<ItemCounter> getInventory() {
        return inventory;
||||||| 0bdb9de
    public ArrayList<String> getInventory() {
        return inventory;
=======
    public void addBounus(int atk, int def, int hp, int spd) {
        this.attack += atk;
        this.defense += def;
        this.maxHealthPoint += hp;
        this.healthPoint += hp;
        this.speed += spd;
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
    }

<<<<<<< HEAD
||||||| 0bdb9de
    public void setMoney(int money) {
        this.money = money;
    }

    public void setInventory(ArrayList<String> inventory) {
        this.inventory = inventory;
    }


=======
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

>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(this.attack);
    }
<<<<<<< HEAD

}
||||||| 0bdb9de
}
=======

}
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590
