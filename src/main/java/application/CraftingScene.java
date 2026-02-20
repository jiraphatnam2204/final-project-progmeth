package application;

import javafx.scene.Scene;
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

/**
 * CraftingScene: Shows all craftable items (weapons + armour) from your logic package.
 *
 * Each card shows: the recipe, the gold cost, and whether the player has the materials.
 * Clicking "Craft" calls item.craft(player) — the exact Craftable interface from your code.
 *
 * After crafting a weapon/armour, the player's stats are boosted via addBonus().
 */
public class CraftingScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final Player    player;
    private final Pickaxe[] pickaxeHolder;
    private String feedbackMsg   = "";
    private Color  feedbackColor = Color.YELLOW;

    // All craftable items (weapons + armors from your logic.item package)
    private final List<Craftable> recipes = new ArrayList<>();

    public CraftingScene(Player player, Pickaxe[] pickaxeHolder) {
        this.player        = player;
        this.pickaxeHolder = pickaxeHolder;
        buildRecipeList();
    }

    /** Populate craftable items — all use your existing Craftable.canCraft/craft logic */
    private void buildRecipeList() {
        // Weapons
        recipes.add(new StoneSword());
        recipes.add(new HardstoneSword());
        recipes.add(new IronSword());
        recipes.add(new PlatinumSword());
        recipes.add(new MithrilSword());
        recipes.add(new VibraniumSword());
        // Armours
        recipes.add(new StoneArmor());
        recipes.add(new HardstoneArmor());
        recipes.add(new IronArmor());
        recipes.add(new PlatinumArmor());
        recipes.add(new MithrilArmor());
        recipes.add(new VibraniumArmor());
    }

    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        int cols=4, cardW=210, cardH=150, sx=18, sy=130, gx=232, gy=165;

        for (int i = 0; i < recipes.size(); i++) {
            Craftable item = recipes.get(i);
            int col=i%cols, row=i/cols;
            double bx = sx + col*gx + cardW/2.0 - 45;
            double by = sy + row*gy + cardH - 30;

            Button btn = makeBtn("Craft");
            btn.setLayoutX(bx); btn.setLayoutY(by);

            final int fi = i;
            btn.setOnAction(e -> {
                Craftable cr = recipes.get(fi);
                if (cr.canCraft(player)) {
                    cr.craft(player);  // calls YOUR Craftable.craft() — deducts items + gold
                    // Apply stat bonuses immediately
                    if (cr instanceof BaseArmor arm) {
                        arm.equip(player);
                        feedbackMsg = "✓ Equipped " + ((logic.base.BaseItem)cr).getName() + "!";
                    } else if (cr instanceof BaseWeapon wpn) {
                        player.addBonus(wpn.getDmg(), 0, 0, 0);
                        feedbackMsg = "✓ Crafted " + ((logic.base.BaseItem)cr).getName()
                                      + " (+"+wpn.getDmg()+" ATK)!";
                    }
                    feedbackColor = Color.LIMEGREEN;
                } else {
                    feedbackMsg   = "✗ Missing materials or gold!";
                    feedbackColor = Color.web("#ff5252");
                }
                redraw(gc, sx, sy, cardW, cardH, gx, gy);
            });
            root.getChildren().add(btn);
        }

        Button backBtn = makeBtn("← Back");
        backBtn.setPrefWidth(120);
        backBtn.setLayoutX(W/2.0 - 60); backBtn.setLayoutY(H-52);
        backBtn.setOnAction(e -> Main.sceneManager.showGame(player, pickaxeHolder[0]));
        root.getChildren().add(backBtn);

        redraw(gc, sx, sy, cardW, cardH, gx, gy);
        return new Scene(root, W, H);
    }

    private void redraw(GraphicsContext gc, int sx, int sy, int cw, int ch, int gx, int gy) {
        LinearGradient bg = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,Color.web("#0d1b2a")), new Stop(1,Color.web("#0a2540")));
        gc.setFill(bg); gc.fillRect(0,0,W,H);

        // Forge glow effect at top
        gc.setFill(Color.rgb(255,120,0,0.08)); gc.fillOval(W/2.0-200, -60, 400, 200);

        // Header
        gc.setFill(Color.web("#80cbc4")); gc.setFont(Font.font("Georgia",FontWeight.BOLD,34));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚒  CRAFTING STATION", W/2.0, 55);
        gc.setFill(Color.web("#b2dfdb")); gc.setFont(Font.font("Arial",14));
        gc.fillText("Gold: "+player.getGold()+"g  |  ATK: "+player.getAttack()
                    +"  |  DEF: "+player.getDefense()+"  |  HP: "+player.getHealth()+"/"+player.getMaxHealth(),
                    W/2.0, 85);
        gc.fillText("Inventory: " + inventorySummary(), W/2.0, 108);
        gc.setTextAlign(TextAlignment.LEFT);

        // Cards
        for (int i = 0; i < recipes.size(); i++) {
            Craftable item = recipes.get(i);
            int col=i%4, row=i/4;
            double x=sx+col*gx, y=sy+row*gy;

            boolean can = item.canCraft(player);
            gc.setFill(can ? Color.rgb(15,30,50,0.95) : Color.rgb(20,15,15,0.90));
            gc.fillRoundRect(x,y,cw,ch,10,10);
            Color border = can ? Color.web("#4dd0e1") : Color.web("#444");
            gc.setStroke(border); gc.setLineWidth(can?2:1);
            gc.strokeRoundRect(x,y,cw,ch,10,10);

            // Type stripe
            boolean isArmor = item instanceof BaseArmor;
            gc.setFill(isArmor ? Color.web("#1565c0",0.85) : Color.web("#880e4f",0.85));
            gc.fillRoundRect(x,y,cw,24,10,0); gc.fillRect(x,y+14,cw,10);

            String itemName = (item instanceof logic.base.BaseItem bi) ? bi.getName() : item.getClass().getSimpleName();
            gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText((isArmor?"🛡":"⚔")+" "+itemName, x+cw/2.0, y+16);

            // Stats line
            gc.setFont(Font.font("Arial",10)); gc.setFill(Color.web("#b2ebf2"));
            if (item instanceof BaseArmor arm)
                gc.fillText("+"+arm.getDef()+" DEF  +"+arm.getHp()+" HP", x+cw/2.0, y+36);
            else if (item instanceof BaseWeapon wpn)
                gc.fillText("+"+wpn.getDmg()+" ATK  cd:"+wpn.getCd()+"s", x+cw/2.0, y+36);

            // Recipe
            gc.setFill(Color.web("#90a4ae")); gc.setFont(Font.font("Arial",9));
            var recipe = item.getRecipe();
            if (recipe != null) {
                StringBuilder sb = new StringBuilder("Needs: ");
                for (int j=0;j<recipe.size();j++) {
                    if (j>0) sb.append(", ");
                    sb.append(recipe.get(j).getItem().getName()).append("×").append(recipe.get(j).getCount());
                }
                // Word-wrap manually for narrow cards
                String full = sb.toString();
                if (full.length() > 30) {
                    gc.fillText(full.substring(0,30), x+cw/2.0, y+52);
                    gc.fillText(full.substring(30), x+cw/2.0, y+63);
                } else {
                    gc.fillText(full, x+cw/2.0, y+52);
                }
            }

            // Gold cost
            gc.setFill(can ? Color.web("#ffd700") : Color.web("#757575"));
            gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
            gc.fillText(item.getCraftingPrice()+"g", x+cw/2.0, y+78);

            // Can-craft indicator
            if (can) {
                gc.setFill(Color.rgb(77,208,225,0.15)); gc.fillRoundRect(x+2,y+2,cw-4,ch-4,9,9);
            }
            gc.setTextAlign(TextAlignment.LEFT);
        }

        // Feedback
        if (!feedbackMsg.isEmpty()) {
            gc.setFill(Color.rgb(0,0,0,0.85)); gc.fillRoundRect(W/2.0-220,H-90,440,32,10,10);
            gc.setFill(feedbackColor); gc.setFont(Font.font("Arial",FontWeight.BOLD,14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(feedbackMsg, W/2.0, H-68);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private String inventorySummary() {
        StringBuilder sb = new StringBuilder();
        for (ItemCounter ic : player.getInventory()) {
            if (sb.length()>0) sb.append("  ");
            sb.append(ic.getItem().getName()).append(":").append(ic.getCount());
        }
        return sb.isEmpty() ? "(empty)" : sb.toString();
    }

    private Button makeBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(90); b.setPrefHeight(24);
        String s = "-fx-background-color:#00acc1;-fx-text-fill:white;" +
                   "-fx-font-weight:bold;-fx-font-size:11px;-fx-background-radius:5;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e->b.setStyle(s.replace("#00acc1","#00bcd4")));
        b.setOnMouseExited(e ->b.setStyle(s));
        return b;
    }
}
