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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
// Data structures stuff
import java.util.ArrayList;
import java.util.List;

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
	static final String route = "E:\\PCEXTERNO\\Completo";

	public static void main(String[] args) {
		//Generate series from route
		List<Series> seriesList = createSeriesFromRoute(route);		
		InputOutput.writeSeriesFileTxt(seriesList, 0, "Route");
		writeSeriesToDB(seriesList);
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