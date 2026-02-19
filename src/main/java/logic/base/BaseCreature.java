package logic.base;

public abstract class   BaseCreature {

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

    public int getHealthPoint() {
        return healthPoint;
    }

    public void setHealthPoint(int healthPoint) {
        this.healthPoint = healthPoint;
    }

    public int getMaxHealthPoint() {
        return maxHealthPoint;
    }

    public void setMaxHealthPoint(int maxHealthPoint) {
        this.maxHealthPoint = maxHealthPoint;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public void heal(int amount) {
        healthPoint = Math.min(maxHealthPoint, healthPoint + amount);
    }

    public abstract void attack(BaseCreature target);
}
