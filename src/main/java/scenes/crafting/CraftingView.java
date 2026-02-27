package scenes.crafting;

import application.SceneManager;
import interfaces.Craftable;
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
import logic.base.BaseItem;
import logic.util.ItemCounter;

public class CraftingView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private static final int COLS = 4;
    private static final int CARD_W = 210;
    private static final int CARD_H = 150;
    private static final int START_X = 18;
    private static final int START_Y = 130;
    private static final int GAP_X = 232;
    private static final int GAP_Y = 165;

    private final CraftingController controller;
    private final Runnable onClose;

    private Canvas canvas;
    private GraphicsContext gc;

    private String feedbackMsg = "";
    private Color feedbackColor = Color.YELLOW;

    public CraftingView(CraftingController controller, Runnable onClose) {
        this.controller = controller;
        this.onClose = onClose;
    }

    public Pane build() {
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);

        for (int i = 0; i < controller.getRecipes().size(); i++) {
            int col = i % COLS;
            int row = i / COLS;

            double bx = START_X + col * GAP_X + CARD_W / 2.0 - 50;
            double by = START_Y + row * GAP_Y + CARD_H - 32;

            Button btn = makeBtn("Craft");
            btn.setLayoutX(bx);
            btn.setLayoutY(by);

            final int recipeIndex = i;
            btn.setOnAction(e -> handleCraftClick(recipeIndex));

            root.getChildren().add(btn);
        }

        Button backBtn = makeBtn("← Back to World");
        backBtn.setPrefWidth(120);
        backBtn.setLayoutX(W / 2.0 - 60);
        backBtn.setLayoutY(H - 80);
        backBtn.setOnAction(e -> onClose.run());
        root.getChildren().add(backBtn);

        redraw();
        return root;
    }

    private void handleCraftClick(int recipeIndex) {
        CraftingController.CraftResult result = controller.craft(recipeIndex);
        feedbackMsg = result.message();
        feedbackColor = result.success() ? Color.LIMEGREEN : Color.web("#ff5252");
        redraw();
    }

    public void update() {
        redraw();
    }

    private void redraw() {
        var player = controller.getPlayer();

        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a1428")),
                new Stop(1, Color.web("#14283c")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.web("#80cbc4")); // A nice teal color for crafting
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚒ CRAFTING STATION", W / 2.0, 60);

        gc.setFill(Color.web("#b2dfdb"));
        gc.setFont(Font.font("Arial", 16));
        gc.fillText(
                "Gold: " + player.getGold() + "g"
                        + "  |  HP: " + player.getHealth() + "/" + player.getMaxHealth()
                        + "  |  ATK: " + player.getAttack()
                        + "  |  DEF: " + player.getDefense(),
                W / 2.0, 95
        );

        gc.setTextAlign(TextAlignment.LEFT);

        Font materialFont = Font.font("Arial", FontWeight.NORMAL, 12);
        Font goldFont = Font.font("Arial", FontWeight.BOLD, 14);

        for (int i = 0; i < controller.getRecipes().size(); i++) {
            Craftable recipe = controller.getRecipes().get(i);

            int col = i % COLS;
            int row = i / COLS;
            double x = START_X + col * GAP_X;
            double y = START_Y + row * GAP_Y;

            boolean canCraft = recipe.canCraft(player);
            Color headerColor = Color.web("#00838f");

            gc.setFill(canCraft
                    ? Color.rgb(40, 40, 40, 0.92)
                    : Color.rgb(30, 20, 20, 0.85));
            gc.fillRoundRect(x, y, CARD_W, CARD_H, 12, 12);

            gc.setStroke(canCraft ? headerColor : Color.web("#444444"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, CARD_W, CARD_H, 12, 12);

            gc.setFill(canCraft ? headerColor.deriveColor(0, 1, 1, 0.8) : Color.web("#444444"));
            gc.fillRoundRect(x, y, CARD_W, 26, 12, 0);
            gc.fillRect(x, y + 14, CARD_W, 12);

            String name = ((BaseItem) recipe).getName();
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(name, x + CARD_W / 2.0, y + 18);

            gc.setFont(materialFont);
            gc.setFill(Color.web("#cfd8dc"));
            int matRow = 0;
            for (ItemCounter mat : recipe.getRecipe()) {
                gc.fillText(
                        mat.getItem().getName() + " : " + mat.getCount(),
                        x + CARD_W / 2.0,
                        y + 48 + (16 * matRow)
                );
                matRow++;
            }

            gc.setFont(goldFont);
            gc.setFill(canCraft ? Color.web("#ffd700") : Color.web("#757575"));
            gc.fillText(recipe.getCraftingPrice() + "g", x + CARD_W / 2.0, y + 90);

            gc.setTextAlign(TextAlignment.LEFT);
        }

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


    private Button makeBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(100);
        b.setPrefHeight(26);

        String baseStyle = "-fx-background-color:#00838f;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:11px;"
                + "-fx-background-radius:5;-fx-cursor:hand;";

        b.setStyle(baseStyle);

        b.setOnMouseEntered(e -> b.setStyle(baseStyle.replace("#00838f", "#00acc1")));
        b.setOnMouseExited(e -> b.setStyle(baseStyle));
        return b;
    }
}