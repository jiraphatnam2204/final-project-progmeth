package Logic.Item.Potion;

import Logic.Base.BasePotion;

public class StrengthPotion extends BasePotion {
    public StrengthPotion(){
        super("Strength Potion",1.5);
    }
    @Override
    public void consume(Player p){
        p.setStrength(p.getStrength()*getStat());
    }
}
