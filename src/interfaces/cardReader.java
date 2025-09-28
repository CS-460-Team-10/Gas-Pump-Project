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

public class cardReader {
    private final ioServer api;

    public cardReader(int connector) throws Exception {
        api = new ioServer(connector);
    }

    private void readCard(String cardNumber) {
        if (cardNumber != null && !cardNumber.isEmpty()) {
            api.send("Card-No. - " + cardNumber);
        }
    }

    private String genRandomCard(){
        // Generate a random 16-digit credit card number
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append((int)(Math.random() * 10));
            if(i == 3 || i == 7){
                cardNumber.append("-");
            }
        }
        return cardNumber.toString();
    }

    public static class CardReaderGraphics extends Application {
        private cardReader cardReader;

        @Override
        public void start(Stage primaryStage) {
            new Thread(() -> {
                try {
                    cardReader = new cardReader(6001);
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
                cardReader.readCard(cardReader.genRandomCard());
            });

            StackPane root = new StackPane(reader);
            Scene scene = new Scene(root, 300, 200);
            primaryStage.setTitle("Credit Card Reader");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) throws Exception {
        Application.launch(cardReader.CardReaderGraphics.class, args);
    }
}
