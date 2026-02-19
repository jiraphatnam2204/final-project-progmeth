package logic.item.potion;

import logic.base.BasePotion;

public class HealPotion extends BasePotion {
    public HealPotion(){
        super("Small Health Potion",20);
    }
    @Override
    public void consume(Player p){
        p.heal(10);
    }
}
