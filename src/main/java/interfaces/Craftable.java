package interfaces;
import logic.creatures.Player;
import logic.util.ItemCounter;
import java.util.ArrayList;
public interface Craftable {
    int getCraftingPrice();
    ArrayList<ItemCounter> getRecipe();
    boolean canCraft(Player p);
    void craft(Player p);
}
