package logic.item.potion;

import logic.base.BasePotion;

/**
 * A large health potion that restores 200 HP when consumed.
 * Can be purchased from the shop.
 */
public class BigHealthPotion extends BasePotion {

    /**
     * Creates a new BigHealthPotion.
     */
    public BigHealthPotion() {
        super("Big Health Potion", 200);
    }
}
