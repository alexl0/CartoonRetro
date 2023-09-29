package cartoonretro.obs;

import io.obswebsocket.community.client.OBSRemoteController;
//import io.obswebsocket.community.client.message.event.ui.StudioModeStateChangedEvent;

public class OBSController {
	private OBSRemoteController obsRemoteController;
	private static String obsWebSocketPass;
	private static String obsWebSocketIp;


	public OBSController(String obsWebSocketPass, String obsWebSocketIp) {
		OBSController.obsWebSocketPass = obsWebSocketPass;
		OBSController.obsWebSocketIp = obsWebSocketIp;
		createOBSRemoteController();
	}

	private void createOBSRemoteController() {
		// Create OBSRemoteController through its builder
		this.obsRemoteController = OBSRemoteController.builder()
				.autoConnect(false)                       // Do not connect automatically
				//YOU SHOULD SET A STATIC IP ON YOUR COMPUTER, IF NOT, THIS IP VARIES ON THE WEBSOCKET SERVER SETTINGS ON OBS EVERY TIME!!        
				.host(obsWebSocketIp)                        // Set the host
				.port(4455)                               // Set the port
				.password(obsWebSocketPass)                       // Set the password
//				.lifecycle()                              // Add some lifecycle callbacks
//				.onReady(this::onReady)                 // Add onReady callback
//				.and()                                  // Build the LifecycleListenerBuilder
//				.registerEventListener(StudioModeStateChangedEvent.class,
//						this::onStudioModeStateChanged)       // Register a StudioModeStateChangedEvent
				.build();                                 // Build the OBSRemoteController
	}

	public void connect() {
		obsRemoteController.connect();
	}

	public void disconnect() {
		obsRemoteController.disconnect();
	}
	
	public void setScene(String sceneName) {
		this.obsRemoteController.setCurrentProgramScene(sceneName, 2000);
		System.out.println("OBS: Changed scene to: " + sceneName);
	}

}
