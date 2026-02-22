package logic.creatures;
import logic.base.BaseCreature; import logic.base.BaseItem;
import logic.util.ItemCounter;
import java.util.ArrayList;
public class Player extends BaseCreature {
    private int gold; private ArrayList<ItemCounter> inventory; private int speed, luck;
    public Player(int hp, int attack, int defense) {
        super(hp, attack, defense); gold = 0; inventory = new ArrayList<>(); speed = 0; luck = 0;
    }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = Math.max(0, gold); }
    public ArrayList<ItemCounter> getInventory() { return inventory; }
    public void addItem(BaseItem item, int amount) {
        for (ItemCounter i : inventory) {
            if (i.getItem().getName().equals(item.getName())) {
                if (i.getItem().isStackable()) { i.addCount(amount); return; }
            }
        }
        inventory.add(new ItemCounter(item, amount));
    }
    public void addBonus(int atk, int def, int hp, int spd) {
        attack += atk; defense += def; maxHealthPoint += hp; healthPoint += hp; speed += spd;
    }
    public void removeBonus(int atk, int def, int hp, int spd) {
        attack = Math.max(0, attack - atk); defense = Math.max(0, defense - def);
        maxHealthPoint = Math.max(1, maxHealthPoint - hp);
        if (healthPoint > maxHealthPoint) healthPoint = maxHealthPoint;
        speed -= spd;
    }
    public int getHealth() { return healthPoint; }
    public int getMaxHealth() { return maxHealthPoint; }
    public void setHealth(int hp) { healthPoint = Math.max(0, Math.min(maxHealthPoint, hp)); }
    public int getStrength() { return attack; }
    public void setStrength(int v) { attack = Math.max(0, v); }
    public int getLuck() { return luck; }
    public void setLuck(int v) { luck = Math.max(0, v); }
    public int getDefense() { return defense; }
    @Override public void attack(BaseCreature target) { target.takeDamage(attack); }
}
