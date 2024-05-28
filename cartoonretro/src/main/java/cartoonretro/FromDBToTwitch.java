package cartoonretro;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cartoonretro.model.Database;
import cartoonretro.model.Episode;
import cartoonretro.model.Schedule;
import cartoonretro.model.Series;
import cartoonretro.obs.OBSController;
import cartoonretro.twitch.TwitchAPI;
import cartoonretro.vlc.VLCController;
import cartoonretro.InputOutput.InputOutput;
//import cartoonretro.chatbot.ChatGPTClient;

/**
 * This class reads the video information from db, executes the video and streams to twitch.
 * Execute this class after executing FromFilesToDB.java.
 */
public class FromDBToTwitch {

	// ChatGPT
	//private static String chatGPTApiKey;

	// Twitch
	private static String twitchBroadcasterId;
	private static String twitchClientId;
	private static String twitchClientSecret;
	private static String twitchUserAccessToken;
	private static String twitchUserRefreshToken;

	// OBS
	private static String obsWebSocketPass;
	private static String obsWebSocketIp;
	private static String so;
	private static String separator; //If on Linux, it'll be set to /, if on windows, \

	private static OBSController obsController;
	private static VLCController vlcController;
	private static TwitchAPI twitchAPI;
	//private static ChatGPTClient chatGPTClient;

	private static List<Series> seriesList;

	private static final Logger log = LoggerFactory.getLogger(FromDBToTwitch.class);

	static { System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");}

	public static final String daysOfWeekSpanish[] = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};
	public static final DayOfWeek[] daysOfWeekEnglish = { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY };

	public static void main(String[] args) {

		log.info("¡Starting CartoonRetro!");

		//Read keys
		readProperties();

		//Generate series from DB
		try {
			seriesList = Database.retrieveSeriesFromDB();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		InputOutput.writeSeriesFileTxt(seriesList, 0, "DB");


		//OBS
		obsController = new OBSController(obsWebSocketPass, obsWebSocketIp);
		obsController.connect();

		//VLC
		vlcController = new VLCController();

		if(so.equals("linux")) {
			separator="/";
			// Set LD_LIBRARY_PATH
			//This is needed for linux TODO make it ONLY for linux
			String vlcLibPath = "/snap/vlc/3777/usr/lib/";
			System.setProperty("jna.library.path", vlcLibPath);
		}
		if(so.equals("windows"))
			separator="\\";

		//Twitch
		twitchAPI = new TwitchAPI(twitchBroadcasterId, twitchClientId, twitchClientSecret, twitchUserAccessToken, twitchUserRefreshToken);

		//ChatGPT
		//chatGPTClient = new ChatGPTClient(chatGPTApiKey);

		System.out.println("Creating planification for the next 365 days.");
		// BE CAREFUL! Put any day but only 00:00 o clock!!
		TreeMap<LocalDateTime, Episode> yearlySchedule = Schedule.createYearlySchedule(LocalDateTime.of(2024, 5, 17, 0, 0), seriesList); //TODO tambien sacar esto a un .properties, pero diferenciar el de cosas secretas como password y otras cosa que puedan estar en git
		System.out.println("Planification stored.");


		//TreeMap<LocalDateTime, Episode> shortSchedule = Schedule.createTestSchedule(seriesList);

		playSchedule(yearlySchedule);


		//Test
		//playEpisodeFromFileNameAndSerie("Doraemon (2005)", "Pesca de andar por casa");
		//playEpisodeFromFileNameAndSerie("Dragon Ball GT", "El regreso de Goku. La ira del guerrero Oob");//1440x1080 (4:3)
		//playEpisodeFromFileNameAndSerie("Pokémon", "027~~019~XYZ.mkv");//1280x720 (16:9)
		//playEpisodeFromFileNameAndSerie("Pokémon", "039~~019~XYZ.mkv");//1280x6.. (casi 16:9)
		//playEpisodeFromFileNameAndSerie("Pokémon", "032~~018~XY - Expediciones en Kalos.mkv");//1024x576 (16:9)
		//playEpisodeFromFileNameAndSerie("Ben 10 (2016)", "021~La niebla del páramo~003.mkv");//960x540 (16:9)
		//playEpisodeFromFileNameAndSerie("Ben 10 Alien Force(2008)", "013~~001.avi");//960x720 (4:3) //TODO bordes negros que te cagas en esos episodios (culpa del vídeo)
		//playEpisodeFromFileNameAndSerie("Ben 10 Alien Force(2008)", "001~~001.AVI");//720x540 (4:3)
		//playEpisodeFromFileNameAndSerie("Ben 10 Serie original (2005)", "008~~001.avi");//720x544 (casi 4:3)
		//playEpisodeFromFileNameAndSerie("Ben 10 Ultimate (2010)", "020~~001.avi");//720x576 (casi 4:3)
		//playEpisodeFromFileNameAndSerie("El Chicho Terremoto", "038~Chicho Contra Todo.avi");//384x288 (4:3)
		//playEpisodeFromFileNameAndSerie("Los Simpson", "001~Buenas Noches~000.mkv");

		// Send whisper to bot
		//twitchAPI.sendWhisper("#plan2 lokoweaaAA22", "961414815");

	}

	private static boolean schedulePrinted = false;
	private static LocalDate lastPrintDate = null;
	private static void playSchedule(TreeMap<LocalDateTime, Episode> yearlySchedule) {
		while(true) { 

			// First of all, if we hadn't already, populate the txt files that the HTML is going to read
			// Those actions are performed every single day
			if (!schedulePrinted || lastPrintDate.getDayOfYear()!=LocalDate.now().getDayOfYear()) {

				//Stop stream
				twitchAPI.sendMessage("Pausando stream. ¡Empezando otra vez en 10 segundos!");
				obsController.stopStream();


				InputOutput.printScheduleDaysForHTML(yearlySchedule);
				schedulePrinted = true;
				lastPrintDate = LocalDate.now();

				// TODO generar tambien los de la que viene
				twitchAPI.sendMessage("¡Horarios para esta semana generados! Puedes verlos escribiendo !lunes, !martes, etc. Puedes reportar cualquier sugerencia en el canal de Telegram.");

				// set the plan obs source properly 
				for(int i=0; i< daysOfWeekEnglish.length; i++) {
					if(daysOfWeekEnglish[i]==LocalDate.now().getDayOfWeek())
						obsController.setPlanDay(daysOfWeekSpanish[i]);
				}

				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				twitchAPI.sendMessage("¡Empezando stream!");
				obsController.startStream();
			}

			Map.Entry<LocalDateTime, Episode> entry = yearlySchedule.floorEntry(LocalDateTime.now());
			// If the first date on the schedule has not arrived yet, we need to wait
			if(entry == null) {
				long timeToStart = Math.abs(ChronoUnit.SECONDS.between(LocalDateTime.now(), yearlySchedule.firstKey()));
				try {
					String timeToStartMessage = "The schedule has not started yet. First episode(" + yearlySchedule.firstEntry().getValue().getNameOfSerie() + ") starts " + yearlySchedule.firstKey().toLocalDate() + " at " + yearlySchedule.firstKey().toLocalTime() + ". Time remaining: " + timeToStart + " seconds.";
					System.out.println(timeToStartMessage);
					twitchAPI.sendMessage(timeToStartMessage);
					TimeUnit.SECONDS.sleep(timeToStart+1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			else {
				long delayFromPreviousEpisode = Math.abs(ChronoUnit.SECONDS.between(LocalDateTime.now(), entry.getKey()));
				// If we go 3 minutes later or more, we wait until the next episode TODO externalizar esta variable tambien
				if(delayFromPreviousEpisode>60*3) {
					Map.Entry<LocalDateTime, Episode> entryNext = yearlySchedule.ceilingEntry(LocalDateTime.now());
					long delayToNextEpisode = Math.abs(ChronoUnit.SECONDS.between(LocalDateTime.now(), entryNext.getKey()));
					if (delayToNextEpisode > 0) {
						try {
							// Sleep to wait until the scheduled time
							// TODO mirar aver por que este mensaje sale al acbara el dia royo 23:55. Deberia salir otro distinto
							// TODO tambien mostrar en pantalla un temporizador en obs con el tiemop que falte mientras espere
							//String mensajeEsperaIngles = "We were " + delayFromPreviousEpisode + " seconds delayed to play the last episode (" + entry.getValue().getNameOfSerie() + "). We were not able to start playing it at " + entry.getKey().toLocalTime() + ", so we're waiting for the next one (" + entryNext.getValue().getNameOfSerie() + ") to be played at " + entryNext.getKey().toLocalTime() + ". " + delayToNextEpisode/60 + " minutes remaining";
							String mensajeEspera = "Tuvimos un retraso de " + delayFromPreviousEpisode + " segundos para reproducir el último episodio (" + entry.getValue().getNameOfSerie() + "). No pudimos empezarlo a las " + entry.getKey().toLocalTime() + ", así que estamos esperando al siguiente (" + entryNext.getValue().getNameOfSerie() + "). Empezará a las: " + entryNext.getKey().toLocalTime() + ". Quedan " + delayToNextEpisode/60 + " minutos.";
							System.out.println(mensajeEspera);
							twitchAPI.sendMessage(mensajeEspera);
							TimeUnit.SECONDS.sleep(delayToNextEpisode);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
				// If not, we play it
				else {
					Episode episodeToPlay = yearlySchedule.floorEntry(LocalDateTime.now()).getValue();
					playEpisode(episodeToPlay);
					// Calculate the time until the next hour
					LocalDateTime currentDateTime = LocalDateTime.now();
					LocalDateTime nextDateTime = currentDateTime.plusSeconds(episodeToPlay.getDurationSeconds());
					long delaySeconds = ChronoUnit.SECONDS.between(currentDateTime, nextDateTime) + 1;
					try {
						// Sleep to wait until the next episode
						TimeUnit.SECONDS.sleep(delaySeconds);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

		}
	}

	/**
	 * Play an episode from a serie. Searchs for a serie that contains the parameter episodeName.
	 * @param seriesName
	 * @param episodeName
	 */
	@SuppressWarnings("unused")
	private static void playEpisodeFromFileNameAndSerie(String seriesName, String fileName) {
		Optional<Series> searchSerie = seriesList.stream().filter(series -> series.getNameOfSerie().equals(seriesName)).findFirst();
		if (searchSerie.isPresent()) {
			Series foundSeries = searchSerie.get();
			Optional<Episode> searchEpisode = foundSeries.getEpisodes().stream().filter(episode -> episode.getFileName().toLowerCase().contains(fileName.toLowerCase())).findFirst();
			if (searchEpisode.isPresent()) {
				Episode foundEpisode = searchEpisode.get();
				vlcController.playEpisode(foundSeries.getPath() + separator + foundEpisode.getFileName(), foundEpisode.getWidth(), foundEpisode.getHeight(), so);
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

	private static void playEpisode(Episode episode) {
		Optional<Series> searchSerie = seriesList.stream().filter(series -> series.getNameOfSerie().equals(episode.getNameOfSerie())).findFirst();
		if (searchSerie.isPresent()) {
			Series foundSeries = searchSerie.get();

			// Change stream info TODO Remove rare characters (accents are permitted) 
			// TODO a LOT of episodeNames has | and other character. Yu gi oh lleva - por ejemplo
			// TODO mirar aver por que los acentos se pierden, mirar la codificación
			// TODO añadir tags solo SI CABE, cortao queda fatal
			List<String> tagsList = new ArrayList<>();
			if (foundSeries.getNameOfSerie() != null && !foundSeries.getNameOfSerie().isBlank()) {
				String sanitizedTag = foundSeries.getNameOfSerie().replaceAll("[^a-zA-Z0-9]", "");
				if (!sanitizedTag.isBlank())
					tagsList.add(truncateTag(sanitizedTag));
			}
			if (episode.getNameOfEpisode() != null && !episode.getNameOfEpisode().isBlank()) {
				String sanitizedTag = episode.getNameOfEpisode().replaceAll("[^a-zA-Z0-9]", "");
				if (!sanitizedTag.isBlank())
					tagsList.add(truncateTag(sanitizedTag));
			}
			if (episode.getSeasonName() != null && !episode.getSeasonName().isBlank()) {
				String sanitizedTag = episode.getSeasonName().replaceAll("[^a-zA-Z0-9]", "");
				if (!sanitizedTag.isBlank())
					tagsList.add(truncateTag(sanitizedTag));
			}
			tagsList.add("SeriesAntiguas");
			tagsList.add("SeriesRetro");
			tagsList.add("Anime");
			tagsList.add("Retro");
			String[] tags = tagsList.toArray(new String[0]);
			String title = foundSeries.getNameOfSerie();
			if(episode.getEpisodeNumber()>0)
				title+= " Ep " + episode.getEpisodeNumber();
			if(episode.getNameOfEpisode()!=null & !episode.getNameOfEpisode().isBlank())
				title+= " " + episode.getNameOfEpisode();
			if(episode.getSeasonNumber()>0) {
				title+= " Temporada " + episode.getSeasonNumber();
				if(episode.getSeasonName()!=null && !episode.getSeasonName().isBlank())
					title+= " " + episode.getSeasonName();
			}
			if(title.length()>140)
				title.substring(0, 140);
			System.out.println("Playing: " + title);
			twitchAPI.changeStreamInfo(title, tags);
			twitchAPI.sendMessage("Ahora: " + title);

			// Calculate aspect ratio
			String aspectRatio = calculateAspectRatio(episode.getWidth(), episode.getHeight());

			System.out.println("OBS: Changed scene to: " + "Series"+ aspectRatio);
			twitchAPI.sendMessage("Escena cambiada a " + aspectRatio);
			obsController.setScene("Series"+ aspectRatio);

			// Play the episode
			vlcController.playEpisode(foundSeries.getPath() + separator + episode.getFileName(), episode.getWidth(), episode.getHeight(), so);
		} else {
			System.out.println("Series not found");
		}
	}

	// Truncate a tag to a maximum of 25 characters
	private static String truncateTag(String tag) {
		if (tag.length() <= 25) {
			return tag;
		} else {
			return tag.substring(0, 25);
		}
	}

	private static void readProperties() {
		// Read properties
		Properties properties = InputOutput.loadPropertiesFile("src/PASSWORDS.properties");
		Properties propertiesPublic = InputOutput.loadPropertiesFile("src/PUBLIC.properties");

		// Access the properties using the keys defined in your .properties file
		//chatGPTApiKey = properties.getProperty("chatgpt_api_key");
		obsWebSocketPass = properties.getProperty("obs_websocket_pass");
		twitchBroadcasterId = properties.getProperty("twitch_broadcaster_id");
		twitchClientId = properties.getProperty("twitch_client_id");
		twitchClientSecret = properties.getProperty("twitch_client_secret");
		twitchUserAccessToken = properties.getProperty("twitch_user_access_token");
		twitchUserRefreshToken = properties.getProperty("twitch_user_refresh_token");

		obsWebSocketIp = propertiesPublic.getProperty("obsWebSocketIp");
		so = propertiesPublic.getProperty("so").toLowerCase();
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
