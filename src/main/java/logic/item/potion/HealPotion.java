package logic.item.potion;

import logic.base.BasePotion;

public class HealPotion extends BasePotion {
    public HealPotion(){
        super("Heal Potion",0.2);
    }
    @Override
    public void consume(Player p){
        double increaseHealth = p.getMaxHealth()*getStat();
        p.setHealth(p.getHealth+increaseHealth);
    }
}
