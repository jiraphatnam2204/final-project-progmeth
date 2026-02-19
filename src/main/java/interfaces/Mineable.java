package interfaces;

import logic.base.BaseItem;
import logic.creatures.Player;

import java.util.List;

public interface Mineable {
    List<BaseItem> mine(int minePower, Player player);   // ขุด 1 ครั้ง
    boolean isBroken();

    int getDurability();
    int getMaxDurability();
}