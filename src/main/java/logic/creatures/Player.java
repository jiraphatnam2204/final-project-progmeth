package logic.creatures;

import logic.base.BaseArmor;
import logic.base.BaseCreature;
import logic.base.BaseItem;
import logic.base.BaseWeapon;
import logic.base.BasePotion;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.Iterator;

public class Player extends BaseCreature {

    private int gold;
    private ArrayList<ItemCounter> inventory;
    private int speed, luck;

    private BaseWeapon equippedWeapon = null;
    private BaseArmor  equippedArmor  = null;

    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense);
        gold = 0;
        inventory = new ArrayList<>();
        speed = 0;
        luck = 0;
    }

    // ───────────────── GOLD ─────────────────
    public int getGold() { return gold; }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    // ───────────────── INVENTORY ─────────────────
    public ArrayList<ItemCounter> getInventory() {
        return inventory;
    }

    public void addItem(BaseItem item, int amount) {
        if (item.isStackable()) {
            int remaining = amount;
            for (ItemCounter i : inventory) {
                if (i.getItem().getName().equals(item.getName())) {
                    int space = item.getMaxStack() - i.getCount();
                    if (space <= 0) continue;
                    int add = Math.min(space, remaining);
                    i.addCount(add);
                    remaining -= add;
                    if (remaining <= 0) return;
                }
            }
            // ยังเหลืออยู่ → สร้าง slot ใหม่ไปเรื่อยๆ จนหมด
            while (remaining > 0) {
                int add = Math.min(item.getMaxStack(), remaining);
                inventory.add(new ItemCounter(item, add));
                remaining -= add;
            }
        } else {
            // ไม่ stackable → เพิ่มแยก slot ทุกชิ้น
            for (int i = 0; i < amount; i++) {
                inventory.add(new ItemCounter(item, 1));
            }
        }
    }

    // ⭐ ใช้ potion โดยตรง (เรียกจาก controller ได้เลย)
    public boolean usePotion(Class<? extends BasePotion> type) {

        Iterator<ItemCounter> iterator = inventory.iterator();

        while (iterator.hasNext()) {

            ItemCounter ic = iterator.next();

            if (type.isInstance(ic.getItem()) && ic.getCount() > 0) {

                BasePotion potion = (BasePotion) ic.getItem();
                potion.consume(this);

                ic.setCount(ic.getCount() - 1);

                if (ic.getCount() <= 0) {
                    iterator.remove();  // safe remove
                }

                return true;
            }
        }

        return false;
    }

    public int countItem(Class<?> type) {

        for (ItemCounter ic : inventory) {
            if (type.isInstance(ic.getItem())) {
                return ic.getCount();
            }
        }

        return 0;
    }

    // ───────────────── EQUIPMENT ─────────────────
    public BaseWeapon getEquippedWeapon() { return equippedWeapon; }

    public void equipWeapon(BaseWeapon weapon) {

        if (equippedWeapon != null) {
            equippedWeapon.unequip(this);
        }

        equippedWeapon = weapon;

        if (weapon != null) {
            weapon.equip(this);
        }
    }

    public void unequipWeapon() {

        if (equippedWeapon != null) {
            equippedWeapon.unequip(this);
            equippedWeapon = null;
        }
    }

    public BaseArmor getEquippedArmor() { return equippedArmor; }

    public void equipArmor(BaseArmor armor) {

        if (equippedArmor != null) {
            equippedArmor.unequip(this);
        }

        equippedArmor = armor;

        if (armor != null) {
            armor.equip(this);
        }
    }

    public void unequipArmor() {

        if (equippedArmor != null) {
            equippedArmor.unequip(this);
            equippedArmor = null;
        }
    }

    // ───────────────── STAT BONUS ─────────────────
    public void addBonus(int atk, int def, int hp, int spd) {
        attack += atk;
        defense += def;
        maxHealthPoint += hp;
        healthPoint += hp;
        speed += spd;
    }

    public void removeBonus(int atk, int def, int hp, int spd) {
        attack = Math.max(0, attack - atk);
        defense = Math.max(0, defense - def);
        maxHealthPoint = Math.max(1, maxHealthPoint - hp);
        if (healthPoint > maxHealthPoint)
            healthPoint = maxHealthPoint;
        speed -= spd;
    }

    // ───────────────── HEAL METHOD ─────────────────
    public void heal(int amount) {
        healthPoint = Math.min(maxHealthPoint, healthPoint + amount);
    }

    // ───────────────── ACCESSORS ─────────────────
    public int getHealth() {
        return healthPoint;
    }

    public int getMaxHealth() {
        return maxHealthPoint;
    }

    public void setHealth(int hp) {
        healthPoint = Math.max(0, Math.min(maxHealthPoint, hp));
    }

    public int getStrength() {
        return attack;
    }

    public void setStrength(int v) {
        attack = Math.max(0, v);
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int v) {
        luck = Math.max(0, v);
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    // ───────────────── COMBAT ─────────────────
    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(attack);
    }
}