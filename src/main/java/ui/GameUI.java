package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import logic.base.BaseArmor;
import logic.base.BaseItem;
import logic.base.BaseWeapon;
import logic.creatures.EasyMonster;
import logic.creatures.HardMonster;
import logic.creatures.MediumMonster;
import logic.creatures.Monster;
import logic.creatures.Player;
import logic.item.armor.*;
import logic.item.potion.*;
import logic.item.weapon.*;
import logic.pickaxe.Pickaxe;
import logic.stone.*;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * GameUI is the main class that builds and manages the entire game interface.
 *
 * Think of it like a TV remote control for your game:
 * it holds references to all the panels and updates them when things change.
 */
public class GameUI {

    // ── Core game state ──────────────────────────────────────────────────────
    private final Player player;
    private Monster currentMonster;
    private Pickaxe currentPickaxe;
    private baseStone currentStone;
    private BaseWeapon equippedWeapon = null;
    private BaseArmor equippedArmor = null;

    // ── UI panels ────────────────────────────────────────────────────────────
    private BorderPane root;
    private TextArea logArea;          // Game log – bottom of screen
    private VBox statsPanel;           // Left sidebar: player stats
    private TabPane tabPane;           // Right area: tabs for each game system

    // ── Stat labels (updated after every action) ─────────────────────────────
    private Label lblHP, lblAtk, lblDef, lblGold, lblSpd, lblLuck;
    private ProgressBar hpBar;

    public GameUI() {
        // Create player: 100 HP, 20 ATK, 5 DEF
        player = new Player(100, 20, 5);
        player.setGold(500);

        buildUI();
        log("⚔️  Welcome, Hero! Your adventure begins...");
        log("💰 You start with " + player.getGold() + " gold.");
    }

    /** Returns the root node that goes into the JavaFX Scene. */
    public BorderPane getRoot() {
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ═══════════════════════════════════════════════════════════════════════

    private void buildUI() {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // ── Top bar ──────────────────────────────────────────────────────────
        HBox topBar = buildTopBar();
        root.setTop(topBar);

        // ── Left: player stats sidebar ───────────────────────────────────────
        statsPanel = buildStatsPanel();
        root.setLeft(statsPanel);

        // ── Center: tabbed panels ────────────────────────────────────────────
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
            buildBattleTab(),
            buildInventoryTab(),
            buildCraftingTab(),
            buildMiningTab(),
            buildShopTab()
        );
        root.setCenter(tabPane);

        // ── Bottom: game log ──────────────────────────────────────────────────
        VBox logBox = buildLogPanel();
        root.setBottom(logBox);
    }

    // ── Top Bar ──────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 20, 10, 20));

        Label title = new Label("⚔️  Adventure RPG");
        title.getStyleClass().add("game-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label subtitle = new Label("A Java RPG Project");
        subtitle.getStyleClass().add("game-subtitle");

        bar.getChildren().addAll(title, spacer, subtitle);
        return bar;
    }

    // ── Stats Sidebar ─────────────────────────────────────────────────────────

    private VBox buildStatsPanel() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("stats-panel");
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(175);

        Label heading = new Label("🧙 Hero Stats");
        heading.getStyleClass().add("section-heading");

        // HP progress bar + label
        hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(145);
        hpBar.getStyleClass().add("hp-bar");

        lblHP   = statLabel("❤️  HP",   player.getHealth() + "/" + player.getMaxHealth());
        lblAtk  = statLabel("⚔️  ATK",  String.valueOf(player.getAttack()));
        lblDef  = statLabel("🛡️  DEF",  String.valueOf(player.getDefense()));
        lblGold = statLabel("💰 Gold",  String.valueOf(player.getGold()));
        lblSpd  = statLabel("💨 SPD",   "0");
        lblLuck = statLabel("🍀 Luck",  String.valueOf(player.getLuck()));

        Separator sep = new Separator();

        Label equipHeading = new Label("🎽 Equipped");
        equipHeading.getStyleClass().add("section-heading");

        Label weaponLbl = new Label("Weapon: None");
        weaponLbl.setId("equip-weapon");
        weaponLbl.getStyleClass().add("equip-label");

        Label armorLbl = new Label("Armor:  None");
        armorLbl.setId("equip-armor");
        armorLbl.getStyleClass().add("equip-label");

        panel.getChildren().addAll(
            heading, hpBar,
            lblHP, lblAtk, lblDef, lblGold, lblSpd, lblLuck,
            sep, equipHeading, weaponLbl, armorLbl
        );
        return panel;
    }

    /** Creates a two-line stat label. */
    private Label statLabel(String key, String value) {
        Label lbl = new Label(key + ": " + value);
        lbl.getStyleClass().add("stat-label");
        return lbl;
    }

    /** Refresh all stat labels from current player state. */
    private void refreshStats() {
        lblHP.setText("❤️  HP: "   + player.getHealth() + "/" + player.getMaxHealth());
        lblAtk.setText("⚔️  ATK: " + player.getAttack());
        lblDef.setText("🛡️  DEF: " + player.getDefense());
        lblGold.setText("💰 Gold: " + player.getGold());
        lblLuck.setText("🍀 Luck: " + player.getLuck());

        double hpFraction = (double) player.getHealth() / player.getMaxHealth();
        hpBar.setProgress(Math.max(0, hpFraction));

        // Update HP bar color based on health
        hpBar.getStyleClass().removeAll("hp-low", "hp-mid");
        if (hpFraction < 0.3)      hpBar.getStyleClass().add("hp-low");
        else if (hpFraction < 0.6) hpBar.getStyleClass().add("hp-mid");

        // Update equip labels
        Label wLbl = (Label) statsPanel.lookup("#equip-weapon");
        Label aLbl = (Label) statsPanel.lookup("#equip-armor");
        if (wLbl != null) wLbl.setText("Weapon: " + (equippedWeapon == null ? "None" : equippedWeapon.getName()));
        if (aLbl != null) aLbl.setText("Armor:  " + (equippedArmor  == null ? "None" : equippedArmor.getName()));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 1 – BATTLE
    // ═══════════════════════════════════════════════════════════════════════

    private Tab buildBattleTab() {
        Tab tab = new Tab("⚔️ Battle");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("tab-content");

        // Monster selector
        Label chooseLabel = new Label("Choose your enemy:");
        chooseLabel.getStyleClass().add("section-heading");

        ComboBox<String> monsterPicker = new ComboBox<>();
        monsterPicker.getItems().addAll("Easy Monster (50 HP)", "Medium Monster (100 HP)", "Hard Monster (200 HP)");
        monsterPicker.setValue("Easy Monster (50 HP)");
        monsterPicker.setPrefWidth(220);

        Button spawnBtn = new Button("⚡ Spawn Monster");
        spawnBtn.getStyleClass().add("btn-primary");

        // Monster HP display
        ProgressBar monsterHP = new ProgressBar(0);
        monsterHP.setPrefWidth(300);
        monsterHP.getStyleClass().add("monster-bar");

        Label monsterLabel = new Label("No monster spawned.");
        monsterLabel.getStyleClass().add("monster-label");

        // Action buttons
        HBox actions = new HBox(10);
        Button attackBtn = new Button("🗡️ Attack");
        attackBtn.getStyleClass().add("btn-danger");
        attackBtn.setDisable(true);

        Button fleeBtn = new Button("🏃 Flee");
        fleeBtn.getStyleClass().add("btn-warning");
        fleeBtn.setDisable(true);

        // Potion buttons
        Button healSmall = new Button("🧪 Small Potion (+10)");
        Button healMed   = new Button("🧪 Med Potion (+50)");
        Button healBig   = new Button("🧪 Big Potion (+100)");
        healSmall.getStyleClass().add("btn-success");
        healMed.getStyleClass().add("btn-success");
        healBig.getStyleClass().add("btn-success");

        actions.getChildren().addAll(attackBtn, fleeBtn);
        HBox potionRow = new HBox(8, healSmall, healMed, healBig);

        // ── Button logic ──────────────────────────────────────────────────────

        spawnBtn.setOnAction(e -> {
            String choice = monsterPicker.getValue();
            if (choice.startsWith("Easy"))        currentMonster = new EasyMonster();
            else if (choice.startsWith("Medium")) currentMonster = new MediumMonster();
            else                                  currentMonster = new HardMonster();

            monsterLabel.setText("👹 " + choice + "  HP: " + currentMonster.getHealthPoint() + "/" + currentMonster.getMaxHealthPoint());
            monsterHP.setProgress(1.0);
            attackBtn.setDisable(false);
            fleeBtn.setDisable(false);
            log("👹 A " + choice + " appears!");
        });

        attackBtn.setOnAction(e -> {
            if (currentMonster == null || !currentMonster.isAlive()) return;

            // Player attacks monster
            int dmg = player.getAttack() + (equippedWeapon != null ? equippedWeapon.getDmg() : 0);
            currentMonster.takeDamage(dmg);
            log("🗡️ You hit for " + dmg + " damage! Monster HP: " + currentMonster.getHealthPoint());

            if (!currentMonster.isAlive()) {
                int gold = currentMonster.dropMoney();
                player.setGold(player.getGold() + gold);
                log("💀 Monster defeated! You earned " + gold + " gold!");
                monsterLabel.setText("💀 Monster defeated!");
                monsterHP.setProgress(0);
                attackBtn.setDisable(true);
                fleeBtn.setDisable(true);
                refreshStats();
                return;
            }

            // Monster attacks player
            currentMonster.attack(player);
            double mFrac = (double) currentMonster.getHealthPoint() / currentMonster.getMaxHealthPoint();
            monsterHP.setProgress(Math.max(0, mFrac));
            monsterLabel.setText("👹 Monster HP: " + currentMonster.getHealthPoint() + "/" + currentMonster.getMaxHealthPoint());
            log("👹 Monster hits you! Your HP: " + player.getHealth() + "/" + player.getMaxHealth());

            if (!player.isAlive()) {
                log("💀 You have been defeated! Respawning with 50% HP...");
                player.setHealth(player.getMaxHealth() / 2);
                attackBtn.setDisable(true);
                fleeBtn.setDisable(true);
            }
            refreshStats();
        });

        fleeBtn.setOnAction(e -> {
            log("🏃 You fled from battle!");
            currentMonster = null;
            monsterLabel.setText("No monster. You fled.");
            monsterHP.setProgress(0);
            attackBtn.setDisable(true);
            fleeBtn.setDisable(true);
        });

        healSmall.setOnAction(e -> { new SmallHealthPotion().consume(player); log("🧪 Used Small Potion! HP: " + player.getHealth()); refreshStats(); });
        healMed.setOnAction(e ->   { new MediumHealthPotion().consume(player); log("🧪 Used Medium Potion! HP: " + player.getHealth()); refreshStats(); });
        healBig.setOnAction(e ->   { new BigHealthPotion().consume(player); log("🧪 Used Big Potion! HP: " + player.getHealth()); refreshStats(); });

        content.getChildren().addAll(
            chooseLabel, monsterPicker, spawnBtn,
            new Separator(), monsterLabel, monsterHP,
            new Separator(), actions,
            new Label("Potions:"), potionRow
        );

        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 2 – INVENTORY
    // ═══════════════════════════════════════════════════════════════════════

    private Tab buildInventoryTab() {
        Tab tab = new Tab("🎒 Inventory");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("tab-content");

        Label heading = new Label("Your Inventory");
        heading.getStyleClass().add("section-heading");

        // Inventory list
        ListView<String> inventoryList = new ListView<>();
        inventoryList.setPrefHeight(220);
        inventoryList.setId("inventory-list");

        Button refreshBtn = new Button("🔄 Refresh Inventory");
        refreshBtn.getStyleClass().add("btn-primary");

        // Equip / unequip buttons
        Button equipBtn   = new Button("✅ Equip Selected");
        Button unequipBtn = new Button("❌ Unequip");
        equipBtn.getStyleClass().add("btn-success");
        unequipBtn.getStyleClass().add("btn-warning");

        HBox equipRow = new HBox(10, equipBtn, unequipBtn);

        // Add starter items button for demo
        Button addStarterBtn = new Button("🎁 Add Starter Items");
        addStarterBtn.getStyleClass().add("btn-primary");

        refreshBtn.setOnAction(e -> refreshInventoryList(inventoryList));

        addStarterBtn.setOnAction(e -> {
            player.addItem(new NormalStone(), 20);
            player.addItem(new HardStone(), 15);
            player.addItem(new StoneSword(), 1);
            player.addItem(new StoneArmor(), 1);
            refreshInventoryList(inventoryList);
            log("🎁 Starter items added to inventory!");
            refreshStats();
        });

        equipBtn.setOnAction(e -> {
            String selected = inventoryList.getSelectionModel().getSelectedItem();
            if (selected == null) { log("⚠️ Select an item to equip!"); return; }

            // Try to find the item in inventory and equip
            for (ItemCounter ic : player.getInventory()) {
                if (selected.contains(ic.getItem().getName())) {
                    if (ic.getItem() instanceof BaseWeapon w) {
                        if (equippedWeapon != null) {
                            log("ℹ️ Unequipped " + equippedWeapon.getName());
                        }
                        equippedWeapon = w;
                        log("✅ Equipped weapon: " + w.getName() + " (+" + w.getDmg() + " dmg)");
                    } else if (ic.getItem() instanceof BaseArmor a) {
                        if (equippedArmor != null) {
                            equippedArmor.unequip(player);
                            log("ℹ️ Unequipped " + equippedArmor.getName());
                        }
                        equippedArmor = a;
                        a.equip(player);
                        log("✅ Equipped armor: " + a.getName() + " (+" + a.getDef() + " def, +" + a.getHp() + " HP)");
                    } else {
                        log("⚠️ That item can't be equipped.");
                    }
                    break;
                }
            }
            refreshStats();
            refreshInventoryList(inventoryList);
        });

        unequipBtn.setOnAction(e -> {
            if (equippedArmor != null) {
                equippedArmor.unequip(player);
                log("❌ Unequipped armor: " + equippedArmor.getName());
                equippedArmor = null;
            }
            equippedWeapon = null;
            log("❌ All gear unequipped.");
            refreshStats();
        });

        refreshInventoryList(inventoryList);

        content.getChildren().addAll(heading, addStarterBtn, refreshBtn, inventoryList, equipRow);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private void refreshInventoryList(ListView<String> list) {
        list.getItems().clear();
        if (player.getInventory().isEmpty()) {
            list.getItems().add("(empty)");
        } else {
            for (ItemCounter ic : player.getInventory()) {
                String type = "";
                if (ic.getItem() instanceof BaseWeapon)  type = " [Weapon]";
                else if (ic.getItem() instanceof BaseArmor) type = " [Armor]";
                list.getItems().add(ic.getItem().getName() + type + "  ×" + ic.getCount());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 3 – CRAFTING
    // ═══════════════════════════════════════════════════════════════════════

    private Tab buildCraftingTab() {
        Tab tab = new Tab("🔨 Crafting");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("tab-content");

        Label heading = new Label("⚒️ Crafting Table");
        heading.getStyleClass().add("section-heading");

        // Weapon crafting
        Label wHeading = new Label("🗡️ Weapons");
        wHeading.getStyleClass().add("sub-heading");

        GridPane weaponGrid = new GridPane();
        weaponGrid.setHgap(8); weaponGrid.setVgap(8);

        List<BaseWeapon> weapons = List.of(
            new WoodenSword(), new StoneSword(), new HardstoneSword(),
            new IronSword(), new PlatinumSword(), new MithrilSword(), new VibraniumSword()
        );

        int col = 0, row = 0;
        for (BaseWeapon w : weapons) {
            Button btn = new Button(w.getName() + "\n⚔️" + w.getDmg() + " DMG  💰" + w.getCraftingPrice() + "g");
            btn.getStyleClass().add("craft-btn");
            btn.setOnAction(e -> {
                if (w.canCraft(player)) {
                    w.craft(player);
                    player.addItem(w, 1);
                    log("🔨 Crafted: " + w.getName() + "!");
                } else {
                    log("❌ Can't craft " + w.getName() + " – check materials & gold.");
                    showRecipe(w.getRecipe(), w.getCraftingPrice());
                }
                refreshStats();
            });
            weaponGrid.add(btn, col, row);
            col++;
            if (col > 2) { col = 0; row++; }
        }

        // Armor crafting
        Label aHeading = new Label("🛡️ Armor");
        aHeading.getStyleClass().add("sub-heading");

        GridPane armorGrid = new GridPane();
        armorGrid.setHgap(8); armorGrid.setVgap(8);

        List<BaseArmor> armors = List.of(
            new StoneArmor(), new HardstoneArmor(), new IronArmor(),
            new PlatinumArmor(), new MithrilArmor(), new VibraniumArmor()
        );

        col = 0; row = 0;
        for (BaseArmor a : armors) {
            Button btn = new Button(a.getName() + "\n🛡️" + a.getDef() + " DEF  💰" + a.getCraftingPrice() + "g");
            btn.getStyleClass().add("craft-btn");
            btn.setOnAction(e -> {
                if (a.canCraft(player)) {
                    a.craft(player);
                    player.addItem(a, 1);
                    log("🔨 Crafted: " + a.getName() + "!");
                } else {
                    log("❌ Can't craft " + a.getName() + " – check materials & gold.");
                    showRecipe(a.getRecipe(), a.getCraftingPrice());
                }
                refreshStats();
            });
            armorGrid.add(btn, col, row);
            col++;
            if (col > 2) { col = 0; row++; }
        }

        content.getChildren().addAll(heading, wHeading, weaponGrid, new Separator(), aHeading, armorGrid);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private void showRecipe(ArrayList<ItemCounter> recipe, int price) {
        if (recipe == null) { log("  📋 No recipe available."); return; }
        log("  📋 Recipe requires:");
        for (ItemCounter ic : recipe) {
            log("     - " + ic.getItem().getName() + " ×" + ic.getCount());
        }
        log("     + 💰 " + price + " gold");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 4 – MINING
    // ═══════════════════════════════════════════════════════════════════════

    private Tab buildMiningTab() {
        Tab tab = new Tab("⛏️ Mining");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("tab-content");

        Label heading = new Label("⛏️ Mining Ground");
        heading.getStyleClass().add("section-heading");

        // Stone picker
        Label stoneLabel = new Label("Select a stone to mine:");
        ComboBox<String> stonePicker = new ComboBox<>();
        stonePicker.getItems().addAll("Normal Stone (5 HP)", "Hard Stone (15 HP)", "Iron (35 HP)", "Platinum (80 HP)", "Mithril (135 HP)", "Vibranium (200 HP)");
        stonePicker.setValue("Normal Stone (5 HP)");

        // Pickaxe picker
        Label pickaxeLabel = new Label("Select your pickaxe:");
        ComboBox<String> pickaxePicker = new ComboBox<>();
        pickaxePicker.getItems().addAll("Normal Stone Pickaxe (Power 2)", "Hard Stone Pickaxe (Power 5)", "Iron Pickaxe (Power 12)", "Platinum Pickaxe (Power 27)", "Mithril Pickaxe (Power 45)", "Vibranium Pickaxe (Power 100)");
        pickaxePicker.setValue("Normal Stone Pickaxe (Power 2)");

        Button startMineBtn = new Button("🪨 Start Mining");
        startMineBtn.getStyleClass().add("btn-primary");

        ProgressBar stoneBar = new ProgressBar(0);
        stoneBar.setPrefWidth(300);
        stoneBar.getStyleClass().add("stone-bar");

        Label stoneDurLabel = new Label("No stone selected.");
        stoneDurLabel.getStyleClass().add("monster-label");

        Button swingBtn = new Button("⛏️ Swing Pickaxe");
        swingBtn.getStyleClass().add("btn-danger");
        swingBtn.setDisable(true);

        // Logic
        startMineBtn.setOnAction(e -> {
            String sc = stonePicker.getValue();
            String pc = pickaxePicker.getValue();

            if (sc.startsWith("Normal"))     currentStone = new NormalStone();
            else if (sc.startsWith("Hard"))  currentStone = new HardStone();
            else if (sc.startsWith("Iron"))  currentStone = new Iron();
            else if (sc.startsWith("Plat"))  currentStone = new Platinum();
            else if (sc.startsWith("Mith"))  currentStone = new Mithril();
            else                             currentStone = new Vibranium();

            if (pc.startsWith("Normal"))     currentPickaxe = Pickaxe.createNormalStonePickaxe();
            else if (pc.startsWith("Hard"))  currentPickaxe = Pickaxe.createHardStonePickaxe();
            else if (pc.startsWith("Iron"))  currentPickaxe = Pickaxe.createIronPickaxe();
            else if (pc.startsWith("Plat"))  currentPickaxe = Pickaxe.createPlatinumPickaxe();
            else if (pc.startsWith("Mith"))  currentPickaxe = Pickaxe.createMithrilPickaxe();
            else                             currentPickaxe = Pickaxe.createVibraniumPickaxe();

            stoneBar.setProgress(1.0);
            stoneDurLabel.setText("🪨 " + currentStone.getName() + "  Durability: " + currentStone.getDurability() + "/" + currentStone.getMaxDurability());
            swingBtn.setDisable(false);
            log("⛏️ You approach a " + currentStone.getName() + " with a " + currentPickaxe.getName());
        });

        swingBtn.setOnAction(e -> {
            if (currentStone == null || currentStone.isBroken()) return;

            List<BaseItem> loot = currentPickaxe.use(currentStone, player);

            if (!loot.isEmpty()) {
                for (BaseItem item : loot) {
                    log("✨ You obtained: " + item.getName());
                }
                log("💥 The stone broke!");
                stoneDurLabel.setText("💥 Stone destroyed! Items collected.");
                stoneBar.setProgress(0);
                swingBtn.setDisable(true);
            } else {
                double frac = (double) currentStone.getDurability() / currentStone.getMaxDurability();
                stoneBar.setProgress(Math.max(0, frac));
                stoneDurLabel.setText("🪨 " + currentStone.getName() + "  Durability: " + currentStone.getDurability() + "/" + currentStone.getMaxDurability());
                log("⛏️ Clang! Durability left: " + currentStone.getDurability());
            }

            // Update inventory tab list
            refreshInventoryIfOpen();
        });

        content.getChildren().addAll(
            heading,
            stoneLabel, stonePicker,
            pickaxeLabel, pickaxePicker,
            startMineBtn, new Separator(),
            stoneDurLabel, stoneBar, swingBtn
        );

        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 5 – SHOP
    // ═══════════════════════════════════════════════════════════════════════

    private Tab buildShopTab() {
        Tab tab = new Tab("🏪 Shop");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("tab-content");

        Label heading = new Label("🏪 General Store");
        heading.getStyleClass().add("section-heading");
        Label goldLbl = new Label("Your gold: " + player.getGold());
        goldLbl.getStyleClass().add("sub-heading");

        // Shop items (name, price, action)
        record ShopItem(String name, int price, Runnable action) {}
        List<ShopItem> items = List.of(
            new ShopItem("Small Health Potion (+10 HP)",  20,  () -> { new SmallHealthPotion().consume(player);  log("🧪 Used Small Potion! HP now: " + player.getHealth()); }),
            new ShopItem("Medium Health Potion (+50 HP)", 75,  () -> { new MediumHealthPotion().consume(player); log("🧪 Used Medium Potion! HP now: " + player.getHealth()); }),
            new ShopItem("Big Health Potion (+100 HP)",   150, () -> { new BigHealthPotion().consume(player);    log("🧪 Used Big Potion! HP now: " + player.getHealth()); }),
            new ShopItem("Wooden Sword (5 DMG)",          50,  () -> { player.addItem(new WoodenSword(), 1); log("🗡️ Bought Wooden Sword!"); }),
            new ShopItem("Stone Sword (15 DMG)",          100, () -> { player.addItem(new StoneSword(), 1); log("🗡️ Bought Stone Sword!"); }),
            new ShopItem("Stone Armor (+5 DEF)",          80,  () -> { player.addItem(new StoneArmor(), 1); log("🛡️ Bought Stone Armor!"); }),
            new ShopItem("50× Normal Stone",              30,  () -> { player.addItem(new NormalStone(), 50); log("🪨 Bought 50× Normal Stone!"); }),
            new ShopItem("30× Hard Stone",                60,  () -> { player.addItem(new HardStone(), 30); log("🪨 Bought 30× Hard Stone!"); })
        );

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        int c = 0, r = 0;
        for (ShopItem si : items) {
            Button btn = new Button(si.name() + "\n💰 " + si.price() + "g");
            btn.getStyleClass().add("shop-btn");
            btn.setOnAction(e -> {
                if (player.getGold() >= si.price()) {
                    player.setGold(player.getGold() - si.price());
                    si.action().run();
                    goldLbl.setText("Your gold: " + player.getGold());
                    refreshStats();
                    refreshInventoryIfOpen();
                } else {
                    log("❌ Not enough gold! Need " + si.price() + "g, have " + player.getGold() + "g.");
                }
            });
            grid.add(btn, c, r);
            c++;
            if (c > 2) { c = 0; r++; }
        }

        content.getChildren().addAll(heading, goldLbl, new Separator(), grid);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LOG PANEL
    // ═══════════════════════════════════════════════════════════════════════

    private VBox buildLogPanel() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8, 10, 8, 10));
        box.getStyleClass().add("log-panel");

        Label logHeading = new Label("📜 Game Log");
        logHeading.getStyleClass().add("log-heading");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(120);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("log-area");

        Button clearBtn = new Button("🗑️ Clear");
        clearBtn.setOnAction(e -> logArea.clear());
        clearBtn.getStyleClass().add("btn-secondary");

        box.getChildren().addAll(logHeading, logArea, clearBtn);
        return box;
    }

    /** Append a message to the game log. */
    private void log(String message) {
        logArea.appendText(message + "\n");
        // Auto-scroll to bottom
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    /** Refresh inventory list if that tab's ListView is visible. */
    private void refreshInventoryIfOpen() {
        if (tabPane == null) return;
        Tab inventoryTab = tabPane.getTabs().get(1);
        ScrollPane sp = (ScrollPane) inventoryTab.getContent();
        VBox content = (VBox) sp.getContent();
        for (var node : content.getChildren()) {
            if (node instanceof ListView<?> lv) {
                @SuppressWarnings("unchecked")
                ListView<String> list = (ListView<String>) lv;
                refreshInventoryList(list);
                break;
            }
        }
    }
}
