package interfaces;

import helpers.imageLoader;
import javafx.application.Application;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import socketAPI.ioServer;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CreditCardReader {
    private final ioServer api;

    public CreditCardReader(int connector) throws Exception {
        api = new ioServer(connector);
    }

    private void readCard(String cardNumber) {
        if (cardNumber != null && !cardNumber.isEmpty()) {
            api.send("card-input(" + cardNumber + ")");
        }
    }

    public static class CardReaderGraphics extends Application {
        private CreditCardReader cardReader;

        @Override
        public void start(Stage primaryStage) {
            new Thread(() -> {
                try {
                    CreditCardReader c = new CreditCardReader(6001);
                    this.cardReader = c;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "CardReader-Conn").start();

            imageLoader img = new imageLoader();
            img.loadImages();

            ImageView reader = new ImageView(img.imageList.get(2));
            reader.setPreserveRatio(true);
            reader.setFitWidth(300);
            reader.setSmooth(true);
            reader.setPickOnBounds(true);

            reader.setOnMouseClicked(e -> {
                reader.setImage(img.imageList.get(2));
                PauseTransition first = new PauseTransition(Duration.millis(1000));
                PauseTransition second = new PauseTransition(Duration.millis(2000)); 
                first.setOnFinished(ev -> { reader.setImage(img.imageList.get(0)); second.play(); });
                second.setOnFinished(ev -> { reader.setImage(img.imageList.get(2)); });
                first.play();
                cardReader.readCard("Card Tapped!");
            });

            StackPane root = new StackPane(reader);
            Scene scene = new Scene(root, 300, 200);
            primaryStage.setTitle("Credit Card Reader");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) throws Exception {
        Application.launch(CreditCardReader.CardReaderGraphics.class, args);
    }
}
