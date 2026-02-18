package logic.base;

public abstract class BaseCreature {

    protected int healthPoint;
    protected int maxHealthPoint;
    protected int attack;
    protected int defense;

    public BaseCreature(int maxHealthPoint, int attack, int defense) {
        this.maxHealthPoint = maxHealthPoint;
        this.healthPoint = maxHealthPoint;
        this.attack = attack;
        this.defense = defense;
    }

    public boolean isAlive() {
        return healthPoint > 0;
    }

    public void takeDamage(int damage) {
        int realDamage = Math.max(0, damage - defense);
        healthPoint -= realDamage;
        if (healthPoint < 0) healthPoint = 0;
    }

    public void heal(int amount) {
        healthPoint = Math.min(maxHealthPoint, healthPoint + amount);
    }

    public abstract void attack(BaseCreature target);
}
