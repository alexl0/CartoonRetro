package cartoonretro.InputOutput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.*;

import cartoonretro.model.Episode;
import cartoonretro.model.Series;

public class InputOutput {
	
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
	

}
