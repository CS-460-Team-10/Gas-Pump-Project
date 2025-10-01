package interfaces;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import socketAPI.ioServer;

public class gasStation {
    private ioServer api; // communication api

    public gasStation(int connector) throws Exception {
        api = new ioServer(connector);
    }

    public static class GasStationGraphics extends Application {
        private gasStation station;

        private TextField[] productFields = new TextField[4];
        private String[] prevValidTexts = new String[4];
        private TextArea logArea;

        @Override
        public void start(Stage primaryStage) throws IOException {
            // background connection loop
            new Thread(() -> {
                try {
                    station = new gasStation(6007);
                    sendPriceList();
                    while (true) {
                        String msg = station.api.get();
                        if (msg != null && !msg.isEmpty()) {
                            System.out.println("GasStation Received: " + msg);

                            Platform.runLater(() ->
                                logArea.appendText("Received: " + msg + "\n")
                            );

                            if (msg.contains("cash-paid(")) {
                                String d = msg.substring(msg.indexOf("(") + 1, msg.length() - 1);
                                System.out.println("Transaction recorded: $" + d);
                                Platform.runLater(() ->
                                    logArea.appendText("Transaction recorded: $" + d + "\n")
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Station-Conn").start();

            // save past values to revert to in case of invalid changes attempted
            productFields[0] = new TextField("Unleaded:3.25");
            productFields[1] = new TextField("Premium:3.75");
            productFields[2] = new TextField("Premium Plus:4.00");
            productFields[3] = new TextField("Gasoline:3.50");

            prevValidTexts[0] = productFields[0].getText();
            prevValidTexts[1] = productFields[1].getText();
            prevValidTexts[2] = productFields[2].getText();
            prevValidTexts[3] = productFields[3].getText();

            for (int i = 0; i < productFields.length; i++) {
                final int idx = i;
                productFields[i].focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        validateField(idx);
                    }
                });
                productFields[i].setOnAction(ev -> validateField(idx)); // Enter key
            }

            Button sendBtn = new Button("Update Prices");
            sendBtn.setOnAction(e -> sendPriceList());

            VBox editor = new VBox(10,
                new Label("Products:"),
                productFields[0],
                productFields[1],
                productFields[2],
                productFields[3],
                sendBtn
            );
            editor.setPrefWidth(200);

            // log area
            logArea = new TextArea();
            logArea.setEditable(false);

            // put side by side
            HBox root = new HBox(10, logArea, editor);

            primaryStage.setScene(new Scene(root, 600, 200));
            primaryStage.setTitle("Gas Station");
            primaryStage.show();
        }

        private boolean validateField(int i) {
            String original = productFields[i].getText();
            String raw = original == null ? "" : original.trim();

            // only allow exactly one colon
            int colon = raw.indexOf(':');
            if (colon <= 0 || colon != raw.lastIndexOf(':')) {
                productFields[i].setText(prevValidTexts[i]);
                if (logArea != null) {
                    logArea.appendText("Invalid entry in product " + (i + 1) + ". Formatting only allows for Product Name:Price (letters/spaces only; Example: Unleaded Things:3.01)\n");
                }
                return false;
            }

            String product = raw.substring(0, colon).trim();
            String price = raw.substring(colon + 1).trim();

            boolean productOk = product.matches("[A-Za-z ]+");

            boolean priceOk = price.matches("\\d+(\\.\\d{1,2})?");

            if (!productOk || !priceOk) {
                productFields[i].setText(prevValidTexts[i]);
                if (logArea != null) {
                    logArea.appendText("Invalid entry in product " + (i + 1) + ". Formatting only allows for Product Name:Price (letters/spaces only; e.g., Unleaded:3.25)\n");
                }
                return false;
            }

            // normalize spaces around the colon
            String normalized = product.replaceAll(" +", " ").trim() + ":" + price;
            productFields[i].setText(normalized);
            prevValidTexts[i] = normalized;
            return true;
        }

        private void sendPriceList() {
            StringBuilder sb = new StringBuilder("Product-List. - ");

            for (int i = 0; i < productFields.length; i++) {
                validateField(i);
            }

            for (int i = 0; i < productFields.length; i++) {
                String[] kv = productFields[i].getText().split(":");
                String grade = kv[0].trim();
                String price = kv[1].trim();
                sb.append(grade).append('-').append(price);

                if (i < productFields.length - 1) {
                    sb.append(':'); // separate each pair with :
                }
            }

            String msg = sb.toString();
            station.api.send(msg);
            System.out.println("Sending: " + msg);
            logArea.appendText("Sent: " + msg + "\n");
        }
    }

    public static void main(String[] args) throws Exception {
        Application.launch(gasStation.GasStationGraphics.class, args);
    }
}