package logic.item.potion;

import logic.base.BasePotion;

/**
 * A small health potion that restores 40 HP when consumed.
 * Can be purchased from the shop.
 */
public class SmallHealthPotion extends BasePotion {

    /**
     * Creates a new SmallHealthPotion.
     */
    public SmallHealthPotion() {
        super("Small Health Potion", 40);
    }
}
