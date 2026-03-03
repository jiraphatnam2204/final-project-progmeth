package logic.creatures;

/**
 * A mid-tier enemy found on the game world map (Enmu tier).
 * Stats: 90 HP, 22 ATK, 4 DEF, drops 40 gold.
 */
public class MediumMonster extends Monster {

    /**
     * Creates a new MediumMonster with preset stats.
     */
    public MediumMonster() {
        super(90, 22, 4, 40);
    }
}
