package Logic.Base;

import Interfaces.Craftable;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public abstract class BaseWeapon extends Item implements Craftable {
    private int dmg;
    private double cd;
    private int craftingPrice;
    public BaseWeapon(String name,int dmg,double cd,int craftingPrice){
        super(name);
        setCd(cd);
        setDmg(dmg);
        setCraftingPrice(craftingPrice);
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
    @Override
    public int getCraftingPrice() {
        return craftingPrice;
    }

    public void setCraftingPrice(int craftingPrice) {
        this.craftingPrice = craftingPrice;
    }

    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = Math.max(1,dmg);
    }

    public double getCd() {
        return cd;
    }

    public void setCd(double cd) {
        this.cd = Math.max(0,cd);
    }
}
