package audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Manages background music playback for the game.
 * Only one BGM track can play at a time; starting a new track automatically stops the previous one.
 */
public class AudioManager {

    private static MediaPlayer mediaPlayer;

    /**
     * Plays a looping background music track from the given classpath resource.
     * Any currently playing track is stopped first.
     *
     * @param resourcePath the classpath-relative path to the audio file (e.g. {@code "/sounds/bgm.mp3"})
     * @param volume       the playback volume, between {@code 0.0} (silent) and {@code 1.0} (full)
     */
    public static void playBGM(String resourcePath, double volume) {
        stopBGM();
        try {
            var url = AudioManager.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("Not found: " + resourcePath);
                return;
            }

            Media media = new Media(url.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(volume);
            mediaPlayer.play();

        } catch (Exception e) {
            System.out.println("Could not play: " + e.getMessage());
        }
    }

    /**
     * Stops the currently playing background music, if any.
     */
    public static void stopBGM() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    /**
     * Adjusts the volume of the currently playing track.
     * Does nothing if no track is playing.
     *
     * @param volume the new volume, between {@code 0.0} and {@code 1.0}
     */
    public static void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }
}