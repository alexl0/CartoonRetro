package cartoonretro.model;

public class Episode {
	private int episodeNumber;
	private int durationSeconds;
	private String nameOfEpisode;
	private String fileName;
	private int width;
	private int height;
	private int seasonNumber;

	public int getEpisodeNumber() {
		return episodeNumber;
	}
	public void setEpisodeNumber(int episodeNumber) {
		this.episodeNumber = episodeNumber;
	}
	public int getDurationSeconds() {
		return durationSeconds;
	}
	public void setDurationSeconds(int durationSeconds) {
		this.durationSeconds = durationSeconds;
	}
	public String getNameOfEpisode() {
		return nameOfEpisode;
	}
	public void setNameOfEpisode(String nameOfEpisode) {
		this.nameOfEpisode = nameOfEpisode;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getSeasonNumber() {
		return seasonNumber;
	}
	public void setSeasonNumber(int seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

}
