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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

	// Path to the folder containing series folders. Use a route like this: E:\\PCEXTERNO\\Completo
	static final String route = "E:\\PCEXTERNO\\Completo";

	public static void main(String[] args) {
		readProperties();
		long time1 = System.currentTimeMillis();
		List<Series> seriesList = createSeriesFromRoute(route);
		long time2=System.currentTimeMillis();
		double executionTimeMillis = time2 - time1;
		double executionTimeMinutes = executionTimeMillis / 1000.0 / 60.0;

		// I print everything to check that everything is alright
		try (PrintWriter writer = new PrintWriter(new FileWriter("SeriesAndEpisodesList.txt"))) {
			for (Series series : seriesList) {
				writer.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nSeries Name: " + series.getNameOfSerie() + "Number of episodes: " + series.getNumberOfEpisodes());
				for (Episode episode : series.getEpisodes()) {
					writer.print("Episode Number: " + episode.getEpisodeNumber());
					writer.print("\t|||||\tDuration Seconds: " + episode.getDurationSeconds());
					writer.print("\t|||||\tDimensions: " + episode.getWidth() + "x" + episode.getHeight());
					writer.print("\t|||||\tSeasonNumber: " + episode.getSeasonNumber());
					writer.print("\t|||||\tSeries Name: " + episode.getNameOfSerie());
					writer.print("\t|||||\tSeasonName: " + episode.getSeasonName());
					writer.print("\t|||||\tEpisode Name: " + episode.getNameOfEpisode());
					writer.println("\t|||||\tFile Name: " + episode.getFileName());
				}
			}
			writer.println("\n\n\n\n\n\n\n\n\n------------------- Execution time in minutes: " + executionTimeMinutes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		//Ejemplo para ver como se reproduce
		VLCController vlcController = new VLCController();
		for(Series series : seriesList)
			if(series.getNameOfSerie().equals("Naruto"))
				for(Episode episode : series.getEpisodes())
					if(episode.getEpisodeNumber()==220)
						vlcController.playVideo(series.getPath() + "\\" + episode.getFileName(), episode.getWidth(), episode.getHeight());
		 */


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

					// Only process video files
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
							episode.setNameOfSerie(series.getNameOfSerie());

							// Extract episode number and name from the file name
							String fileName = videoFile.getName().replace(".mp4", "")
									.replace(".mkv", "")
									.replace(".avi", "");
							String[] parts = fileName.split("~", 4);
							try {
								if (parts.length == 4) {
									episode.setEpisodeNumber(Integer.parseInt(parts[0]));
									episode.setNameOfEpisode(parts[1]);
									episode.setSeasonNumber(Integer.parseInt(parts[2]));
									episode.setSeasonName(parts[3]);
								} else if (parts.length == 3) {
									episode.setEpisodeNumber(Integer.parseInt(parts[0]));
									episode.setNameOfEpisode(parts[1]);
									episode.setSeasonNumber(Integer.parseInt(parts[2]));
								} else if (parts.length == 2) {
									episode.setEpisodeNumber(Integer.parseInt(parts[0]));
									episode.setNameOfEpisode(parts[1]);
								} else {
									episode.setNameOfEpisode(fileName);
									episode.setEpisodeNumber(-1); // Invalid episode number
								}
							} catch (NumberFormatException e) {
								System.out.println("Invalid episode number or season number");
								episode.setEpisodeNumber(-1); // Invalid episode number
								episode.setNameOfEpisode(fileName);
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
			if (durationString != null) {
				// Remove any trailing decimals if present
				durationString = durationString.split("\\.")[0];
				int durationInSeconds = Integer.parseInt(durationString) / 1000;

				return durationInSeconds;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
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