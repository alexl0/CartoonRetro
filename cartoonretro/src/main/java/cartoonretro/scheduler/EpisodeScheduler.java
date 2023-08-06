package cartoonretro.scheduler;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cartoonretro.model.Episode;

public class EpisodeScheduler {
    private Map<DayOfWeek, List<Episode>> schedule;

    public EpisodeScheduler() {
        schedule = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            schedule.put(day, new ArrayList<>());
        }
    }

    public void addEpisode(DayOfWeek dayOfWeek, Episode episode) {
        List<Episode> episodes = schedule.get(dayOfWeek);
        episodes.add(episode);
    }

    public List<Episode> getEpisodesForDay(DayOfWeek dayOfWeek) {
        return schedule.get(dayOfWeek);
    }

    public void printSchedule() {
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Episode> episodes = schedule.get(day);
            if (!episodes.isEmpty()) {
                System.out.println("Day: " + day);
                LocalTime startTime = LocalTime.of(9, 0);
                for (Episode episode : episodes) {
                    System.out.println(startTime + " - " + startTime.plusSeconds(episode.getDurationSeconds())
                            + " | Series: " + episode.getNameOfSerie() + " | Episode: " + episode.getNameOfEpisode());
                    startTime = startTime.plusSeconds(episode.getDurationSeconds());
                }
            }
        }
    }

    public static void main(String[] args) {
        EpisodeScheduler scheduler = new EpisodeScheduler();

        // Add episodes for different days
//        scheduler.addEpisode(DayOfWeek.MONDAY, episode1);
//        scheduler.addEpisode(DayOfWeek.MONDAY, episode2);
//        scheduler.addEpisode(DayOfWeek.WEDNESDAY, episode3);

        // Print the schedule
        scheduler.printSchedule();
    }
}
