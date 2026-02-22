package application;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import logic.base.BaseArmor;
import logic.base.BasePotion;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.util.ItemCounter;
import logic.base.BaseItem;

import java.util.List;

public class InventoryScene {

    private static final int ITEMS_PER_PAGE = 8;

    private final Player   player;
    private final Runnable onClose;

    private VBox   itemListBox;
    private Text   goldText;
    private Text   statText;
    private Text   equippedText;
    private Text   pageLabel;
    private Button prevBtn, nextBtn;

    private int currentPage = 0;   // 0-indexed

    public InventoryScene(Player player, Runnable onClose) {
        this.player  = player;
        this.onClose = onClose;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BUILD
    // ─────────────────────────────────────────────────────────────────────────

    public Pane build() {
        StackPane overlay = new StackPane();
        overlay.setPrefSize(960, 720);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");

        VBox window = new VBox(10);
        window.setAlignment(Pos.TOP_CENTER);
        window.setPrefWidth(580);
        window.setMaxHeight(660);
        window.setStyle(
                "-fx-background-color: #1e1e2e;" +
                        "-fx-padding: 22;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #44475a;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1.5;"
        );

        // ── Title ─────────────────────────────────────────────────────────────
        Text title = new Text("⚔  INVENTORY");
        title.setFill(Color.web("#f8f8f2"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 26));

        // ── Player stats ──────────────────────────────────────────────────────
        goldText = new Text();
        goldText.setFill(Color.web("#ffd700"));
        goldText.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        statText = new Text();
        statText.setFill(Color.web("#8be9fd"));
        statText.setFont(Font.font("Arial", 12));

        // ── Equipped gear display ─────────────────────────────────────────────
        equippedText = new Text();
        equippedText.setFill(Color.web("#50fa7b"));
        equippedText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // ── Unequip buttons ───────────────────────────────────────────────────
        HBox unequipRow = new HBox(10);
        unequipRow.setAlignment(Pos.CENTER);

        Button unequipWeaponBtn = makeBtn("❌ Unequip Weapon", "#6272a4");
        unequipWeaponBtn.setOnAction(e -> {
            if (player.getEquippedWeapon() != null) { player.unequipWeapon(); refresh(); }
        });

        Button unequipArmorBtn = makeBtn("❌ Unequip Armor", "#6272a4");
        unequipArmorBtn.setOnAction(e -> {
            if (player.getEquippedArmor() != null) { player.unequipArmor(); refresh(); }
        });

        unequipRow.getChildren().addAll(unequipWeaponBtn, unequipArmorBtn);

        // ── Item list area ────────────────────────────────────────────────────
        itemListBox = new VBox(5);
        itemListBox.setPrefHeight(ITEMS_PER_PAGE * 46.0);

        // ── Pagination row ────────────────────────────────────────────────────
        prevBtn   = makeBtn("◀  Prev", "#44475a");
        nextBtn   = makeBtn("Next  ▶", "#44475a");
        pageLabel = new Text("Page 1 / 1");
        pageLabel.setFill(Color.web("#bd93f9"));
        pageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        prevBtn.setOnAction(e -> { currentPage--; refresh(); });
        nextBtn.setOnAction(e -> { currentPage++; refresh(); });

        HBox pageRow = new HBox(16, prevBtn, pageLabel, nextBtn);
        pageRow.setAlignment(Pos.CENTER);

        // ── Close button ──────────────────────────────────────────────────────
        Button closeBtn = makeBtn("Close  [E]", "#ff5555");
        closeBtn.setOnAction(e -> onClose.run());

        window.getChildren().addAll(
                title,
                goldText, statText, equippedText,
                new Separator(),
                unequipRow,
                new Separator(),
                itemListBox,
                new Separator(),
                pageRow,
                closeBtn
        );

        overlay.getChildren().add(window);
        return overlay;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UPDATE  (called every frame while inventory is open)
    // ─────────────────────────────────────────────────────────────────────────

    public void update() {
        goldText.setText("💰 Gold: " + player.getGold());
        statText.setText(
                "❤ HP: "  + player.getHealth() + "/" + player.getMaxHealth() +
                        "   ⚔ ATK: " + player.getStrength() +
                        "   🛡 DEF: " + player.getDefense()
        );
        updateEquippedText();
    }

    private void updateEquippedText() {
        String w = player.getEquippedWeapon() != null
                ? "⚔ " + player.getEquippedWeapon().getName() : "⚔ None";
        String a = player.getEquippedArmor()  != null
                ? "🛡 " + player.getEquippedArmor().getName()  : "🛡 None";
        equippedText.setText("Equipped:  " + w + "   |   " + a);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  REFRESH  (rebuild item list for current page)
    // ─────────────────────────────────────────────────────────────────────────

    public void refresh() {
        update();
        itemListBox.getChildren().clear();

        List<ItemCounter> inv = player.getInventory();

        if (inv.isEmpty()) {
            Text empty = new Text("  (Inventory is empty)");
            empty.setFill(Color.web("#6272a4"));
            empty.setFont(Font.font("Arial", 13));
            itemListBox.getChildren().add(empty);
            pageLabel.setText("Page 1 / 1");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            return;
        }

        // Clamp page index
        int totalPages = (int) Math.ceil((double) inv.size() / ITEMS_PER_PAGE);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0)           currentPage = 0;

        int start = currentPage * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, inv.size());

        // Update pagination controls
        pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);

        // Render items for this page slice
        for (int idx = start; idx < end; idx++) {
            ItemCounter counter = inv.get(idx);
            HBox row = buildItemRow(counter, idx + 1, idx % 2 == 0);
            itemListBox.getChildren().add(row);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BUILD A SINGLE ITEM ROW
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildItemRow(ItemCounter counter, int rowNum, boolean even) {
        BaseItem item = counter.getItem();

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(40);
        row.setStyle(
                "-fx-background-color: " + (even ? "#282a36" : "#21222c") + ";" +
                        "-fx-background-radius: 7;" +
                        "-fx-padding: 4 12 4 12;"
        );

        // Row number
        Text numLbl = new Text(String.format("%2d.", rowNum));
        numLbl.setFill(Color.web("#6272a4"));
        numLbl.setFont(Font.font("Arial", 11));

        // Type icon
        String icon = item instanceof BaseWeapon ? "⚔"
                : item instanceof BaseArmor  ? "🛡"
                : item instanceof BasePotion ? "🧪"
                : "📦";
        Text iconLbl = new Text(icon);
        iconLbl.setFont(Font.font("Arial", 15));

        // Name + stat suffix
        String suffix = buildSuffix(item);
        Text nameLbl = new Text(item.getName() + suffix + "  ×" + counter.getCount());
        nameLbl.setFill(
                item instanceof BaseWeapon ? Color.web("#ffb86c") :
                        item instanceof BaseArmor  ? Color.web("#8be9fd") :
                                item instanceof BasePotion ? Color.web("#50fa7b") :
                                        Color.web("#f8f8f2")
        );
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(numLbl, iconLbl, nameLbl, spacer);
        addActionButton(row, item, counter);

        return row;
    }

    /** Build the small stat/status suffix shown next to item name. */
    private String buildSuffix(BaseItem item) {
        if (item instanceof BaseWeapon w) {
            boolean eq = player.getEquippedWeapon() != null
                    && player.getEquippedWeapon().getName().equals(item.getName());
            return eq ? "  ✅" : "  (+atk " + w.getDmg() + ")";
        }
        if (item instanceof BaseArmor a) {
            boolean eq = player.getEquippedArmor() != null
                    && player.getEquippedArmor().getName().equals(item.getName());
            return eq ? "  ✅" : "  (+def " + a.getDef() + ")";
        }
        return "";
    }

    /** Append the correct action button to the row depending on item type. */
    private void addActionButton(HBox row, BaseItem item, ItemCounter counter) {
        if (item instanceof BasePotion potion) {
            Button useBtn = makeBtn("Use", "#50fa7b");
            useBtn.setTextFill(Color.web("#1e1e2e"));
            useBtn.setOnAction(e -> {
                potion.consume(player);
                counter.addCount(-1);
                if (counter.getCount() <= 0) player.getInventory().remove(counter);
                refresh();
            });
            row.getChildren().add(useBtn);

        } else if (item instanceof BaseWeapon weapon) {
            boolean isEq = player.getEquippedWeapon() != null
                    && player.getEquippedWeapon().getName().equals(weapon.getName());
            Button btn = makeBtn(isEq ? "Equipped" : "Equip", isEq ? "#44475a" : "#bd93f9");
            btn.setDisable(isEq);
            btn.setOnAction(e -> { player.equipWeapon(weapon); refresh(); });
            row.getChildren().add(btn);

        } else if (item instanceof BaseArmor armor) {
            boolean isEq = player.getEquippedArmor() != null
                    && player.getEquippedArmor().getName().equals(armor.getName());
            Button btn = makeBtn(isEq ? "Equipped" : "Equip", isEq ? "#44475a" : "#ff79c6");
            btn.setDisable(isEq);
            btn.setOnAction(e -> { player.equipArmor(armor); refresh(); });
            row.getChildren().add(btn);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private Button makeBtn(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setPrefHeight(28);
        String base =
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 3 10 3 10;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + "-fx-opacity:0.82;"));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }
}