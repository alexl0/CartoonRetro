package cartoonretro.chatbot;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class ChatGPTClient {
    private String chatGPTApiKey;
    private TwitchClient twitchClient;

    public ChatGPTClient(String chatGPTApiKey) {
        this.chatGPTApiKey = chatGPTApiKey;
        initializeTwitchClient();
    }

    private void initializeTwitchClient() {
        // Initialize the OAuth2Credential with your bot's OAuth token
//        OAuth2Credential credential = new OAuth2Credential("your_twitch_bot_oauth_token");

        // Initialize the TwitchClient with your credentials using the TwitchClientBuilder
//        twitchClient = TwitchClientBuilder.builder()
//                .withClientId("your_twitch_client_id") // Replace with your Twitch application client ID
//                .withClientSecret("your_twitch_client_secret") // Replace with your Twitch application client secret
//                .withEnableChat(true) // Enable chat functionality
//                .withChatAccount(credential) // Set the OAuth2Credential
//                .build();

        // Register a listener for channel messages
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, this::onChannelMessage);

        // Connect to Twitch chat
        twitchClient.getChat().connect();
    }

    private void onChannelMessage(ChannelMessageEvent event) {
        String message = event.getMessage().toLowerCase();
        String username = event.getUser().getName();

        // Check if the message is a command or normal chat
        if (message.startsWith("!")) {
            // Respond to commands (optional)
            handleChatCommands(username, message);
        } else {
            // Respond to normal chat messages
            handleChatMessages(username, message);
        }
    }

    private void handleChatCommands(String username, String message) {
        // Implement your command handling logic here (if needed)
        // Example: if (message.equals("!hello")) { twitchClient.getChat().sendMessage("Hello, " + username + "!"); }
    }

    private void handleChatMessages(String username, String message) {
        // Implement your ChatGPT API call here to generate a response
        // Example:
        // String response = callChatGPTAPI(message);
        // twitchClient.getChat().sendMessage(response);
    }

    // Implement your method to call the ChatGPT API and generate a response
    // private String callChatGPTAPI(String message) {
    //     // Make the API call and return the response
    // }

    public void disconnect() {
        // Disconnect from Twitch chat
        twitchClient.getChat().disconnect();
    }
}
