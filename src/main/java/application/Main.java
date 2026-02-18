package application;

import logic.creatures.Player;
import logic.item.armor.HardstoneArmor;
import logic.item.potion.HealPotion;
import logic.item.potion.StrengthPotion;
import logic.stone.HardStone;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== GAME START ===");

        // 1. Initialize Player
        // HP: 100, Attack: 20, Defense: 5
        Player hero = new Player(100, 20, 5);
        hero.setGold(500); // Give starting money
        System.out.println("Hero Created -> HP: " + hero.getHealth() + " | Gold: " + hero.getGold());

        // ---------------------------------------------------------
        // SCENARIO 1: GATHERING & CRAFTING
        // ---------------------------------------------------------
        System.out.println("\n--- 1. Gathering Resources ---");

        // Simulate finding stones (Ingredients for HardstoneArmor)
        // Recipe requires: 5 NormalStone, 10 HardStone
        NormalStone ns = new NormalStone();
        HardStone hs = new HardStone();

        hero.addItem(new ItemCounter(ns, 10)); // Found 10 Normal Stones
        hero.addItem(new ItemCounter(hs, 12)); // Found 12 Hard Stones

        System.out.println("Gathered resources. Inventory check:");
        for(ItemCounter i : hero.getInventory()){
            System.out.println("- " + i.getItem().getName() + ": " + i.getCount());
        }

        System.out.println("\n--- 2. Crafting Armor ---");
        HardstoneArmor armor = new HardstoneArmor();

        if (armor.canCraft(hero)) {
            armor.craft(hero); // Deducts resources and 50 Gold
            hero.addItem(new ItemCounter(armor, 1)); // Put the crafted armor into inventory
            System.out.println("Crafting Successful: " + armor.getName());
        } else {
            System.out.println("Not enough resources to craft!");
        }

        System.out.println("Gold Remaining: " + hero.getGold()); // Should be 450
        System.out.println("Inventory after crafting:");
        for(ItemCounter i : hero.getInventory()){
            System.out.println("- " + i.getItem().getName() + ": " + i.getCount());
        }

        // ---------------------------------------------------------
        // SCENARIO 2: EQUIPPING
        // ---------------------------------------------------------
        System.out.println("\n--- 3. Equipping Gear ---");
        System.out.println("Stats BEFORE: Def = " + hero.getDefense() + ", MaxHP = " + hero.getMaxHealth());

        // Hardstone Armor gives: +10 Def, +15 MaxHP
        armor.equip(hero);

        System.out.println("Stats AFTER : Def = " + hero.getDefense() + ", MaxHP = " + hero.getMaxHealth());

        // ---------------------------------------------------------
        // SCENARIO 3: BATTLE & DAMAGE
        // ---------------------------------------------------------
        System.out.println("\n--- 4. Battle Simulation ---");
        // Simulate taking damage from a monster
        int monsterDamage = 50;
        System.out.println("Monster hits for " + monsterDamage + " damage!");

        hero.takeDamage(monsterDamage); // Logic handles def reduction: 50 - 15 (Def) = 35 dmg
        System.out.println("Hero HP remaining: " + hero.getHealth() + "/" + hero.getMaxHealth());

        // ---------------------------------------------------------
        // SCENARIO 4: POTIONS
        // ---------------------------------------------------------
        System.out.println("\n--- 5. Using Potions ---");

        // Use Heal Potion (Restores 20% of Max HP)
        HealPotion hp = new HealPotion();
        hp.consume(hero);
        System.out.println("Used Heal Potion. Current HP: " + hero.getHealth());

        // Use Strength Potion (Boosts Attack by 50%)
        System.out.println("Attack BEFORE potion: " + hero.getStrength());
        StrengthPotion sp = new StrengthPotion();
        sp.consume(hero);
        System.out.println("Used Strength Potion. Attack AFTER: " + hero.getStrength());

        System.out.println("\n=== GAME OVER ===");
    }
}