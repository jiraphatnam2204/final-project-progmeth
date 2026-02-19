package logic.item.potion;

import logic.base.BasePotion;
import logic.creatures.Player;

public class HealPotion extends BasePotion {
    public HealPotion() {
        super("Heal Potion", 0.2); // Heal 20% of max health
    }

    @Override
    public void consume(Player p) {
        int healAmount = (int)(p.getMaxHealth() * getStat());
        int newHealth = p.getHealth() + healAmount;

        if (newHealth > p.getMaxHealth()) {
            newHealth = p.getMaxHealth();
        }
        p.setHealth(newHealth);
    }
}