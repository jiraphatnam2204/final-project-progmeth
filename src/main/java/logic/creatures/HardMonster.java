package logic.creatures;

/**
 * A tough enemy found on the game world map (Daki tier).
 * Stats: 160 HP, 32 ATK, 8 DEF, drops 80 gold.
 */
public class HardMonster extends Monster {

    /**
     * Creates a new HardMonster with preset stats.
     */
    public HardMonster() {
        super(160, 32, 8, 80);
    }
}
