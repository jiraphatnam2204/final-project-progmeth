package scenes.gameover;

import application.SceneManager;
import logic.creatures.Player;

import java.util.Random;

/**
 * GameOverController — the "brain" of the game-over screen.
 * <p>
 * Responsibility: ONLY state management.
 * - Holds whether the player won or lost
 * - Manages the 80 particle positions/velocities
 * - Updates particle positions each frame
 * <p>
 * The View decides what colour/shape each particle is — that's not our job here.
 */
public class GameOverController {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final int PARTICLE_COUNT = 80;

    private final boolean won;
    private final Player player;

    // Particle arrays — public-read so the View can draw them
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

    // ── Initialisation ────────────────────────────────────────────────────────

    private void initParticles() {
        Random rng = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] = rng.nextDouble() * W;
            py[i] = rng.nextDouble() * H;
            pvx[i] = (rng.nextDouble() - 0.5) * 2;

            // Victory: particles float UP like confetti; defeat: they fall DOWN like ash
            pvy[i] = won
                    ? -(0.5 + rng.nextDouble() * 2)
                    : (0.5 + rng.nextDouble() * 2);

            pr[i] = 2 + rng.nextDouble() * 5;
        }
    }

    // ── Per-frame update ─────────────────────────────────────────────────────

    /**
     * Moves each particle by its velocity.
     * Wraps particles around when they leave the screen.
     * Call once per animation frame (inside AnimationTimer).
     */
    public void updateParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] += pvx[i];
            py[i] += pvy[i];

            // Victory: particles fly upward — wrap from top to bottom
            if (won && py[i] < -10) {
                py[i] = H + 5;
                px[i] = Math.random() * W;
            }

            // Defeat: particles fall downward — wrap from bottom to top
            if (!won && py[i] > H + 10) {
                py[i] = -5;
                px[i] = Math.random() * W;
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isWon() {
        return won;
    }

    public Player getPlayer() {
        return player;
    }

    // Particle arrays — View uses these to draw each particle
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
