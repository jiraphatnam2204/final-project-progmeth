package logic.item.potion;

import logic.base.BasePotion;

public class LuckPotion extends BasePotion {
    public LuckPotion(){
        super("Luck Potion",1.5);
    }
    @Override
    public void consume(Player p){
        p.setLuck(p.getLuck()*getStat());
    }
}
