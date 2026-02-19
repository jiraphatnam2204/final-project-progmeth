package logic.base;

import interfaces.Craftable;
import interfaces.Equipable;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.ArrayList;

public abstract class BaseArmor extends BaseItem implements Equipable,Craftable {
    protected int def = 0;
    protected int atk = 0;
    protected int spd = 0;
    protected int hp = 0;
    protected int craftingPrice = 0;
    public BaseArmor(String name,int atk,int def,int hp,int spd,int craftingPrice){
        super(name);
        setAtk(atk);
        setDef(def);
        setHp(hp);
        setSpd(spd);
        setCraftingPrice(craftingPrice);
    }
    public void setCraftingPrice(int craftingPrice){
        this.craftingPrice = Math.max(0,craftingPrice);
    }
    @Override
    public int getCraftingPrice() {
        return craftingPrice;
    }

    @Override
    public void equip(Player p){
        p.addBonus(atk,def,hp,spd);
    }
    @Override
    public void unequip(Player p){
        p.removeBonus(atk,def,hp,spd);
    }
    @Override
    public boolean canCraft(Player p){
        if(p.getGold() < craftingPrice) return false;
        ArrayList<ItemCounter> recipe = getRecipe();
        int findCount = 0;
        int itemRequire = recipe.size();
        for(ItemCounter it : recipe){
            boolean find = false;
            int cnt = 0;
            for(ItemCounter i : p.getInventory()) {
                if(it.equals(i)) cnt += i.getCount() ;
            }
            if(it.getCount()>cnt) return false;
        }
        return true;
    }
    @Override
    public void craft(Player p){
        if(!canCraft(p)) return;
        ArrayList<ItemCounter> recipe = getRecipe();
        for(ItemCounter it : recipe){
            int remaining = it.getCount();
            for(ItemCounter i : p.getInventory()) {
                int start = i.getCount();
                if(it.equals(i)) i.setCount(i.getCount()-it.getCount());
                remaining -= (start-i.getCount());
                if(i.getCount()==0){
                    p.getInventory().remove(i);
                }
                if(remaining==0) break;
            }
        }
        p.setGold(p.getGold()-getCraftingPrice());
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
