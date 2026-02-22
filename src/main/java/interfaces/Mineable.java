package interfaces;
import logic.base.BaseItem;
import logic.creatures.Player;
import java.util.List;
public interface Mineable {
    List<BaseItem> mine(int minePower, Player player);
    boolean isBroken();
    int getDurability();
    int getMaxDurability();
}
