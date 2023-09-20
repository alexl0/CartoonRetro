package cartoonretro.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import cartoonretro.InputOutput.InputOutput;

import java.time.Duration;

public class Schedule {

	static Map<LocalDateTime, Episode> schedule;

	/**
	 * Para cada serie, coje el episodio siguiente
	 * (o el primero si es la primera vez, o el ultimo en reproducirse ha sido el ultimo)
	 * y lo añade a la planificación
	 * @param startDateTime Fecha en la cual empezara la planificacion
	 * @param seriesList Lista con todas las series
	 * @return
	 */
	public static Map<LocalDateTime, Episode> createYearlySchedule(LocalDateTime startDateTime, List<Series> seriesList) {
		schedule = new TreeMap<>();

		for( int day=1; day<365; day++) {
			// Seconds of the day (cannot play more than that in a day xD)
			int maxDuration = 24*60*60;
			int totalDuration = 0;
			
			// Calculate what NEW episodes to be played
			for (Series series : seriesList) {
				// Find what episode comes next
				int playOrderOfEpisodeToBePlayed;
				// If the series has never been played, or there is no more episodes
				if(series.getLastEpisodePlayed()==null || series.getLastEpisodePlayed().getPlayOrder() >= series.getEpisodes().size())
					playOrderOfEpisodeToBePlayed=1;
				else
					playOrderOfEpisodeToBePlayed = series.getLastEpisodePlayed().getPlayOrder()+1;

				// Add this episode to the schedule
				Optional<Episode> foundEpisode = series.getEpisodes().stream().filter(e -> e.getPlayOrder()==playOrderOfEpisodeToBePlayed).findFirst();
				if (foundEpisode.isPresent()) {
					Episode episode = foundEpisode.get();
					series.setLastEpisodePlayed(episode);

					// Calculate the air date and time for the episode
					LocalDateTime airDateTime = startDateTime;
					Duration episodeDuration = Duration.ofSeconds(episode.getDurationSeconds());
					totalDuration+=episodeDuration.getSeconds();

					// Associate the episode with the air date and time in the schedule
					schedule.put(airDateTime, episode);

					// Increment the current date and time by the episode's duration
					startDateTime = startDateTime.plus(episodeDuration);
				} else {
					System.out.println("Episode not found with playOrder: " + playOrderOfEpisodeToBePlayed);
				}
			}
			
			// Fill remaining time of the day with REPEATED episodes
			
		}
		InputOutput.printScheduleToFile(schedule);
		return schedule;
	}



}
