package scenes.mainmenu;

import application.SceneManager;

import java.util.Random;

/**
 * Controller for the main-menu scene.
 * Manages an array of animated falling stars used as a parallax background effect.
 */
public class MainMenuController {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final int STAR_COUNT = 120;

    private final double[] starX = new double[STAR_COUNT];
    private final double[] starY = new double[STAR_COUNT];
    private final double[] starSpd = new double[STAR_COUNT];
    private final double[] starR = new double[STAR_COUNT];

    /**
     * Creates a new MainMenuController and initialises star positions,
     * speeds, and radii with random values.
     */
    public MainMenuController() {
        Random rng = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rng.nextDouble() * W;
            starY[i] = rng.nextDouble() * H;
            starSpd[i] = 0.3 + rng.nextDouble() * 0.8;
            starR[i] = 1 + rng.nextDouble() * 2;
        }
    }

    /**
     * Advances every star's Y position by its individual speed.
     * Stars that fall off the bottom of the screen are reset to the top
     * at a random X position, creating the illusion of infinite falling stars.
     *
     * @param dt elapsed time since the last frame, in seconds
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

    /**
     * Returns the X-coordinate array for all stars.
     *
     * @return star X positions
     */
    public double[] getStarX() {
        return starX;
    }

    /**
     * Returns the Y-coordinate array for all stars.
     *
     * @return star Y positions
     */
    public double[] getStarY() {
        return starY;
    }

    /**
     * Returns the radius array for all stars.
     *
     * @return star radii
     */
    public double[] getStarR() {
        return starR;
    }

    /**
     * Returns the total number of stars managed by this controller.
     *
     * @return the star count
     */
    public int getStarCount() {
        return STAR_COUNT;
    }
}