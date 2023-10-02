package cartoonretro.twitch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import org.json.JSONObject;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.TwitchHelixBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import cartoonretro.InputOutput.InputOutput;

public class TwitchAPI {

	//private String twitchStreamKey;
	private static String twitchBroadcasterId;
	private static String twitchClientId;
	private static String twitchClientSecret;
	private static String twitchUserAccessToken;
	private static String twitchUserRefreshToken;

	private final String channelName = "CartoonRetro";

	private static final String BASE_URL = "https://api.twitch.tv/helix";

	public TwitchAPI(String twitchBroadcasterId, String twitchClientId, String twitchClientSecret, String twitchUserAccessToken, String twitchUserRefreshToken) {
		//this.twitchStreamKey = twitchStreamKey;
		TwitchAPI.twitchBroadcasterId = twitchBroadcasterId;
		TwitchAPI.twitchClientId = twitchClientId;
		TwitchAPI.twitchClientSecret = twitchClientSecret;
		if(twitchClientSecret!=null && !twitchClientSecret.isBlank() && twitchUserRefreshToken!=null && !twitchUserRefreshToken.isBlank()) {
			TwitchAPI.twitchUserAccessToken = twitchUserAccessToken;
			TwitchAPI.twitchUserRefreshToken = twitchUserRefreshToken;
		} else {
			getCredentials();
		}

		//Refresh token
		try {
			refreshAccessToken();
			//System.out.println("newAccessToken: " + twitchUserAccessToken);
			//System.out.println("newRefreshToken: " + twitchUserRefreshToken);
		} catch (ClientProtocolException e) {
			System.out.println("TwitchAPI.java: Error refreshing the token.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("TwitchAPI.java: Error refreshing the token.");
			e.printStackTrace();
		}


	}

	public static void refreshAccessToken() throws ClientProtocolException, IOException {
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://id.twitch.tv/oauth2/token");

		// Set headers
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

		String encodedRefreshToken = URLEncoder.encode(twitchUserRefreshToken, StandardCharsets.UTF_8.toString());

		// Set form parameters
		BasicNameValuePair[] params = {
				new BasicNameValuePair("grant_type", "refresh_token"),
				new BasicNameValuePair("refresh_token", encodedRefreshToken),
				new BasicNameValuePair("client_id", twitchClientId),
				new BasicNameValuePair("client_secret", twitchClientSecret)
		};
		httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));

		// Execute the POST request
		HttpResponse response = httpClient.execute(httpPost);

		// Parse the response
		HttpEntity entity = response.getEntity();
		String responseBody = EntityUtils.toString(entity);

		// Parse the JSON response
		JSONObject jsonResponse = new JSONObject(responseBody);

		// Extract the access token, refresh token, and other data from the JSON response
		twitchUserAccessToken = jsonResponse.getString("access_token");
		twitchUserRefreshToken = jsonResponse.getString("refresh_token");

		// Store the tokens in the properties file
		Properties properties = InputOutput.loadPropertiesFile("src/PASSWORDS.properties");
		properties.setProperty("twitch_user_access_token", twitchUserAccessToken);
		properties.setProperty("twitch_user_refresh_token", twitchUserRefreshToken);
		InputOutput.savePropertiesFile("src/PASSWORDS.properties", properties);

	}


	public JSONObject changeStreamInfo(String title, String[] tags) {
		System.out.println("Changing streaming info: Title: " + title);
		//System.out.println("twitchBroadcasterId: " + twitchBroadcasterId);
		//System.out.println("twitchUserAccessToken: " + twitchUserAccessToken);
		//System.out.println("twitchClientId: " + twitchClientId);
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			refreshAccessToken();

			String apiUrl = BASE_URL + "/channels?broadcaster_id=" + twitchBroadcasterId;
			HttpPatch httpPatch = new HttpPatch(apiUrl);

			// Set Authorization header with your access token
			httpPatch.setHeader("Authorization", "Bearer " + twitchUserAccessToken);

			// Set Client-Id header with your Twitch Client ID
			httpPatch.setHeader("Client-Id", twitchClientId);

			// Set Content-Type header
			httpPatch.setHeader("Content-Type", "application/json");

			// Create JSON request body
			JSONObject requestBody = new JSONObject();
			requestBody.put("title", title);
			requestBody.put("broadcaster_language", "es");
			requestBody.put("tags", tags);

			StringEntity entity = new StringEntity(requestBody.toString());
			httpPatch.setEntity(entity);

			//System.out.println("requestBody: " + requestBody.toString());
			//System.out.println("entity: " + entity.toString());

			HttpResponse response = httpClient.execute(httpPatch);
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				String responseString = EntityUtils.toString(responseEntity);
				JSONObject jsonResponse = new JSONObject(responseString);

				if (responseString.toLowerCase().contains("error"))
					System.err.println("Error message in response: " + jsonResponse.getString("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void sendMessage(String message) {
		OAuth2Credential credential = new OAuth2Credential("twitch", twitchUserAccessToken);
		// Configure and create a TwitchClient
		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withDefaultAuthToken(credential) // Replace with your bot's username and OAuth token
				.withEnableHelix(true)
				.withEnableChat(true)
				.withChatAccount(credential)
				//.withChatListener(new YourChatListener()) // Implement your own chat listener
				.build();

		TwitchChat myChat = twitchClient.getChat();
		myChat.joinChannel(channelName);
		myChat.sendMessage(channelName, message);

		//twitchClient.getHelix().sendWhisper(twitchUserAccessToken, twitchBroadcasterId, "803497219", message);
	}

	/**
	 * Con la consola de twitch CLI no funcionan los whispers, asi que lo hago con una peticion http
	 * @param message
	 */
	public void sendWhisper(String message, String broadcasterIdTo) {
		try {
			// Create an HttpClient
			HttpClient httpClient = HttpClients.createDefault();

			// Create an HttpPost request to send the whisper
			HttpPost httpPost = new HttpPost("https://api.twitch.tv/helix/whispers");

			// Set headers
			httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + twitchUserAccessToken);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
			httpPost.setHeader("Client-Id", twitchClientId);

			// Create the JSON request body
			JSONObject requestBody = new JSONObject();
			requestBody.put("from_user_id", twitchBroadcasterId);
			requestBody.put("to_user_id", broadcasterIdTo);
			requestBody.put("message", message);
			StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);

			// Execute the POST request
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				String responseString = EntityUtils.toString(responseEntity);
				JSONObject jsonResponse = new JSONObject(responseString);

				if (responseString.toLowerCase().contains("error"))
					System.err.println("Error message in response: " + jsonResponse.getString("message"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * This code doesn't work. You need to retrieve the credentials manually using this command:
	 * twitch token -u -s 'user:read:email user:edit channel:manage:broadcast user:edit:broadcast moderator:manage:announcements'
	 */
	public void getCredentials() {
		try {
			// Create a temporary file to store the command's output
			File tempFile = File.createTempFile("twitch_token_output_", ".txt");

			// Execute the twitch token command and redirect its output to the temporary file
			ProcessBuilder processBuilder = new ProcessBuilder("twitch", "token", "-u", "-s", "user:read:email user:edit channel:manage:broadcast user:edit:broadcast moderator:manage:announcements");
			processBuilder.redirectOutput(tempFile);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			//Process process = Runtime.getRuntime().exec("twitch token -u -s \"user:read:email user:edit channel:manage:broadcast user:edit:broadcast moderator:manage:announcements\"");

			// Wait for the command to complete
			int exitCode = process.waitFor();


			// Check if the command was successful
			if (exitCode == 0) {
				try (BufferedReader fileReader = new BufferedReader(new FileReader(tempFile))) {
					String line;
					while ((line = fileReader.readLine()) != null) {
						// Check if the line contains the tokens
						if (line.startsWith("User Access Token:") || line.startsWith("Refresh Token:")) {
							// Parse and set the tokens
							if (line.startsWith("User Access Token:")) {
								twitchUserAccessToken = line.substring("User Access Token:".length()).trim();
							} else {
								twitchUserRefreshToken = line.substring("Refresh Token:".length()).trim();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// Delete the temporary file
					tempFile.delete();
				}

				// Also write them
				Properties properties = InputOutput.loadPropertiesFile("src/PASSWORDS.properties");
				// Update the properties with the obtained tokens
				properties.setProperty("twitch_user_access_token", twitchUserAccessToken!=null?twitchUserAccessToken:"error");
				properties.setProperty("twitch_user_refresh_token", twitchUserRefreshToken!=null?twitchUserRefreshToken:"error");
				// And display them
				System.out.println("User Access Token: " + twitchUserAccessToken);
				System.out.println("Refresh Token: " + twitchUserRefreshToken);

				// Save the updated properties back to the file
				InputOutput.savePropertiesFile("src/PASSWORDS.properties", properties);
			} else {
				System.err.println("Command failed with exit code: " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	// Helper method to extract tokens from the command's output
	/*private static String extractToken(String output, String tokenName) {
		int startIndex = output.indexOf(tokenName);
		if (startIndex == -1) {
			return null;
		}
		int endIndex = output.indexOf("\n", startIndex);
		if (endIndex == -1) {
			return null;
		}
		return output.substring(startIndex + tokenName.length(), endIndex).trim();
	}*/


	/* bad methods
	public static void changeStreamTitle(String newTitle) throws IOException {

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String apiUrl = "https://api.twitch.tv/helix/channels?broadcaster_id="+twitchBroadcasterId;
			HttpPut httpPut = new HttpPut(apiUrl);

			// Set OAuth token in the Authorization header
			httpPut.setHeader("Authorization", "Bearer " + OAuthToken);

			// Set Client-Id token in the Authorization header
			httpPut.setHeader("Client-Id", twitchClientId);

			// Set the new stream title in the request body
			String requestBody = "{\"title\": \"" + newTitle + "\"}";
			httpPut.setEntity(new StringEntity(requestBody));

			try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);

				// Handle the API response here (e.g., check for success or error messages)
				System.out.println("API Response: " + responseString);
			}
		}
	}
	public void updateStreamProperties(String newTitle, String[] tags) throws IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String apiUrl = "https://api.twitch.tv/helix/channels?broadcaster_id=" + twitchBroadcasterId;
			HttpPatch httpPatch = new HttpPatch(apiUrl);

			// Set OAuth token in the Authorization header
			httpPatch.setHeader("Authorization", "Bearer " + OAuthToken);

			// Set the Client ID header
			httpPatch.setHeader("Client-ID", twitchClientId);

			// Set the Content-Type header
			httpPatch.setHeader("Content-Type", "application/json");

			// Create a JSON request body
			String requestBody = "{" +
					"\"title\": \"" + newTitle + "\"," +
					"\"broadcaster_language\": \"" + "es" + "\"," +
					"\"tags\": " + Arrays.toString(tags) +
					"}";

			httpPatch.setEntity(new StringEntity(requestBody));

			try (CloseableHttpResponse response = httpClient.execute(httpPatch)) {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);

				// Handle the API response here (e.g., check for success or error messages)
				System.out.println("API Response: " + responseString);
			}
		}
	}
	 */



}
