package logic.creatures;

/**
 * Represents Kokushibo, the medium-tier boss demon.
 * Stats: 900 HP, 80 ATK, 22 DEF, drops 700 gold.
 */
public class MediumBoss extends Monster {

    /**
     * Creates a new MediumBoss (Kokushibo) with preset stats.
     */
    public MediumBoss() {
        super(900, 80, 22, 700);
    }
}
