package cartoonretro.model;

import java.util.List;

public class Series {
	private String nameOfSerie;

	private List<Episode> episodes;
	private String path;

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
