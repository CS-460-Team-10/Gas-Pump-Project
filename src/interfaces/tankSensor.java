package interfaces;

import java.io.IOException;
import socketAPI.ioServer;

public class tankSensor {
    private final ioServer api;
    private boolean tankFull = false;
    private double fuelStartLevel;
    private double maxFuel;

    // Start the tank sensor server on the given port (e.g., 6005)
    public tankSensor(int port) throws IOException {
        api = new ioServer(port);
        // Optionally announce initial state:
        tankFull = false;
        maxFuel = 12.0;
        fuelStartLevel = Math.random()*6.0;
        System.out.println("Max Fuel Capacity: " + this.maxFuel);
        System.out.println("Current Fuel Level: " + this.fuelStartLevel);

        reading();
    }

    private void reading(){
        String msg = null;
        double currentFuelLevel = fuelStartLevel;
        
        while(true){
            msg = api.get();

            if (msg != null && !msg.isEmpty()) {

                if(msg.contains("Gal Pumped: ")){
                    String[] tokens = msg.split(":");
                    if (tokens.length > 1) {
                        try {
                            double fuelBought = Double.parseDouble(tokens[1].trim());
                            currentFuelLevel = fuelBought + fuelStartLevel;
                            if(currentFuelLevel >= maxFuel) { 
                                tankFull = true; 
                                api.send("Tank-Full."); 

                                // Reset simulate next fuel tank
                                fuelStartLevel = Math.random()*6.0;
                                tankFull = false; 
                            }
                            System.out.println("Fuel Level updated: " + String.format("%.2f", currentFuelLevel) + " gallons");
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing fuel amount from message: " + msg);
                        }
                    }
                }
            }
        }
    }

    // Set / toggle fullness and notify Hub
    public synchronized void setFull(boolean full) {
        if (full != tankFull) {
            tankFull = full;
            api.send(tankFull ? "Tank-Full." : "Tank-Not-Full.");
        }
    }

    public synchronized boolean isFull() {
        return tankFull;
    }

    public void close() throws IOException {
        api.close();
    }

    public static void main(String[] args) throws Exception {
        new tankSensor(6005);
    }
}
