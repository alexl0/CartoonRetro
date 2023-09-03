package cartoonretro.vlc;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;

public class VLCController {

    public void playVideo(String path, int episodeWidth, int episodeHeight) {
        SwingUtilities.invokeLater(() -> {

            // Create the media player component
            EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

            // Create the JFrame to display the video
            JFrame frame = new JFrame("VLCJ Video Player");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(mediaPlayerComponent);

            frame.setPreferredSize(new Dimension(episodeWidth, episodeHeight + 28)); // Add 28 due to the 28 pixels of the window bar
            frame.pack();
            frame.setVisible(true);

            MediaPlayer mediaPlayer = mediaPlayerComponent.mediaPlayer();

            mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    // This method is called when playback reaches the end of the media
                    System.out.println("Video playback finished");

                    // Add your code to handle what happens after the video ends
                    // For example, you can close the video window or play the next episode
                }
            });
            
            mediaPlayer.media().play(path);
        });
    }
}
