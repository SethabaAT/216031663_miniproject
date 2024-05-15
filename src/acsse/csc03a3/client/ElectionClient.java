package acsse.csc03a3.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * The ElectionClient class serves as the entry point for the client
 * application.
 * 
 * @author Arnold Thabo Sethaba
 */
public class ElectionClient extends Application {

	/**
	 * The start method is called after the init method has returned and the JavaFX
	 * application thread has been started.
	 * It loads the main FXML scene and displays it in the primary stage.
	 * 
	 * @param primaryStage The primary stage for this application, onto which the
	 *                     application scene is set.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The main method is the entry point of the client application.
	 * 
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
