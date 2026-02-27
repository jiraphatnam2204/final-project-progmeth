package logic.item.armor;

import logic.creatures.Player;
import logic.stone.*;
import logic.util.ItemCounter;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ArmorTest {

    // Player with base stats: hp=100, atk=10, def=5
    private Player player;

    @BeforeEach
    void setup() {
        player = new Player(100, 10, 5);
    }

    // ─── StoneArmor ──────────────────────────────────────────────────────────

    @Nested
    class StoneArmorTests {

        StoneArmor armor;

        @BeforeEach
        void init() { armor = new StoneArmor(); }

        @Test
        void name() { assertEquals("Stone Armor", armor.getName()); }

        @Test
        void def() { assertEquals(5, armor.getDef()); }

        @Test
        void hp() { assertEquals(10, armor.getHp()); }

        @Test
        void atk() { assertEquals(0, armor.getAtk()); }

        @Test
        void spd() { assertEquals(0, armor.getSpd()); }

        @Test
        void craftingPrice() { assertEquals(10, armor.getCraftingPrice()); }

        @Test
        void recipeSize() { assertEquals(1, armor.getRecipe().size()); }

        @Test
        void recipeContent() {
            ItemCounter ic = armor.getRecipe().get(0);
            assertEquals("Normal Stone", ic.getItem().getName());
            assertEquals(10, ic.getCount());
        }

        @Test
        void equip_increasesDefAndMaxHp() {
            int defBefore = player.getDefense();
            int hpBefore  = player.getMaxHealth();
            armor.equip(player);
            assertEquals(defBefore + 5,  player.getDefense());
            assertEquals(hpBefore  + 10, player.getMaxHealth());
        }

        @Test
        void equip_increasesCurrentHp() {
            int hpBefore = player.getHealth();
            armor.equip(player);
            assertEquals(hpBefore + 10, player.getHealth());
        }

        @Test
        void unequip_restoresDefAndMaxHp() {
            armor.equip(player);
            int defAfter = player.getDefense();
            int hpAfter  = player.getMaxHealth();
            armor.unequip(player);
            assertEquals(defAfter - 5,  player.getDefense());
            assertEquals(hpAfter  - 10, player.getMaxHealth());
        }

        @Test
        void canCraft_trueWhenHasGoldAndMaterials() {
            player.setGold(10);
            player.addItem(new NormalStone(), 10);
            assertTrue(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughGold() {
            player.setGold(9);
            player.addItem(new NormalStone(), 10);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughMaterials() {
            player.setGold(10);
            player.addItem(new NormalStone(), 9);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNoMaterials() {
            player.setGold(10);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void craft_deductsGold() {
            player.setGold(20);
            player.addItem(new NormalStone(), 10);
            armor.craft(player);
            assertEquals(10, player.getGold());
        }

        @Test
        void craft_consumesMaterials() {
            player.setGold(10);
            player.addItem(new NormalStone(), 10);
            armor.craft(player);
            assertEquals(0, player.countItem(NormalStone.class));
        }

        @Test
        void craft_doesNothingWhenCannotCraft() {
            player.setGold(5);
            player.addItem(new NormalStone(), 10);
            armor.craft(player);
            assertEquals(5, player.getGold());
            assertEquals(10, player.countItem(NormalStone.class));
        }
    }

    // ─── HardstoneArmor ──────────────────────────────────────────────────────

    @Nested
    class HardstoneArmorTests {

        HardstoneArmor armor;

        @BeforeEach
        void init() { armor = new HardstoneArmor(); }

        @Test
        void name() { assertEquals("Hardstone Armor", armor.getName()); }

        @Test
        void def() { assertEquals(10, armor.getDef()); }

        @Test
        void hp() { assertEquals(15, armor.getHp()); }

        @Test
        void atk() { assertEquals(0, armor.getAtk()); }

        @Test
        void spd() { assertEquals(0, armor.getSpd()); }

        @Test
        void craftingPrice() { assertEquals(50, armor.getCraftingPrice()); }

        @Test
        void recipeSize() { assertEquals(2, armor.getRecipe().size()); }

        @Test
        void recipeContainsNormalStone() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Normal Stone") && ic.getCount() == 5);
            assertTrue(found);
        }

        @Test
        void recipeContainsHardStone() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Hard Stone") && ic.getCount() == 10);
            assertTrue(found);
        }

        @Test
        void equip_increasesDefAndMaxHp() {
            int defBefore = player.getDefense();
            int hpBefore  = player.getMaxHealth();
            armor.equip(player);
            assertEquals(defBefore + 10, player.getDefense());
            assertEquals(hpBefore  + 15, player.getMaxHealth());
        }

        @Test
        void canCraft_trueWhenHasAllMaterials() {
            player.setGold(50);
            player.addItem(new NormalStone(), 5);
            player.addItem(new HardStone(), 10);
            assertTrue(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughHardStone() {
            player.setGold(50);
            player.addItem(new NormalStone(), 5);
            player.addItem(new HardStone(), 9);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughNormalStone() {
            player.setGold(50);
            player.addItem(new NormalStone(), 4);
            player.addItem(new HardStone(), 10);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void craft_deductsGold() {
            player.setGold(100);
            player.addItem(new NormalStone(), 5);
            player.addItem(new HardStone(), 10);
            armor.craft(player);
            assertEquals(50, player.getGold());
        }

        @Test
        void craft_consumesMaterials() {
            player.setGold(50);
            player.addItem(new NormalStone(), 5);
            player.addItem(new HardStone(), 10);
            armor.craft(player);
            assertEquals(0, player.countItem(NormalStone.class));
            assertEquals(0, player.countItem(HardStone.class));
        }
    }

    // ─── IronArmor ───────────────────────────────────────────────────────────

    @Nested
    class IronArmorTests {

        IronArmor armor;

        @BeforeEach
        void init() { armor = new IronArmor(); }

        @Test
        void name() { assertEquals("Iron Armor", armor.getName()); }

        @Test
        void def() { assertEquals(15, armor.getDef()); }

        @Test
        void hp() { assertEquals(40, armor.getHp()); }

        @Test
        void atk() { assertEquals(0, armor.getAtk()); }

        @Test
        void spd() { assertEquals(0, armor.getSpd()); }

        @Test
        void craftingPrice() { assertEquals(100, armor.getCraftingPrice()); }

        @Test
        void recipeSize() { assertEquals(2, armor.getRecipe().size()); }

        @Test
        void recipeContainsNormalStone() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Normal Stone") && ic.getCount() == 5);
            assertTrue(found);
        }

        @Test
        void recipeContainsIron() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Iron") && ic.getCount() == 8);
            assertTrue(found);
        }

        @Test
        void equip_increasesDefAndMaxHp() {
            int defBefore = player.getDefense();
            int hpBefore  = player.getMaxHealth();
            armor.equip(player);
            assertEquals(defBefore + 15, player.getDefense());
            assertEquals(hpBefore  + 40, player.getMaxHealth());
        }

        @Test
        void canCraft_trueWhenHasAllMaterials() {
            player.setGold(100);
            player.addItem(new NormalStone(), 5);
            player.addItem(new Iron(), 8);
            assertTrue(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughGold() {
            player.setGold(99);
            player.addItem(new NormalStone(), 5);
            player.addItem(new Iron(), 8);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void craft_deductsGold() {
            player.setGold(200);
            player.addItem(new NormalStone(), 5);
            player.addItem(new Iron(), 8);
            armor.craft(player);
            assertEquals(100, player.getGold());
        }

        @Test
        void craft_consumesMaterials() {
            player.setGold(100);
            player.addItem(new NormalStone(), 5);
            player.addItem(new Iron(), 8);
            armor.craft(player);
            assertEquals(0, player.countItem(NormalStone.class));
            assertEquals(0, player.countItem(Iron.class));
        }
    }

    // ─── PlatinumArmor ───────────────────────────────────────────────────────

    @Nested
    class PlatinumArmorTests {

        PlatinumArmor armor;

        @BeforeEach
        void init() { armor = new PlatinumArmor(); }

        @Test
        void name() { assertEquals("Platinum Armor", armor.getName()); }

        @Test
        void def() { assertEquals(25, armor.getDef()); }

        @Test
        void hp() { assertEquals(60, armor.getHp()); }

        @Test
        void atk() { assertEquals(0, armor.getAtk()); }

        @Test
        void spd() { assertEquals(0, armor.getSpd()); }

        @Test
        void craftingPrice() { assertEquals(160, armor.getCraftingPrice()); }

        @Test
        void recipeSize() { assertEquals(2, armor.getRecipe().size()); }

        @Test
        void recipeContainsIron() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Iron") && ic.getCount() == 8);
            assertTrue(found);
        }

        @Test
        void recipeContainsPlatinum() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Platinum") && ic.getCount() == 10);
            assertTrue(found);
        }

        @Test
        void equip_increasesDefAndMaxHp() {
            int defBefore = player.getDefense();
            int hpBefore  = player.getMaxHealth();
            armor.equip(player);
            assertEquals(defBefore + 25, player.getDefense());
            assertEquals(hpBefore  + 60, player.getMaxHealth());
        }

        @Test
        void canCraft_trueWhenHasAllMaterials() {
            player.setGold(160);
            player.addItem(new Iron(), 8);
            player.addItem(new Platinum(), 10);
            assertTrue(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughPlatinum() {
            player.setGold(160);
            player.addItem(new Iron(), 8);
            player.addItem(new Platinum(), 9);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void craft_deductsGold() {
            player.setGold(200);
            player.addItem(new Iron(), 8);
            player.addItem(new Platinum(), 10);
            armor.craft(player);
            assertEquals(40, player.getGold());
        }

        @Test
        void craft_consumesMaterials() {
            player.setGold(160);
            player.addItem(new Iron(), 8);
            player.addItem(new Platinum(), 10);
            armor.craft(player);
            assertEquals(0, player.countItem(Iron.class));
            assertEquals(0, player.countItem(Platinum.class));
        }
    }

    // ─── MithrilArmor ────────────────────────────────────────────────────────

    @Nested
    class MithrilArmorTests {

        MithrilArmor armor;

        @BeforeEach
        void init() { armor = new MithrilArmor(); }

        @Test
        void name() { assertEquals("Mithril Armor", armor.getName()); }

        @Test
        void def() { assertEquals(40, armor.getDef()); }

        @Test
        void hp() { assertEquals(100, armor.getHp()); }

        @Test
        void atk() { assertEquals(0, armor.getAtk()); }

        @Test
        void spd() { assertEquals(0, armor.getSpd()); }

        @Test
        void craftingPrice() { assertEquals(230, armor.getCraftingPrice()); }

        @Test
        void recipeSize() { assertEquals(2, armor.getRecipe().size()); }

        @Test
        void recipeContainsPlatinum() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Platinum") && ic.getCount() == 5);
            assertTrue(found);
        }

        @Test
        void recipeContainsMithril() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Mithril") && ic.getCount() == 15);
            assertTrue(found);
        }

        @Test
        void equip_increasesDefAndMaxHp() {
            int defBefore = player.getDefense();
            int hpBefore  = player.getMaxHealth();
            armor.equip(player);
            assertEquals(defBefore + 40,  player.getDefense());
            assertEquals(hpBefore  + 100, player.getMaxHealth());
        }

        @Test
        void canCraft_trueWhenHasAllMaterials() {
            player.setGold(230);
            player.addItem(new Platinum(), 5);
            player.addItem(new Mithril(), 15);
            assertTrue(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughMithril() {
            player.setGold(230);
            player.addItem(new Platinum(), 5);
            player.addItem(new Mithril(), 14);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void craft_deductsGold() {
            player.setGold(300);
            player.addItem(new Platinum(), 5);
            player.addItem(new Mithril(), 15);
            armor.craft(player);
            assertEquals(70, player.getGold());
        }

        @Test
        void craft_consumesMaterials() {
            player.setGold(230);
            player.addItem(new Platinum(), 5);
            player.addItem(new Mithril(), 15);
            armor.craft(player);
            assertEquals(0, player.countItem(Platinum.class));
            assertEquals(0, player.countItem(Mithril.class));
        }
    }

    // ─── VibraniumArmor ──────────────────────────────────────────────────────

    @Nested
    class VibraniumArmorTests {

        VibraniumArmor armor;

        @BeforeEach
        void init() { armor = new VibraniumArmor(); }

        @Test
        void name() { assertEquals("Vibranium Armor", armor.getName()); }

        @Test
        void def() { assertEquals(55, armor.getDef()); }

        @Test
        void hp() { assertEquals(150, armor.getHp()); }

        @Test
        void atk() { assertEquals(0, armor.getAtk()); }

        @Test
        void spd() { assertEquals(0, armor.getSpd()); }

        @Test
        void craftingPrice() { assertEquals(310, armor.getCraftingPrice()); }

        @Test
        void recipeSize() { assertEquals(2, armor.getRecipe().size()); }

        @Test
        void recipeContainsMithril() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Mithril") && ic.getCount() == 10);
            assertTrue(found);
        }

        @Test
        void recipeContainsVibranium() {
            ArrayList<ItemCounter> recipe = armor.getRecipe();
            boolean found = recipe.stream()
                    .anyMatch(ic -> ic.getItem().getName().equals("Vibranium") && ic.getCount() == 15);
            assertTrue(found);
        }

        @Test
        void equip_increasesDefAndMaxHp() {
            int defBefore = player.getDefense();
            int hpBefore  = player.getMaxHealth();
            armor.equip(player);
            assertEquals(defBefore + 55,  player.getDefense());
            assertEquals(hpBefore  + 150, player.getMaxHealth());
        }

        @Test
        void canCraft_trueWhenHasAllMaterials() {
            player.setGold(310);
            player.addItem(new Mithril(), 10);
            player.addItem(new Vibranium(), 15);
            assertTrue(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughVibranium() {
            player.setGold(310);
            player.addItem(new Mithril(), 10);
            player.addItem(new Vibranium(), 14);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void canCraft_falseWhenNotEnoughGold() {
            player.setGold(309);
            player.addItem(new Mithril(), 10);
            player.addItem(new Vibranium(), 15);
            assertFalse(armor.canCraft(player));
        }

        @Test
        void craft_deductsGold() {
            player.setGold(400);
            player.addItem(new Mithril(), 10);
            player.addItem(new Vibranium(), 15);
            armor.craft(player);
            assertEquals(90, player.getGold());
        }

        @Test
        void craft_consumesMaterials() {
            player.setGold(310);
            player.addItem(new Mithril(), 10);
            player.addItem(new Vibranium(), 15);
            armor.craft(player);
            assertEquals(0, player.countItem(Mithril.class));
            assertEquals(0, player.countItem(Vibranium.class));
        }

        @Test
        void craft_doesNothingWhenCannotCraft() {
            player.setGold(100);
            player.addItem(new Mithril(), 10);
            player.addItem(new Vibranium(), 15);
            armor.craft(player);
            assertEquals(100, player.getGold());
            assertEquals(10, player.countItem(Mithril.class));
            assertEquals(15, player.countItem(Vibranium.class));
        }
    }
}