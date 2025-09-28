package interfaces;

import java.io.IOException;

import socketAPI.ioServer;

public class pump {
    // This is going to be True = on, False = off
    private boolean pumping;
    private final ioServer api;

    /**
     * Constructor to initialize the pump device.
     * @param connector Connector/port number to connect the device
     * @throws IOException if the device initialization fails
     */
    public pump(int connector) throws IOException {
        pumping = false;
        api = new ioServer(connector);
        System.out.println("Pump is connected on port: " + connector);
    }

    /**
     * this is the action that will be used for gas pump controller to
     * that will turn on the pump.
     */
    public synchronized void pumpOn() {
        if(!pumping) {
            pumping = true;
            System.out.println("Pump is On - pumping gas");
            api.send("Pump ON");
        }
    }

    /**
     * Action that will be used for gas pump controller
     * when the gas pump is off
     */
    public synchronized void pumpOff() {
        if (pumping) {
            pumping = false;
            System.out.println("Pump is OFF - not pumping");
            api.send("Pump OFF");
        }
    }

    /**
     * updates the pump sensor status and sends messages ti the API.
     * @param sensorPumping boolean that indicates if pump is on or off
     */
    public void updateSensor(boolean sensorPumping) {
        if (sensorPumping && !pumping) {
            pumpOn();
        } else if (!sensorPumping && pumping) {
            pumpOff();
        }
    }

    public boolean isPumping() {
        return pumping;
    }

    public static void main(String[] args) throws Exception {
        new pump(6003);
    }
}
