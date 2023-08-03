package cartoonretro.model;

public class Episode {
	private int episodeNumber;
	private int durationSeconds;
	private String nameOfEpisode;
	private String fileName;

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

}
