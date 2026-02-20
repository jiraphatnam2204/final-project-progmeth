package logic.base;
import interfaces.Craftable; import interfaces.Equipable;
import logic.creatures.Player; import logic.util.ItemCounter;
import java.util.ArrayList;
public abstract class BaseArmor extends BaseItem implements Equipable, Craftable {
    protected int def=0, atk=0, spd=0, hp=0, craftingPrice=0;
    public BaseArmor(String name, int atk, int def, int hp, int spd, int craftingPrice) {
        super(name, false, 1);
        setAtk(atk); setDef(def); setHp(hp); setSpd(spd); setCraftingPrice(craftingPrice);
    }
    public void setCraftingPrice(int v) { craftingPrice = Math.max(0, v); }
    @Override public int getCraftingPrice() { return craftingPrice; }
    @Override public void equip(Player p) { p.addBonus(atk, def, hp, spd); }
    @Override public void unequip(Player p) { p.removeBonus(atk, def, hp, spd); }
    @Override public boolean canCraft(Player p) {
        if (p.getGold() < craftingPrice) return false;
        for (ItemCounter it : getRecipe()) {
            int cnt = 0;
            for (ItemCounter i : p.getInventory()) if (it.equals(i)) cnt += i.getCount();
            if (it.getCount() > cnt) return false;
        }
        return true;
    }
    @Override public void craft(Player p) {
        if (!canCraft(p)) return;
        for (ItemCounter it : getRecipe()) {
            int remaining = it.getCount();
            for (ItemCounter i : p.getInventory()) {
                int start = i.getCount();
                if (it.equals(i)) i.setCount(i.getCount() - it.getCount());
                remaining -= (start - i.getCount());
                if (i.getCount() == 0) { p.getInventory().remove(i); break; }
                if (remaining == 0) break;
            }
        }
        p.setGold(p.getGold() - getCraftingPrice());
    }
    public int getDef() { return def; } public void setDef(int v) { def = v; }
    public int getAtk() { return atk; } public void setAtk(int v) { atk = v; }
    public int getHp()  { return hp;  } public void setHp(int v)  { hp = v; }
    public int getSpd() { return spd; } public void setSpd(int v) { spd = v; }
}
