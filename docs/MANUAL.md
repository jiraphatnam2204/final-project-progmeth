# Tanjiro: The Swordsmith — Game Manual

## Table of Contents

1. [Overview](#1-overview)
2. [Getting Started](#2-getting-started)
3. [Controls](#3-controls)
4. [The World Map](#4-the-world-map)
5. [Mining](#5-mining)
6. [World Combat](#6-world-combat)
7. [The Shop](#7-the-shop)
8. [Crafting](#8-crafting)
9. [Boss Battles](#9-boss-battles)
10. [Items & Equipment](#10-items--equipment)
11. [Damage Formula](#11-damage-formula)
12. [Tips & Strategy](#12-tips--strategy)

---

## 1. Overview

**Tanjiro: The Swordsmith** is a 2D top-down action RPG inspired by *Demon Slayer: Kimetsu no Yaiba*.

Your goal is to survive in the world, mine rare ores, craft powerful weapons and armor, and defeat three demon bosses in sequence. The game ends in **victory** when all three bosses are slain, or in **defeat** if the player's HP reaches zero at any point.

**Core loop:**

```
Mine Ores → Earn Gold from Monsters → Shop / Craft → Enter Boss Room → Repeat
```

---

## 2. Getting Started

When you launch the game, you are taken to the **Main Menu**. Press **Start** to begin.

You start with:
- A **Wooden Pickaxe** (power: 2)
- No weapon or armor equipped
- 0 Gold

You are placed at the center of the world map and must begin mining and fighting monsters to progress.

---

## 3. Controls

| Input | Action |
|---|---|
| `W` / `↑` | Move up |
| `S` / `↓` | Move down |
| `A` / `←` | Move left |
| `D` / `→` | Move right |
| **Left Click** (hold) | Attack monsters in melee range |
| **Right Click** (hold) | Mine the tile you are facing |
| `E` | Enter a nearby building |

> Diagonal movement speed is automatically normalized so you cannot move faster diagonally.

---

## 4. The World Map

The map is a **20×15 tile grid**. It contains:

| Element | Description |
|---|---|
| Ground / Grass | Walkable terrain |
| Ore Deposits | Mineable rocks scattered across the map |
| Monsters | Roam freely; aggro when you get close |
| **Shop** | Top-left area — buy potions and pickaxes |
| **Crafting Station** | Top-right area — craft weapons and armor |
| **Boss Door** | Bottom-center — enter to start boss battles |

### Respawning
- Destroyed ore deposits **respawn** within 1–2 seconds at a random location.
- Defeated monsters **respawn** within 1–3 seconds at a random location.

> Ores and monsters never run out — the world is always replenished.

---

## 5. Mining

Hold **Right Click** while facing an ore tile to mine it. Each click reduces the ore's durability by your pickaxe's power. When durability reaches zero, the ore breaks and drops materials into your inventory.

### Ore Stats

| Ore | Durability | Drops |
|---|---|---|
| Normal Stone | 5 | 1× Normal Stone |
| Hard Stone | 15 | 2× Hard Stone |
| Iron | 36 | 3× Iron |
| Platinum | 80 | 3× Platinum |
| Mithril | 120 | 3× Mithril |
| Vibranium | 210 | 3× Vibranium |

### Pickaxe Power

A higher-power pickaxe reduces durability faster. For example, a Wooden Pickaxe (power 2) takes **18 hits** to break an Iron deposit (durability 36), while an Iron Pickaxe (power 12) breaks it in **3 hits**.

| Pickaxe | Power |
|---|---|
| Wooden Pickaxe | 2 |
| Normal Stone Pickaxe | 3 |
| Hard Stone Pickaxe | 5 |
| Iron Pickaxe | 12 |
| Platinum Pickaxe | 27 |
| Mithril Pickaxe | 45 |
| Vibranium Pickaxe | 100 |

> Vibranium (durability 210) requires at least a **Platinum Pickaxe** (power 27) to mine in a reasonable number of hits. A Wooden Pickaxe would take 105 hits.

---

## 6. World Combat

Hold **Left Click** to attack monsters within melee range. There is a **600 ms cooldown** between attacks.

Monsters that spot you (within ~5 tiles) will chase and attack you. When not aggroed, they wander randomly. After taking damage you have a brief **invincibility window** so you cannot be hit repeatedly.

### Monsters

| Monster | HP | ATK | DEF | Gold Drop |
|---|---|---|---|---|
| Easy Monster | 40 | 12 | 1 | 20g |
| Medium Monster | 90 | 22 | 4 | 40g |
| Hard Monster | 160 | 32 | 8 | 80g |

> Killed monsters drop gold directly into your wallet and respawn shortly after.

---

## 7. The Shop

Walk near the **Shop building** (top-left) and press `E` to enter. The shop sells potions for healing and pickaxes for faster mining. Weapons and armor are **not sold here** — they must be crafted.

### Potions

| Item | Effect | Price |
|---|---|---|
| Small Potion | Restores 40 HP | 50g |
| Medium Potion | Restores 100 HP | 100g |
| Big Potion | Restores 200 HP | 200g |

### Pickaxes

| Pickaxe | Power | Price |
|---|---|---|
| Wooden Pick | 2 | 5g |
| Normal Pick | 3 | 10g |
| Hardstone Pick | 5 | 50g |
| Iron Pickaxe | 12 | 100g |
| Platinum Pick | 27 | 160g |
| Mithril Pick | 45 | 230g |
| Vibranium Pick | 100 | 310g |

> Buying a higher-tier pickaxe replaces your current one immediately.

---

## 8. Crafting

Walk near the **Crafting Station** (top-right) and press `E` to open it. Crafting requires both **materials** from mining and **gold** as a crafting fee.

Crafted items go to your inventory. To gain their stat bonuses, you must **equip** them from the inventory screen.

### Weapons

Equipping a weapon increases your **ATK** stat by the weapon's damage value.

| Weapon | ATK Bonus | Crafting Cost | Materials Required |
|---|---|---|---|
| Stone Sword | +15 | 10g | 10× Normal Stone |
| Hardstone Sword | +20 | 50g | 5× Normal Stone, 10× Hard Stone |
| Iron Sword | +30 | 100g | 5× Normal Stone, 8× Iron |
| Platinum Sword | +45 | 160g | 8× Iron, 10× Platinum |
| Mithril Sword | +70 | 230g | 5× Platinum, 15× Mithril |
| Vibranium Sword | +100 | 310g | 10× Mithril, 15× Vibranium |

### Armor

Equipping armor increases your **DEF** and **Max HP** stats.

| Armor | DEF Bonus | HP Bonus | Crafting Cost | Materials Required |
|---|---|---|---|---|
| Stone Armor | +5 | +10 | 10g | 10× Normal Stone |
| Hardstone Armor | +10 | +15 | 50g | 5× Normal Stone, 10× Hard Stone |
| Iron Armor | +15 | +40 | 100g | 5× Normal Stone, 8× Iron |
| Platinum Armor | +25 | +60 | 160g | 8× Iron, 10× Platinum |
| Mithril Armor | +40 | +100 | 230g | 5× Platinum, 15× Mithril |
| Vibranium Armor | +55 | +150 | 310g | 10× Mithril, 15× Vibranium |

> You can only equip **one weapon** and **one armor** at a time. Equipping a new piece automatically unequips the previous one and returns its stat bonuses.

---

## 9. Boss Battles

Walk near the **Boss Door** (bottom-center) and press `E` to enter the boss room. Boss fights are **turn-based** — you and the boss alternate actions.

You must defeat **3 bosses in sequence** without leaving the room. Your HP carries over between fights.

### Bosses

| Boss | HP | ATK | DEF | Gold Reward |
|---|---|---|---|---|
| Akaza | 500 | 60 | 15 | 900g |
| Kokushibo | 900 | 80 | 22 | 2,100g |
| Muzan | 1,600 | 100 | 38 | 4,500g |

> Boss gold rewards are 3× their base drop value.

### Your Turn — Available Actions

#### Attack
A normal attack dealing `ATK − Boss DEF` damage (minimum 1).

#### Skills
Each skill has an individual cooldown measured in turns.

| Skill | Effect | Cooldown |
|---|---|---|
| ⚡ Kagura Dance | Deal **2× ATK** damage to the boss | 3 turns |
| 🛡 Dead Calm | Reduce the next incoming hit by **50%** | 2 turns |
| 💢 Constant Flux | Deal **3 rapid hits** (3× ATK total), but your DEF is halved next turn | 4 turns |
| 🩸 Water Wheel | Deal normal damage and **heal 30%** of the damage dealt | 5 turns |

#### Defend
Temporarily doubles your DEF for the upcoming enemy turn. Resets after the enemy attacks.

#### Heal
Opens your potion inventory. Select a potion to consume it immediately.

#### Rest
Recover `max(5, MaxHP / 10)` HP without using a potion. Counts as your turn.

### Enemy Turn
After every player action the boss attacks. Bosses have a **20% chance to land a critical hit** that deals **1.6× damage**.

Damage you receive: `max(0, Boss ATK − Your DEF)`.

### Winning & Losing
- Defeat all 3 bosses → **Victory screen**
- Player HP reaches 0 at any point → **Game Over screen**

---

## 10. Items & Equipment

### Potions

| Item | Heal Amount |
|---|---|
| Small Health Potion | 40 HP |
| Medium Health Potion | 100 HP |
| Big Health Potion | 200 HP |

Potions can be used both during **world exploration** (from inventory) and during **boss battles** (via the Heal action).

### Equipment Slots

| Slot | Effect |
|---|---|
| Weapon | Increases ATK |
| Armor | Increases DEF and Max HP |

You can view and change equipment from the **Inventory** screen.

---

## 11. Damage Formula

All damage in the game follows this formula:

```
Damage Dealt = max(0, Attacker ATK − Defender DEF)
```

If the attacker's ATK is less than or equal to the defender's DEF, the attack deals **0 damage**. This makes DEF very important against high-ATK bosses.

**Boss Critical Hit:**
```
Critical Damage = Boss ATK × 1.6   (20% chance)
Final Damage = max(0, Critical Damage − Your DEF)
```

---

## 12. Tips & Strategy

**Early game:**
- Start by mining **Normal Stone** with your Wooden Pickaxe and craft a **Stone Sword** and **Stone Armor** as soon as possible.
- Kill Easy Monsters to earn early gold for a better pickaxe.

**Mid game:**
- Upgrade to an **Iron Pickaxe** before attempting to mine Platinum efficiently.
- Craft **Iron** or **Platinum** gear before entering the boss room.

**Boss battles:**
- Use **Dead Calm** before a boss attack when you anticipate high damage.
- **Constant Flux** deals the most total damage but leaves you vulnerable — use it when your armor can absorb the counter-attack.
- **Water Wheel** is sustainable — use it to stay healed without burning potions.
- Stock up on **Medium or Big Potions** before entering the boss room.
- Use **Defend** instead of a skill when all skills are on cooldown.

**Gear recommendation for each boss:**

| Boss | Recommended Minimum Gear |
|---|---|
| Akaza | Iron Sword + Iron Armor |
| Kokushibo | Platinum Sword + Platinum Armor |
| Muzan | Vibranium Sword + Vibranium Armor |
