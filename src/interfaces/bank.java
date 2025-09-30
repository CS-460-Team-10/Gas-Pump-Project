package interfaces;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import socketAPI.ioServer;

public class bank {
    private final ioServer api; // communication api
    private double balance = 0; // dummy balance variable
    private static TextArea logArea; // GUI log

    public bank(int connector) throws Exception {
        api = new ioServer(connector);
        authorization();
    }

    private boolean validCard() {
        balance = Math.random() * 100; // Balance of the card
        return balance > 10; // 90% chance of being valid
    }

    // main bank loop
    public void authorization() throws Exception {
        while (true) {
            String msg = api.get();
            if (msg != null) {
                log("Bank Received: " + msg);

                if (msg.contains("Card-No. - ") && validCard()) {
                    api.send("Card-Approved.");
                    log("Bank approved");

                } else if (msg.contains("Card-No. - ")) {
                    api.send("Card-Denied.");
                    log("Bank denied");
                }

                if (msg.contains("Transaction-Complete. Amount: $")) {
                    msg = msg.trim().replace("Transaction-Complete. Amount: $", "");
                    balance -= Double.parseDouble(msg);
                    String bal = String.format("$%.2f", balance);
                    log("Bank charged card. New balance: " + bal);
                }
            }
            Thread.sleep(50);
        }
    }

    // helper for bank log
    private static void log(String text) {
        System.out.println(text);
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(text + "\n"));
        }
    }

    public static class BankGraphics extends Application {
        @Override
        public void start(Stage primaryStage) {
            logArea = new TextArea();
            logArea.setEditable(false);
            logArea.setPrefHeight(400);

            VBox root = new VBox(10, new Label("Bank Log:"), logArea);

            primaryStage.setScene(new Scene(root, 600, 200));
            primaryStage.setTitle("Bank");
            primaryStage.show();

            // process connection
            new Thread(() -> {
                try {
                    new bank(6006);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Bank-Thread").start();
        }
    }

    public static void main(String[] args) {
        Application.launch(BankGraphics.class, args);
    }
}