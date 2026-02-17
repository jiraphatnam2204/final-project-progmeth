package logic.stone;

import logic.item.BaseItem;

public class Mithril extends Stone {

    public Mithril() {
        super(135, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Mithril");
    }
}