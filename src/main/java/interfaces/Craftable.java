package interfaces;

import logic.util.ItemCounter;

import java.util.ArrayList;

public interface Craftable {
    public int getCraftingPrice();
    public ArrayList<ItemCounter> getRecipe();
    public boolean canCraft(Player p);
    public void craft(Player p);
}
