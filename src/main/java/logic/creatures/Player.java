package logic.creatures;

import logic.base.*;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents the player character.
 * The player can carry an inventory, equip weapons and armor,
 * use potions, mine resources, and perform combat skills.
 */
public class Player extends BaseCreature {

    private int gold;
    private ArrayList<ItemCounter> inventory;
    private int speed, luck;

    private BaseWeapon equippedWeapon = null;
    private BaseArmor equippedArmor = null;

    /**
     * Creates a new player with the given base stats.
     * Gold, speed, and luck are initialized to zero.
     *
     * @param hp      the starting and maximum health points
     * @param attack  the base attack power
     * @param defense the base defense value
     */
    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense);
        gold = 0;
        inventory = new ArrayList<>();
        speed = 0;
        luck = 0;
    }

    // ───────────────── GOLD ─────────────────

    /**
     * Returns the player's current gold amount.
     *
     * @return gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * Sets the player's gold. Value cannot go below zero.
     *
     * @param gold the new gold amount
     */
    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    // ───────────────── INVENTORY ─────────────────

    /**
     * Returns the player's inventory as a list of {@link ItemCounter} entries.
     *
     * @return inventory list
     */
    public ArrayList<ItemCounter> getInventory() {
        return inventory;
    }

    /**
     * Adds the specified amount of an item to the player's inventory.
     * Stackable items are distributed across existing and new slots.
     * Non-stackable items each occupy their own slot.
     *
     * @param item   the item to add
     * @param amount the quantity to add
     */
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

            while (remaining > 0) {
                int add = Math.min(item.getMaxStack(), remaining);
                inventory.add(new ItemCounter(item, add));
                remaining -= add;
            }
        } else {
            for (int i = 0; i < amount; i++) {
                inventory.add(new ItemCounter(item, 1));
            }
        }
    }

    /**
     * Uses one potion of the specified type from the inventory, applying its effect.
     *
     * @param type the class of the potion to use
     * @return {@code true} if a potion was found and used, {@code false} otherwise
     */
    public boolean usePotion(Class<? extends BasePotion> type) {

        Iterator<ItemCounter> iterator = inventory.iterator();

        while (iterator.hasNext()) {

            ItemCounter ic = iterator.next();

            if (type.isInstance(ic.getItem()) && ic.getCount() > 0) {

                BasePotion potion = (BasePotion) ic.getItem();
                potion.consume(this);

                ic.setCount(ic.getCount() - 1);

                if (ic.getCount() <= 0) {
                    iterator.remove();
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Returns the count of items of the specified type in the inventory.
     *
     * @param type the class of the item to count
     * @return the quantity found, or 0 if none
     */
    public int countItem(Class<?> type) {

        for (ItemCounter ic : inventory) {
            if (type.isInstance(ic.getItem())) {
                return ic.getCount();
            }
        }

        return 0;
    }

    // ───────────────── EQUIPMENT ─────────────────

    /**
     * Returns the currently equipped weapon, or {@code null} if none.
     *
     * @return equipped weapon
     */
    public BaseWeapon getEquippedWeapon() {
        return equippedWeapon;
    }

    /**
     * Equips the given weapon, unequipping any previously equipped weapon first.
     *
     * @param weapon the weapon to equip, or {@code null} to unequip
     */
    public void equipWeapon(BaseWeapon weapon) {

        if (equippedWeapon != null) {
            equippedWeapon.unequip(this);
        }

        equippedWeapon = weapon;

        if (weapon != null) {
            weapon.equip(this);
        }
    }

    /**
     * Unequips the currently equipped weapon, removing its stat bonuses.
     * Does nothing if no weapon is equipped.
     */
    public void unequipWeapon() {

        if (equippedWeapon != null) {
            equippedWeapon.unequip(this);
            equippedWeapon = null;
        }
    }

    /**
     * Returns the currently equipped armor, or {@code null} if none.
     *
     * @return equipped armor
     */
    public BaseArmor getEquippedArmor() {
        return equippedArmor;
    }

    /**
     * Equips the given armor, unequipping any previously equipped armor first.
     *
     * @param armor the armor to equip, or {@code null} to unequip
     */
    public void equipArmor(BaseArmor armor) {

        if (equippedArmor != null) {
            equippedArmor.unequip(this);
        }

        equippedArmor = armor;

        if (armor != null) {
            armor.equip(this);
        }
    }

    /**
     * Unequips the currently equipped armor, removing its stat bonuses.
     * Does nothing if no armor is equipped.
     */
    public void unequipArmor() {

        if (equippedArmor != null) {
            equippedArmor.unequip(this);
            equippedArmor = null;
        }
    }

    // ───────────────── STAT BONUS ─────────────────

    /**
     * Adds stat bonuses to the player, typically when equipping an item.
     *
     * @param atk the attack bonus to add
     * @param def the defense bonus to add
     * @param hp  the max HP bonus to add (also increases current HP)
     * @param spd the speed bonus to add
     */
    public void addBonus(int atk, int def, int hp, int spd) {
        attack += atk;
        defense += def;
        maxHealthPoint += hp;
        healthPoint += hp;
        speed += spd;
    }

    /**
     * Removes stat bonuses from the player, typically when unequipping an item.
     * Stats are clamped to prevent going below minimum values.
     *
     * @param atk the attack bonus to remove
     * @param def the defense bonus to remove
     * @param hp  the max HP bonus to remove
     * @param spd the speed bonus to remove
     */
    public void removeBonus(int atk, int def, int hp, int spd) {
        attack = Math.max(0, attack - atk);
        defense = Math.max(0, defense - def);
        maxHealthPoint = Math.max(1, maxHealthPoint - hp);
        if (healthPoint > maxHealthPoint)
            healthPoint = maxHealthPoint;
        speed -= spd;
    }

    // ───────────────── HEAL METHOD ─────────────────

    /**
     * Restores the player's health by the given amount, capped at max health.
     *
     * @param amount the amount of health to restore
     */
    public void heal(int amount) {
        healthPoint = Math.min(maxHealthPoint, healthPoint + amount);
    }

    // ─── SKILLS ─────────────────────────────────────────────────────────────────

    /**
     * Skill: Kagura Dance — deals double attack damage to the target.
     *
     * @param target the creature to attack
     * @return the skill result containing damage dealt
     */
    public SkillResult skillKaguraDance(BaseCreature target) {
        int dmg = Math.max(1, attack - target.getDefense()) * 2;
        target.takeDamage(attack * 2);
        return new SkillResult(dmg, 0, false, false);
    }

    /**
     * Skill: Dead Calm — activates a shield wall effect, blocking the next attack.
     *
     * @return the skill result with shield wall flag set to {@code true}
     */
    public SkillResult skillDeadCalm() {
        return new SkillResult(0, 0, true, false);
    }

    /**
     * Skill: Constant Flux — strikes the target 3 times and applies a berserk debuff.
     *
     * @param target the creature to attack
     * @return the skill result containing total damage dealt and berserk debuff flag
     */
    public SkillResult skillConstantFlux(BaseCreature target) {
        int base = Math.max(1, attack - target.getDefense());
        int total = base * 3;
        for (int i = 0; i < 3; i++) target.takeDamage(attack);
        return new SkillResult(total, 0, false, true);
    }

    /**
     * Skill: Water Wheel — deals damage and heals the player for 30% of the net damage dealt.
     *
     * @param target the creature to attack
     * @return the skill result containing damage dealt and heal amount
     */
    public SkillResult skillWaterWheel(BaseCreature target) {
        int base = Math.max(1, attack - target.getDefense());
        target.takeDamage(attack);
        int healAmt = Math.max(1, (int) (base * 0.30));
        heal(healAmt);
        return new SkillResult(base, healAmt, false, false);
    }

    // ───────────────── ACCESSORS ─────────────────

    /**
     * Returns the player's current health points.
     *
     * @return current HP
     */
    public int getHealth() {
        return healthPoint;
    }

    /**
     * Sets the player's current health, clamped between 0 and max health.
     *
     * @param hp the new health value
     */
    public void setHealth(int hp) {
        healthPoint = Math.max(0, Math.min(maxHealthPoint, hp));
    }

    /**
     * Returns the player's maximum health points.
     *
     * @return max HP
     */
    public int getMaxHealth() {
        return maxHealthPoint;
    }

    /**
     * Returns the player's attack (strength) stat.
     *
     * @return attack stat
     */
    public int getStrength() {
        return attack;
    }

    /**
     * Sets the player's attack stat. Minimum value is 0.
     *
     * @param v the new attack value
     */
    public void setStrength(int v) {
        attack = Math.max(0, v);
    }

    /**
     * Returns the player's luck stat.
     *
     * @return luck stat
     */
    public int getLuck() {
        return luck;
    }

    /**
     * Sets the player's luck stat. Minimum value is 0.
     *
     * @param v the new luck value
     */
    public void setLuck(int v) {
        luck = Math.max(0, v);
    }

    /**
     * Returns the player's defense stat.
     *
     * @return defense stat
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Returns the player's speed stat.
     *
     * @return speed stat
     */
    public int getSpeed() {
        return speed;
    }

    // ───────────────── COMBAT ─────────────────

    /**
     * Performs a basic attack on the target, dealing damage equal to the player's attack stat.
     *
     * @param target the creature to attack
     */
    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(attack);
    }

    // ─── SKILL RESULT ───────────────────────────────────────────────────────────

    /**
     * Immutable record representing the outcome of a player skill.
     *
     * @param damage       the total damage dealt to the target
     * @param heal         the amount healed by the player
     * @param shieldWall   whether a shield wall effect was activated
     * @param berserkDebuff whether a berserk debuff was applied
     */
    public record SkillResult(int damage, int heal, boolean shieldWall, boolean berserkDebuff) {
    }
}
