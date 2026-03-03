package logic.pickaxe;

import interfaces.Mineable;
import logic.base.BaseItem;
import logic.creatures.Player;

import java.util.List;

/**
 * Represents a pickaxe used to mine ore nodes on the game world map.
 * Higher mining power breaks nodes faster and allows harder ores to be mined.
 */
public class Pickaxe {
    private String name;
    private int power;

    /**
     * Creates a new pickaxe with the given name and mining power.
     *
     * @param name  the display name of the pickaxe
     * @param power the mining power; clamped to a minimum of 1
     */
    public Pickaxe(String name, int power) {
        this.name = name;
        this.power = Math.max(1, power);
    }

    /**
     * Creates a Wooden Pickaxe with mining power 2.
     *
     * @return a new wooden pickaxe
     */
    public static Pickaxe createWoodenPickaxe() {
        return new Pickaxe("Wooden Pickaxe", 2);
    }

    /**
     * Creates a Normal Stone Pickaxe with mining power 3.
     *
     * @return a new normal stone pickaxe
     */
    public static Pickaxe createNormalStonePickaxe() {
        return new Pickaxe("Normal Stone Pickaxe", 3);
    }

    /**
     * Creates a Hard Stone Pickaxe with mining power 5.
     *
     * @return a new hard stone pickaxe
     */
    public static Pickaxe createHardStonePickaxe() {
        return new Pickaxe("Hard Stone Pickaxe", 5);
    }

    /**
     * Creates an Iron Pickaxe with mining power 12.
     *
     * @return a new iron pickaxe
     */
    public static Pickaxe createIronPickaxe() {
        return new Pickaxe("Iron Pickaxe", 12);
    }

    /**
     * Creates a Platinum Pickaxe with mining power 27.
     *
     * @return a new platinum pickaxe
     */
    public static Pickaxe createPlatinumPickaxe() {
        return new Pickaxe("Platinum Pickaxe", 27);
    }

    /**
     * Creates a Mithril Pickaxe with mining power 45.
     *
     * @return a new mithril pickaxe
     */
    public static Pickaxe createMithrilPickaxe() {
        return new Pickaxe("Mithril Pickaxe", 45);
    }

    /**
     * Creates a Vibranium Pickaxe with mining power 100.
     *
     * @return a new vibranium pickaxe
     */
    public static Pickaxe createVibraniumPickaxe() {
        return new Pickaxe("Vibranium Pickaxe", 100);
    }

    /**
     * Uses this pickaxe on the given mineable target, applying the mining power.
     *
     * @param target the ore node to mine
     * @param player the player performing the mining action
     * @return the list of items dropped if the node broke, or an empty list otherwise
     */
    public List<BaseItem> use(Mineable target, Player player) {
        return target.mine(power, player);
    }

    /**
     * Returns the display name of this pickaxe.
     *
     * @return pickaxe name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the mining power of this pickaxe.
     *
     * @return mining power
     */
    public int getPower() {
        return power;
    }

    /**
     * Sets the mining power of this pickaxe.
     *
     * @param power the new mining power
     */
    public void setPower(int power) {
        this.power = power;
    }
}
