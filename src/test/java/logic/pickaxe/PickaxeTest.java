package logic.pickaxe;

import logic.base.BaseItem;
import logic.creatures.Player;
import logic.stone.NormalStone;
import logic.stone.HardStone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PickaxeTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(200, 20, 10);
    }

    // ── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructor_setsNameAndPower() {
        Pickaxe pickaxe = new Pickaxe("Test Pick", 10);
        assertEquals("Test Pick", pickaxe.getName());
        assertEquals(10, pickaxe.getPower());
    }

    @Test
    void constructor_powerBelowOne_clampsToOne() {
        Pickaxe pickaxe = new Pickaxe("Weak Pick", 0);
        assertEquals(1, pickaxe.getPower());
    }

    @Test
    void constructor_negativePower_clampsToOne() {
        Pickaxe pickaxe = new Pickaxe("Negative Pick", -5);
        assertEquals(1, pickaxe.getPower());
    }

    // ── Factory Methods ──────────────────────────────────────────────────────

    @Test
    void createWoodenPickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createWoodenPickaxe();
        assertEquals("Wooden Pickaxe", p.getName());
        assertEquals(2, p.getPower());
    }

    @Test
    void createNormalStonePickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createNormalStonePickaxe();
        assertEquals("Normal Stone Pickaxe", p.getName());
        assertEquals(3, p.getPower());
    }

    @Test
    void createHardStonePickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createHardStonePickaxe();
        assertEquals("Hard Stone Pickaxe", p.getName());
        assertEquals(5, p.getPower());
    }

    @Test
    void createIronPickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createIronPickaxe();
        assertEquals("Iron Pickaxe", p.getName());
        assertEquals(12, p.getPower());
    }

    @Test
    void createPlatinumPickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createPlatinumPickaxe();
        assertEquals("Platinum Pickaxe", p.getName());
        assertEquals(27, p.getPower());
    }

    @Test
    void createMithrilPickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createMithrilPickaxe();
        assertEquals("Mithril Pickaxe", p.getName());
        assertEquals(45, p.getPower());
    }

    @Test
    void createVibraniumPickaxe_hasCorrectNameAndPower() {
        Pickaxe p = Pickaxe.createVibraniumPickaxe();
        assertEquals("Vibranium Pickaxe", p.getName());
        assertEquals(100, p.getPower());
    }

    // ── setPower ─────────────────────────────────────────────────────────────

    @Test
    void setPower_updatesPickaxePower() {
        Pickaxe p = Pickaxe.createWoodenPickaxe();
        p.setPower(50);
        assertEquals(50, p.getPower());
    }

    // ── use() ────────────────────────────────────────────────────────────────

    @Test
    void use_onUnminedStone_returnsEmptyList() {
        Pickaxe p = Pickaxe.createWoodenPickaxe(); // power 2
        NormalStone stone = new NormalStone();      // durability 5
        List<BaseItem> drops = p.use(stone, player);
        assertTrue(drops.isEmpty(), "No drops expected before stone breaks");
    }

    @Test
    void use_repeatedUntilBroken_returnsDrops() {
        Pickaxe p = Pickaxe.createHardStonePickaxe(); // power 5
        NormalStone stone = new NormalStone();          // durability 5 → breaks in 1 hit
        List<BaseItem> drops = p.use(stone, player);
        assertFalse(drops.isEmpty(), "Should return drops when stone breaks");
        assertTrue(stone.isBroken());
    }

    @Test
    void use_onBrokenStone_returnsEmptyList() {
        Pickaxe p = Pickaxe.createVibraniumPickaxe(); // power 100
        NormalStone stone = new NormalStone();          // durability 5
        p.use(stone, player);                           // breaks it
        List<BaseItem> drops = p.use(stone, player);    // second hit on broken stone
        assertTrue(drops.isEmpty(), "Should return nothing for already broken stone");
    }

    @Test
    void use_weakPickaxeRequiresMultipleHitsToBreakHardStone() {
        Pickaxe p = Pickaxe.createWoodenPickaxe(); // power 2
        HardStone stone = new HardStone();          // durability 15
        int hits = 0;
        List<BaseItem> drops = List.of();
        while (!stone.isBroken() && hits < 20) {
            drops = p.use(stone, player);
            hits++;
        }
        assertTrue(stone.isBroken(), "HardStone should eventually break");
        assertFalse(drops.isEmpty(), "Should get drops when stone breaks");
    }
}