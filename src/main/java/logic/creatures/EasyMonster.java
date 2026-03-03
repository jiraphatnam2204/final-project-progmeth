package logic.creatures;

/**
 * A weak enemy found on the game world map (Rui tier).
 * Stats: 40 HP, 12 ATK, 1 DEF, drops 20 gold.
 */
public class EasyMonster extends Monster {

    /**
     * Creates a new EasyMonster with preset stats.
     */
    public EasyMonster() {
        super(40, 12, 1, 20);
    }
}
