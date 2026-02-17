package Interfaces;

import Logic.Base.Item;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public interface Craftable {
    public ArrayList<ItemCounter> getRecipe();
    public boolean canCraft(Player p);
    public void craft(Player p);
}
