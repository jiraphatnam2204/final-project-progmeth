package Logic.Base;

import Interfaces.Craftable;

public abstract class BaseWeapon extends Item implements Craftable {
    private int dmg;
    private float cd;
    public BaseWeapon(int dmg,float cd){
        super("Weapon");
        setCd(cd);
        setDmg(dmg);
    }

    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = Math.max(1,dmg);
    }

    public float getCd() {
        return cd;
    }

    public void setCd(float cd) {
        this.cd = Math.max(0,cd);
    }
}
