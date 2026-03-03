package logic.item.potion;

import logic.base.BasePotion;
import logic.creatures.Player;

/**
 * A percentage-based healing potion that restores 20% of the player's maximum HP.
 * Overrides {@link logic.base.BasePotion#consume(Player)} to calculate a relative heal amount.
 */
public class HealPotion extends BasePotion {

    /**
     * Creates a new HealPotion.
     */
    public HealPotion() {
        super("Heal Potion", 0); // stat=0 because we override consume()
    }

    /**
     * Consumes this potion, restoring 20% of the player's maximum health.
     *
     * @param p the player who consumes this potion
     */
    @Override
    public void consume(Player p) {
        int healAmount = (int)(p.getMaxHealth() * 0.20); // 20% of max HP
        p.heal(healAmount);
    }
}
