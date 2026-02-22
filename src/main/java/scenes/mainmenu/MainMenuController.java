package scenes.mainmenu;

import application.SceneManager;

import java.util.Random;

public class MainMenuController {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final int STAR_COUNT = 120;

    private final double[] starX = new double[STAR_COUNT];
    private final double[] starY = new double[STAR_COUNT];
    private final double[] starSpd = new double[STAR_COUNT];
    private final double[] starR = new double[STAR_COUNT];

    public MainMenuController() {
        Random rng = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rng.nextDouble() * W;
            starY[i] = rng.nextDouble() * H;
            starSpd[i] = 0.3 + rng.nextDouble() * 0.8;
            starR[i] = 1 + rng.nextDouble() * 2;
        }
    }

    // Updates the position of every star frame-by-frame.
    // Think of this like a digital waterfall. We loop through every single star and
    // push it down the screen by adding its unique speed to its Y-coordinate.
    // If a star falls past the bottom edge of the screen (starY > H), we instantly
    // teleport it back to the top (starY = 0) at a brand-new random X position.
    // This creates the illusion of infinite falling stars using only 120 objects.
    public void update(double dt) {
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpd[i];
            if (starY[i] > H) {
                starY[i] = 0;
                starX[i] = Math.random() * W;
            }
        }
    }

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