package logic.pickaxe;

import interfaces.Mineable;
import logic.base.BaseItem;
import logic.creatures.Player;

import java.util.List;

public class Pickaxe {
    private String name;
    private int power;

    public Pickaxe(String name, int power) {
        this.name = name;
        this.power = Math.max(1, power);
    }

    public static Pickaxe createWoodenPickaxe() {
        return new Pickaxe("Wooden Pickaxe", 1);
    }

    public static Pickaxe createNormalStonePickaxe() {
        return new Pickaxe("Normal Stone Pickaxe", 2);
    }

    public static Pickaxe createHardStonePickaxe() {
        return new Pickaxe("Hard Stone Pickaxe", 5);
    }

    public static Pickaxe createIronPickaxe() {
        return new Pickaxe("Iron Pickaxe", 12);
    }

    public static Pickaxe createPlatinumPickaxe() {
        return new Pickaxe("Platinum Pickaxe", 27);
    }

    public static Pickaxe createMithrilPickaxe() {
        return new Pickaxe("Mithril Pickaxe", 45);
    }

    public static Pickaxe createVibraniumPickaxe() {
        return new Pickaxe("Vibranium Pickaxe", 100);
    }

    public List<BaseItem> use(Mineable target, Player player) {
        return target.mine(power, player);
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }
}
