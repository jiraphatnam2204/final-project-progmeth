package logic.stone;

import interfaces.Mineable;
import logic.item.BaseItem;

import java.util.ArrayList;
import java.util.List;

public abstract class baseStone implements Mineable {

    protected int durability;
    protected final int maxDurability;
    protected final int dropAmount;

    protected baseStone(int maxDurability, int dropAmount) {
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.dropAmount = dropAmount;
    }

    @Override
    public List<BaseItem> mine(int minePower) {
        if (isBroken()) return List.of();

        durability -= Math.max(1, minePower);

        if (durability <= 0) {
            return dropItems(); // แตกแล้วค่อยดรอป
        }
        return List.of();
    }

    protected List<BaseItem> dropItems() {
        List<BaseItem> drops = new ArrayList<>();
        for (int i = 0; i < dropAmount; i++) {
            drops.add(createItem());
        }
        return drops;
    }

    protected abstract BaseItem createItem();

    @Override
    public boolean isBroken() {
        return durability <= 0;
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public int getMaxDurability() {
        return maxDurability;
    }

}
