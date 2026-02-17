package Logic.Base;

import Interfaces.Consumable;

public abstract class BasePotion extends Item implements Consumable {
    private double stat;
    public BasePotion(String name,double stat){
        super(name);
        setStat(stat);
    }
    public double getStat() {
        return stat;
    }

    public void setStat(double stat) {
        this.stat = Math.max(0,stat);
    }
}
