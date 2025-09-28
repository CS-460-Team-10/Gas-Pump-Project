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

            imageLoader img = new imageLoader();
            img.loadImages();

            ImageView reader = new ImageView(img.imageList.get(2));
            reader.setPreserveRatio(true);
            reader.setFitWidth(300);
            reader.setSmooth(true);
            reader.setPickOnBounds(true);

            try {
                cardReader = new cardReader(6001);
            } catch (Exception e) {
                e.printStackTrace();
            }

            reader.setOnMouseClicked(e -> {
                reader.setImage(img.imageList.get(2));
                PauseTransition blue = new PauseTransition(Duration.millis(1000));
                PauseTransition red = new PauseTransition(Duration.millis(1000)); 
                PauseTransition green = new PauseTransition(Duration.millis(1000)); 
                blue.setOnFinished(ev -> { 
                    reader.setImage(img.imageList.get(0)); 
                    String cardApproval = null; int i = 0;

                    while(cardApproval == null || cardApproval.isEmpty()){
                        i++;
                        try { Thread.sleep(100); } catch (InterruptedException e1) { e1.printStackTrace(); }
                        if(i > 100) break; // timeout after 10 seconds

                        cardApproval = cardReader.api.get();
                        if(cardApproval != null && !cardApproval.isEmpty()){
                            if(cardApproval.contains("C1")){
                                green.play();
                            } else if(cardApproval.contains("C0")){
                                red.play(); 
                            }
                        }
                    }
                });
                green.setOnFinished(ev -> { reader.setImage(img.imageList.get(1)); red.play(); });
                red.setOnFinished(ev -> { reader.setImage(img.imageList.get(2)); });
                
                blue.play();
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
