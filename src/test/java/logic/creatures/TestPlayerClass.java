package logic.creatures;

import logic.item.potion.HealPotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestPlayerClass {

    private Player player;
    private EasyBoss boss;

    @BeforeEach
    void setUp() {
        player = new Player(100, 20, 10);
        boss = new EasyBoss();
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Test
    void initialStats() {
        assertEquals(100, player.getHealth());
        assertEquals(100, player.getMaxHealth());
        assertEquals(20, player.getAttack());
        assertEquals(10, player.getDefense());
        assertEquals(0, player.getGold());
    }

    @Test
    void addAndRemoveBonus() {
        player.addBonus(10, 5, 0, 0);
        assertEquals(30, player.getAttack());
        assertEquals(15, player.getDefense());

        player.removeBonus(10, 5, 0, 0);
        assertEquals(20, player.getAttack());
        assertEquals(10, player.getDefense());
    }

    @Test
    void bonusNeverGoesNegative() {
        player.removeBonus(999, 999, 999, 0);
        assertEquals(0, player.getAttack());
        assertEquals(1, player.getMaxHealth()); // clamp ที่ 1
    }

    @Test
    void goldNotNegative() {
        player.setGold(-100);
        assertEquals(0, player.getGold());
    }

    // ── Heal ──────────────────────────────────────────────────────────────────

    @Test
    void healNormal() {
        player.setHealth(50);
        player.heal(20);
        assertEquals(70, player.getHealth());
    }

    @Test
    void healCappedAtMaxHp() {
        player.setHealth(90);
        player.heal(999);
        assertEquals(100, player.getHealth());
    }

    @Test
    void setHealthClamped() {
        player.setHealth(-1);
        assertEquals(0, player.getHealth());

        player.setHealth(9999);
        assertEquals(100, player.getHealth());
    }

    // ── Inventory & Potion ────────────────────────────────────────────────────

    @Test
    void addItemStacksCorrectly() {
        player.addItem(new HealPotion(), 2);
        player.addItem(new HealPotion(), 3);
        assertEquals(1, player.getInventory().size()); // 1 entry
        assertEquals(5, player.countItem(HealPotion.class));
    }

    @Test
    void usePotionHealsAndDecrements() {
        player.setHealth(50);
        player.addItem(new HealPotion(), 2);
        assertTrue(player.usePotion(HealPotion.class));
        assertEquals(70, player.getHealth()); // +20% of 100
        assertEquals(1, player.countItem(HealPotion.class));
    }

    @Test
    void usePotionRemovesEntryWhenEmpty() {
        player.addItem(new HealPotion(), 1);
        player.usePotion(HealPotion.class);
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void usePotionReturnsFalseWhenNoItem() {
        assertFalse(player.usePotion(HealPotion.class));
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    @Test
    void kaguraDance_doubleDamage() {
        int bossHpBefore = boss.getHealthPoint();
        Player.SkillResult r = player.skillKaguraDance(boss);

        int expectedDmg = Math.max(1, player.getAttack() - boss.getDefense()) * 2;
        assertEquals(expectedDmg, r.damage());
        assertEquals(0, r.heal());
        assertFalse(r.shieldWall());
        assertFalse(r.berserkDebuff());
        assertTrue(boss.getHealthPoint() < bossHpBefore);
    }

    @Test
    void deadCalm_setsShieldWallFlag() {
        Player.SkillResult r = player.skillDeadCalm();

        assertTrue(r.shieldWall());
        assertEquals(0, r.damage());
        assertEquals(0, r.heal());
        assertFalse(r.berserkDebuff());
        assertEquals(500, boss.getHealthPoint()); // boss ไม่โดนตี
    }

    @Test
    void constantFlux_threeHitsAndBerserkDebuff() {
        int bossHpBefore = boss.getHealthPoint();
        Player.SkillResult r = player.skillConstantFlux(boss);

        int perHit = Math.max(0, player.getAttack() - boss.getDefense());
        assertEquals(bossHpBefore - perHit * 3, boss.getHealthPoint());
        assertEquals(perHit * 3, r.damage());
        assertTrue(r.berserkDebuff());
        assertFalse(r.shieldWall());
    }

    @Test
    void waterWheel_damageAndHeal() {
        player.setHealth(50);
        Player.SkillResult r = player.skillWaterWheel(boss);

        int base = Math.max(1, player.getAttack() - boss.getDefense());
        int expectedHeal = Math.max(1, (int) (base * 0.30));
        assertEquals(base, r.damage());
        assertEquals(expectedHeal, r.heal());
        assertEquals(50 + expectedHeal, player.getHealth()); // HP เพิ่มจริง
        assertFalse(r.shieldWall());
        assertFalse(r.berserkDebuff());
    }

    @Test
    void skills_minDamageAlwaysAtLeastOne() {
        EasyBoss tank = new EasyBoss();
        assertTrue(player.skillKaguraDance(tank).damage() >= 1);
        assertTrue(player.skillConstantFlux(tank).damage() >= 3);
        assertTrue(player.skillWaterWheel(tank).heal() >= 1);
    }
}