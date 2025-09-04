import helpers.imageLoader;
import javafx.application.Application;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CreditCardReader {
    private final ioPort api;

    public CreditCardReader(int deviceType, int connector) throws Exception {
        api = ioPort.ChooseDevice(deviceType);  
        api.ioport(connector);
    }

    private void readCard(String cardNumber) {
        boolean authorized = cardNumber != null && !cardNumber.isEmpty();
        if (authorized) {
            api.send("Authorized.");
        } else {
            api.send("Unauthorized.");
        }
    }

    public static class CardReaderGraphics extends Application {
        private CreditCardReader cardReader;

        @Override
        public void start(Stage primaryStage) {
            new Thread(() -> {
                try {
                    CreditCardReader c = new CreditCardReader(1, 3);
                    this.cardReader = c;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "CardReader-Conn").start();

            imageLoader img = new imageLoader();
            img.loadImages();

            // Show idle reader image
            ImageView reader = new ImageView(img.imageList.get(3));
            reader.setPreserveRatio(true);
            reader.setFitWidth(300);
            reader.setSmooth(true);
            reader.setPickOnBounds(true);

            reader.setOnMouseClicked(e -> {
                reader.setImage(img.imageList.get(2));

                PauseTransition first = new PauseTransition(Duration.millis(1000));
                PauseTransition second = new PauseTransition(Duration.millis(2000)); 
                first.setOnFinished(ev -> { reader.setImage(img.imageList.get(0)); second.play(); });
                second.setOnFinished(ev -> { reader.setImage(img.imageList.get(3)); });
                first.play();

                this.cardReader.readCard("Card Tapped!");
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
