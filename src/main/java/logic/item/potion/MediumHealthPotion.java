package logic.item.potion;

import logic.base.BasePotion;
import logic.creatures.Player;

public class LuckPotion extends BasePotion {
    public LuckPotion() {
        super("Luck Potion", 1.5); // Increases Luck by 50%
    }

    @Override
    public void consume(Player p) {
        p.setLuck((int)(p.getLuck() * getStat()));
    }
}