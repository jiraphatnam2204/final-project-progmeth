package scenes.gameover;

import application.SceneManager;
import logic.creatures.Player;

import java.util.Random;

/**
 * Controller for the game-over / victory scene.
 * Manages an array of animated particles: confetti rising upward on victory,
 * falling ash drifting downward on defeat.
 */
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

    /**
     * Creates a new GameOverController and initialises particle positions and velocities.
     * Particles rise for a victory result and fall for a defeat result.
     *
     * @param won    {@code true} if the player defeated all bosses; {@code false} on defeat
     * @param player the player whose final stats are displayed on the game-over screen
     */
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

    /**
     * Advances every particle's position by its velocity for one frame.
     * Particles that leave the screen are wrapped back to the opposite edge.
     */
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


    /**
     * Returns whether the player won the game.
     *
     * @return {@code true} if all bosses were defeated
     */
    public boolean isWon() {
        return won;
    }

    /**
     * Returns the player associated with this game-over session.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the X-coordinate array for all particles.
     *
     * @return particle X positions
     */
    public double[] getPx() {
        return px;
    }

    /**
     * Returns the Y-coordinate array for all particles.
     *
     * @return particle Y positions
     */
    public double[] getPy() {
        return py;
    }

    /**
     * Returns the radius array for all particles.
     *
     * @return particle radii
     */
    public double[] getPr() {
        return pr;
    }

    /**
     * Returns the total number of particles managed by this controller.
     *
     * @return the particle count
     */
    public int getParticleCount() {
        return PARTICLE_COUNT;
    }
}
