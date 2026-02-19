package logic.stone;

import logic.item.BaseItem;

public class Vibranium extends baseStone {

    public Vibranium() {
        super(200, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Vibranium");
    }
}