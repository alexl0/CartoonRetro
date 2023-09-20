package cartoonretro.model;

import java.util.List;

public class Series {
	private String nameOfSerie;

	private List<Episode> episodes;
	private String path;
	private int popularity;
	private Episode lastEpisodePlayed;

	public String getNameOfSerie() {
		return nameOfSerie;
	}
	public void setNameOfSerie(String nameOfSerie) {
		this.nameOfSerie = nameOfSerie;
	}
	public List<Episode> getEpisodes() {
		return episodes;
	}
	public void setEpisodes(List<Episode> episodes) {
		this.episodes = episodes;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getPopularity() {
		return popularity;
	}
	public void setPopularity(int popularity) {
		this.popularity = popularity;
	}
	public Episode getLastEpisodePlayed() {
		return lastEpisodePlayed;
	}
	public void setLastEpisodePlayed(Episode lastEpisodePlayed) {
		this.lastEpisodePlayed = lastEpisodePlayed;
	}
}
