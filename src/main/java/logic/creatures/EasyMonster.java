package logic.creatures;

import logic.base.BaseCreature;
import interfaces.Lootable;

public class EasyMonster extends BaseCreature implements Lootable {

    private int moneyDrop;

    public EasyMonster(int hp, int attack, int defense) {
        super(hp, attack, defense);
        this.moneyDrop = 10;
    }

    @Override
    public void attack(BaseCreature target) {
        target.takeDamage(this.attack);
    }

    @Override
    public int dropMoney() {
        return moneyDrop;
    }
}
