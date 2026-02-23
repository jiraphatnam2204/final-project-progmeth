package logic.stone;

import logic.base.BaseItem;

public class Vibranium extends baseStone {
    public Vibranium() {
        super("Vibranium", 210, 3);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Vibranium");
    }
}
