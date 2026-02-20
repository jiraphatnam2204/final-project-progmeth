package logic.creatures;
import logic.base.BaseCreature; import interfaces.Lootable;
public abstract class Monster extends BaseCreature implements Lootable {
    protected int moneyDrop;
    public Monster(int hp, int attack, int defense, int moneyDrop) {
        super(hp, attack, defense); this.moneyDrop = moneyDrop;
    }
    @Override public void attack(BaseCreature target) { target.takeDamage(attack); }
    @Override public int dropMoney() { return moneyDrop; }
}
