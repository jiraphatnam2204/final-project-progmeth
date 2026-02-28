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
public class WeaponTest {

    private Player player;

    @BeforeEach
    void setUp() {
        // Player: 100 HP, 20 ATK, 10 DEF
        player = new Player(100, 20, 10);
        player.setGold(9999);
    }
    @Test
    void woodenSword_equip_increasesATK() {
        int before = player.getAttack();
        WoodenSword w = new WoodenSword();
        player.equipWeapon(w);
        assertEquals(before + w.getDmg(), player.getAttack());
    }

    @Test
    void woodenSword_unequip_restoresATK() {
        int before = player.getAttack();
        WoodenSword w = new WoodenSword();
        player.equipWeapon(w);
        player.unequipWeapon();
        assertEquals(before, player.getAttack());
    }

    @Test
    void woodenSword_stats() {
        WoodenSword w = new WoodenSword();
        assertEquals("Wooden Sword", w.getName());
        assertEquals(5, w.getDmg());
    }

    @Test
    void stoneSword_equip_increasesATK() {
        int before = player.getAttack();
        StoneSword w = new StoneSword();
        player.equipWeapon(w);
        assertEquals(before + w.getDmg(), player.getAttack());
    }

    @Test
    void stoneSword_stats() {
        StoneSword w = new StoneSword();
        assertEquals("Stone Sword", w.getName());
        assertEquals(15, w.getDmg());
    }

    @Test
    void stoneSword_canCraft_withEnoughMaterials() {
        player.addItem(new NormalStone(), 10);
        assertTrue(new StoneSword().canCraft(player));
    }

    @Test
    void stoneSword_cannotCraft_withoutMaterials() {
        assertFalse(new StoneSword().canCraft(player));
    }

    @Test
    void hardstoneSword_stats() {
        HardstoneSword w = new HardstoneSword();
        assertEquals("Hardstone Sword", w.getName());
        assertEquals(20, w.getDmg());
    }

    @Test
    void hardstoneSword_canCraft_withEnoughMaterials() {
        player.addItem(new NormalStone(), 5);
        player.addItem(new HardStone(), 10);
        assertTrue(new HardstoneSword().canCraft(player));
    }

    @Test
    void ironSword_stats() {
        IronSword w = new IronSword();
        assertEquals("Iron Sword", w.getName());
        assertEquals(30, w.getDmg());
    }

    @Test
    void ironSword_canCraft_withEnoughMaterials() {
        player.addItem(new NormalStone(), 5);
        player.addItem(new Iron(), 8);
        assertTrue(new IronSword().canCraft(player));
    }

    @Test
    void platinumSword_stats() {
        PlatinumSword w = new PlatinumSword();
        assertEquals("Platinum Sword", w.getName());
        assertEquals(45, w.getDmg());
    }

    @Test
    void platinumSword_canCraft_withEnoughMaterials() {
        player.addItem(new Iron(), 8);
        player.addItem(new Platinum(), 10);
        assertTrue(new PlatinumSword().canCraft(player));
    }

    @Test
    void mithrilSword_stats() {
        MithrilSword w = new MithrilSword();
        assertEquals("Mithril Sword", w.getName());
        assertEquals(70, w.getDmg());
    }

    @Test
    void mithrilSword_canCraft_withEnoughMaterials() {
        player.addItem(new Platinum(), 5);
        player.addItem(new Mithril(), 15);
        assertTrue(new MithrilSword().canCraft(player));
    }

    @Test
    void vibraniumSword_stats() {
        VibraniumSword w = new VibraniumSword();
        assertEquals("Vibranium Sword", w.getName());
        assertEquals(100, w.getDmg());
    }

    @Test
    void vibraniumSword_canCraft_withEnoughMaterials() {
        player.addItem(new Mithril(), 10);
        player.addItem(new Vibranium(), 15);
        assertTrue(new VibraniumSword().canCraft(player));
    }

    @Test
    void equippingNewWeapon_replacesOldWeapon() {
        WoodenSword old = new WoodenSword();
        IronSword newer = new IronSword();
        int base = player.getAttack();

        player.equipWeapon(old);
        player.equipWeapon(newer); // should remove old bonus first

        assertEquals(base + newer.getDmg(), player.getAttack());
    }

    @Test
    void weapon_isNotStackable() {
        assertFalse(new StoneSword().isStackable());
        assertFalse(new IronSword().isStackable());
        assertFalse(new VibraniumSword().isStackable());
    }

    @Test
    void weapon_cannotCraft_withoutGold() {
        player.setGold(0);
        player.addItem(new NormalStone(), 10);
        assertFalse(new StoneSword().canCraft(player)); // requires 10g
    }

    @Test
    void weapon_craftDeductsGold() {
        player.addItem(new NormalStone(), 10);
        player.setGold(100);
        StoneSword w = new StoneSword();
        w.craft(player);
        assertEquals(100 - w.getCraftingPrice(), player.getGold());
    }

    @Test
    void weapon_craftDeductsMaterials() {
        player.addItem(new NormalStone(), 10);
        StoneSword w = new StoneSword();
        w.craft(player);
        // NormalStone should be consumed
        assertTrue(player.getInventory().stream()
                .filter(ic -> ic.getItem().getName().equals("Normal Stone"))
                .allMatch(ic -> ic.getCount() == 0));
    }
}
