package Logic.Base;

import Interfaces.Craftable;
import Interfaces.Equipable;

public abstract class BaseArmor extends Item implements Equipable,Craftable {
    private int def;
    private int atk;
    private int hp;
    private int spd;
    public BaseArmor(String name,int atk,int def,int hp,int spd){
        super(name);
        setAtk(atk);
        setDef(def);
        setHp(hp);
        setSpd(spd);
    }
    @Override
    public void equip(Player p){
        p.addBounus(atk,def,hp,spd);
    }
    @Override
    public void unequip(Player p){
        p.removeBonus(atk,def,hp,spd);
    }
    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getSpd() {
        return spd;
    }

    public void setSpd(int spd) {
        this.spd = spd;
    }
}
