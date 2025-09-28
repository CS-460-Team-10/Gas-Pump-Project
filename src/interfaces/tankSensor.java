package interfaces;

import java.io.IOException;
import socketAPI.ioServer;

public class tankSensor {
    private final ioServer api;
    private volatile boolean tankFull = false;

    // Start the tank sensor server on the given port (e.g., 6005)
    public tankSensor(int port) throws IOException {
        this.api = new ioServer(port);
        // Optionally announce initial state:
        api.send("Tank-Not-Full.");

        reading();
    }

    private void reading(){
        String msg = null;
        
        while(true){
            msg = api.get();

            if (msg != null && !msg.isEmpty()) {
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
