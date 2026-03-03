package logic.creatures;

import logic.base.BaseCreature;
import interfaces.Lootable;

/**
 * Abstract base class for all monsters in the game.
 * Monsters can attack the player and drop gold when defeated.
 */
public abstract class Monster extends BaseCreature implements Lootable {

    protected int moneyDrop;

    /**
     * Creates a new monster with the given stats and gold drop.
     *
     * @param hp         the maximum health points
     * @param attack     the attack power
     * @param defense    the defense value
     * @param moneyDrop  the amount of gold dropped when defeated
     */
    public Monster(int hp, int attack, int defense, int moneyDrop) {
        super(hp, attack, defense); this.moneyDrop = moneyDrop;
    }

    /**
     * Attacks the target creature by dealing damage equal to this monster's attack stat.
     *
     * @param target the creature to attack
     */
    @Override
    public void attack(BaseCreature target) { target.takeDamage(attack); }

    /**
     * Returns the amount of gold this monster drops upon defeat.
     *
     * @return gold dropped
     */
    @Override
    public int dropMoney() { return moneyDrop; }
}
