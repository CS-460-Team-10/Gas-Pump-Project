package clients;
import java.io.IOException;

import socketAPI.ioPort;

public class GasStation {
    private final ioPort gasStationPort;

    // GasStation connects to the gas station server
    public GasStation(String gasStationHost, int gasStationPortNum) throws IOException {
        this.gasStationPort = new ioPort(gasStationHost, gasStationPortNum);
    }

    // Send a message to the GasStation server
    public void sendToStation(String msg) {
        gasStationPort.send(msg);
    }

    // Get the buffered card data and clear it
    public String getFromStation() {
        return gasStationPort.get();
    }

    // Shutdown
    public void close() throws IOException {
        gasStationPort.close();
    }

    public void turnOn() {
        gasStationPort.send("ON");
    }

    public void turnOff() {
        gasStationPort.send("OFF");
    }

    public void sendPriceList(String priceList) {
        gasStationPort.send("Price-list(p) " + priceList);
    }

    public void recordCashPayment(double amount) {
        gasStationPort.send("Cash-paid(d) " + amount);
    }
}