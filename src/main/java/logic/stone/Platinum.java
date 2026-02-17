package logic.stone;

import logic.item.BaseItem;

public class Platinum extends Stone {

    public Platinum() {
        super(80, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Platinum");
    }
}