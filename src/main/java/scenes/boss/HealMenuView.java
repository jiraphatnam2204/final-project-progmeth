package scenes.boss;

import application.SceneManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Overlay panel displayed when the player opens the BAG menu during a boss battle.
 * Shows all potions in the player's inventory and a Rest option.
 * Delegates purchase/use logic to callbacks supplied at construction time.
 */
public class HealMenuView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final double PX = W / 2.0 + 10;
    private static final double PY = H * 0.64;
    private static final double PW = W / 2.0 - 20;
    private static final double ROW_H = 38;

    private final BattleMenuController menuCtrl;
    private final Consumer<BattleMenuController.PotionEntry> onUsePotion;
    private final Runnable onRest;
    private final Runnable onClose;

    private Canvas canvas;
    private GraphicsContext gc;
    private final List<Button> dynamicBtns = new ArrayList<>();
    private Pane root;
    private List<BattleMenuController.PotionEntry> lastPotions = new ArrayList<>();

    /**
     * Creates a new HealMenuView.
     *
     * @param menuCtrl     the battle menu controller providing potion data
     * @param onUsePotion  callback invoked when the player selects a potion to use
     * @param onRest       callback invoked when the player selects the Rest option
     * @param onClose      callback invoked when the player presses the Back button
     */
    public HealMenuView(BattleMenuController menuCtrl,
                        Consumer<BattleMenuController.PotionEntry> onUsePotion,
                        Runnable onRest,
                        Runnable onClose) {
        this.menuCtrl    = menuCtrl;
        this.onUsePotion = onUsePotion;
        this.onRest      = onRest;
        this.onClose     = onClose;
    }

    /**
     * Builds the overlay {@link Pane} containing the canvas and dynamic buttons.
     *
     * @return the constructed pane (initially empty — call {@link #refresh} to populate)
     */
    public Pane build() {
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();
        root = new Pane(canvas);
        return root;
    }

    /**
     * Rebuilds the menu buttons and canvas for the given list of potions.
     * Must be called before the panel becomes visible.
     *
     * @param potions the current list of potions in the player's inventory
     */
    public void refresh(List<BattleMenuController.PotionEntry> potions) {
        lastPotions = potions;

        for (Button b : dynamicBtns) root.getChildren().remove(b);
        dynamicBtns.clear();

        int rows = potions.size() + 2; // potions + Rest + Back
        double totalH = rows * ROW_H + 50;
        double startY = PY + 34;

        for (int i = 0; i < potions.size(); i++) {
            BattleMenuController.PotionEntry entry = potions.get(i);
            Button btn = makePotionBtn(entry);
            btn.setLayoutX(PX + PW - 90);
            btn.setLayoutY(startY + i * ROW_H + (ROW_H - 26) / 2.0);
            final BattleMenuController.PotionEntry e = entry;
            btn.setOnAction(ev -> onUsePotion.accept(e));
            dynamicBtns.add(btn);
            root.getChildren().add(btn);
        }

        // Rest button
        double restY = startY + potions.size() * ROW_H + 4;
        Button restBtn = makeRestBtn();
        restBtn.setLayoutX(PX + PW - 90);
        restBtn.setLayoutY(restY);
        restBtn.setOnAction(e -> onRest.run());
        dynamicBtns.add(restBtn);
        root.getChildren().add(restBtn);

        // Back button
        Button backBtn = makeBackBtn();
        backBtn.setLayoutX(PX + PW - 90);
        backBtn.setLayoutY(restY + ROW_H);
        backBtn.setOnAction(e -> onClose.run());
        dynamicBtns.add(backBtn);
        root.getChildren().add(backBtn);

        redraw(potions);
    }

    /**
     * Redraws the panel and updates button states for the given potion list.
     * Called every frame while the heal panel is visible.
     *
     * @param potions the current list of potions in the player's inventory
     */
    public void update(List<BattleMenuController.PotionEntry> potions) {
        redraw(potions);
        // update button disabled states
        int bi = 0;
        for (BattleMenuController.PotionEntry entry : potions) {
            if (bi < dynamicBtns.size()) {
                dynamicBtns.get(bi).setDisable(!entry.hasStock());
                bi++;
            }
        }
    }

    private void redraw(List<BattleMenuController.PotionEntry> potions) {
        int rows  = potions.size() + 2;
        double ph = rows * ROW_H + 50;
        gc.clearRect(PX - 2, PY - 2, PW + 4, ph + 10);

        // Panel bg
        gc.setFill(Color.rgb(10, 25, 15, 0.92));
        gc.fillRoundRect(PX - 2, PY - 2, PW + 4, ph, 14, 14);
        gc.setStroke(Color.web("#388e3c", 0.9));
        gc.setLineWidth(2);
        gc.strokeRoundRect(PX - 2, PY - 2, PW + 4, ph, 14, 14);

        // Title
        gc.setFill(Color.web("#388e3c", 0.9));
        gc.fillRoundRect(PX - 2, PY - 2, PW + 4, 28, 14, 14);
        gc.fillRect(PX - 2, PY + 10, PW + 4, 14);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ITEMS", PX + PW / 2.0, PY + 15);
        gc.setTextAlign(TextAlignment.LEFT);

        double startY = PY + 34;

        if (potions.isEmpty()) {
            gc.setFill(Color.web("#aaaaaa"));
            gc.setFont(Font.font("Arial", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No potions in bag", PX + PW / 2.0, startY + 22);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        for (int i = 0; i < potions.size(); i++) {
            BattleMenuController.PotionEntry entry = potions.get(i);
            double ry = startY + i * ROW_H;
            boolean hasStock = entry.hasStock();

            // Row bg
            Color rowBg = (i % 2 == 0)
                    ? Color.rgb(30, 50, 35, 0.7)
                    : Color.rgb(20, 40, 25, 0.7);
            gc.setFill(rowBg);
            gc.fillRoundRect(PX + 4, ry + 2, PW - 8, ROW_H - 4, 8, 8);

            // Icon circle
            Color potCol = getPotionColor(entry.name());
            gc.setFill(hasStock ? potCol : potCol.deriveColor(0, 0.3, 0.5, 1));
            gc.fillOval(PX + 12, ry + 10, 18, 18);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("HP", PX + 21, ry + 23);
            gc.setTextAlign(TextAlignment.LEFT);

            // Potion name
            gc.setFill(hasStock ? Color.WHITE : Color.web("#666666"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(entry.name(), PX + 36, ry + 17);

            // Count badge
            String countStr = "x" + entry.count();
            gc.setFill(hasStock ? potCol : Color.web("#444444"));
            gc.fillRoundRect(PX + PW - 130, ry + 8, 36, 20, 6, 6);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(countStr, PX + PW - 112, ry + 22);
            gc.setTextAlign(TextAlignment.LEFT);

            // Empty label
            if (!hasStock) {
                gc.setFill(Color.web("#ff5252"));
                gc.setFont(Font.font("Arial", 10));
                gc.fillText("EMPTY", PX + 36, ry + 30);
            }
        }

        // Rest row
        double restY = startY + potions.size() * ROW_H;
        gc.setFill(Color.rgb(50, 50, 20, 0.7));
        gc.fillRoundRect(PX + 4, restY + 2, PW - 8, ROW_H - 4, 8, 8);
        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Rest  (+10% HP, no items)", PX + 12, restY + 22);
    }

    private Color getPotionColor(String name) {
        if (name.contains("Small"))  return Color.web("#e53935");
        if (name.contains("Medium")) return Color.web("#ff9800");
        if (name.contains("Big"))    return Color.web("#43a047");
        return Color.web("#9c27b0");
    }

    private Button makePotionBtn(BattleMenuController.PotionEntry entry) {
        Button b = new Button(entry.hasStock() ? "Use" : "Empty");
        b.setPrefWidth(80);
        b.setPrefHeight(26);
        b.setDisable(!entry.hasStock());
        Color c   = entry.hasStock() ? Color.web("#43a047") : Color.web("#555555");
        String hex = toHex(c);
        String s = "-fx-background-color:" + hex + ";-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:11px;"
                + "-fx-background-radius:6;-fx-cursor:hand;";
        b.setStyle(s);
        return b;
    }

    private Button makeRestBtn() {
        Button b = new Button("Rest");
        b.setPrefWidth(80);
        b.setPrefHeight(26);
        String s = "-fx-background-color:#c8a800;-fx-text-fill:#1a1a00;"
                + "-fx-font-weight:bold;-fx-font-size:11px;"
                + "-fx-background-radius:6;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(s.replace("#c8a800", "#ffd700")));
        b.setOnMouseExited(e  -> b.setStyle(s));
        return b;
    }

    private Button makeBackBtn() {
        Button b = new Button("Back");
        b.setPrefWidth(80);
        b.setPrefHeight(26);
        String s = "-fx-background-color:#555;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:11px;"
                + "-fx-background-radius:6;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(s.replace("#555", "#777")));
        b.setOnMouseExited(e  -> b.setStyle(s));
        return b;
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()   * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue()  * 255));
    }
}