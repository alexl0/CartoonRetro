package cartoonretro.InputOutput;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.io.*;

import cartoonretro.model.Episode;
import cartoonretro.model.Series;

public class InputOutput {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	
	// Helper method to load properties from a file
	public static Properties loadPropertiesFile(String filePath) {
		Properties properties = new Properties();
		try (InputStream input = new FileInputStream(filePath)) {
			properties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	// Helper method to save properties to a file
	public static void savePropertiesFile(String filePath, Properties properties) {
		try (OutputStream output = new FileOutputStream(filePath)) {
			properties.store(output, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeSeriesFileTxt(List<Series> seriesList, double executionTimeMinutes, String medio) {
		// I print everything to check that everything is alright
		try (PrintWriter writer = new PrintWriter(new FileWriter("SeriesAndEpisodesList" + medio + ".txt"))) {
			for (Series series : seriesList) {
				writer.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nSeries Name: " + series.getNameOfSerie() + "\tNumber of episodes: " + series.getEpisodes().size());
				for (Episode episode : series.getEpisodes()) {
					writer.print("Episode Number: " + episode.getEpisodeNumber());
					writer.print("\t|||||\tDuration Seconds: " + episode.getDurationSeconds());
					writer.print("\t|||||\tDimensions: " + episode.getWidth() + "x" + episode.getHeight()+"\t");
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
	}

	public static void printScheduleToFile(Map<LocalDateTime, Episode> schedule, String nameOfFile) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try (PrintWriter writer = new PrintWriter(new FileWriter(nameOfFile))) {
			for (Map.Entry<LocalDateTime, Episode> entry : schedule.entrySet()) {
				LocalDateTime airDateTime = entry.getKey();
				Episode episode = entry.getValue();

				String formattedDateTime = airDateTime.format(formatter);
				writer.write(formattedDateTime + "\t" + episode.getNameOfSerie() + "\t" + episode.getSeasonNumber() + "\t" + episode.getEpisodeNumber() + "\t" + episode.getNameOfEpisode() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void printScheduleDaysForHTML(Map<LocalDateTime, Episode> schedule) {
		// Define the days of the week in the correct order
		DayOfWeek[] daysOfTheWeek = { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY };

		// Get the current day of the week
		DayOfWeek currentDay = LocalDate.now().getDayOfWeek();

		// Iterate over the days of the week starting from the current day
		for (int i = currentDay.getValue() - 1; i < daysOfTheWeek.length; i++) {
			// Get the day of the week
			DayOfWeek dayOfWeek = daysOfTheWeek[i];
			// Define the path to the text file relative to your Maven project directory
			String filePath = "../html/planificationHTML" + dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/your-text-file.txt";
			try {
				// Open the file for writing, and set the append mode to false
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false));

				// Iterate over the schedule and write the relevant entries for the current day
				for (Map.Entry<LocalDateTime, Episode> entry : schedule.entrySet()) {
					if (entry.getKey().toLocalDate().isEqual(LocalDate.now())) {
						// Write the schedule entry to the file
						String line = entry.getKey().toLocalTime().format(formatter) + " " + entry.getValue().getNameOfSerie();
						writer.write(line);
						// If is not the last line, we print a new line
						//if(i!=daysOfTheWeek.length-1) //Comented because on Sunday it didn't print any line break
						writer.newLine();
					}
				}

				// Close the writer to save changes
				writer.close();

				System.out.println("Text file modified successfully for " + dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
