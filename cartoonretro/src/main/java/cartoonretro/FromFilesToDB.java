package cartoonretro;

//Project stuff
import cartoonretro.InputOutput.InputOutput;
import cartoonretro.model.Database;
import cartoonretro.model.Episode;
import cartoonretro.model.Series;
// Input / output stuff (to read the properties file with the passwords and api keys)
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
// Data structures stuff
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;

/**
 * 1. Place new series in the folder.
 * 2. Execute this class to transfer the information about the video files to the database.
 * This saves a lot of time because reading the info from the video files is slow, while reading from DB is fast.
 * You only need to execute this class if you've added new series to the folder.
 */
public class FromFilesToDB {


	// TODO en algunos animes como oliver y benji en la temporada road to 2002, hay que seleccionar manualmente el audio en castellano, por defecto viene en latino.
	// igual también valdría borrar el latino del archivo de vídeo

	/**
	 * TODO También estaría guay que se tuviera en cuenta el número de personas viendo el streaming. Por ejemplo, si hay solo 1 persona, darle permisos para que cambié la serie
	 * que se está mostrando, y elija el qué capítulo quiere ver (sin afectar a la planificación, si ese capítulo se tenía que mostrar dentro de 6 horas, se mostrará igual)
	 * En cambio, si hay en el streaming 10 personas, que tengan que estar de acuerdo 4 para hacer un comando !skip. Si un 40% de los espectadores hace !skip se pasa a la siguiente serie
	 * en la planificación. Todo esto mostrándolo por comandos de chat. Cuando haces !skip, que te diga:
	 * 		"1 persona quiere saltar el capítulo y pasar a otra serie. Si 3 más hacen !skip, se saltará."
	 * 
	 *  
	 */

	// Path to the folder containing series folders. Use a route like this: E:\\PCEXTERNO\\Completo

	// Windows
	//static final String osUsed = "Windows" ;

	// Linux
	static final String osUsed = "Linux" ;
	static String route; 
	static String separator; 


	@SuppressWarnings("unused")
	public static void main(String[] args) {
		if(osUsed=="Windows") {
			route = "E:\\PCEXTERNO\\Completo";
			separator = "\\";
		}
		if(osUsed=="Linux") {
			route = "/media/al/TOSHIBA EXT/PCEXTERNO/Completo";
			separator = "/";
		}

		//Generate series from route
		List<Series> seriesList = createSeriesFromRoute(route);

		long startTime = System.currentTimeMillis();
		populatePlayOrder(seriesList);
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		double elapsedTimeSeconds = elapsedTime / 1000.0;

		long startTime2 = System.currentTimeMillis();
		writeSeriesToDB(seriesList);
		long endTime2 = System.currentTimeMillis();
		long elapsedTime2 = endTime2 - startTime2;
		double elapsedTimeMinutes2 = elapsedTime2 / 60000.0;

		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n###############################################################################################################################\n\n\n\n");
		System.out.println("Total Time to sort episodes for series: " + elapsedTimeSeconds + " seconds");
		System.out.println("Total Time to write episodes and series on DB: " + elapsedTimeMinutes2 + " minutes");

		InputOutput.writeSeriesFileTxt(seriesList, elapsedTimeMinutes2, "Route");
	}



	@SuppressWarnings("unused")
	private static List<Series> createSeriesFromRoute(String route) {
		long startTime = System.currentTimeMillis();

		List<Series> seriesList = new ArrayList<>();

		File routeDir = new File(route);
		if (routeDir.exists() && routeDir.isDirectory()) {
			File[] seriesDirs = routeDir.listFiles(File::isDirectory);
			if (seriesDirs != null) {
				for (File seriesDir : seriesDirs) {
					long startTimeSerie = System.currentTimeMillis();
					Series series = new Series();
					series.setNameOfSerie(seriesDir.getName());
					series.setPath(route + separator + series.getNameOfSerie());
					//TODO borrar esto o comprobar si funciona
					//String seriesPath = Paths.get(route, series.getNameOfSerie()).toString();
					//series.setPath(seriesPath);

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
									.replace(".avi", "")
									.replace(".MP4", "")
									.replace(".MKV", "")
									.replace(".AVI", "");
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
								System.err.println("Invalid episode number or season number: " + videoFile.getName());
								episode.setEpisodeNumber(-1); // Invalid episode number
								episode.setNameOfEpisode(fileName);
							}

							// Set the duration of the episode (you can obtain this information from the video file)
							episode.setDurationSeconds(getVideoDurationInSeconds(series.getPath() + separator + episode.getFileName())); // Replace with actual duration

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
					seriesList.add(series);		long endTime = System.currentTimeMillis();

					long endTimeSerie = System.currentTimeMillis();
					long elapsedTimeSerie = endTimeSerie - startTimeSerie;
					double elapsedTimeMinutesSerie = elapsedTimeSerie / 60000.0;
					System.out.println("Time to create series " + series.getNameOfSerie() + " from route: " + elapsedTimeMinutesSerie + " minutes");
				}
			}
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		double elapsedTimeMinutes = elapsedTime / 60000.0;
		System.out.println("Total Time to create series from route: " + elapsedTimeMinutes + " minutes");

		return seriesList;
	}

	public static void populatePlayOrder(List<Series> seriesList) {
		for (Series series : seriesList) {
			long startTimeSerie = System.currentTimeMillis();
			List<Episode> episodes = series.getEpisodes();

			// Sort episodes by season number and episode number
			episodes.sort(Comparator.comparing(Episode::getSeasonNumber)
					.thenComparing(Episode::getEpisodeNumber));

			int playOrder = 1; // Initialize playOrder

			for (Episode episode : episodes) {
				episode.setPlayOrder(playOrder);
				playOrder++;
			}
			long endTimeSerie = System.currentTimeMillis();
			long elapsedTimeSerie = endTimeSerie - startTimeSerie;
			System.out.println("Time to sort series " + series.getNameOfSerie() + " : " + elapsedTimeSerie + " seconds");
		}
	}

	private static void writeSeriesToDB(List<Series> seriesList) {
		try {
			Database.initializeDatabase();
			for(Series series:seriesList) {
				Database.insertSeries(series);
				for(Episode episode:series.getEpisodes()) {
					Database.insertEpisode(episode);
				}
			} 
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// IO output operations with video with metadata using ProcessBuilder and the mediainfo command
	@SuppressWarnings("unused")
	public static int getVideoDurationInSeconds(String videoFilePath) {
		try {
			Path path = Paths.get(videoFilePath);
			ProcessBuilder processBuilder;
			// Execute mediainfo command and capture output
			List<String> command = new ArrayList<>();
			command.add("mediainfo");
			command.add("--Inform=Video;%Duration%");
			command.add(path.toString());
			processBuilder = new ProcessBuilder(command);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			assert processBuilder.redirectInput() == Redirect.PIPE;
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
			System.err.println("Error with the duration: ");
			System.err.println("videoFilePath: " + videoFilePath);
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