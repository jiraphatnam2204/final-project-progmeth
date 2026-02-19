package logic.item.potion;

import logic.base.BasePotion;
import logic.creatures.Player;

public class HealPotion extends BasePotion {
<<<<<<< HEAD:src/main/java/logic/item/potion/SmallHealthPotion.java
    public HealPotion(){
        super("Small Health Potion",20);
||||||| 0bdb9de:src/main/java/logic/item/potion/HealPotion.java
    public HealPotion(){
        super("Heal Potion",0.2);
=======
    public HealPotion() {
        super("Heal Potion", 0.2); // Heal 20% of max health
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590:src/main/java/logic/item/potion/HealPotion.java
    }

    @Override
<<<<<<< HEAD:src/main/java/logic/item/potion/SmallHealthPotion.java
    public void consume(Player p){
        p.heal(10);
||||||| 0bdb9de:src/main/java/logic/item/potion/HealPotion.java
    public void consume(Player p){
        double increaseHealth = p.getMaxHealth()*getStat();
        p.setHealth(p.getHealth+increaseHealth);
=======
    public void consume(Player p) {
        int healAmount = (int)(p.getMaxHealth() * getStat());
        int newHealth = p.getHealth() + healAmount;

        if (newHealth > p.getMaxHealth()) {
            newHealth = p.getMaxHealth();
        }
        p.setHealth(newHealth);
>>>>>>> 1abf33e7d1f929facf2c9ceaaad430d9ac58d590:src/main/java/logic/item/potion/HealPotion.java
    }
}