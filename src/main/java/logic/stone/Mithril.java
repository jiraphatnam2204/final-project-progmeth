package logic.stone;

import logic.base.BaseItem;

public class Mithril extends baseStone {

    public Mithril() {
        super("Mithril", 135, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Mithril");
    }
}