package logic.item.potion;

import logic.base.BasePotion;

/**
 * A medium health potion that restores 100 HP when consumed.
 * Can be purchased from the shop.
 */
public class MediumHealthPotion extends BasePotion {

    /**
     * Creates a new MediumHealthPotion.
     */
    public MediumHealthPotion() {
        super("Medium Health Potion", 100);
    }
}
