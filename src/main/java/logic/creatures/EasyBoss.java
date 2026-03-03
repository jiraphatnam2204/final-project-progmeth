package logic.creatures;

/**
 * Represents Akaza, the easy-tier boss demon.
 * Stats: 500 HP, 60 ATK, 15 DEF, drops 300 gold.
 */
public class EasyBoss extends Monster {

    /**
     * Creates a new EasyBoss (Akaza) with preset stats.
     */
    public EasyBoss() {
        super(500, 60, 15, 300);
    }
}
