package application;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import logic.creatures.Player;
import logic.item.potion.SmallHealthPotion;
import logic.item.potion.HealPotion;
import logic.item.potion.MediumHealthPotion;
import logic.item.potion.BigHealthPotion;
import logic.item.weapon.*;
import logic.pickaxe.Pickaxe;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ShopScene: A store where the player spends gold on potions and better weapons/pickaxes.
 *
 * Each ShopItem has a name, price, and an "onBuy" action.
 * We draw item cards on a canvas and place JavaFX Buttons over them.
 */
public class ShopScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private GraphicsContext gc;
    private final Player    player;
    private final Pickaxe[] pickaxeHolder;
    private String          feedbackMsg  = "";
    private Color           feedbackColor = Color.YELLOW;
    private Runnable onClose;

    // ─── Inner class: one item for sale ───────────────────────────────
    private static class ShopItem {
        String name, description;
        int price;
        Color color;
        Consumer<Player> onBuy;

        ShopItem(String name, String desc, int price, Color color, Consumer<Player> onBuy) {
            this.name=name; this.description=desc; this.price=price; this.color=color; this.onBuy=onBuy;
        }
    }

    private final List<ShopItem> items = new ArrayList<>();

    public ShopScene(Player player, Pickaxe[] pickaxeHolder,Runnable onClose) {
        this.player        = player;
        this.pickaxeHolder = pickaxeHolder;
        this.onClose = onClose;
        buildInventory();
    }

    /** Define everything for sale */
    private void buildInventory() {
        // ── Potions ──────────────────────────────────────────────────
        items.add(new ShopItem(
            "Small Potion", "Heals 10 HP", 20, Color.web("#e53935"),
            p -> { p.getInventory().add(new ItemCounter(new SmallHealthPotion(), 1)); p.heal(10); }
        ));
        items.add(new ShopItem(
            "Heal Potion", "Heals 20% max HP", 50, Color.web("#f44336"),
            p -> { var pot = new HealPotion(); pot.consume(p); p.getInventory().add(new ItemCounter(pot,1)); }
        ));
        items.add(new ShopItem(
            "Medium Potion", "Heals 50 HP", 80, Color.web("#c62828"),
            p -> { p.getInventory().add(new ItemCounter(new MediumHealthPotion(), 1)); p.heal(50); }
        ));
        items.add(new ShopItem(
            "Big Potion", "Heals 100 HP", 120, Color.web("#b71c1c"),
            p -> { p.getInventory().add(new ItemCounter(new BigHealthPotion(), 1)); p.heal(100); }
        ));

        // ── Pickaxes ─────────────────────────────────────────────────
        items.add(new ShopItem(
            "Hardstone Pick", "Power: 5", 80, Color.web("#607d8b"),
            p -> pickaxeHolder[0] = Pickaxe.createHardStonePickaxe()
        ));
        items.add(new ShopItem(
            "Iron Pickaxe", "Power: 12", 200, Color.web("#78909c"),
            p -> pickaxeHolder[0] = Pickaxe.createIronPickaxe()
        ));
        items.add(new ShopItem(
            "Platinum Pick", "Power: 27", 500, Color.web("#90caf9"),
            p -> pickaxeHolder[0] = Pickaxe.createPlatinumPickaxe()
        ));
        items.add(new ShopItem(
            "Mithril Pick", "Power: 45", 1200, Color.web("#ce93d8"),
            p -> pickaxeHolder[0] = Pickaxe.createMithrilPickaxe()
        ));

        // ── Weapons (add bonus ATK via addBonus) ─────────────────────
        items.add(new ShopItem(
            "Stone Sword", "+5 ATK", 30, Color.web("#9e9e9e"),
            p -> p.addBonus(5, 0, 0, 0)
        ));
        items.add(new ShopItem(
            "Iron Sword", "+15 ATK", 150, Color.web("#b0bec5"),
            p -> p.addBonus(15, 0, 0, 0)
        ));
        items.add(new ShopItem(
            "Platinum Sword", "+30 ATK", 400, Color.web("#80d8ff"),
            p -> p.addBonus(30, 0, 0, 0)
        ));
    }
    public Pane close(){
        return null;
    }
    public Pane build() {
        Canvas canvas = new Canvas(W, H);
        this.gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        // ── Buy buttons placed over each item card ────────────────────
        int cols  = 4;
        int cardW = 200, cardH = 130;
        int startX = 40, startY = 160, gapX = 220, gapY = 150;

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            int col = i % cols, row = i / cols;
            double bx = startX + col*gapX + cardW/2.0 - 50;
            double by = startY + row*gapY + cardH - 32;

            Button btn = makeBtn("Buy  " + item.price + "g");
            btn.setLayoutX(bx); btn.setLayoutY(by);

            final ShopItem fi = item;
            btn.setOnAction(e -> {
                if (player.getGold() >= fi.price) {
                    player.setGold(player.getGold() - fi.price);
                    fi.onBuy.accept(player);
                    feedbackMsg   = "✓ Bought " + fi.name + "!";
                    feedbackColor = Color.LIMEGREEN;
                } else {
                    feedbackMsg   = "✗ Not enough gold! (need " + fi.price + "g)";
                    feedbackColor = Color.web("#ff5252");
                }
                redraw(gc, startX, startY, cardW, cardH, gapX, gapY);
            });
            root.getChildren().add(btn);
        }

        // Back button
        Button backBtn = makeBtn("← Back t  o World");
        backBtn.setLayoutX(W/2.0 - 80   ); backBtn.setLayoutY(H - 54);
        backBtn.setOnAction(e -> onClose.run());
        root.getChildren().add(backBtn);

        // Initial draw
        redraw(gc, startX, startY, cardW, cardH, gapX, gapY);
        root.setPrefWidth(500);
        root.setPrefHeight(300);
        return root;
    }
    public void update(){
        int cardW = 200, cardH = 130;
        int startX = 40, startY = 160, gapX = 220, gapY = 150;
        redraw(gc, startX, startY, cardW, cardH, gapX, gapY);
    }
    private void redraw(GraphicsContext gc, int sx, int sy, int cw, int ch, int gx, int gy) {
        // Background
        LinearGradient bg = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,Color.web("#1a0a00")), new Stop(1,Color.web("#3e1f00")));
        gc.setFill(bg); gc.fillRect(0,0,W,H);

        // Header
        gc.setFill(Color.web("#ffd700")); gc.setFont(Font.font("Georgia",FontWeight.BOLD,36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("🛒  SHOP", W/2.0, 60);
        gc.setFill(Color.web("#ffcc80")); gc.setFont(Font.font("Arial",16));
        gc.fillText("Gold: " + player.getGold() + "g  |  HP: " + player.getHealth()+"/"+player.getMaxHealth()
                    + "  |  ATK: "+player.getAttack()+"  |  DEF: "+player.getDefense(), W/2.0, 95);
        gc.setFont(Font.font("Arial",13)); gc.setFill(Color.web("#a5d6a7"));
        gc.fillText("Current pickaxe: " + pickaxeHolder[0].getName()
                    + " (Power: "+pickaxeHolder[0].getPower()+")", W/2.0, 118);
        gc.setTextAlign(TextAlignment.LEFT);

        // Item cards
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            int col=i%4, row=i/4;
            double x=sx+col*gx, y=sy+row*gy;

            // Card background
            boolean canAfford = player.getGold() >= item.price;
            gc.setFill(canAfford ? Color.rgb(40,40,40,0.92) : Color.rgb(30,20,20,0.85));
            gc.fillRoundRect(x, y, cw, ch, 12, 12);
            gc.setStroke(canAfford ? item.color : Color.web("#444444"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, cw, ch, 12, 12);

            // Colour stripe at top
            gc.setFill(item.color.deriveColor(0,1,1,0.8));
            gc.fillRoundRect(x, y, cw, 26, 12, 0);
            gc.fillRect(x, y+14, cw, 12);

            // Item name
            gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(item.name, x+cw/2.0, y+18);

            // Description
            gc.setFill(Color.web("#cfd8dc")); gc.setFont(Font.font("Arial",11));
            gc.fillText(item.description, x+cw/2.0, y+48);

            // Price
            gc.setFill(canAfford ? Color.web("#ffd700") : Color.web("#757575"));
            gc.setFont(Font.font("Arial",FontWeight.BOLD,14));
            gc.fillText(item.price + " gold", x+cw/2.0, y+72);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        // Feedback message
        if (!feedbackMsg.isEmpty()) {
            gc.setFill(Color.rgb(0,0,0,0.8)); gc.fillRoundRect(W/2.0-200, H-96, 400, 32, 10, 10);
            gc.setFill(feedbackColor); gc.setFont(Font.font("Arial",FontWeight.BOLD,14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(feedbackMsg, W/2.0, H-74);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private Button makeBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(100); b.setPrefHeight(26);
        String s = "-fx-background-color:#f9a825;-fx-text-fill:#1a0000;-fx-font-weight:bold;" +
                   "-fx-font-size:11px;-fx-background-radius:5;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e->b.setStyle(s.replace("#f9a825","#ffca28")));
        b.setOnMouseExited(e ->b.setStyle(s));
        return b;
    }
}
