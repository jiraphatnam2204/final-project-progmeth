package logic.item.potion;

import logic.base.BasePotion;
import logic.creatures.Player;

public class StrengthPotion extends BasePotion {
    public StrengthPotion() {
        super("Strength Potion", 1.5); // Increases Strength by 50%
    }

    @Override
    public void consume(Player p) {
        p.setStrength((int)(p.getStrength() * getStat()));
    }
}