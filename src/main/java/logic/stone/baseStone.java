package logic.stone;

import interfaces.Mineable;
import logic.base.BaseItem;

import java.util.ArrayList;
import java.util.List;

public abstract class baseStone extends BaseItem implements Mineable {

    protected int durability;
    protected final int maxDurability;
    protected final int dropAmount;

    protected baseStone(String name, int maxDurability, int dropAmount) {
        super(name);
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.dropAmount = dropAmount;
    }

    @Override
    public List<BaseItem> mine(int minePower) {
        if (isBroken()) return List.of();

        durability -= Math.max(1, minePower);

        if (durability <= 0) {
            return dropItems();
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

    // Assuming Mineable interface requires these getters
    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }
}