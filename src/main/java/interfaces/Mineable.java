package interfaces;

import logic.base.BaseItem;

import java.util.List;

public interface Mineable {
    List<BaseItem> mine(int minePower);   // ขุด 1 ครั้ง
    boolean isBroken();

    int getDurability();
    int getMaxDurability();
}