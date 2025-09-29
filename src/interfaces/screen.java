package interfaces;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import socketAPI.ioServer;

public class screen {
    private final ioServer api;

    public screen(int connector) throws UnknownHostException, IOException { 
        api = new ioServer(connector);
    }

    /**
     * Inner class for the GUI of the screen.
     */
    public static class screenGraphics extends Application {
        private screen display;

        private ArrayList<horizontal> hList = new ArrayList<>();
        private String[] textFont = { "Serif", "Times New Roman", "Courier New" }; // 3 Fonts Supported
        private Color[] textColor = { Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE }; // 5 Text Colors Supported
        private Color myBlue = Color.web("#1e3a8a"); // Custom blue color
        private Color[] screenColor = { Color.BLACK, Color.RED, Color.GREEN, myBlue, Color.WHITE }; // 5 Screen Colors Supported
        private int[] textSize = { 30, 20, 15 }; // 3 Text Sizes Supported
        private int hCount = 5; // Number of rows to create in the terminal
        private ArrayList<Button> buttonSequence = new ArrayList<>();
        private ArrayList<Integer> buttonIDs = new ArrayList<>();
        private int sequenceConfirmId = -1;
        private boolean sequencer = false;
        private int sequenceLength = 0;

        @Override
        public void start(Stage primaryStage) {
            initializeScreen(primaryStage);

            // Process connections
            new Thread(() -> {
                try {
                    display = new screen(6000);

                    // Update screen via messages from hub
                    while (true) {
                        String msg = display.api.get();

                        if (msg != null && !msg.isBlank()) {
                            String uiMsg = msg.replace("Port 1: ", "");
                            System.out.println("Screen Receiving: \n" + uiMsg);

                            Platform.runLater(() -> {
                                blankScreen();
                                interpretMessage(uiMsg);
                            });
                        }
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Screen-Conn").start();
        }

        // Initialize the screen
        private void initializeScreen(Stage screenStage){
            VBox vertical = new VBox(3);
            vertical.setFillWidth(true); // Children stretch to VBox width
            vertical.setMaxWidth(Double.MAX_VALUE); // VBox fills Scene width
            vertical.setMaxHeight(Double.MAX_VALUE); // VBox fills Scene height

            // Create screen layout
            for(int i = 1; i <= hCount; i++){

                horizontal h = new horizontal(i);
                h.bL.setStyle("-fx-background-color: gray; -fx-text-fill: black; -fx-font-weight: bold;");
                h.bR.setStyle("-fx-background-color: gray; -fx-text-fill: black; -fx-font-weight: bold;");
                h.tL.setStyle("-fx-text-fill: white;");
                h.tLR.setStyle("-fx-text-fill: white;");
                h.tR.setStyle("-fx-text-fill: white;");

                hList.add(h); // Add horizontal's to a list for access
                VBox.setVgrow(h.h, Priority.ALWAYS); // Make HBox grow vertically
                vertical.getChildren().add(h.h);
            }

            String initialScreenColor = String.format("rgb(%d, %d, %d);",
                (int) (screenColor[3].getRed() * 255),
                (int) (screenColor[3].getGreen() * 255),
                (int) (screenColor[3].getBlue() * 255));
            vertical.setStyle("-fx-background-color: " + initialScreenColor); // use array value
            Scene scene = new Scene(vertical, 600, 400);
            screenStage.setTitle("Gas Pump Terminal");
            screenStage.setScene(scene);
            screenStage.show();
        }

        // Horizontal subclass consisting of L/R buttons, fields, and the combined text field - all accessible in hList
        private class horizontal {
            HBox h;
            Button bL;
            Label tL;
            Label tLR;
            Label tR;
            Button bR;

            // Initializes all horizontal entities in a single line (buttons & fields)
            private horizontal(int count) {
                int leftID = (count-1)*2;
                int rightID = leftID+1;
                
                h = new HBox(10); // HBox to wrap everything into

                bL = new Button();
                bL.setOnAction(event -> {
                    sendButtonData
                    (bL, leftID);
                });

                tL = new Label();
                tLR = new Label();
                tR = new Label();

                bR = new Button();
                bR.setOnAction(event -> {
                    sendButtonData
                    (bR, rightID);
                });

                // Wrap buttons and fields into HBox (In the order: Button, Field, Field, Field, Button)
                HBox.setHgrow(bL, Priority.ALWAYS);
                bL.setMaxWidth(80);
                bL.setMinWidth(80);
                bL.setMaxHeight(80);
                bL.setMinHeight(80);

                HBox.setHgrow(tL, Priority.ALWAYS);
                tL.setMaxWidth(Double.MAX_VALUE);
                tL.setMaxHeight(Double.MAX_VALUE);
                tL.setAlignment(Pos.CENTER_LEFT);
                tL.setWrapText(true);
                HBox.setHgrow(tLR, Priority.ALWAYS);
                tLR.setMaxWidth(Double.MAX_VALUE);
                tLR.setMaxHeight(Double.MAX_VALUE);
                tLR.setAlignment(Pos.CENTER);
                tLR.setWrapText(true);
                HBox.setHgrow(tR, Priority.ALWAYS);
                tR.setMaxWidth(Double.MAX_VALUE);
                tR.setMaxHeight(Double.MAX_VALUE);
                tR.setAlignment(Pos.CENTER_RIGHT);
                tR.setWrapText(true);

                HBox.setHgrow(bR, Priority.ALWAYS);
                bR.setMaxWidth(80);
                bR.setMinWidth(80);
                bR.setMaxHeight(80);
                bR.setMinHeight(80);
                
                h.getChildren().addAll(bL, tL, tLR, tR, bR);
            }
        }

        // Send button data through api
        private void sendButtonData
        (Button b, int id){
            if(sequencer) buttonSequencer(b, id);
            else{
                System.out.println("SENDING: bp" + id);
                display.api.send("bp" + id);
            }
        }

        // Create a blank screen
        public void blankScreen(){              
            for(horizontal row : hList){
                row.tL.setText("");
                row.tLR.setText("");
                row.tR.setText("");
                divideTextFields(row.tL, row.tLR, row.tR);
            }
        }

        // Interpret markup message (Only pass in what is changing)
        private void interpretMessage(String msg){
            String[] tokens = msg.split(":");
            boolean wasCombined = false;

            for (String token : tokens) {
                if (token.isBlank()) continue;
                String[] settings = token.split("/");

                if (token.charAt(0) == 't') {
                    String id = settings[0];
                    int fieldNum = Character.getNumericValue(id.charAt(1));
                    boolean combinedField = id.length() > 2 && Character.isDigit(id.charAt(2));

                    if (fieldNum % 2 == 0) wasCombined = false;      // reset on even
                    int rowNum = fieldNum / 2;
                    horizontal row = hList.get(rowNum);

                    char c = token.charAt(1);
                    boolean isOdd = Character.isDigit(c) && ((c - '0') % 2 == 1);
                    if(isOdd){ wasCombined = false;}

                    if (combinedField) {
                        wasCombined = true;
                        combineTextFields(row.tL, row.tLR, row.tR);
                        fieldConfig(row.tLR, settings);
                    } else if (fieldNum % 2 == 0 && !wasCombined) {
                        fieldConfig(row.tL, settings);
                    } else if (!wasCombined) {
                        fieldConfig(row.tR, settings);
                    }
                    continue;
                }

                if (token.equals("bp")) {
                    sequencer = false;
                    sequenceLength = 0;
                    sequenceConfirmId = -1;
                    buttonSequence.clear();
                    buttonIDs.clear();
                    continue;
                }
                if (token.startsWith("bps/")) {
                    String spec = token.substring(4);
                    int b = spec.indexOf('b');
                    if (b > 0 && b + 1 < spec.length()) {
                        sequenceLength   = Integer.parseInt(spec.substring(0, b));
                        sequenceConfirmId = Integer.parseInt(spec.substring(b + 1));
                        sequencer = true;
                        buttonSequence.clear();
                        buttonIDs.clear();
                    } else {
                        // malformed — disable sequencer
                        sequencer = false;
                        sequenceLength = 0;
                        sequenceConfirmId = -1;
                    }
                    continue;
                }

                // BUTTON CONFIG: "b0/a1", "b1/a0", ...
                if (token.charAt(0) == 'b') {
                    // ignore "bps" here — already handled above
                    if (token.startsWith("bps")) continue;

                    int buttonNum = Character.getNumericValue(token.charAt(1)); // 0..9 only
                    int rowNum = buttonNum / 2;
                    horizontal row = hList.get(rowNum);

                    if (buttonNum % 2 == 0) buttonConfig(row.bL, settings);
                    else buttonConfig(row.bR, settings);
                }
            }
        }

        // Configure text field
        private void fieldConfig(Label t, String[] tokens){
            int size = Character.getNumericValue(tokens[1].charAt(1))-1;
            char style = tokens[1].charAt(2);
            int font = Character.getNumericValue(tokens[2].charAt(1))-1;
            int color = Character.getNumericValue(tokens[3].charAt(1))-1;
            String displayText = tokens[4].replace("\"", "").replace("\\n", "\n");

            t.setTextFill(textColor[color]);
            t.setFont(Font.font(textFont[font], textSize[size]));
            if(style == 'B'){makeBold(t);}
            else if(style == 'I') {makeItalic(t);}
            t.setText(displayText);
        }

        // Configure button
        private void buttonConfig(Button b, String[] tokens){
            boolean active = tokens[1].substring(1).equals("1");
            b.setDisable(!active);
        }

        // Create button sequence
        private void buttonSequencer(Button b, int id){
            if (!sequencer) return;

            // record this press
            buttonSequence.add(b);
            buttonIDs.add(id);
            
            if (id == sequenceConfirmId && buttonSequence.size() == sequenceLength) {
                sendFuelSelection();
                resetSequence();
                return;
            }

            // if too many presses then reset
            if (buttonSequence.size() > sequenceLength) {
                resetSequence();
            }

            System.out.println("bps " + id);
            b.setDisable(true);
        }

        // Reset button sequence
        private void resetSequence(){
            sequencer = false;
            sequenceLength = 0;
            sequenceConfirmId = -1;
            for (Button btn : buttonSequence) btn.setDisable(false);
            buttonSequence.clear();
            buttonIDs.clear();
        }


        // Send fuel selection
        private void sendFuelSelection(){

            // Determine row number
            horizontal row = hList.get(buttonIDs.getFirst()/2);
            String msg = row.tL.getText();
            System.out.println("SENDING: Fuel-Grade. - " + msg);
            Platform.runLater(() -> display.api.send("Fuel-Grade. - " + msg));
            resetSequence();
        }

        // Make textfield Bold
        private void makeBold(Label a){ a.setFont(Font.font(a.getFont().getFamily(), FontWeight.BOLD, a.getFont().getSize())); }

        // Make textfield italic
        private void makeItalic(Label a){ a.setFont(Font.font(a.getFont().getFamily(), FontPosture.ITALIC, a.getFont().getSize())); }

        // Combine 2 left/right text fields into 1 center field
        private void combineTextFields(Label a, Label ab, Label b){
            a.setMaxWidth(0);
            b.setMaxWidth(0);
            ab.setMaxWidth(Double.MAX_VALUE);
            a.setText("");
            b.setText("");
        }

        // Divide 1 center text field into 2 left/right fields
        private void divideTextFields(Label a, Label ab, Label b){
            a.setMaxWidth(Double.MAX_VALUE);
            b.setMaxWidth(Double.MAX_VALUE);
            ab.setMaxWidth(0);
            ab.setText("");
        }
    }
    /**
     * Main method to launch the JavaFX app.
     */
    public static void main(String[] args) { Application.launch(screen.screenGraphics.class, args); }
}