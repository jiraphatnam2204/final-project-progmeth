package scenes.gameover;

import application.SceneManager;
import logic.creatures.Player;

import java.util.Random;

public class GameOverController {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final int PARTICLE_COUNT = 80;

    private final boolean won;
    private final Player player;

    private final double[] px = new double[PARTICLE_COUNT];
    private final double[] py = new double[PARTICLE_COUNT];
    private final double[] pvx = new double[PARTICLE_COUNT]; // velocity X
    private final double[] pvy = new double[PARTICLE_COUNT]; // velocity Y
    private final double[] pr = new double[PARTICLE_COUNT]; // radius

    public GameOverController(boolean won, Player player) {
        this.won = won;
        this.player = player;
        initParticles();
    }


    private void initParticles() {
        Random rng = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] = rng.nextDouble() * W;
            py[i] = rng.nextDouble() * H;
            pvx[i] = (rng.nextDouble() - 0.5) * 2;

            pvy[i] = won
                    ? -(0.5 + rng.nextDouble() * 2)
                    : (0.5 + rng.nextDouble() * 2);

            pr[i] = 2 + rng.nextDouble() * 5;
        }
    }

    public void updateParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] += pvx[i];
            py[i] += pvy[i];

            if (won && py[i] < -10) {
                py[i] = H + 5;
                px[i] = Math.random() * W;
            }

            if (!won && py[i] > H + 10) {
                py[i] = -5;
                px[i] = Math.random() * W;
            }
        }
    }


    public boolean isWon() {
        return won;
    }

    public Player getPlayer() {
        return player;
    }

    public double[] getPx() {
        return px;
    }

    public double[] getPy() {
        return py;
    }

    public double[] getPr() {
        return pr;
    }

    public int getParticleCount() {
        return PARTICLE_COUNT;
    }
}
