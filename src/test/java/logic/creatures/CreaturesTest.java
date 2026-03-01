package logic.creatures;

import logic.item.armor.StoneArmor;
import logic.item.potion.SmallHealthPotion;
import logic.item.weapon.WoodenSword;
import logic.stone.NormalStone;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CreaturesTest {

    // ─── EasyMonster ─────────────────────────────────────────────────────────

    @Nested
    class EasyMonsterTests {

        EasyMonster monster;

        @BeforeEach
        void init() { monster = new EasyMonster(); }

        @Test
        void initialHp() { assertEquals(40, monster.getHealthPoint()); }

        @Test
        void maxHp() { assertEquals(40, monster.getMaxHealthPoint()); }

        @Test
        void initialAttack() { assertEquals(12, monster.getAttack()); }

        @Test
        void initialDefense() { assertEquals(1, monster.getDefense()); }

        @Test
        void isAlive_trueInitially() { assertTrue(monster.isAlive()); }

        @Test
        void dropMoney() { assertEquals(20, monster.dropMoney()); }

        @Test
        void takeDamage_reducesHp() {
            monster.takeDamage(10); // realDamage = 10-1 = 9
            assertEquals(31, monster.getHealthPoint());
        }

        @Test
        void takeDamage_blockedByDefense() {
            monster.takeDamage(1); // damage == defense → 0
            assertEquals(40, monster.getHealthPoint());
        }

        @Test
        void takeDamage_zeroDamage() {
            monster.takeDamage(0);
            assertEquals(40, monster.getHealthPoint());
        }

        @Test
        void takeDamage_clampsToZero() {
            monster.takeDamage(999);
            assertEquals(0, monster.getHealthPoint());
        }

        @Test
        void isAlive_falseAfterLethalDamage() {
            monster.takeDamage(999);
            assertFalse(monster.isAlive());
        }

        @Test
        void heal_restoresHp() {
            monster.takeDamage(20); // hp = 40-(20-1) = 21
            monster.heal(10);
            assertEquals(31, monster.getHealthPoint());
        }

        @Test
        void heal_cappedAtMaxHp() {
            monster.heal(999);
            assertEquals(40, monster.getHealthPoint());
        }

        @Test
        void attack_dealsDamageToTarget() {
            Player target = new Player(100, 10, 0);
            monster.attack(target); // 100 - 12 = 88
            assertEquals(88, target.getHealthPoint());
        }

        @Test
        void attack_blockedByTargetDefense() {
            Player target = new Player(100, 10, 20); // defense > attack
            monster.attack(target);
            assertEquals(100, target.getHealthPoint());
        }
    }

    // ─── MediumMonster ───────────────────────────────────────────────────────

    @Nested
    class MediumMonsterTests {

        MediumMonster monster;

        @BeforeEach
        void init() { monster = new MediumMonster(); }

        @Test
        void initialHp() { assertEquals(90, monster.getHealthPoint()); }

        @Test
        void maxHp() { assertEquals(90, monster.getMaxHealthPoint()); }

        @Test
        void initialAttack() { assertEquals(22, monster.getAttack()); }

        @Test
        void initialDefense() { assertEquals(4, monster.getDefense()); }

        @Test
        void isAlive_trueInitially() { assertTrue(monster.isAlive()); }

        @Test
        void dropMoney() { assertEquals(40, monster.dropMoney()); }

        @Test
        void takeDamage_reducesHp() {
            monster.takeDamage(14); // realDamage = 14-4 = 10
            assertEquals(80, monster.getHealthPoint());
        }

        @Test
        void takeDamage_blockedByDefense() {
            monster.takeDamage(4);
            assertEquals(90, monster.getHealthPoint());
        }

        @Test
        void takeDamage_clampsToZero() {
            monster.takeDamage(9999);
            assertEquals(0, monster.getHealthPoint());
            assertFalse(monster.isAlive());
        }

        @Test
        void heal_restoresHp() {
            monster.takeDamage(30); // hp = 90-(30-4) = 64
            monster.heal(20);
            assertEquals(84, monster.getHealthPoint());
        }

        @Test
        void heal_cappedAtMaxHp() {
            monster.heal(999);
            assertEquals(90, monster.getHealthPoint());
        }

        @Test
        void attack_dealsDamageToTarget() {
            Player target = new Player(100, 10, 0);
            monster.attack(target); // 100 - 22 = 78
            assertEquals(78, target.getHealthPoint());
        }
    }

    // ─── HardMonster ─────────────────────────────────────────────────────────

    @Nested
    class HardMonsterTests {

        HardMonster monster;

        @BeforeEach
        void init() { monster = new HardMonster(); }

        @Test
        void initialHp() { assertEquals(160, monster.getHealthPoint()); }

        @Test
        void maxHp() { assertEquals(160, monster.getMaxHealthPoint()); }

        @Test
        void initialAttack() { assertEquals(32, monster.getAttack()); }

        @Test
        void initialDefense() { assertEquals(8, monster.getDefense()); }

        @Test
        void isAlive_trueInitially() { assertTrue(monster.isAlive()); }

        @Test
        void dropMoney() { assertEquals(80, monster.dropMoney()); }

        @Test
        void takeDamage_reducesHp() {
            monster.takeDamage(18); // realDamage = 18-8 = 10
            assertEquals(150, monster.getHealthPoint());
        }

        @Test
        void takeDamage_blockedByDefense() {
            monster.takeDamage(8);
            assertEquals(160, monster.getHealthPoint());
        }

        @Test
        void takeDamage_clampsToZero() {
            monster.takeDamage(9999);
            assertEquals(0, monster.getHealthPoint());
            assertFalse(monster.isAlive());
        }

        @Test
        void heal_cappedAtMaxHp() {
            monster.heal(999);
            assertEquals(160, monster.getHealthPoint());
        }

        @Test
        void attack_dealsDamageToTarget() {
            Player target = new Player(200, 10, 0);
            monster.attack(target); // 200 - 32 = 168
            assertEquals(168, target.getHealthPoint());
        }
    }

    // ─── EasyBoss ────────────────────────────────────────────────────────────

    @Nested
    class EasyBossTests {

        EasyBoss boss;

        @BeforeEach
        void init() { boss = new EasyBoss(); }

        @Test
        void initialHp() { assertEquals(500, boss.getHealthPoint()); }

        @Test
        void maxHp() { assertEquals(500, boss.getMaxHealthPoint()); }

        @Test
        void initialAttack() { assertEquals(60, boss.getAttack()); }

        @Test
        void initialDefense() { assertEquals(15, boss.getDefense()); }

        @Test
        void isAlive_trueInitially() { assertTrue(boss.isAlive()); }

        @Test
        void dropMoney() { assertEquals(300, boss.dropMoney()); }

        @Test
        void takeDamage_reducesHp() {
            boss.takeDamage(25); // 500-(25-15) = 490
            assertEquals(490, boss.getHealthPoint());
        }

        @Test
        void takeDamage_blockedByDefense() {
            boss.takeDamage(15);
            assertEquals(500, boss.getHealthPoint());
        }

        @Test
        void takeDamage_clampsToZero() {
            boss.takeDamage(9999);
            assertEquals(0, boss.getHealthPoint());
            assertFalse(boss.isAlive());
        }

        @Test
        void heal_afterDamage() {
            boss.takeDamage(115); // hp = 500-(115-15) = 400
            boss.heal(50);
            assertEquals(450, boss.getHealthPoint());
        }

        @Test
        void heal_cappedAtMaxHp() {
            boss.heal(9999);
            assertEquals(500, boss.getHealthPoint());
        }

        @Test
        void attack_dealsDamageToTarget() {
            Player target = new Player(200, 10, 0);
            boss.attack(target); // 200 - 60 = 140
            assertEquals(140, target.getHealthPoint());
        }
    }

    // ─── MediumBoss ──────────────────────────────────────────────────────────

    @Nested
    class MediumBossTests {

        MediumBoss boss;

        @BeforeEach
        void init() { boss = new MediumBoss(); }

        @Test
        void initialHp() { assertEquals(900, boss.getHealthPoint()); }

        @Test
        void maxHp() { assertEquals(900, boss.getMaxHealthPoint()); }

        @Test
        void initialAttack() { assertEquals(80, boss.getAttack()); }

        @Test
        void initialDefense() { assertEquals(22, boss.getDefense()); }

        @Test
        void isAlive_trueInitially() { assertTrue(boss.isAlive()); }

        @Test
        void dropMoney() { assertEquals(700, boss.dropMoney()); }

        @Test
        void takeDamage_reducesHp() {
            boss.takeDamage(32); // 900-(32-22) = 890
            assertEquals(890, boss.getHealthPoint());
        }

        @Test
        void takeDamage_blockedByDefense() {
            boss.takeDamage(22);
            assertEquals(900, boss.getHealthPoint());
        }

        @Test
        void takeDamage_clampsToZero() {
            boss.takeDamage(99999);
            assertEquals(0, boss.getHealthPoint());
            assertFalse(boss.isAlive());
        }

        @Test
        void attack_dealsDamageToTarget() {
            Player target = new Player(200, 10, 0);
            boss.attack(target); // 200 - 80 = 120
            assertEquals(120, target.getHealthPoint());
        }
    }

    // ─── HardBoss ────────────────────────────────────────────────────────────

    @Nested
    class HardBossTests {

        HardBoss boss;

        @BeforeEach
        void init() { boss = new HardBoss(); }

        @Test
        void initialHp() { assertEquals(1600, boss.getHealthPoint()); }

        @Test
        void maxHp() { assertEquals(1600, boss.getMaxHealthPoint()); }

        @Test
        void initialAttack() { assertEquals(100, boss.getAttack()); }

        @Test
        void initialDefense() { assertEquals(38, boss.getDefense()); }

        @Test
        void isAlive_trueInitially() { assertTrue(boss.isAlive()); }

        @Test
        void dropMoney() { assertEquals(1500, boss.dropMoney()); }

        @Test
        void takeDamage_reducesHp() {
            boss.takeDamage(48); // 1600-(48-38) = 1590
            assertEquals(1590, boss.getHealthPoint());
        }

        @Test
        void takeDamage_blockedByDefense() {
            boss.takeDamage(38);
            assertEquals(1600, boss.getHealthPoint());
        }

        @Test
        void takeDamage_clampsToZero() {
            boss.takeDamage(999999);
            assertEquals(0, boss.getHealthPoint());
            assertFalse(boss.isAlive());
        }

        @Test
        void attack_dealsDamageToTarget() {
            Player target = new Player(200, 10, 0);
            boss.attack(target); // 200 - 100 = 100
            assertEquals(100, target.getHealthPoint());
        }
    }

    // ─── Player ──────────────────────────────────────────────────────────────

    @Nested
    class PlayerTests {

        Player player;

        @BeforeEach
        void init() { player = new Player(100, 10, 5); }

        // ── Initial stats ──

        @Test
        void initialHp() { assertEquals(100, player.getHealth()); }

        @Test
        void initialMaxHp() { assertEquals(100, player.getMaxHealth()); }

        @Test
        void initialAttack() { assertEquals(10, player.getStrength()); }

        @Test
        void initialDefense() { assertEquals(5, player.getDefense()); }

        @Test
        void initialSpeed() { assertEquals(0, player.getSpeed()); }

        @Test
        void initialLuck() { assertEquals(0, player.getLuck()); }

        @Test
        void initialGold() { assertEquals(0, player.getGold()); }

        @Test
        void isAlive_trueInitially() { assertTrue(player.isAlive()); }

        // ── Gold ──

        @Test
        void setGold_positive() {
            player.setGold(500);
            assertEquals(500, player.getGold());
        }

        @Test
        void setGold_negativeClampedToZero() {
            player.setGold(100);
            player.setGold(-50);
            assertEquals(0, player.getGold());
        }

        // ── Inventory ──

        @Test
        void addItem_countItem() {
            player.addItem(new NormalStone(), 5);
            assertEquals(5, player.countItem(NormalStone.class));
        }

        @Test
        void addItem_stackable_addsToExisting() {
            player.addItem(new NormalStone(), 5);
            player.addItem(new NormalStone(), 3);
            assertEquals(8, player.countItem(NormalStone.class));
        }

        @Test
        void countItem_returnsZeroForMissing() {
            assertEquals(0, player.countItem(NormalStone.class));
        }

        // ── Use potion ──

        @Test
        void usePotion_healsPlayer() {
            player.setHealth(50);
            player.addItem(new SmallHealthPotion(), 1);
            boolean used = player.usePotion(SmallHealthPotion.class);
            assertTrue(used);
            assertEquals(90, player.getHealth()); // 50 + 40
        }

        @Test
        void usePotion_cappedAtMaxHp() {
            player.setHealth(80);
            player.addItem(new SmallHealthPotion(), 1);
            player.usePotion(SmallHealthPotion.class);
            assertEquals(100, player.getHealth()); // 80+40=120 capped at 100
        }

        @Test
        void usePotion_consumesOneItem() {
            player.addItem(new SmallHealthPotion(), 3);
            player.usePotion(SmallHealthPotion.class);
            assertEquals(2, player.countItem(SmallHealthPotion.class));
        }

        @Test
        void usePotion_returnsFalseWhenNone() {
            assertFalse(player.usePotion(SmallHealthPotion.class));
        }

        // ── Equipment: Weapon ──

        @Test
        void equipWeapon_increasesAttack() {
            int before = player.getStrength();
            player.equipWeapon(new WoodenSword()); // dmg = 5
            assertEquals(before + 5, player.getStrength());
        }

        @Test
        void unequipWeapon_restoresAttack() {
            int before = player.getStrength();
            player.equipWeapon(new WoodenSword());
            player.unequipWeapon();
            assertEquals(before, player.getStrength());
        }

        @Test
        void equipWeapon_replacesExisting() {
            int base = player.getStrength();
            player.equipWeapon(new WoodenSword()); // +5
            player.equipWeapon(new WoodenSword()); // unequip old (-5), equip new (+5)
            assertEquals(base + 5, player.getStrength());
        }

        @Test
        void getEquippedWeapon_nullByDefault() {
            assertNull(player.getEquippedWeapon());
        }

        @Test
        void getEquippedWeapon_afterEquip() {
            WoodenSword sword = new WoodenSword();
            player.equipWeapon(sword);
            assertSame(sword, player.getEquippedWeapon());
        }

        // ── Equipment: Armor ──

        @Test
        void equipArmor_increasesDefenseAndMaxHp() {
            int defBefore  = player.getDefense();
            int maxHpBefore = player.getMaxHealth();
            player.equipArmor(new StoneArmor()); // def+5, hp+10
            assertEquals(defBefore  + 5,  player.getDefense());
            assertEquals(maxHpBefore + 10, player.getMaxHealth());
        }

        @Test
        void unequipArmor_restoresStats() {
            int defBefore   = player.getDefense();
            int maxHpBefore = player.getMaxHealth();
            player.equipArmor(new StoneArmor());
            player.unequipArmor();
            assertEquals(defBefore,   player.getDefense());
            assertEquals(maxHpBefore, player.getMaxHealth());
        }

        @Test
        void getEquippedArmor_nullByDefault() {
            assertNull(player.getEquippedArmor());
        }

        // ── addBonus / removeBonus ──

        @Test
        void addBonus_updatesStats() {
            player.addBonus(5, 3, 20, 2);
            assertEquals(15,  player.getStrength());
            assertEquals(8,   player.getDefense());
            assertEquals(120, player.getMaxHealth());
            assertEquals(120, player.getHealth()); // hp also increases
            assertEquals(2,   player.getSpeed());
        }

        @Test
        void removeBonus_updatesStats() {
            player.addBonus(5, 3, 20, 2);
            player.removeBonus(5, 3, 20, 2);
            assertEquals(10,  player.getStrength());
            assertEquals(5,   player.getDefense());
            assertEquals(100, player.getMaxHealth());
            assertEquals(0,   player.getSpeed());
        }

        @Test
        void removeBonus_clampsHpToMaxHp() {
            player.addBonus(0, 0, 50, 0); // maxHp=150, hp=150
            player.removeBonus(0, 0, 50, 0); // maxHp=100, hp clamped to 100
            assertEquals(100, player.getMaxHealth());
            assertEquals(100, player.getHealth());
        }

        // ── Combat ──

        @Test
        void attack_dealsDamageToTarget() {
            EasyMonster target = new EasyMonster(); // def=1
            player.attack(target); // atk=10, realDamage=9
            assertEquals(31, target.getHealthPoint()); // 40-9
        }

        @Test
        void takeDamage_reducesHp() {
            player.takeDamage(15); // realDamage = 15-5 = 10
            assertEquals(90, player.getHealth());
        }

        @Test
        void takeDamage_blockedByDefense() {
            player.takeDamage(5); // damage == defense
            assertEquals(100, player.getHealth());
        }

        @Test
        void takeDamage_clampsToZero() {
            player.takeDamage(9999);
            assertEquals(0, player.getHealth());
            assertFalse(player.isAlive());
        }

        @Test
        void heal_restoresHp() {
            player.takeDamage(35); // hp = 100-(35-5) = 70
            player.heal(20);
            assertEquals(90, player.getHealth());
        }

        @Test
        void heal_cappedAtMaxHp() {
            player.heal(999);
            assertEquals(100, player.getHealth());
        }

        // ── setHealth ──

        @Test
        void setHealth_setsCorrectly() {
            player.setHealth(50);
            assertEquals(50, player.getHealth());
        }

        @Test
        void setHealth_clampsAboveMax() {
            player.setHealth(200);
            assertEquals(100, player.getHealth());
        }

        @Test
        void setHealth_clampsToZero() {
            player.setHealth(-10);
            assertEquals(0, player.getHealth());
        }

        // ── setStrength / setLuck ──

        @Test
        void setStrength_positive() {
            player.setStrength(25);
            assertEquals(25, player.getStrength());
        }

        @Test
        void setStrength_negativeClampedToZero() {
            player.setStrength(-5);
            assertEquals(0, player.getStrength());
        }

        @Test
        void setLuck_setsCorrectly() {
            player.setLuck(7);
            assertEquals(7, player.getLuck());
        }
    }
}