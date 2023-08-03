package cartoonretro.obs;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.ui.StudioModeStateChangedEvent;
import io.obswebsocket.community.client.message.request.ui.GetStudioModeEnabledRequest;
import io.obswebsocket.community.client.message.response.ui.GetStudioModeEnabledResponse;

public class OBSController {
	private OBSRemoteController obsRemoteController;
	private boolean isReconnecting = false;
	private static String obsWebSocketPass;


	public OBSController(String obsWebSocketPass) {
		OBSController.obsWebSocketPass = obsWebSocketPass;
		createOBSRemoteController();
	}

	private void createOBSRemoteController() {
		// Create OBSRemoteController through its builder
		this.obsRemoteController = OBSRemoteController.builder()
				.autoConnect(false)                       // Do not connect automatically
				.host("192.168.1.137")                        // Set the host
				.port(4455)                               // Set the port
				.password(obsWebSocketPass)                       // Set the password
				.lifecycle()                              // Add some lifecycle callbacks
				.onReady(this::onReady)                 // Add onReady callback
				.and()                                  // Build the LifecycleListenerBuilder
				.registerEventListener(StudioModeStateChangedEvent.class,
						this::onStudioModeStateChanged)       // Register a StudioModeStateChangedEvent
				.build();                                 // Build the OBSRemoteController
	}

	public void connect() {
		obsRemoteController.connect();
	}

	private void onReady() {
		if (!isReconnecting) {
			// First connection
			onFirstConnection();
		} else {
			// Second connection
			onSecondConnection();
		}
	}

	private void onFirstConnection() {
		// Send a blocking call (last parameter is a timeout instead of a callback)
		this.obsRemoteController.setStudioModeEnabled(true, 1000);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {}

		// Send a request through a convenience method
		this.obsRemoteController.getSceneList(getSceneListResponse -> {
			if (getSceneListResponse.isSuccessful()) {
				// Print each Scene
				getSceneListResponse.getScenes().forEach(scene -> System.out.println(scene));
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {}

			this.obsRemoteController.setStudioModeEnabled(false, 1000);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {}

			// My testing code
			this.obsRemoteController.triggerMediaInputAction("Video640x480", "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_PLAY", 1000);
			
			this.disconnectAndReconnect();
		});
	}

	private void onSecondConnection() {
		// Send a Request through specific Request builder
		this.obsRemoteController.sendRequest(GetStudioModeEnabledRequest.builder().build(),
				(GetStudioModeEnabledResponse requestResponse) -> {
					if (requestResponse.isSuccessful()) {
						if (requestResponse.getStudioModeEnabled()) {
							System.out.println("Studio mode enabled");
						} else {
							System.out.println("Studio mode not enabled");
						}
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {}

					// Disconnect
					this.obsRemoteController.disconnect();
				});
	}

	private void disconnectAndReconnect() {
		// Disconnect
		this.obsRemoteController.disconnect();

		// Set a flag
		this.isReconnecting = true;

		//    // Recreate instance
		this.createOBSRemoteController();

		// Reconnect
		this.obsRemoteController.connect(); // onReady will be called another time
	}

	private void onStudioModeStateChanged(StudioModeStateChangedEvent studioModeStateChangedEvent) {
		System.out.println("Studio Mode State Changed to: {}" + studioModeStateChangedEvent.getStudioModeEnabled());
	}


}
