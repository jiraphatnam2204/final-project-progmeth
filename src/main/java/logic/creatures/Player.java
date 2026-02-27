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

        for (ItemCounter i : inventory) {

            if (i.getItem().getName().equals(item.getName())) {

                if (i.getItem().isStackable()) {
                    i.addCount(amount);
                    return;
                }
            }
        }

        inventory.add(new ItemCounter(item, amount));
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

    // เพิ่มที่ท้าย Player.java ก่อน closing brace

    // ─── SKILL RESULT ───────────────────────────────────────────────────────────
    public record SkillResult(int damage, int heal, boolean shieldWall, boolean berserkDebuff) {}

// ─── SKILLS ─────────────────────────────────────────────────────────────────


    public SkillResult skillKaguraDance(BaseCreature target) {
        int dmg = Math.max(1, attack - target.getDefense()) * 2;
        target.takeDamage(attack * 2);
        return new SkillResult(dmg, 0, false, false);
    }


    public SkillResult skillDeadCalm() {
        return new SkillResult(0, 0, true, false);
    }


    public SkillResult skillConstantFlux(BaseCreature target) {
        int base = Math.max(1, attack - target.getDefense());
        int total = base * 3;
        for (int i = 0; i < 3; i++) target.takeDamage(attack);
        return new SkillResult(total, 0, false, true);
    }


    public SkillResult skillWaterWheel(BaseCreature target) {
        int base = Math.max(1, attack - target.getDefense());
        target.takeDamage(attack);
        int healAmt = Math.max(1, (int) (base * 0.30));
        heal(healAmt);
        return new SkillResult(base, healAmt, false, false);
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