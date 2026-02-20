package logic.item.potion;

import logic.base.BasePotion;
import logic.creatures.Player;

/**
 * HealPotion: Restores 20% of the player's maximum HP.
 * Extends BasePotion which already handles the consume() logic.
 */
public class HealPotion extends BasePotion {
    public HealPotion() {
        super("Heal Potion", 0); // stat=0 because we override consume()
    }

    @Override
    public void consume(Player p) {
        int healAmount = (int)(p.getMaxHealth() * 0.20); // 20% of max HP
        p.heal(healAmount);
    }
}
