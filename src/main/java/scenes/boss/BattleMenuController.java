package scenes.boss;

import logic.base.BasePotion;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;

public class BattleMenuController {

    public enum MenuState { NONE, SKILLS, HEAL }
    private MenuState menuState = MenuState.NONE;

    public static final int SKILL_COUNT = 4;

    public static final String[] SKILL_NAMES = {
            "Power Strike",
            "Shield Wall",
            "Berserk",
            "Soul Drain"
    };

    public static final String[] SKILL_ICONS = { "\u26A1", "\uD83D\uDEE1", "\uD83D\uDCA2", "\uD83E\uDE78" };

    public static final String[] SKILL_DESCS = {
            "Deal 2x damage to the boss",
            "Reduce next incoming damage by 50%",
            "3 hits but lose 50% DEF next turn",
            "Damage boss + heal 30% of damage"
    };

    public static final int[] SKILL_MAX_CD = { 3, 2, 4, 5 };

    private final int[] cooldowns = new int[SKILL_COUNT];
    private boolean shieldWallActive    = false;
    private boolean berserkDebuffActive = false;

    public MenuState getMenuState()            { return menuState; }
    public void openSkills()                   { menuState = MenuState.SKILLS; }
    public void openHeal()                     { menuState = MenuState.HEAL;   }
    public void close()                        { menuState = MenuState.NONE;   }

    public int  getCooldown(int i)             { return cooldowns[i]; }
    public boolean isReady(int i)              { return cooldowns[i] <= 0; }
    public void setCooldown(int i, int turns)  { cooldowns[i] = turns; }

    public void tickCooldowns() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            if (cooldowns[i] > 0) cooldowns[i]--;
        }
    }

    public boolean isShieldWallActive()        { return shieldWallActive; }
    public boolean isBerserkDebuffActive()      { return berserkDebuffActive; }
    public void setShieldWall(boolean v)        { shieldWallActive    = v; }
    public void setBerserkDebuff(boolean v)     { berserkDebuffActive = v; }

    public List<PotionEntry> getPotions(Player player) {
        List<PotionEntry> list = new ArrayList<>();
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem() instanceof BasePotion) {
                list.add(new PotionEntry(ic));
            }
        }
        return list;
    }

    public record PotionEntry(ItemCounter counter) {
        public String name()      { return counter.getItem().getName(); }
        public int    count()     { return counter.getCount(); }
        public boolean hasStock() { return counter.getCount() > 0; }
    }
}