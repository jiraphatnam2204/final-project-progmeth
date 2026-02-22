package scenes.mainmenu;

import application.SceneManager;

import java.util.Random;

/**
 * MainMenuController — the "brain" of the main menu.
 * <p>
 * Responsibility: ONLY animation state management.
 * - Holds the 120 falling-star positions, speeds, and sizes
 * - Updates star positions each frame (moves them downward)
 * - Wraps stars back to the top when they fall off-screen
 * <p>
 * Why put this in a controller?  Even though it's purely visual data,
 * separating it means the View can be replaced (e.g. different background style)
 * without touching the animation math.
 */
public class MainMenuController {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final int STAR_COUNT = 120;

    // Star arrays — public-read so MainMenuView can draw them
    private final double[] starX = new double[STAR_COUNT];
    private final double[] starY = new double[STAR_COUNT];
    private final double[] starSpd = new double[STAR_COUNT]; // pixels per frame
    private final double[] starR = new double[STAR_COUNT]; // radius

    public MainMenuController() {
        Random rng = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rng.nextDouble() * W;
            starY[i] = rng.nextDouble() * H;
            starSpd[i] = 0.3 + rng.nextDouble() * 0.8;
            starR[i] = 1 + rng.nextDouble() * 2;
        }
    }

    // ── Per-frame update ─────────────────────────────────────────────────────

    /**
     * Moves each star downward by its speed.
     * Stars that fall past the bottom wrap back to the top at a random X.
     *
     * @param dt time elapsed since last frame (in seconds) — not currently used
     *           but good practice to accept for future speed scaling
     */
    public void update(double dt) {
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpd[i];
            if (starY[i] > H) {
                starY[i] = 0;
                starX[i] = Math.random() * W;
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public double[] getStarX() {
        return starX;
    }

    public double[] getStarY() {
        return starY;
    }

    public double[] getStarR() {
        return starR;
    }

    public int getStarCount() {
        return STAR_COUNT;
    }
}
