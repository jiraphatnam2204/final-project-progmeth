package logic.base;

/**
 * Abstract base class for all creatures in the game (players and monsters).
 * Manages core combat stats: health, attack, and defense.
 */
public abstract class BaseCreature {

    protected int healthPoint, maxHealthPoint, attack, defense;

    /**
     * Creates a new creature with the given stats.
     * Health is initialized to the max value.
     *
     * @param maxHealthPoint the maximum (and starting) health points
     * @param attack         the base attack power
     * @param defense        the base defense value
     */
    public BaseCreature(int maxHealthPoint, int attack, int defense) {
        this.maxHealthPoint = maxHealthPoint;
        this.healthPoint = maxHealthPoint;
        this.attack = attack;
        this.defense = defense;
    }

    /**
     * Returns whether this creature is still alive.
     *
     * @return {@code true} if health points are greater than zero
     */
    public boolean isAlive() {
        return healthPoint > 0;
    }

    /**
     * Reduces this creature's health by the given damage minus its defense.
     * Health will not go below zero.
     *
     * @param damage the incoming damage before defense reduction
     */
    public void takeDamage(int damage) {
        int realDamage = Math.max(0, damage - defense);
        healthPoint -= realDamage;
        if (healthPoint < 0) healthPoint = 0;
    }

    /**
     * Restores health by the given amount, capped at max health.
     *
     * @param amount the amount of health to restore
     */
    public void heal(int amount) {
        healthPoint = Math.min(maxHealthPoint, healthPoint + amount);
    }

    /**
     * Returns the current health points.
     *
     * @return current HP
     */
    public int getHealthPoint() {
        return healthPoint;
    }

    /**
     * Sets the current health points directly.
     *
     * @param v the new health value
     */
    public void setHealthPoint(int v) {
        healthPoint = v;
    }

    /**
     * Returns the maximum health points.
     *
     * @return max HP
     */
    public int getMaxHealthPoint() {
        return maxHealthPoint;
    }

    /**
     * Sets the maximum health points.
     *
     * @param v the new max HP value
     */
    public void setMaxHealthPoint(int v) {
        maxHealthPoint = v;
    }

    /**
     * Returns the base attack power.
     *
     * @return attack stat
     */
    public int getAttack() {
        return attack;
    }

    /**
     * Sets the attack power.
     *
     * @param v the new attack value
     */
    public void setAttack(int v) {
        attack = v;
    }

    /**
     * Returns the defense value used to reduce incoming damage.
     *
     * @return defense stat
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Sets the defense value.
     *
     * @param v the new defense value
     */
    public void setDefense(int v) {
        defense = v;
    }

    /**
     * Performs an attack on the target creature.
     *
     * @param target the creature to attack
     */
    public abstract void attack(BaseCreature target);
}
