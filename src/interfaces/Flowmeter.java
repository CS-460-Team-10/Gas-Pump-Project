package interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import socketAPI.ioServer;
import javafx.scene.control.Label;
import helpers.imageLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class flowMeter {
    private final double FLOW_RATE = 0.04; // Gal/tenth-sec
    private double gallonsPumped;
    private double pricePerGallon = 1.00; // Default to 1 dollar
    private boolean pumping;
    static PrintWriter out;
    static BufferedReader bf;
    private final ioServer api;
    private String[] productList = new String[4];
    private String fuelChosen = "";

    public flowMeter(int connector) throws IOException {
        gallonsPumped = 0.0;
        pumping = false;

        api = new ioServer(connector);
    }

    /**
     * This code simulates fuel flow increasing gallons pumped.
     * in a real station.
     * @param gallons the amount of gallons being pumped.
     */
    public void flow(double gallons) {
        if (pumping) {
            gallonsPumped += gallons;
        }
    }
    // gets total gallons being pumped
    public double getGallonsPumped() {
        return gallonsPumped;
    }
    // get total cost
    public double getTotalCost() {
        return gallonsPumped * pricePerGallon;
    }

    // Selects fuel to flow in pump
    public void selectGrade(int i) {
        fuelChosen = productList[i];
        System.out.println("Fuel Selected: " + fuelChosen);
        String[] choice = fuelChosen.split("-", 2);
        pricePerGallon = Double.parseDouble(choice[1].trim());
    }

    /**
     * Inner class for the GUI representation of the hose.
     */
    public static class FlowmeterGraphics extends Application {
        private flowMeter meter;

        @Override
        public void start(Stage primaryStage) {
            imageLoader img = new imageLoader();
            img.loadImages();

            // Show idle meter off image
            StackPane root;
            ImageView meterView = new ImageView(img.imageList.get(3));
            meterView.setPreserveRatio(true);
            meterView.setFitWidth(300);
            meterView.setSmooth(true);
            meterView.setPickOnBounds(true);

            // Fuel flow label
            Label fuelCostLabel = new Label();
            fuelCostLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                "-fx-text-fill: green;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;"
            );

            root = new StackPane(meterView, fuelCostLabel);
            StackPane.setAlignment(fuelCostLabel, Pos.CENTER);
            fuelCostLabel.setTranslateY(-48);

            Scene scene = new Scene(root, 300, 200);
            primaryStage.setTitle("Flowmeter");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Process connections
            new Thread(() -> {
                try {
                    meter = new flowMeter(6002);

                    while (true) {
                        String msg = meter.api.get();

                        if (msg != null && !msg.isEmpty()) {

                            // Message Interpretations
                            if (msg.contains("FM1")) {
                                System.out.println("Meter turning ON");
                                Platform.runLater(() -> {
                                    fuelCostLabel.setText("00.00-Gal");
                                });

                            } else if (msg.contains("FM0")) {
                                System.out.println("Meter turning OFF");
                                meter.gallonsPumped = 0.0; // reset between sessions
                                Platform.runLater(() -> {
                                    fuelCostLabel.setText("");
                                });

                            } else if (msg.contains("P1")) { // fuel flowing
                                meter.pumping = true;
                                Platform.runLater(() -> {
                                    meterView.setImage(img.imageList.get(4));
                                });

                            } else if (msg.contains("P0")) { // fuel stop flowing
                                meter.pumping = false;
                                double fuelPurchased = meter.pricePerGallon * meter.getGallonsPumped();
                                fuelPurchased = Math.round(fuelPurchased * 100.0) / 100.0;
                                String amount = String.format("%.2f", fuelPurchased);
                                Platform.runLater(() -> {
                                    meterView.setImage(img.imageList.get(3));
                                });
                                meter.api.send("Transaction-Complete. Amount: $" + amount);

                            } else if (msg.contains("Fuel-Grade. - ")) {
                                System.out.println(msg);
                                msg.replace("Product-List. - ", "");
                                msg.replace("[\\d-]", "");
                                System.out.println(msg);
                                int i = 0;
                                for (String product: meter.productList) {
                                    i++;
                                    System.out.println(product + "  |||  " + msg);
                                    if (msg.contains(product)) {
                                        meter.selectGrade(i);
                                        break;
                                    }
                                }
                            } else if (msg.contains("Product-List. - ")) {
                                msg = msg.replace("Product-List. - ", "");
                                System.out.println("Product-List: " + msg);
                                meter.productList = msg.split(":");
                            }
                        }

                        // Simulate flow and update label if ON
                        if (meter.pumping) {
                            meter.flow(meter.FLOW_RATE);
                            double g = meter.getGallonsPumped();
                            String gal = String.format("%.2f-Gal", g);
                            Platform.runLater(() -> fuelCostLabel.setText(gal));
                            meter.api.send("Gal Pumped: " + String.format("%.2f", g));
                            Thread.sleep(100); // Pause per second to flow x-gal/tenth-sec
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Flowmeter-Conn").start();
        }
    }

    /**
     * Main method to launch JavaFX app.
     */
    public static void main(String[] args) throws InterruptedException, Exception {
        Application.launch(flowMeter.FlowmeterGraphics.class, args);
    }
}