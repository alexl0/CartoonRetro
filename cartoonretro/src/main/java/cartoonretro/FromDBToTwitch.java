package cartoonretro;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import cartoonretro.model.Database;
import cartoonretro.model.Episode;
import cartoonretro.model.Series;
import cartoonretro.obs.OBSController;
import cartoonretro.vlc.VLCController;
import cartoonretro.InputOutput.InputOutput;

/**
 * This class reads the video information from db, executes the video and streams to twitch.
 * Execute this class after executing FromFilesToDB.java.
 */
public class FromDBToTwitch {

	// Passwords and api keys
	private static String chatGPTApiKey;
	private static String twitchStreamKey;
	private static String obsWebSocketPass;

	private static OBSController obsController;
	private static VLCController vlcController;

	private static List<Series> seriesList;

	public static void main(String[] args) {

		//Read keys
		readProperties();

		//Generate series from DB
		try {
			seriesList = Database.retrieveSeriesFromDB();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		InputOutput.writeSeriesFileTxt(seriesList, 0, "DB");


		//Connect to OBS
		obsController = new OBSController(obsWebSocketPass);
		obsController.connect();

		//Simple example to reproduce
		vlcController = new VLCController();

		/*for(Series series : seriesList) {
			if(series.getNameOfSerie().toLowerCase().equals("Doraemon (2005)")) {
				for(Episode episode : series.getEpisodes()) {
					if(episode.getNameOfEpisode().toLowerCase().equals("Pintando el mundo ┃ El día libre de Doraemon")) {
						vlcController.playVideo(series.getPath() + "\\" + episode.getFileName(), episode.getWidth(), episode.getHeight());

						//Calculate aspect ratio
						String aspectRatio = calculateAspectRatio(episode.getWidth(), episode.getHeight());
						obsController.setScene("Series"+ aspectRatio);

						try {
							Thread.sleep(episode.getDurationSeconds()*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}*/

		//Test
		//playEpisodeFromFileNameAndSerie("Doraemon (2005)", "Pesca de andar por casa");
		//playEpisodeFromFileNameAndSerie("Dragon Ball GT", "El regreso de Goku. La ira del guerrero Oob");//1440x1080 (4:3)
		//playEpisodeFromFileNameAndSerie("Pokémon", "027~~019~XYZ.mkv");//1280x720 (16:9)
		//playEpisodeFromFileNameAndSerie("Pokémon", "039~~019~XYZ.mkv");//1280x6.. (casi 16:9)
		//playEpisodeFromFileNameAndSerie("Pokémon", "032~~018~XY - Expediciones en Kalos.mkv");//1024x576 (16:9)
		playEpisodeFromFileNameAndSerie("Ben 10 (2016)", "021~La niebla del páramo~003.mkv");//960x540 (16:9)
		//playEpisodeFromFileNameAndSerie("Ben 10 Alien Force(2008)", "013~~001.avi");//960x720 (4:3) //TODO bordes negros que te cagas en esos episodios (culpa del vídeo)
		//playEpisodeFromFileNameAndSerie("Ben 10 Alien Force(2008)", "001~~001.AVI");//720x540 (4:3)
		//playEpisodeFromFileNameAndSerie("Ben 10 Serie original (2005)", "008~~001.avi");//720x544 (casi 4:3)
		//playEpisodeFromFileNameAndSerie("Ben 10 Ultimate (2010)", "020~~001.avi");//720x576 (casi 4:3)
		//playEpisodeFromFileNameAndSerie("El Chicho Terremoto", "038~Chicho Contra Todo.avi");//384x288 (4:3)



		//TwitchAPI twitchAPI = new TwitchAPI(twitchStreamKey);

		//ChatGPTClient chatGPTClient = new ChatGPTClient(chatGPTApiKey);
	}

	/**
	 * Play an episode from a serie. Searchs for a serie that contains the parameter episodeName.
	 * @param seriesName
	 * @param episodeName
	 */
	private static void playEpisodeFromFileNameAndSerie(String seriesName, String fileName) {
		Optional<Series> searchSerie = seriesList.stream().filter(series -> series.getNameOfSerie().equals(seriesName)).findFirst();
		if (searchSerie.isPresent()) {
			Series foundSeries = searchSerie.get();
			Optional<Episode> searchEpisode = foundSeries.getEpisodes().stream().filter(episode -> episode.getFileName().toLowerCase().contains(fileName.toLowerCase())).findFirst();
			if (searchEpisode.isPresent()) {
				Episode foundEpisode = searchEpisode.get();
				vlcController.playEpisode(foundSeries.getPath() + "\\" + foundEpisode.getFileName(), foundEpisode.getWidth(), foundEpisode.getHeight());
				//Calculate aspect ratio
				String aspectRatio = calculateAspectRatio(foundEpisode.getWidth(), foundEpisode.getHeight());
				obsController.setScene("Series"+ aspectRatio);
			} else {
				System.out.println("Episode not found");
			}
		} else {
			System.out.println("Series not found");
		}
	}

	private static void readProperties() {
		// Read properties
		Properties properties = new Properties();
		try (InputStream input = new FileInputStream("src/PASSWORDS.properties")) {
			properties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// Access the properties using the keys defined in your .properties file
		chatGPTApiKey = properties.getProperty("chatgpt_api_key");
		twitchStreamKey = properties.getProperty("twitch_stream_key");
		obsWebSocketPass = properties.getProperty("obs_websocket_pass");
	}

	public static String calculateAspectRatio(int width, int height) {
		double aspectRatio = (double) width / height;

		// Define common aspect ratios with a small error margin
		double[] ratios = { 4.0 / 3, 16.0 / 9 };

		// Check if the aspect ratio is within the error margin of any defined ratio
		for (double ratio : ratios) {
			if (Math.abs(aspectRatio - ratio) <= 0.2) {
				return ratio == 4.0 / 3 ? "4:3" : "16:9";
			}
		}

		// If no match is found, return the actual aspect ratio
		return String.format("%d:%d", width, height);
	}


}
