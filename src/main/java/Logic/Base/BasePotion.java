package Logic.Base;

import Interfaces.Consumable;

public abstract class BasePotion extends Item implements Consumable {
    private int stat;
    public BasePotion(int stat){
        super("Potion");
        setStat(stat);
    }
    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = Math.max(0,stat);
    }
}
