package interfaces;

/**
 * Represents an item that can be purchased from a shop.
 */
public interface Buyable {

    /**
     * Returns the purchase price of this item.
     *
     * @return the price in gold
     */
    int getPrice();
}
