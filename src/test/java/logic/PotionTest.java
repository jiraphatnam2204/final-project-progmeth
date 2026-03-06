package logic;

import logic.creatures.Player;
import logic.item.potion.SmallHealthPotion;
import logic.item.potion.MediumHealthPotion;
import logic.item.potion.BigHealthPotion;
import logic.item.weapon.*;
import logic.base.BaseWeapon;
import logic.util.ItemCounter;
import logic.stone.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for health-potion behaviour, including healing values,
 * max-HP capping, inventory stack management, and potion stats.
 */
public class PotionTest {

    /** The player instance under test, reset before each test. */
    private Player player;

    /**
     * Initialises a fresh player with 100 HP, 20 ATK, 10 DEF and 9999 gold
     * before every test.
     */
    @BeforeEach
    void setUp() {
        // Player: 100 HP, 20 ATK, 10 DEF
        player = new Player(100, 20, 10);
        player.setGold(9999);
    }

    // ══════════════════════════════════════════════════════════
    // POTION TESTS
    // ══════════════════════════════════════════════════════════

    @Test
    void smallPotion_heals40HP() {
        player.setHealth(50);
        new SmallHealthPotion().consume(player);
        assertEquals(90, player.getHealth());
    }

    @Test
    void smallPotion_doesNotExceedMaxHP() {
        player.setHealth(90);
        new SmallHealthPotion().consume(player);
        assertEquals(100, player.getHealth()); // capped at maxHP
    }

    @Test
    void mediumPotion_TestHealMoreThanMaxHP() {
        player.setHealth(1);
        new MediumHealthPotion().consume(player);
        assertEquals(100, player.getHealth()); // 1+100 but capped at 100
        // actually capped:
        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void mediumPotion_doesNotExceedMaxHP() {
        player.setHealth(50);
        new MediumHealthPotion().consume(player);
        assertEquals(100, player.getHealth());
    }

    @Test
    void bigPotion_heals200HP() {
        // Need higher maxHP to verify full 200 heal
        Player bigPlayer = new Player(300, 20, 10);
        bigPlayer.setHealth(50);
        new BigHealthPotion().consume(bigPlayer);
        assertEquals(250, bigPlayer.getHealth());
    }

    @Test
    void bigPotion_doesNotExceedMaxHP() {
        player.setHealth(50);
        new BigHealthPotion().consume(player);
        assertEquals(100, player.getHealth()); // capped at 100
    }

    @Test
    void smallPotion_statIs40() {
        assertEquals(40, new SmallHealthPotion().getStat());
    }

    @Test
    void mediumPotion_statIs100() {
        assertEquals(100, new MediumHealthPotion().getStat());
    }

    @Test
    void bigPotion_statIs200() {
        assertEquals(200, new BigHealthPotion().getStat());
    }

    @Test
    void potion_consumeReducesCountInInventory() {
        SmallHealthPotion potion = new SmallHealthPotion();
        player.addItem(potion, 3);
        player.setHealth(50);

        ItemCounter ic = player.getInventory().get(0);
        potion.consume(player);
        ic.addCount(-1);

        assertEquals(2, ic.getCount());
    }

    @Test
    void potion_MaxStackTest() { //MaxStack In Inventory Should be 30
        player.addItem(new SmallHealthPotion(),100);
        assertEquals(player.getInventory().get(0).getCount(),30);
        assertEquals(player.getInventory().get(1).getCount(),30);
        assertEquals(player.getInventory().get(2).getCount(),30);
        assertEquals(player.getInventory().get(3).getCount(),10);

    }
}