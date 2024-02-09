package cartoonretro.obs;

import java.util.List;
import java.util.Optional;

import cartoonretro.FromDBToTwitch;
import io.obswebsocket.community.client.OBSRemoteController;
//import io.obswebsocket.community.client.message.event.ui.StudioModeStateChangedEvent;
import io.obswebsocket.community.client.model.SceneItem;

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
		this.obsRemoteController.setCurrentProgramScene(sceneName, 10000);
	}

	public void setPlanDay(String day) {
		String[] daysOfTheWeek = FromDBToTwitch.daysOfWeekSpanish;
		for(int i=0; i<daysOfTheWeek.length; i++) {
			final int innerI = i;
			
			String[] scenes = {"Series16:9", "Series4:3"};
			for(int j=0;j<scenes.length;j++) {
				List<SceneItem> sceneItems = this.obsRemoteController.getSceneItemList(scenes[j], 10000).getSceneItems();
				Optional<SceneItem> foundItem = sceneItems.stream().filter(sceneItem -> sceneItem.getSourceName().toLowerCase().equals("plan"+daysOfTheWeek[innerI].toString())).findFirst();
				if (foundItem.isPresent()) {
					SceneItem item = foundItem.get();
					if(daysOfTheWeek[i].equals(day))
						this.obsRemoteController.setSceneItemEnabled(scenes[j], item.getSceneItemId(), true, 10000);
					else
						this.obsRemoteController.setSceneItemEnabled(scenes[j], item.getSceneItemId(), false, 10000);
				} else {
					System.out.println("Scene Item not found: " + "plan" + daysOfTheWeek[i]);
				}
			}
		}
	}

	public void startStream() {
		this.obsRemoteController.startStream(10000);
	}
	
	public void stopStream() {
		this.obsRemoteController.stopStream(10000);
	}

}
