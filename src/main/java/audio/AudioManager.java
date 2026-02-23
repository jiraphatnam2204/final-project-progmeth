package audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {

    private static MediaPlayer mediaPlayer;

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

    public static void stopBGM() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    public static void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }
}