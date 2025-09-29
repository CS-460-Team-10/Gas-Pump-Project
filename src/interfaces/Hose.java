package interfaces;
import java.io.IOException;

import helpers.imageLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import socketAPI.ioServer;

public class hose {
    private boolean attached;
    private final ioServer api;

    /**
     * Constructor to initialize the hose device.
     * @param connector Connector/port number to connect the device
     * @throws IOException if the device initialization fails
     */
    public hose(int port) throws IOException {
        this.attached = false;
        this.api = new ioServer(port);
        System.out.println("Hose is up: " + port);

    }

    /**
     * Updates the hose sensor status and sends messages to the API.
     * @param sensorAttached boolean from the sensor that indicates if the hose is attached.
     * @param sensorTankFull boolean from the sensor that indicates if the tank is full
     */
    public void updateSenor(boolean sensorAttached) {
        // checks if the hose is attached
        if (sensorAttached && !attached) {
            attached = true;
            System.out.println("Hose attached");
            api.send("Hose-Attached.");
        } else if (!sensorAttached && attached) {
            attached = false;
            System.out.println("Hose detached");
            api.send("Hose-Detached.");
        }
    }

    /**
     * Inner class for the GUI representation of the hose.
     */
    public static class HoseGraphics extends Application {
        private hose hose;

        @Override
        public void start(Stage primaryStage) {

            imageLoader img = new imageLoader();
            img.loadImages();

            // Show idle reader image
            ImageView hoseView = new ImageView(img.imageList.get(5));
            hoseView.setPreserveRatio(true);
            hoseView.setFitWidth(300);
            hoseView.setSmooth(true);
            hoseView.setPickOnBounds(true);

            try {
                hose = new hose(6004);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean[] toggled = {false};
            hoseView.setOnMouseClicked(e -> {
                toggled[0] = !toggled[0];
                if (toggled[0]) {
                    hoseView.setImage(img.imageList.get(6));
                    hose.updateSenor(true);
                } else {
                    hoseView.setImage(img.imageList.get(5));
                    hose.updateSenor(false);
                }
            });

            // Set up GUI layout
            StackPane root = new StackPane(hoseView);
            Scene scene = new Scene(root, 300, 200);

            primaryStage.setTitle("Hose");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    /**
     * Main method to launch JavaFX app.
     */
    public static void main(String[] args) throws InterruptedException, Exception {
        Application.launch(hose.HoseGraphics.class, args);
    }
}
