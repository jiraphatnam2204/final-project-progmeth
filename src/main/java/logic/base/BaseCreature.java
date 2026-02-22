package logic.base;
public abstract class BaseCreature {
    protected int healthPoint, maxHealthPoint, attack, defense;
    public BaseCreature(int maxHealthPoint, int attack, int defense) {
        this.maxHealthPoint = maxHealthPoint; this.healthPoint = maxHealthPoint;
        this.attack = attack; this.defense = defense;
    }
    public boolean isAlive() { return healthPoint > 0; }
    public void takeDamage(int damage) {
        int realDamage = Math.max(0, damage - defense);
        healthPoint -= realDamage;
        if (healthPoint < 0) healthPoint = 0;
    }
    public void heal(int amount) { healthPoint = Math.min(maxHealthPoint, healthPoint + amount); }
    public int getHealthPoint() { return healthPoint; }
    public void setHealthPoint(int v) { healthPoint = v; }
    public int getMaxHealthPoint() { return maxHealthPoint; }
    public void setMaxHealthPoint(int v) { maxHealthPoint = v; }
    public int getAttack() { return attack; }
    public void setAttack(int v) { attack = v; }
    public int getDefense() { return defense; }
    public void setDefense(int v) { defense = v; }
    public abstract void attack(BaseCreature target);
}
