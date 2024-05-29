package cartoonretro.vlc;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;

import java.awt.*;

public class VLCController {

	String[] args = {
			"--video-filter", 
			"transform",
			"--transform-type",
			"hflip",
			"--blur-factor",
			"127"
	};

	public void playEpisode(String path, int episodeWidth, int episodeHeight, String so) {
		SwingUtilities.invokeLater(() -> {

			// Create the media player component
			//TODO differentiate linux from windows here, in windos args doesnt give any errors
			//EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent(args);
			EmbeddedMediaPlayerComponent mediaPlayerComponent;
			if(so.equals("windows"))
				mediaPlayerComponent = new EmbeddedMediaPlayerComponent(args);
			else // linux
				mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

			// Create the JFrame to display the video
			JFrame frame = new JFrame("VLCJ Video Player");
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setUndecorated(true);
			frame.setContentPane(mediaPlayerComponent);

			if(so.equals("windows"))
				frame.setPreferredSize(new Dimension(episodeWidth, episodeHeight + 28)); // Add 28 due to the 28 pixels of the window bar
			else // linux
				frame.setPreferredSize(new Dimension(episodeWidth, episodeHeight)); // Add 28 due to the 28 pixels of the window bar

			frame.pack();
			frame.setVisible(true);

			MediaPlayer mediaPlayer = mediaPlayerComponent.mediaPlayer();

			mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
				@Override
				public void finished(MediaPlayer mediaPlayer) {
					// This method is called when playback reaches the end of the media
					System.out.println("Video: " + path + " playback finished");

					// Dispose of the media player component
					//mediaPlayerComponent.release(); //We cannot do this because it closes itself forever and stops the program

					// Close the JFrame
					frame.dispose(); 

					// Add your code to handle what happens after the video ends
					// For example, you can close the video window or play the next episode
				}
			});

			mediaPlayer.media().play(path);
		});
	}

}
