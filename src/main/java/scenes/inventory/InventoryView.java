package scenes.inventory;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import logic.base.BaseArmor;
import logic.base.BaseItem;
import logic.base.BasePotion;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.List;

public class InventoryView {

    private final InventoryController controller;
    private final Runnable onClose;

    private VBox itemListBox;
    private Text goldText;
    private Text statText;
    private Text equippedText;
    private Text pageLabel;
    private Button prevBtn, nextBtn;

    public InventoryView(InventoryController controller, Runnable onClose) {
        this.controller = controller;
        this.onClose = onClose;
    }

    // Assembles the foundational layout of the inventory window.
    // This acts like building the physical dashboard of a car. You only do this once
    // to put the dials and screens in place. Later methods will update what those dials actually say.
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

        Text title = new Text("⚔  INVENTORY");
        title.setFill(Color.web("#f8f8f2"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 26));

        goldText = new Text();
        goldText.setFill(Color.web("#ffd700"));
        goldText.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        statText = new Text();
        statText.setFill(Color.web("#8be9fd"));
        statText.setFont(Font.font("Arial", 12));

        equippedText = new Text();
        equippedText.setFill(Color.web("#50fa7b"));
        equippedText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox unequipRow = new HBox(10);
        unequipRow.setAlignment(Pos.CENTER);

        Button unequipWeaponBtn = makeBtn("❌ Unequip Weapon", "#6272a4");
        unequipWeaponBtn.setOnAction(e -> {
            controller.unequipWeapon();
            refresh();
        });

        Button unequipArmorBtn = makeBtn("❌ Unequip Armor", "#6272a4");
        unequipArmorBtn.setOnAction(e -> {
            controller.unequipArmor();
            refresh();
        });
        unequipRow.getChildren().addAll(unequipWeaponBtn, unequipArmorBtn);

        itemListBox = new VBox(5);
        itemListBox.setPrefHeight(InventoryController.ITEMS_PER_PAGE * 46.0);

        prevBtn = makeBtn("◀  Prev", "#44475a");
        nextBtn = makeBtn("Next  ▶", "#44475a");
        pageLabel = new Text("Page 1 / 1");
        pageLabel.setFill(Color.web("#bd93f9"));
        pageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        prevBtn.setOnAction(e -> {
            controller.prevPage();
            refresh();
        });
        nextBtn.setOnAction(e -> {
            controller.nextPage();
            refresh();
        });

        HBox pageRow = new HBox(16, prevBtn, pageLabel, nextBtn);
        pageRow.setAlignment(Pos.CENTER);

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

    // Refreshes the simple text values (HP, Gold, currently equipped gear).
    // This is like updating the digital numbers on a scoreboard. It doesn't tear down
    // the stadium, it just changes the fast-moving data.
    public void update() {
        Player player = controller.getPlayer();
        goldText.setText("💰 Gold: " + player.getGold());
        statText.setText(
                "❤ HP: " + player.getHealth() + "/" + player.getMaxHealth() +
                        "   ⚔ ATK: " + player.getStrength() +
                        "   🛡 DEF: " + player.getDefense()
        );
        String w = player.getEquippedWeapon() != null
                ? "⚔ " + player.getEquippedWeapon().getName() : "⚔ None";
        String a = player.getEquippedArmor() != null
                ? "🛡 " + player.getEquippedArmor().getName() : "🛡 None";
        equippedText.setText("Equipped:  " + w + "   |   " + a);
    }

    // Completely wipes the visible items and redraws them based on the current page.
    // If you drink a potion or flip the page, this acts like wiping a whiteboard clean
    // and re-writing the newly updated list from scratch so it stays perfectly accurate.
    public void refresh() {
        update();
        itemListBox.getChildren().clear();

        List<ItemCounter> inv = controller.getPlayer().getInventory();

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

        int totalPages = controller.getTotalPages();
        controller.clampPage();
        int page = controller.getCurrentPage();

        pageLabel.setText("Page " + (page + 1) + " / " + totalPages);
        prevBtn.setDisable(page == 0);
        nextBtn.setDisable(page >= totalPages - 1);

        List<ItemCounter> pageItems = controller.getPageItems();
        int startIndex = page * InventoryController.ITEMS_PER_PAGE;
        for (int i = 0; i < pageItems.size(); i++) {
            HBox row = buildItemRow(pageItems.get(i), startIndex + i + 1, i % 2 == 0);
            itemListBox.getChildren().add(row);
        }
    }

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

        Text numLbl = new Text(String.format("%2d.", rowNum));
        numLbl.setFill(Color.web("#6272a4"));
        numLbl.setFont(Font.font("Arial", 11));

        String icon = item instanceof BaseWeapon ? "⚔"
                : item instanceof BaseArmor ? "🛡"
                : item instanceof BasePotion ? "🧪"
                : "📦";
        Text iconLbl = new Text(icon);
        iconLbl.setFont(Font.font("Arial", 15));

        String suffix = controller.buildStatSuffix(item);
        Text nameLbl = new Text(item.getName() + suffix + "  ×" + counter.getCount());
        nameLbl.setFill(
                item instanceof BaseWeapon ? Color.web("#ffb86c") :
                        item instanceof BaseArmor ? Color.web("#8be9fd") :
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

    private void addActionButton(HBox row, BaseItem item, ItemCounter counter) {
        if (item instanceof BasePotion potion) {
            Button useBtn = makeBtn("Use", "#50fa7b");
            useBtn.setTextFill(Color.web("#1e1e2e"));
            useBtn.setOnAction(e -> {
                controller.usePotion(counter);
                refresh();
            });
            row.getChildren().add(useBtn);

        } else if (item instanceof BaseWeapon weapon) {
            boolean equipped = controller.isWeaponEquipped(weapon);
            Button btn = makeBtn(equipped ? "Equipped" : "Equip", equipped ? "#44475a" : "#bd93f9");
            btn.setDisable(equipped);
            btn.setOnAction(e -> {
                controller.equipWeapon(weapon);
                refresh();
            });
            row.getChildren().add(btn);

        } else if (item instanceof BaseArmor armor) {
            boolean equipped = controller.isArmorEquipped(armor);
            Button btn = makeBtn(equipped ? "Equipped" : "Equip", equipped ? "#44475a" : "#ff79c6");
            btn.setDisable(equipped);
            btn.setOnAction(e -> {
                controller.equipArmor(armor);
                refresh();
            });
            row.getChildren().add(btn);
        }
    }

    private Button makeBtn(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setPrefHeight(28);
        String base = "-fx-background-color: " + bgColor + ";"
                + "-fx-text-fill: white;-fx-font-weight: bold;"
                + "-fx-font-size: 11px;-fx-background-radius: 5;"
                + "-fx-cursor: hand;-fx-padding: 3 10 3 10;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + "-fx-opacity:0.82;"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button makeBtn(String text) {
        return makeBtn(text, "#00acc1");
    }
}