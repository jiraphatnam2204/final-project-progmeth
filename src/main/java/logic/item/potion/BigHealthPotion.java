package logic.item.potion;
import logic.base.BasePotion; import logic.creatures.Player;
public class BigHealthPotion extends BasePotion {
    public BigHealthPotion() { super("Big Health Potion", 100); }
    @Override public void consume(Player p) { p.heal(this.getStat()); }
}
