package scenes.shop;

import application.SceneManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * ShopView — the "face" of the shop screen.
 * <p>
 * Responsibility: ONLY drawing and layout. No purchase logic.
 * - Renders item cards on a Canvas
 * - Creates Buy buttons and wires them to ShopController
 * - Displays feedback messages received from the controller
 */
public class ShopView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    // Layout constants — used in both build() and redraw() so defined once here
    private static final int COLS = 4;
    private static final int CARD_W = 200;
    private static final int CARD_H = 130;
    private static final int START_X = 40;
    private static final int START_Y = 160;
    private static final int GAP_X = 220;
    private static final int GAP_Y = 150;

    // Colours matching item categories — parallel to ShopController catalogue order
    private static final Color[] CARD_COLORS = {
            Color.web("#e53935"), Color.web("#c62828"), Color.web("#b71c1c"), // potions
            Color.web("#607d8b"), Color.web("#78909c"), Color.web("#90caf9"), Color.web("#ce93d8"), // pickaxes
            Color.web("#9e9e9e"), Color.web("#b0bec5"), Color.web("#80d8ff")  // swords
    };

    private final ShopController controller;
    private final Runnable onClose;

    private GraphicsContext gc;
    private String feedbackMsg = "";
    private Color feedbackColor = Color.YELLOW;

    public ShopView(ShopController controller, Runnable onClose) {
        this.controller = controller;
        this.onClose = onClose;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /**
     * Builds and returns the complete shop overlay Pane.
     */
    public Pane build() {
        Canvas canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        List<ShopController.ShopItem> items = controller.getItems();

        for (int i = 0; i < items.size(); i++) {
            ShopController.ShopItem item = items.get(i);
            int col = i % COLS;
            int row = i / COLS;
            double bx = START_X + col * GAP_X + CARD_W / 2.0 - 50;
            double by = START_Y + row * GAP_Y + CARD_H - 32;

            Button btn = makeBtn("Buy  " + item.price() + "g");
            btn.setLayoutX(bx);
            btn.setLayoutY(by);

            final ShopController.ShopItem finalItem = item;
            btn.setOnAction(e -> handleBuyClick(finalItem));

            root.getChildren().add(btn);
        }

        // Back button
        Button backBtn = makeBtn("← Back to World");
        backBtn.setLayoutX(W / 2.0 - 80);
        backBtn.setLayoutY(H - 80);
        backBtn.setOnAction(e -> onClose.run());
        root.getChildren().add(backBtn);

        redraw();
        return root;
    }

    // ── Event handling ────────────────────────────────────────────────────────

    /**
     * Called when a "Buy" button is clicked.
     * Delegates to controller, then updates the display with the result.
     */
    private void handleBuyClick(ShopController.ShopItem item) {
        ShopController.BuyResult result = controller.buy(item);
        feedbackMsg = result.message();
        feedbackColor = result.success() ? Color.LIMEGREEN : Color.web("#ff5252");
        redraw();
    }

    /**
     * Called every frame to refresh gold/HP display.
     */
    public void update() {
        redraw();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void redraw() {
        var player = controller.getPlayer();
        var items = controller.getItems();

        // ── Background gradient ─────────────────────────────────────────────
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a0a00")),
                new Stop(1, Color.web("#3e1f00")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // ── Title ───────────────────────────────────────────────────────────
        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("🛒  SHOP", W / 2.0, 60);

        // ── Player stats ────────────────────────────────────────────────────
        gc.setFill(Color.web("#ffcc80"));
        gc.setFont(Font.font("Arial", 16));
        gc.fillText(
                "Gold: " + player.getGold() + "g"
                        + "  |  HP: " + player.getHealth() + "/" + player.getMaxHealth()
                        + "  |  ATK: " + player.getAttack()
                        + "  |  DEF: " + player.getDefense(),
                W / 2.0, 95);

        // ── Current pickaxe info ────────────────────────────────────────────
        var pickaxe = controller.getPickaxeHolder()[0];
        gc.setFont(Font.font("Arial", 13));
        gc.setFill(Color.web("#a5d6a7"));
        gc.fillText(
                "Current pickaxe: " + pickaxe.getName()
                        + " (Power: " + pickaxe.getPower() + ")",
                W / 2.0, 118);

        gc.setTextAlign(TextAlignment.LEFT);

        // ── Item cards ──────────────────────────────────────────────────────
        for (int i = 0; i < items.size(); i++) {
            ShopController.ShopItem item = items.get(i);
            int col = i % COLS;
            int row = i / COLS;
            double x = START_X + col * GAP_X;
            double y = START_Y + row * GAP_Y;
            boolean canAfford = player.getGold() >= item.price();
            Color cardColor = i < CARD_COLORS.length ? CARD_COLORS[i] : Color.GRAY;

            // Card body
            gc.setFill(canAfford
                    ? Color.rgb(40, 40, 40, 0.92)
                    : Color.rgb(30, 20, 20, 0.85));
            gc.fillRoundRect(x, y, CARD_W, CARD_H, 12, 12);

            // Coloured header stripe
            gc.setStroke(canAfford ? cardColor : Color.web("#444444"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, CARD_W, CARD_H, 12, 12);
            gc.setFill(cardColor.deriveColor(0, 1, 1, 0.8));
            gc.fillRoundRect(x, y, CARD_W, 26, 12, 0);
            gc.fillRect(x, y + 14, CARD_W, 12);

            // Item name (in the header)
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(item.name(), x + CARD_W / 2.0, y + 18);

            // Description
            gc.setFill(Color.web("#cfd8dc"));
            gc.setFont(Font.font("Arial", 11));
            gc.fillText(item.description(), x + CARD_W / 2.0, y + 48);

            // Price (greyed out if unaffordable)
            gc.setFill(canAfford ? Color.web("#ffd700") : Color.web("#757575"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText(item.price() + " gold", x + CARD_W / 2.0, y + 72);

            gc.setTextAlign(TextAlignment.LEFT);
        }

        // ── Feedback banner ─────────────────────────────────────────────────
        if (!feedbackMsg.isEmpty()) {
            gc.setFill(Color.rgb(0, 0, 0, 0.8));
            gc.fillRoundRect(W / 2.0 - 200, H - 96, 400, 32, 10, 10);
            gc.setFill(feedbackColor);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(feedbackMsg, W / 2.0, H - 74);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Button makeBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(100);
        b.setPrefHeight(26);
        String s = "-fx-background-color:#f9a825;-fx-text-fill:#1a0000;"
                + "-fx-font-weight:bold;-fx-font-size:11px;"
                + "-fx-background-radius:5;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(s.replace("#f9a825", "#ffca28")));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
}
