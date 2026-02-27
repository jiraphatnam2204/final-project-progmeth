package grader;

import logic.base.BaseCreature;
import logic.creatures.Player;
import logic.item.potion.HealPotion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Player class
 * ครอบคลุม: stats, heal, addBonus/removeBonus, gold, inventory/potion,
 *           attack(), และ skill methods ทั้ง 4 ตัว (Kagura Dance, Dead Calm,
 *           Constant Flux, Water Wheel)
 */
class TestPlayerClass {

    // ── Dummy target สำหรับทดสอบ skill ──────────────────────────────────────
    // ใช้ EasyBoss แบบ inline เพื่อไม่ต้อง import JavaFX
    static class DummyMonster extends BaseCreature {
        public DummyMonster(int hp, int attack, int defense) {
            super(hp, attack, defense);
        }
        @Override
        public void attack(BaseCreature target) { target.takeDamage(attack); }
    }

    // Player มาตรฐานที่ใช้ในทุก test: HP=100, ATK=30, DEF=10
    private Player player;
    private DummyMonster boss;

    @BeforeEach
    void setUp() {
        player = new Player(100, 30, 10);
        boss   = new DummyMonster(500, 60, 5);  // boss.def=5 → net dmg = player.atk - 5
    }

    // ════════════════════════════════════════════════════════════════════════
    // 1. Constructor / Initial State
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. Constructor / Initial State")
    class ConstructorTests {

        @Test
        @DisplayName("HP เริ่มต้นเท่ากับ maxHP")
        void initialHpEqualsMaxHp() {
            assertEquals(100, player.getHealth());
            assertEquals(100, player.getMaxHealth());
        }

        @Test
        @DisplayName("ATK และ DEF เริ่มต้นถูกต้อง")
        void initialAttackAndDefense() {
            assertEquals(30, player.getAttack());
            assertEquals(10, player.getDefense());
        }

        @Test
        @DisplayName("Gold เริ่มต้นเป็น 0")
        void initialGoldIsZero() {
            assertEquals(0, player.getGold());
        }

        @Test
        @DisplayName("Inventory เริ่มต้นว่าง")
        void initialInventoryEmpty() {
            assertTrue(player.getInventory().isEmpty());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2. Heal
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. heal()")
    class HealTests {

        @Test
        @DisplayName("ฮีลปกติเพิ่ม HP")
        void healIncreasesHp() {
            player.setHealth(50);
            player.heal(20);
            assertEquals(70, player.getHealth());
        }

        @Test
        @DisplayName("ฮีลเกิน maxHP ถูก cap ที่ maxHP")
        void healCappedAtMaxHp() {
            player.setHealth(90);
            player.heal(50);
            assertEquals(100, player.getHealth());
        }

        @Test
        @DisplayName("ฮีลตอน HP เต็มไม่เปลี่ยน")
        void healWhenFullHpNoChange() {
            player.heal(999);
            assertEquals(100, player.getHealth());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. addBonus / removeBonus
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. addBonus() / removeBonus()")
    class BonusTests {

        @Test
        @DisplayName("addBonus เพิ่ม ATK, DEF, MaxHP ถูกต้อง")
        void addBonusIncreasesStats() {
            player.addBonus(10, 5, 20, 0);
            assertEquals(40,  player.getAttack());
            assertEquals(15,  player.getDefense());
            assertEquals(120, player.getMaxHealth());
            assertEquals(120, player.getHealth()); // HP ควรเพิ่มด้วย
        }

        @Test
        @DisplayName("removeBonus ลด ATK, DEF, MaxHP ถูกต้อง")
        void removeBonusDecreasesStats() {
            player.addBonus(10, 5, 0, 0);
            player.removeBonus(10, 5, 0, 0);
            assertEquals(30, player.getAttack());
            assertEquals(10, player.getDefense());
        }

        @Test
        @DisplayName("removeBonus ไม่ให้ ATK ติดลบ")
        void removeBonusAtkNotNegative() {
            player.removeBonus(999, 0, 0, 0);
            assertEquals(0, player.getAttack());
        }

        @Test
        @DisplayName("removeBonus ไม่ให้ MaxHP ต่ำกว่า 1")
        void removeBonusMaxHpNotBelowOne() {
            player.removeBonus(0, 0, 999, 0);
            assertEquals(1, player.getMaxHealth());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4. Gold
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. setGold()")
    class GoldTests {

        @Test
        @DisplayName("setGold ปกติทำงานถูกต้อง")
        void setGoldNormal() {
            player.setGold(500);
            assertEquals(500, player.getGold());
        }

        @Test
        @DisplayName("setGold ค่าลบถูก clamp เป็น 0")
        void setGoldNegativeClamped() {
            player.setGold(-100);
            assertEquals(0, player.getGold());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5. Inventory / usePotion
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("5. Inventory & usePotion()")
    class InventoryTests {

        @Test
        @DisplayName("addItem เพิ่ม item ลง inventory")
        void addItemAddsToInventory() {
            player.addItem(new HealPotion(), 3);
            assertEquals(1, player.getInventory().size());
            assertEquals(3, player.countItem(HealPotion.class));
        }

        @Test
        @DisplayName("addItem stackable item เพิ่ม count ไม่สร้าง entry ใหม่")
        void addItemStackableIncreasesCount() {
            player.addItem(new HealPotion(), 2);
            player.addItem(new HealPotion(), 3);
            assertEquals(1, player.getInventory().size()); // ยังคง 1 entry
            assertEquals(5, player.countItem(HealPotion.class));
        }

        @Test
        @DisplayName("usePotion (HealPotion) ฮีล 20% maxHP และลด count")
        void usePotionHealsAndDecrementsCount() {
            player.setHealth(50);
            player.addItem(new HealPotion(), 2);

            boolean used = player.usePotion(HealPotion.class);

            assertTrue(used);
            assertEquals(70, player.getHealth()); // 50 + 20% of 100 = 70
            assertEquals(1, player.countItem(HealPotion.class));
        }

        @Test
        @DisplayName("usePotion item สุดท้าย → ลบออกจาก inventory")
        void usePotionRemovesWhenEmpty() {
            player.addItem(new HealPotion(), 1);
            player.usePotion(HealPotion.class);
            assertTrue(player.getInventory().isEmpty());
        }

        @Test
        @DisplayName("usePotion ไม่มี item → return false")
        void usePotionNoItemReturnsFalse() {
            boolean used = player.usePotion(HealPotion.class);
            assertFalse(used);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6. attack()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("6. attack()")
    class AttackTests {

        @Test
        @DisplayName("attack ทำ damage = ATK - boss.DEF")
        void attackDealsDamage() {
            // player.atk=30, boss.def=5 → realDmg=25, boss.hp = 500-25 = 475
            player.attack(boss);
            assertEquals(475, boss.getHealthPoint());
        }

        @Test
        @DisplayName("takeDamage ไม่ติดลบ")
        void takeDamageNotNegative() {
            DummyMonster tankBoss = new DummyMonster(100, 10, 999);
            player.attack(tankBoss);
            assertEquals(100, tankBoss.getHealthPoint()); // 0 dmg → HP ไม่เปลี่ยน
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 7. Skill — skillKaguraDance()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("7. Skill: Kagura Dance (2x damage)")
    class KaguraDanceTests {

        @Test
        @DisplayName("damage ใน SkillResult = (atk - boss.def) * 2")
        void damageIsDouble() {
            Player.SkillResult r = player.skillKaguraDance(boss);
            int expected = Math.max(1, player.getAttack() - boss.getDefense()) * 2;
            assertEquals(expected, r.damage());
        }

        @Test
        @DisplayName("boss HP ลดลงตาม damage 2x")
        void bossHpDecreased() {
            int bossHpBefore = boss.getHealthPoint();
            player.skillKaguraDance(boss);
            int realDmg = Math.max(0, player.getAttack() * 2 - boss.getDefense()); // BaseCreature logic
            assertEquals(bossHpBefore - realDmg, boss.getHealthPoint());
        }

        @Test
        @DisplayName("ไม่มี heal, ไม่มี flags")
        void noHealNoFlags() {
            Player.SkillResult r = player.skillKaguraDance(boss);
            assertEquals(0, r.heal());
            assertFalse(r.shieldWall());
            assertFalse(r.berserkDebuff());
        }

        @Test
        @DisplayName("player HP ไม่เปลี่ยน")
        void playerHpUnchanged() {
            int before = player.getHealth();
            player.skillKaguraDance(boss);
            assertEquals(before, player.getHealth());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 8. Skill — skillDeadCalm()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("8. Skill: Dead Calm (Shield Wall)")
    class DeadCalmTests {

        @Test
        @DisplayName("shieldWall flag เป็น true")
        void shieldWallFlagTrue() {
            Player.SkillResult r = player.skillDeadCalm();
            assertTrue(r.shieldWall());
        }

        @Test
        @DisplayName("ไม่ทำ damage, ไม่ heal")
        void noDamageNoHeal() {
            int bossBefore = boss.getHealthPoint();
            int playerBefore = player.getHealth();
            Player.SkillResult r = player.skillDeadCalm();

            assertEquals(0, r.damage());
            assertEquals(0, r.heal());
            assertEquals(bossBefore, boss.getHealthPoint());
            assertEquals(playerBefore, player.getHealth());
        }

        @Test
        @DisplayName("berserkDebuff flag เป็น false")
        void noBerserkDebuff() {
            assertFalse(player.skillDeadCalm().berserkDebuff());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 9. Skill — skillConstantFlux()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("9. Skill: Constant Flux (3 hits + DEF debuff)")
    class ConstantFluxTests {

        @Test
        @DisplayName("damage = (atk - boss.def) * 3")
        void damageIsTriple() {
            Player.SkillResult r = player.skillConstantFlux(boss);
            int expected = Math.max(1, player.getAttack() - boss.getDefense()) * 3;
            assertEquals(expected, r.damage());
        }

        @Test
        @DisplayName("boss ถูกตี 3 ครั้ง (HP ลดลง 3x)")
        void bossHitThreeTimes() {
            int bossHpBefore = boss.getHealthPoint();
            player.skillConstantFlux(boss);
            int realDmgPerHit = Math.max(0, player.getAttack() - boss.getDefense());
            assertEquals(bossHpBefore - realDmgPerHit * 3, boss.getHealthPoint());
        }

        @Test
        @DisplayName("berserkDebuff flag เป็น true")
        void berserkDebuffTrue() {
            assertTrue(player.skillConstantFlux(boss).berserkDebuff());
        }

        @Test
        @DisplayName("ไม่มี heal, shieldWall false")
        void noHealNoShield() {
            Player.SkillResult r = player.skillConstantFlux(boss);
            assertEquals(0, r.heal());
            assertFalse(r.shieldWall());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 10. Skill — skillWaterWheel()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("10. Skill: Water Wheel (Damage + Heal 30%)")
    class WaterWheelTests {

        @Test
        @DisplayName("damage ใน SkillResult = atk - boss.def")
        void damageCorrect() {
            Player.SkillResult r = player.skillWaterWheel(boss);
            int expected = Math.max(1, player.getAttack() - boss.getDefense());
            assertEquals(expected, r.damage());
        }

        @Test
        @DisplayName("heal = 30% ของ damage (อย่างน้อย 1)")
        void healIsThirtyPercent() {
            Player.SkillResult r = player.skillWaterWheel(boss);
            int base = Math.max(1, player.getAttack() - boss.getDefense());
            int expectedHeal = Math.max(1, (int) (base * 0.30));
            assertEquals(expectedHeal, r.heal());
        }

        @Test
        @DisplayName("player HP เพิ่มขึ้นจริง (ไม่ใช่แค่ใน result)")
        void playerHpActuallyIncreases() {
            player.setHealth(50);
            int before = player.getHealth();
            Player.SkillResult r = player.skillWaterWheel(boss);
            assertEquals(before + r.heal(), player.getHealth());
        }

        @Test
        @DisplayName("heal ไม่เกิน maxHP")
        void healCappedAtMaxHp() {
            player.setHealth(99); // เกือบเต็ม
            player.skillWaterWheel(boss);
            assertTrue(player.getHealth() <= player.getMaxHealth());
        }

        @Test
        @DisplayName("boss HP ลดลง")
        void bossHpDecreased() {
            int before = boss.getHealthPoint();
            player.skillWaterWheel(boss);
            assertTrue(boss.getHealthPoint() < before);
        }

        @Test
        @DisplayName("ไม่มี flags")
        void noFlags() {
            Player.SkillResult r = player.skillWaterWheel(boss);
            assertFalse(r.shieldWall());
            assertFalse(r.berserkDebuff());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 11. Edge Cases
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("11. Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("skill ตี boss ที่ DEF สูงมาก → damage อย่างน้อย 1")
        void skillMinDamageIsOne() {
            DummyMonster tankBoss = new DummyMonster(500, 10, 9999);
            Player.SkillResult r = player.skillKaguraDance(tankBoss);
            assertTrue(r.damage() >= 1);
        }

        @Test
        @DisplayName("skillConstantFlux boss ที่ DEF สูง → damage อย่างน้อย 3")
        void constantFluxMinDamageIsThree() {
            DummyMonster tankBoss = new DummyMonster(500, 10, 9999);
            Player.SkillResult r = player.skillConstantFlux(tankBoss);
            assertTrue(r.damage() >= 3); // 3 hits, each at least 1
        }

        @Test
        @DisplayName("skillWaterWheel heal อย่างน้อย 1 เสมอ")
        void waterWheelMinHealIsOne() {
            DummyMonster tankBoss = new DummyMonster(500, 10, 9999);
            Player.SkillResult r = player.skillWaterWheel(tankBoss);
            assertTrue(r.heal() >= 1);
        }

        @Test
        @DisplayName("setHealth ไม่ให้ HP เกิน maxHP")
        void setHealthCappedAtMax() {
            player.setHealth(9999);
            assertEquals(100, player.getHealth());
        }

        @Test
        @DisplayName("setHealth ไม่ให้ HP ติดลบ")
        void setHealthNotNegative() {
            player.setHealth(-50);
            assertEquals(0, player.getHealth());
        }
    }
}