package cartoonretro;

// Project stuff
import cartoonretro.chatbot.ChatGPTClient;
import cartoonretro.obs.OBSController;
import cartoonretro.twitch.TwitchAPI;
import cartoonretro.vlc.VLCController;
import cartoonretro.model.Episode;
import cartoonretro.model.Series;
// Input / output stuff (to read the properties file with the passwords and api keys)
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
// Data structures stuff
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;


public class Main {

	// Passwords and api keys
	private static String chatGPTApiKey;
	private static String twitchStreamKey;
	private static String obsWebSocketPass;

	// Path to the folder containing series folders TODO the series doesn't get the path when running createSeriesFromRoute()
	static final String route = "E:\\PCEXTERNO\\Completo";

	public static void main(String[] args) {
		readProperties();
		List<Series> seriesList = createSeriesFromRoute(route);

		// Now you have a list of Series objects with their corresponding episodes
		// You can use this list to set up your schedule or perform other operations
		for (Series series : seriesList) {
			System.out.println("Series Name: " + series.getNameOfSerie());
			for (Episode episode : series.getEpisodes()) {
				System.out.print("Episode Number: " + episode.getEpisodeNumber());
				System.out.print("\t|||||\tEpisode Name: " + episode.getNameOfEpisode());
				System.out.print("\t|||||\tFile Name: " + episode.getFileName());
				System.out.print("\t|||||\tDuration Seconds: " + episode.getDurationSeconds());
				System.out.println("\t|||||\tDimensions: " + episode.getWidth() + "x" + episode.getHeight());
			}
			System.out.println("-------------------");
		}

		//Ejemplo para ver como se reproduce
		VLCController vlcController = new VLCController();
		for(Series series : seriesList)
			if(series.getNameOfSerie().equals("Naruto"))
				for(Episode episode : series.getEpisodes())
					if(episode.getEpisodeNumber()==222)
						vlcController.playVideo(series.getPath() + "\\" + episode.getFileName(), episode.getWidth(), episode.getHeight());



		//		OBSController obsController = new OBSController(obsWebSocketPass);
		//		obsController.connect();

		//TwitchAPI twitchAPI = new TwitchAPI(twitchStreamKey);

		//ChatGPTClient chatGPTClient = new ChatGPTClient(chatGPTApiKey);

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

	private static List<Series> createSeriesFromRoute(String route) {
		List<Series> seriesList = new ArrayList<>();

		File routeDir = new File(route);
		if (routeDir.exists() && routeDir.isDirectory()) {
			File[] seriesDirs = routeDir.listFiles(File::isDirectory);
			if (seriesDirs != null) {
				for (File seriesDir : seriesDirs) {
					Series series = new Series();
					series.setNameOfSerie(seriesDir.getName());
					series.setPath(route + "\\" + series.getNameOfSerie());

					List<Episode> episodes = new ArrayList<>();
					File[] videoFiles = seriesDir.listFiles((dir, name) -> {
					    String lowerCaseName = name.toLowerCase();
					    return lowerCaseName.endsWith(".mp4")
					            || lowerCaseName.endsWith(".mkv")
					            || lowerCaseName.endsWith(".avi");
					});
					if (videoFiles != null) {
						for (File videoFile : videoFiles) {
							Episode episode = new Episode();
							episode.setFileName(videoFile.getName());

							// Extract episode number and name from the file name
							String fileName = videoFile.getName().replace(".mp4", "")
							        .replace(".mkv", "")
							        .replace(".avi", "");
							String[] parts = fileName.split("~", 2);
							if (parts.length == 2) {
								try {
									episode.setEpisodeNumber(Integer.parseInt(parts[0]));
									episode.setNameOfEpisode(parts[1]);
								} catch (NumberFormatException e) {
									System.out.println("Invalid episode number");
									episode.setEpisodeNumber(-1); // Invalid episode number
									episode.setNameOfEpisode(fileName);
								}
							} else {
								episode.setNameOfEpisode(fileName);
								episode.setEpisodeNumber(-1); // Invalid episode number
							}

							// Set the duration of the episode (you can obtain this information from the video file)
							episode.setDurationSeconds(getVideoDurationInSeconds(series.getPath() + "\\" + episode.getFileName())); // Replace with actual duration

							// Set the width and height of the episode
							String videoInfo = getVideoInfo(videoFile.getAbsolutePath());
							String[] dimensions = videoInfo.split("x");
							if (dimensions.length == 2) {
								try {
									episode.setWidth(Integer.parseInt(dimensions[0].trim()));
									episode.setHeight(Integer.parseInt(dimensions[1].trim()));
								} catch (NumberFormatException e) {
									System.out.println("Invalid width or height");
								}
							}

							episodes.add(episode);
						}
					}

					series.setEpisodes(episodes);
					seriesList.add(series);
				}
			}
		}

		return seriesList;
	}

	// IO output operations with video with metadata using ProcessBuilder and the mediainfo command
	public static int getVideoDurationInSeconds(String videoFilePath) {
		try {
			Path path = Paths.get(videoFilePath);

			// Execute mediainfo command and capture output
			ProcessBuilder processBuilder = new ProcessBuilder("mediainfo", "--Inform=Video;%Duration%", path.toString());
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// Read the duration from the output
			String durationString = reader.readLine();
			int durationInSeconds = Integer.parseInt(durationString) / 1000;

			return durationInSeconds;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return -1; // Failed to retrieve duration
	}
	public static String getVideoInfo(String videoFilePath) {
		try {
			Path path = Paths.get(videoFilePath);

			// Execute mediainfo command and capture output
			ProcessBuilder processBuilder = new ProcessBuilder("mediainfo", "--Inform=Video;%Width%x%Height%", path.toString());
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// Read the video info from the output
			String videoInfo = reader.readLine();

			return videoInfo;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ""; // Failed to retrieve video info
	}
}