package scenes.boss;

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

import java.util.function.IntConsumer;

/**
 * SkillMenuView
 *
 * Pokemon-style 2x2 skill grid rendered over the battle scene.
 * Each cell shows: skill name, description, type colour, and cooldown status.
 */
public class SkillMenuView {

    private static final int W  = SceneManager.W;
    private static final int H  = SceneManager.H;

    // Panel dimensions (bottom-right quadrant, like Pokemon)
    private static final double PX = W / 2.0 + 10;
    private static final double PY = H * 0.64;
    private static final double PW = W / 2.0 - 20;
    private static final double PH = H * 0.30;

    // Cell layout (2 columns x 2 rows)
    private static final double CW = PW / 2.0 - 6;
    private static final double CH = PH / 2.0 - 6;

    // Skill type colours (one per skill slot)
    private static final Color[] SKILL_COLORS = {
            Color.web("#FFD700"),  // Power Strike - gold
            Color.web("#64B5F6"),  // Shield Wall  - ice blue
            Color.web("#EF5350"),  // Berserk      - red
            Color.web("#AB47BC"),  // Soul Drain   - purple
    };

    private final BattleMenuController menuCtrl;
    private final IntConsumer onSkillSelected;  // callback with skill index
    private final Runnable onClose;

    private Canvas canvas;
    private GraphicsContext gc;
    private final Button[] skillBtns = new Button[BattleMenuController.SKILL_COUNT];
    private Button backBtn;

    public SkillMenuView(BattleMenuController menuCtrl,
                         IntConsumer onSkillSelected,
                         Runnable onClose) {
        this.menuCtrl        = menuCtrl;
        this.onSkillSelected = onSkillSelected;
        this.onClose         = onClose;
    }

    /** Builds the overlay Pane (invisible by default). */
    public Pane build() {
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        // Create 4 skill buttons (positioned inside cells)
        for (int i = 0; i < BattleMenuController.SKILL_COUNT; i++) {
            int col = i % 2;
            int row = i / 2;
            double cx = PX + col * (CW + 6);
            double cy = PY + row * (CH + 6);

            Button btn = makeSkillBtn(i);
            btn.setLayoutX(cx + CW / 2.0 - 55);
            btn.setLayoutY(cy + CH - 32);
            final int idx = i;
            btn.setOnAction(e -> onSkillSelected.accept(idx));
            skillBtns[i] = btn;
            root.getChildren().add(btn);
        }

        // Back button
        backBtn = makeBackBtn();
        backBtn.setLayoutX(PX + PW / 2.0 - 55);
        backBtn.setLayoutY(PY + PH - 20);
        backBtn.setOnAction(e -> onClose.run());
        root.getChildren().add(backBtn);

        redraw();
        return root;
    }

    /** Called every frame to keep cooldown indicators fresh. */
    public void update() {
        redraw();
        updateButtonStates();
    }

    private void redraw() {
        // Clear only the panel area
        gc.clearRect(PX - 2, PY - 2, PW + 4, PH + 60);

        // ── Outer panel background ──────────────────────────────────────────
        gc.setFill(Color.rgb(10, 10, 25, 0.92));
        gc.fillRoundRect(PX - 2, PY - 2, PW + 4, PH + 4, 14, 14);
        gc.setStroke(Color.web("#9C27B0", 0.8));
        gc.setLineWidth(2);
        gc.strokeRoundRect(PX - 2, PY - 2, PW + 4, PH + 4, 14, 14);

        // ── Title strip ────────────────────────────────────────────────────
        gc.setFill(Color.web("#9C27B0", 0.9));
        gc.fillRoundRect(PX - 2, PY - 2, PW + 4, 28, 14, 14);
        gc.fillRect(PX - 2, PY + 10, PW + 4, 14);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SKILLS", PX + PW / 2.0, PY + 15);
        gc.setTextAlign(TextAlignment.LEFT);

        // ── Skill cells ────────────────────────────────────────────────────
        for (int i = 0; i < BattleMenuController.SKILL_COUNT; i++) {
            int col = i % 2;
            int row = i / 2;
            double cx = PX + col * (CW + 6);
            double cy = PY + 30 + row * (CH + 6);

            Color skillCol = SKILL_COLORS[i];
            boolean ready  = menuCtrl.isReady(i);
            int cd         = menuCtrl.getCooldown(i);

            // Cell background
            Color bgCol = ready
                    ? skillCol.deriveColor(0, 0.4, 0.25, 0.85)
                    : Color.rgb(30, 30, 40, 0.85);
            gc.setFill(bgCol);
            gc.fillRoundRect(cx, cy, CW, CH, 10, 10);

            // Cell border
            gc.setStroke(ready ? skillCol : Color.web("#555555"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(cx, cy, CW, CH, 10, 10);

            // Skill name
            gc.setFill(ready ? Color.WHITE : Color.GRAY);
            gc.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(BattleMenuController.SKILL_NAMES[i], cx + CW / 2.0, cy + 20);

            // Skill description
            gc.setFill(ready ? Color.web("#e0e0e0") : Color.web("#666666"));
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(BattleMenuController.SKILL_DESCS[i], cx + CW / 2.0, cy + 34);

            // Cooldown indicator
            if (!ready) {
                gc.setFill(Color.web("#FF5722"));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                gc.fillText("CD: " + cd + " turns", cx + CW / 2.0, cy + CH - 34);

                // Cooldown bar
                double maxCd  = BattleMenuController.SKILL_MAX_CD[i];
                double cdPct  = cd / maxCd;
                double barW   = CW - 20;
                gc.setFill(Color.rgb(80, 20, 20));
                gc.fillRoundRect(cx + 10, cy + CH - 24, barW, 8, 4, 4);
                gc.setFill(Color.web("#FF5722"));
                gc.fillRoundRect(cx + 10, cy + CH - 24, barW * cdPct, 8, 4, 4);
            } else {
                gc.setFill(skillCol);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                gc.fillText("READY", cx + CW / 2.0, cy + CH - 28);

                // Ready pip dots
                int maxCd = BattleMenuController.SKILL_MAX_CD[i];
                double dotSize = 7;
                double dotGap  = dotSize + 3;
                double startX  = cx + CW / 2.0 - (maxCd * dotGap) / 2.0 + dotSize / 2.0;
                for (int d = 0; d < maxCd; d++) {
                    gc.setFill(skillCol.deriveColor(0, 1, 1.2, 0.9));
                    gc.fillOval(startX + d * dotGap, cy + CH - 18, dotSize, dotSize);
                }
            }

            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void updateButtonStates() {
        for (int i = 0; i < BattleMenuController.SKILL_COUNT; i++) {
            boolean ready = menuCtrl.isReady(i);
            skillBtns[i].setDisable(!ready);
            Color c = SKILL_COLORS[i];
            String hex  = toHex(c);
            String hex2 = toHex(c.brighter());
            String base = ready
                    ? "-fx-background-color:" + hex + ";-fx-text-fill:white;"
                    + "-fx-font-weight:bold;-fx-font-size:11px;"
                    + "-fx-background-radius:6;-fx-cursor:hand;"
                    : "-fx-background-color:#3a3a3a;-fx-text-fill:#666666;"
                    + "-fx-font-weight:bold;-fx-font-size:11px;"
                    + "-fx-background-radius:6;";
            skillBtns[i].setStyle(base);
        }
    }

    private Button makeSkillBtn(int idx) {
        Button b = new Button("Use");
        b.setPrefWidth(110);
        b.setPrefHeight(24);
        Color c   = SKILL_COLORS[idx];
        String hex = toHex(c);
        String s = "-fx-background-color:" + hex + ";-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:11px;"
                + "-fx-background-radius:6;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(s.replace(hex, toHex(c.brighter()))));
        b.setOnMouseExited(e  -> b.setStyle(s));
        return b;
    }

    private Button makeBackBtn() {
        Button b = new Button("Back");
        b.setPrefWidth(110);
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