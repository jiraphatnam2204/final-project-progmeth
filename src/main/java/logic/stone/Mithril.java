package logic.stone;

import logic.base.BaseItem;

public class Mithril extends baseStone {
    public Mithril() {
        super("Mithril", 120, 3);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Mithril");
    }
}
