package application;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.text.*;

import interfaces.Craftable;
import logic.base.BaseArmor;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.item.armor.*;
import logic.item.weapon.*;
import logic.pickaxe.Pickaxe;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;

public class CraftingScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final Player player;
    private final Pickaxe[] pickaxeHolder;
    private final Runnable onClose;

    private final List<Craftable> recipes = new ArrayList<>();

    private Canvas canvas;
    private GraphicsContext gc;

    private String feedbackMsg = "";
    private Color feedbackColor = Color.YELLOW;

    public CraftingScene(Player player, Pickaxe[] pickaxeHolder, Runnable onClose) {
        this.player = player;
        this.pickaxeHolder = pickaxeHolder;
        this.onClose = onClose;
        buildRecipeList();
    }

    private void buildRecipeList() {
        recipes.add(new StoneSword());
        recipes.add(new HardstoneSword());
        recipes.add(new IronSword());
        recipes.add(new PlatinumSword());
        recipes.add(new MithrilSword());
        recipes.add(new VibraniumSword());

        recipes.add(new StoneArmor());
        recipes.add(new HardstoneArmor());
        recipes.add(new IronArmor());
        recipes.add(new PlatinumArmor());
        recipes.add(new MithrilArmor());
        recipes.add(new VibraniumArmor());
    }

    public Pane build() {
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

        int cols=4, cardW=210, cardH=150, sx=18, sy=130, gx=232, gy=165;

        for (int i = 0; i < recipes.size(); i++) {
            Craftable item = recipes.get(i);

            int col=i%cols, row=i/cols;
            double bx = sx + col*gx + cardW/2.0 - 45;
            double by = sy + row*gy + cardH - 30;

            Button btn = makeBtn("Craft");
            btn.setLayoutX(bx);
            btn.setLayoutY(by);

            final int fi = i;
            btn.setOnAction(e -> handleCraft(fi));

            root.getChildren().add(btn);
        }

        Button backBtn = makeBtn("← Back");
        backBtn.setPrefWidth(120);
        backBtn.setLayoutX(W/2.0 - 60);
        backBtn.setLayoutY(H-52);
        backBtn.setOnAction(e -> onClose.run());
        root.getChildren().add(backBtn);

        redraw();
        return root;
    }

    private void handleCraft(int index) {
        Craftable cr = recipes.get(index);

        if (cr.canCraft(player)) {
            cr.craft(player);

            if (cr instanceof BaseArmor arm) {
                arm.equip(player);
                feedbackMsg = "✓ Equipped " + ((logic.base.BaseItem)cr).getName();
            }
            else if (cr instanceof BaseWeapon wpn) {
                player.addBonus(wpn.getDmg(), 0, 0, 0);
                feedbackMsg = "✓ Crafted " + ((logic.base.BaseItem)cr).getName();
            }

            feedbackColor = Color.LIMEGREEN;
        } else {
            feedbackMsg = "✗ Missing materials or gold!";
            feedbackColor = Color.web("#ff5252");
        }

        redraw();
    }

    public void update() {
        redraw();
    }

    private void redraw() {
        gc.setFill(Color.rgb(10,20,40,0.95));
        gc.fillRect(0,0,W,H);

        gc.setFill(Color.web("#80cbc4"));
        gc.setFont(Font.font("Georgia",FontWeight.BOLD,34));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚒ CRAFTING STATION", W/2.0, 55);

        gc.setFont(Font.font("Arial",14));
        gc.setFill(Color.WHITE);
        gc.fillText("Gold: "+player.getGold()+
                        " | ATK: "+player.getAttack()+
                        " | DEF: "+player.getDefense()+
                        " | HP: "+player.getHealth()+"/"+player.getMaxHealth(),
                W/2.0, 85);

        gc.setTextAlign(TextAlignment.LEFT);

        int cols=4, cardW=210, cardH=150, sx=18, sy=130, gx=232, gy=165;

        for (int i = 0; i < recipes.size(); i++) {
            Craftable item = recipes.get(i);
            int col=i%4, row=i/4;
            double x=sx+col*gx, y=sy+row*gy;

            boolean can = item.canCraft(player);

            gc.setFill(can ? Color.rgb(20,40,70,0.95)
                    : Color.rgb(40,20,20,0.95));
            gc.fillRoundRect(x,y,cardW,cardH,10,10);

            String name = ((logic.base.BaseItem)item).getName();

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(name, x+cardW/2.0, y+20);

            gc.setFont(Font.font("Arial",10));
            gc.setFill(Color.web("#ffd700"));
            gc.fillText(item.getCraftingPrice()+"g", x+cardW/2.0, y+40);

            gc.setTextAlign(TextAlignment.LEFT);
        }

        if (!feedbackMsg.isEmpty()) {
            gc.setFill(Color.rgb(0,0,0,0.85));
            gc.fillRoundRect(W/2.0-200,H-90,400,32,10,10);

            gc.setFill(feedbackColor);
            gc.setFont(Font.font("Arial",FontWeight.BOLD,14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(feedbackMsg, W/2.0, H-68);

            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private Button makeBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(90);
        b.setPrefHeight(24);
        b.setStyle("-fx-background-color:#00acc1;-fx-text-fill:white;");
        return b;
    }
}