package logic.creatures;

/**
 * Represents Muzan, the hard-tier final boss demon.
 * Stats: 1600 HP, 100 ATK, 38 DEF, drops 1500 gold.
 */
public class HardBoss extends Monster {

    /**
     * Creates a new HardBoss (Muzan) with preset stats.
     */
    public HardBoss() {
        super(1600, 100, 38, 1500);
    }
}
