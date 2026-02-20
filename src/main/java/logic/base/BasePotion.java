package logic.base;
import interfaces.Consumable; import interfaces.Stackable; import logic.creatures.Player;
public abstract class BasePotion extends BaseItem implements Consumable, Stackable {
    private int stat;
    public BasePotion(String name, int stat) { super(name); setStat(stat); }
    @Override public void consume(Player p) { p.heal(stat); }
    public int getStat() { return stat; }
    public void setStat(int stat) { this.stat = Math.max(0, stat); }
}
