package cartoonretro.model;

import cartoonretro.model.Episode;
import cartoonretro.model.Series;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Database {
	private static final String DATABASE_URL = "jdbc:sqlite:videodata.db";

	public static void initializeDatabase() throws SQLException {
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			String createSeriesTableSQL = "CREATE TABLE IF NOT EXISTS series (" +
					"name_of_serie TEXT PRIMARY KEY NOT NULL, " +
					"path TEXT NOT NULL)";

			String createEpisodesTableSQL = "CREATE TABLE IF NOT EXISTS episodes (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"episode_number INT, " +
					"duration_seconds INT, " +
					"name_of_episode TEXT, " +
					"name_of_serie TEXT, " +
					"file_name TEXT, " +
					"width INT, " +
					"height INT, " +
					"season_number INT, " +
					"season_name TEXT, " +
					"play_order INT, " +
					"FOREIGN KEY (name_of_serie) REFERENCES series(name_of_serie), " + 
					"UNIQUE (file_name, name_of_serie))";// Foreign key reference to series table

			String createScheduleTableSQL = "CREATE TABLE IF NOT EXISTS schedule (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"episode_id INT, " +
					"play_datetime DATETIME, " +
					"FOREIGN KEY (episode_id) REFERENCES episodes(id))";

			connection.createStatement().executeUpdate(createSeriesTableSQL);
			connection.createStatement().executeUpdate(createEpisodesTableSQL);
			connection.createStatement().executeUpdate(createScheduleTableSQL);
		}
	}

	// Insert methods 

	public static void insertSeries(Series series) throws SQLException {
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			String insertSeriesSQL = "INSERT OR REPLACE INTO series (name_of_serie, path) VALUES (?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(insertSeriesSQL);
			preparedStatement.setString(1, series.getNameOfSerie());
			preparedStatement.setString(2, series.getPath());

			preparedStatement.executeUpdate();
		}
	}

	public static void insertEpisode(Episode episode) throws SQLException {
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			String insertEpisodeSQL = "INSERT OR REPLACE INTO episodes (episode_number, duration_seconds, " +
					"name_of_episode, name_of_serie, file_name, width, height, season_number, season_name, play_order) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(insertEpisodeSQL);
			preparedStatement.setInt(1, episode.getEpisodeNumber());
			preparedStatement.setInt(2, episode.getDurationSeconds());
			preparedStatement.setString(3, episode.getNameOfEpisode());
			preparedStatement.setString(4, episode.getNameOfSerie());
			preparedStatement.setString(5, episode.getFileName());
			preparedStatement.setInt(6, episode.getWidth());
			preparedStatement.setInt(7, episode.getHeight());
			preparedStatement.setInt(8, episode.getSeasonNumber());
			preparedStatement.setString(9, episode.getSeasonName());
			preparedStatement.setInt(10, episode.getPlayOrder());

			preparedStatement.executeUpdate();
		}
	}

	// Retrieve methods

	public static List<Series> retrieveSeriesFromDB() throws SQLException {
		List<Series> seriesList = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			String retrieveSeriesNamesSQL = "SELECT * FROM series";
			PreparedStatement preparedStatement = connection.prepareStatement(retrieveSeriesNamesSQL);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Series series = new Series();
				series.setNameOfSerie(resultSet.getString("name_of_serie"));
				series.setPath(resultSet.getString("path"));
				series.setEpisodes(retrieveEpisodesForSeries(series.getNameOfSerie()));
				seriesList.add(series);
			}
		}
		return seriesList;
	}

	public static List<Episode> retrieveEpisodesForSeries(String seriesName) throws SQLException {
		List<Episode> episodes = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			String retrieveEpisodesSQL = "SELECT * FROM episodes WHERE name_of_serie = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(retrieveEpisodesSQL);
			preparedStatement.setString(1, seriesName);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Episode episode = new Episode();
				episode.setEpisodeNumber(resultSet.getInt("episode_number"));
				episode.setDurationSeconds(resultSet.getInt("duration_seconds"));
				episode.setNameOfEpisode(resultSet.getString("name_of_episode"));
				episode.setNameOfSerie(resultSet.getString("name_of_serie"));
				episode.setFileName(resultSet.getString("file_name"));
				episode.setWidth(resultSet.getInt("width"));
				episode.setHeight(resultSet.getInt("height"));
				episode.setSeasonNumber(resultSet.getInt("season_number"));
				episode.setSeasonName(resultSet.getString("season_name"));
				episode.setPlayOrder(resultSet.getInt("play_order"));
				episodes.add(episode);
			}
		}
		return episodes;
	}


	// Scheduler

	// Add an episode to the schedule table (you need to implement this)
	private static void addEpisodeToSchedule(int episodeId, LocalTime playTime, int day) {
		String insertSql = "INSERT INTO schedule (episode_id, play_datetime, day) VALUES (?, ?, ?)";
		try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
			PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
			preparedStatement.setInt(1, episodeId);
			preparedStatement.setString(2, playTime.toString());
			preparedStatement.setInt(3, day);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
