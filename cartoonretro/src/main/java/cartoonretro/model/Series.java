package cartoonretro.model;

import java.util.List;

public class Series {
	private int numberOfEpisodes;
	private String nameOfSerie;

	private List<Episode> episodes;
	private String path;
	
	public int getNumberOfEpisodes() {
		return numberOfEpisodes;
	}
	public void setNumberOfEpisodes(int numberOfEpisodes) {
		this.numberOfEpisodes = numberOfEpisodes;
	}
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
	
}
