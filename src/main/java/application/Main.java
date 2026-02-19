package application;

import interfaces.Mineable;
import logic.base.BaseItem;
import logic.creatures.Player;
import logic.item.armor.HardstoneArmor;
import logic.item.potion.HealPotion;
import logic.item.potion.StrengthPotion;
import logic.pickaxe.Pickaxe;
import logic.stone.HardStone;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.List;

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

        // ---------------------------------------------------------
        // SCENARIO 6: MINING (Durability Test)
        // ---------------------------------------------------------
        System.out.println("\n--- 6. Mining Simulation ---");

        // 1. Create a WEAKER pickaxe to test durability logic
        // Normal Stone Pickaxe has Power: 2
        Pickaxe beginnerPickaxe = Pickaxe.createNormalStonePickaxe();

        // 2. Create a fresh Stone
        // Normal Stone has Durability: 5
        NormalStone rock = new NormalStone();

        System.out.println("Target: " + rock.getName() + " [HP: " + rock.getDurability() + "]");
        System.out.println("Tool: " + beginnerPickaxe.getName() + " [Power: " + beginnerPickaxe.getPower() + "]");

        // 3. Mining Loop: Keep hitting until it breaks
        int swings = 0;
        boolean broken = false;

        while (!broken) {
            swings++;
            System.out.print("Swing #" + swings + "... ");

            // Use the pickaxe
            List<BaseItem> loot = beginnerPickaxe.use(rock);

            // Check if we got loot (means it broke)
            if (!loot.isEmpty()) {
                System.out.println("CRACK! The stone broke!");
                for (BaseItem item : loot) {
                    System.out.println(" -> You obtained: " + item.getName());
                }
                broken = true;
            } else {
                System.out.println("Clang! (Stone HP left: " + rock.getDurability() + ")");
            }

            // Safety break to prevent infinite loops if logic fails
            if (swings > 10) {
                System.out.println("You got tired and stopped.");
                break;
            }
        }

        System.out.println("\n=== GAME OVER ===");
    }
}