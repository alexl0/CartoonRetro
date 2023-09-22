package cartoonretro.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import cartoonretro.InputOutput.InputOutput;

import java.time.Duration;
import java.time.LocalDate;

public class Schedule {

	private static TreeMap<LocalDateTime, Episode> schedule;

	/**
	 * Para cada serie, coje el episodio siguiente
	 * (o el primero si es la primera vez, o el ultimo en reproducirse ha sido el ultimo)
	 * y lo añade a la planificación
	 * @param startDateTime Fecha en la cual empezara la planificacion
	 * @param seriesList Lista con todas las series
	 * @return
	 */
	public static TreeMap<LocalDateTime, Episode> createYearlySchedule(LocalDateTime startDateTime, List<Series> seriesList) {
		schedule = new TreeMap<>();
		LocalDateTime currentDateTime = startDateTime;

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

					// Associate the episode with the air date and time in the schedule
					schedule.put(currentDateTime, episode);
					// Increment the current date and time by the episode's duration
					currentDateTime = currentDateTime.plus(Duration.ofSeconds(episode.getDurationSeconds()));
					totalDuration+=episode.getDurationSeconds();
				} else {
					System.out.println("Episode not found with playOrder: " + playOrderOfEpisodeToBePlayed);
				}
			}

			// Fill remaining time of the day with REPEATED episodes
			List<Episode> episodesFromDay = getEpisodesFromDay(currentDateTime.toLocalDate());

			boolean canPutMoreEpisodes = true;
			while(canPutMoreEpisodes) {
				canPutMoreEpisodes = false;
				for(Episode e : episodesFromDay) {
					if(e.getDurationSeconds()+totalDuration<maxDuration) {
						schedule.put(currentDateTime, e);
						currentDateTime = currentDateTime.plus(Duration.ofSeconds(e.getDurationSeconds()));
						totalDuration+=e.getDurationSeconds();
						canPutMoreEpisodes = true;
					}
				}
			}
		}
		InputOutput.printScheduleToFile(schedule);
		return schedule;
	}

	private static List<Episode> getEpisodesFromDay(LocalDate targetDate) {
		// Define the start and end LocalDateTime instances for the target day
		LocalDateTime startOfDay = targetDate.atStartOfDay();
		LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
		// Retrieve episodes scheduled for the target day
		List<Episode> episodesFromDay = new ArrayList<>();

		for (Map.Entry<LocalDateTime, Episode> entry : schedule.entrySet()) {
			LocalDateTime airDateTime = entry.getKey();

			if (!airDateTime.isBefore(startOfDay) && !airDateTime.isAfter(endOfDay)) {
				episodesFromDay.add(entry.getValue());
			}
		}
		return episodesFromDay;
	}



}
