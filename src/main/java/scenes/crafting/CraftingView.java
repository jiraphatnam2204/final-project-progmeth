package scenes.crafting;

import application.SceneManager;
import interfaces.Craftable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.base.BaseItem;
import logic.util.ItemCounter;

/**
 * CraftingView — the "face" of the crafting screen.
 *
 * Responsibility: ONLY drawing and layout. No game logic.
 *   - Builds the Pane with all Craft buttons
 *   - Draws recipe cards on a Canvas
 *   - Shows feedback messages (success/fail) from the controller
 *   - Delegates every craft attempt to CraftingController
 */
public class CraftingView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    // Layout constants — column/card dimensions used in both build() and redraw()
    private static final int COLS   = 4;
    private static final int CARD_W = 210;
    private static final int CARD_H = 150;
    private static final int START_X = 18;
    private static final int START_Y = 130;
    private static final int GAP_X  = 232;
    private static final int GAP_Y  = 165;

    private final CraftingController controller;
    private final Runnable onClose;

    // Canvas is kept as a field so redraw() can access it any time
    private Canvas canvas;
    private GraphicsContext gc;

    // Feedback message shown at the bottom (updated after every craft attempt)
    private String feedbackMsg  = "";
    private Color  feedbackColor = Color.YELLOW;

    public CraftingView(CraftingController controller, Runnable onClose) {
        this.controller = controller;
        this.onClose    = onClose;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /**
     * Creates and returns the complete crafting overlay Pane.
     * Call this once; then call update() every frame to keep stats fresh.
     */
    public Pane build() {
        canvas = new Canvas(W, H);
        gc     = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

        // Create one "Craft" button per recipe
        for (int i = 0; i < controller.getRecipes().size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            double bx = START_X + col * GAP_X + CARD_W / 2.0 - 45;
            double by = START_Y + row * GAP_Y + CARD_H - 30;

            Button btn = makeBtn("Craft");
            btn.setLayoutX(bx);
            btn.setLayoutY(by);

            final int recipeIndex = i; // must be effectively final for the lambda
            btn.setOnAction(e -> handleCraftClick(recipeIndex));

            root.getChildren().add(btn);
        }

        // "Back" button returns to the game world
        Button backBtn = makeBtn("← Back");
        backBtn.setPrefWidth(120);
        backBtn.setLayoutX(W / 2.0 - 60);
        backBtn.setLayoutY(H - 80);
        backBtn.setOnAction(e -> onClose.run());
        root.getChildren().add(backBtn);

        redraw();
        return root;
    }

    // ── Event handling ────────────────────────────────────────────────────────

    /**
     * Called when the player clicks a "Craft" button.
     * The View asks the Controller to do the real work, then updates the display.
     *
     * This is the key MVC pattern in action:
     *   View gets click → asks Controller → Controller returns result → View displays it
     */
    private void handleCraftClick(int recipeIndex) {
        CraftingController.CraftResult result = controller.craft(recipeIndex);
        feedbackMsg   = result.message();
        feedbackColor = result.success() ? Color.LIMEGREEN : Color.web("#ff5252");
        redraw();
    }

    /** Called every frame by GameScene to keep player stats fresh on screen. */
    public void update() {
        redraw();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void redraw() {
        var player = controller.getPlayer();

        // ── Background ──────────────────────────────────────────────────────
        gc.setFill(Color.rgb(10, 20, 40, 0.95));
        gc.fillRect(0, 0, W, H);

        // ── Title ───────────────────────────────────────────────────────────
        gc.setFill(Color.web("#80cbc4"));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 34));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚒ CRAFTING STATION", W / 2.0, 55);

        // ── Player stats bar ────────────────────────────────────────────────
        gc.setFont(Font.font("Arial", 14));
        gc.setFill(Color.WHITE);
        gc.fillText(
                "Gold: " + player.getGold()
                + " | ATK: "  + player.getAttack()
                + " | DEF: "  + player.getDefense()
                + " | HP: "   + player.getHealth() + "/" + player.getMaxHealth(),
                W / 2.0, 85
        );

        gc.setTextAlign(TextAlignment.LEFT);

        // ── Recipe cards ────────────────────────────────────────────────────
        for (int i = 0; i < controller.getRecipes().size(); i++) {
            Craftable recipe = controller.getRecipes().get(i);
            int    col  = i % COLS;
            int    row  = i / COLS;
            double x    = START_X + col * GAP_X;
            double y    = START_Y + row * GAP_Y;
            boolean canCraft = recipe.canCraft(player);

            // Card background — blue if craftable, dark red if not
            gc.setFill(canCraft
                    ? Color.rgb(20, 40, 70, 0.95)
                    : Color.rgb(40, 20, 20, 0.95));
            gc.fillRoundRect(x, y, CARD_W, CARD_H, 10, 10);

            // Item name
            String name = ((BaseItem) recipe).getName();
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(name, x + CARD_W / 2.0, y + 20);

            // Required materials list
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            int matRow = 0;
            for (ItemCounter mat : recipe.getRecipe()) {
                gc.fillText(
                        mat.getItem().getName() + " : " + mat.getCount(),
                        x + CARD_W / 2.0, y + 40 + 10 * matRow++
                );
            }

            // Gold cost
            gc.setFont(Font.font("Arial", 10));
            gc.setFill(Color.web("#ffd700"));
            gc.fillText(recipe.getCraftingPrice() + "g", x + CARD_W / 2.0, y + CARD_H / 1.35);

            gc.setTextAlign(TextAlignment.LEFT);
        }

        // ── Feedback banner ─────────────────────────────────────────────────
        if (!feedbackMsg.isEmpty()) {
            gc.setFill(Color.rgb(0, 0, 0, 0.85));
            gc.fillRoundRect(W / 2.0 - 200, H - 90, 400, 32, 10, 10);
            gc.setFill(feedbackColor);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(feedbackMsg, W / 2.0, H - 68);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Button makeBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(90);
        b.setPrefHeight(24);
        b.setStyle("-fx-background-color:#00acc1;-fx-text-fill:white;");
        return b;
    }
}
