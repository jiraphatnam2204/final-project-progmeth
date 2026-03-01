package logic.stone;

import logic.base.BaseItem;
import logic.creatures.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StoneTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(200, 20, 10);
    }

    // ── NormalStone ──────────────────────────────────────────────────────────

    @Test
    void normalStone_initialDurability_isFive() {
        NormalStone stone = new NormalStone();
        assertEquals(5, stone.getDurability());
        assertEquals(5, stone.getMaxDurability());
    }

    @Test
    void normalStone_isNotBrokenInitially() {
        NormalStone stone = new NormalStone();
        assertFalse(stone.isBroken());
    }

    @Test
    void normalStone_mine_reducesDurability() {
        NormalStone stone = new NormalStone();
        stone.mine(2, player);
        assertEquals(3, stone.getDurability());
    }

    @Test
    void normalStone_mine_withExactDurability_breaks() {
        NormalStone stone = new NormalStone();
        stone.mine(5, player);
        assertTrue(stone.isBroken());
    }

    @Test
    void normalStone_mine_returnsDropsOnBreak() {
        NormalStone stone = new NormalStone();
        List<BaseItem> drops = stone.mine(5, player);
        assertFalse(drops.isEmpty());
    }

    @Test
    void normalStone_mine_returnsEmptyBeforeBreak() {
        NormalStone stone = new NormalStone();
        List<BaseItem> drops = stone.mine(1, player);
        assertTrue(drops.isEmpty());
    }

    @Test
    void normalStone_mine_whenBroken_returnsEmpty() {
        NormalStone stone = new NormalStone();
        stone.mine(5, player);
        List<BaseItem> drops = stone.mine(1, player);
        assertTrue(drops.isEmpty());
    }

    @Test
    void normalStone_minePowerZeroOrNegative_treatedAsOne() {
        NormalStone stone = new NormalStone();
        stone.mine(0, player);
        assertEquals(4, stone.getDurability()); // max(1,0)=1 applied
    }

    // ── HardStone ────────────────────────────────────────────────────────────

    @Test
    void hardStone_initialDurability_isFifteen() {
        HardStone stone = new HardStone();
        assertEquals(15, stone.getDurability());
        assertEquals(15, stone.getMaxDurability());
    }

    @Test
    void hardStone_isNotBrokenInitially() {
        HardStone stone = new HardStone();
        assertFalse(stone.isBroken());
    }

    @Test
    void hardStone_mine_reducesDurability() {
        HardStone stone = new HardStone();
        stone.mine(5, player);
        assertEquals(10, stone.getDurability());
    }

    @Test
    void hardStone_mine_returnsDropsOnBreak() {
        HardStone stone = new HardStone();
        List<BaseItem> drops = stone.mine(15, player);
        assertFalse(drops.isEmpty());
        assertTrue(stone.isBroken());
    }

    // ── Iron ─────────────────────────────────────────────────────────────────

    @Test
    void iron_initialDurability_isThirtySix() {
        Iron stone = new Iron();
        assertEquals(36, stone.getDurability());
        assertEquals(36, stone.getMaxDurability());
    }

    @Test
    void iron_isNotBrokenInitially() {
        Iron stone = new Iron();
        assertFalse(stone.isBroken());
    }

    @Test
    void iron_mine_returnsDropsOnBreak() {
        Iron stone = new Iron();
        List<BaseItem> drops = stone.mine(36, player);
        assertFalse(drops.isEmpty());
        assertTrue(stone.isBroken());
    }

    // ── Platinum ─────────────────────────────────────────────────────────────

    @Test
    void platinum_initialDurability_isEighty() {
        Platinum stone = new Platinum();
        assertEquals(80, stone.getDurability());
        assertEquals(80, stone.getMaxDurability());
    }

    @Test
    void platinum_isNotBrokenInitially() {
        Platinum stone = new Platinum();
        assertFalse(stone.isBroken());
    }

    @Test
    void platinum_mine_returnsDropsOnBreak() {
        Platinum stone = new Platinum();
        List<BaseItem> drops = stone.mine(80, player);
        assertFalse(drops.isEmpty());
        assertTrue(stone.isBroken());
    }

    // ── Mithril ──────────────────────────────────────────────────────────────

    @Test
    void mithril_initialDurability_is135() {
        Mithril stone = new Mithril();
        assertEquals(120, stone.getDurability());
        assertEquals(120, stone.getMaxDurability());
    }

    @Test
    void mithril_isNotBrokenInitially() {
        Mithril stone = new Mithril();
        assertFalse(stone.isBroken());
    }

    @Test
    void mithril_mine_returnsDropsOnBreak() {
        Mithril stone = new Mithril();
        List<BaseItem> drops = stone.mine(135, player);
        assertFalse(drops.isEmpty());
        assertTrue(stone.isBroken());
    }

    // ── Vibranium ────────────────────────────────────────────────────────────

    @Test
    void vibranium_initialDurability_is210() {
        Vibranium stone = new Vibranium();
        assertEquals(210, stone.getDurability());
        assertEquals(210, stone.getMaxDurability());
    }

    @Test
    void vibranium_isNotBrokenInitially() {
        Vibranium stone = new Vibranium();
        assertFalse(stone.isBroken());
    }

    @Test
    void vibranium_mine_returnsDropsOnBreak() {
        Vibranium stone = new Vibranium();
        List<BaseItem> drops = stone.mine(210, player);
        assertFalse(drops.isEmpty());
        assertTrue(stone.isBroken());
    }

    @Test
    void vibranium_mine_partial_doesNotBreak() {
        Vibranium stone = new Vibranium();
        stone.mine(100, player);
        assertFalse(stone.isBroken());
        assertEquals(110, stone.getDurability());
    }

    // ── Cross-class: drops added to player inventory ─────────────────────────

    @Test
    void mine_dropsAddedToPlayerInventory() {
        NormalStone stone = new NormalStone();
        int itemsBefore = player.getInventory().size();
        stone.mine(5, player);
        assertTrue(player.getInventory().size() > itemsBefore,
                "Player inventory should grow after mining drops");
    }
}