package application;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import logic.base.BasePotion;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.util.ItemCounter;
import logic.base.BaseItem;
import logic.item.potion.HealPotion;
import logic.item.potion.SmallHealthPotion;

public class InventoryScene {

    private Player player;
    private Runnable onClose;

    private VBox itemListBox;
    private Text goldText;
    private Text statText;

    public InventoryScene(Player player, Runnable onClose) {
        this.player = player;
        this.onClose = onClose;
    }

    public Pane build() {

        StackPane overlay = new StackPane();
        overlay.setPrefSize(960, 720);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");

        VBox window = new VBox(15);
        window.setAlignment(Pos.TOP_CENTER);
        window.setPrefWidth(500);
        window.setStyle("-fx-background-color: #2e2e2e; -fx-padding: 20; -fx-background-radius: 15;");

        Text title = new Text("INVENTORY");
        title.setFill(Color.GOLD);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        goldText = new Text();
        goldText.setFill(Color.GOLD);

        statText = new Text();
        statText.setFill(Color.LIGHTBLUE);

        itemListBox = new VBox(10);

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> onClose.run());

        window.getChildren().addAll(title, goldText, statText, itemListBox, closeBtn);
        overlay.getChildren().add(window);

        return overlay;
    }

    public void update() {

        goldText.setText("Gold: " + player.getGold());

        statText.setText(
                "HP: " + player.getHealth() + "/" + player.getMaxHealth() +
                        "   ATK: " + player.getStrength() +
                        "   DEF: " + player.getDefense()
        );
    }
    public void refresh(){
        itemListBox.getChildren().clear();

        if (player.getInventory().isEmpty()) {
            Text empty = new Text("(Inventory Empty)");
            empty.setFill(Color.GRAY);
            itemListBox.getChildren().add(empty);
            return;
        }

        for (ItemCounter counter : player.getInventory()) {

            BaseItem item = counter.getItem();

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);

            Text name = new Text(item.getName() + " x" + counter.getCount());
            name.setFill(Color.WHITE);
            Button useBtn = null;
            if(item instanceof BasePotion) {
                useBtn = new Button("Use");

                useBtn.setOnAction(e -> {
                    ((BasePotion) item).consume(player);
                    counter.addCount(-1);

                    if (counter.getCount() <= 0) {
                        player.getInventory().remove(counter);
                    }
                    refresh();
                });
                row.getChildren().addAll(name, useBtn);

            }
            else if(item instanceof BaseWeapon) {
                useBtn = new Button("Equip");

                useBtn.setOnAction(e -> {
                    ((BaseWeapon) item).equip(player);
                    counter.addCount(-1);

                    if (counter.getCount() <= 0) {
                        player.getInventory().remove(counter);
                    }
                    refresh();
                });
                row.getChildren().addAll(name, useBtn);

            }
            else{
                row.getChildren().addAll(name);
            }
            itemListBox.getChildren().add(row);
        }
    }
}