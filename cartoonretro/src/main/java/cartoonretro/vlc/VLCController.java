package cartoonretro.vlc;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;

public class VLCController {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Create the media player component
            EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

            // Create the JFrame to display the video
            JFrame frame = new JFrame("VLCJ Video Player");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(mediaPlayerComponent);
            frame.setPreferredSize(new Dimension(800, 600));
            frame.pack();
            frame.setVisible(true);

            // Play the video (replace "/path/to/your/video.mp4" with your video file path)
            MediaPlayer mediaPlayer = mediaPlayerComponent.mediaPlayer();
            mediaPlayer.media().play("E:\\PCEXTERNO\\Completo Descargado\\Naruto\\Naruto\\Naruto - Capitulo 001 - Entra En Escena Naruto Uzumaki.mp4");
        });
    }
}
