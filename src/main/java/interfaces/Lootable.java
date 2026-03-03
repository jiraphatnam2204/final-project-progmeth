package interfaces;

/**
 * Represents an entity that drops money when defeated.
 */
public interface Lootable {

    /**
     * Returns the amount of gold dropped by this entity upon defeat.
     *
     * @return gold dropped
     */
    int dropMoney();
}
