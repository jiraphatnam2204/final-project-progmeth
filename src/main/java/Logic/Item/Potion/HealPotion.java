package Logic.Item.Potion;

import Logic.Base.BasePotion;

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
