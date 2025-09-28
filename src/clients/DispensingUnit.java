package clients;
import java.io.IOException;

import socketAPI.ioPort;

public class DispensingUnit {
    private final ioPort flowMeterPort;
    private final ioPort gasPumpPort;

    // Customer connects to the FlowMeter and GasPump servers
    public DispensingUnit(String flowMeterHost, int flowMeterPortNum,
                    String gasPumpHost, int gasPumpPortNum) throws IOException {
        this.flowMeterPort = new ioPort(flowMeterHost, flowMeterPortNum);
        this.gasPumpPort = new ioPort(gasPumpHost, gasPumpPortNum);
    }

    // Send toggle to the FlowMeter server
    public void turnMeterOn() {
        flowMeterPort.send("FM1");
    }

    public void turnMeterOff() {
        flowMeterPort.send("FM0");
    }

    // Send toggle to the GasPump server
    public void turnPumpOn() {
        gasPumpPort.send("P1");
    }

    public void turnPumpOff() {
        gasPumpPort.send("P0");
    }

    // Send grade selection to the GasPump server
    public void sendGradeSelection(String msg) {
        gasPumpPort.send(msg);
    }

    // Send fuel products to flow meter and pump
    public void sendProductList(String msg){
        gasPumpPort.send(msg);
        flowMeterPort.send(msg);
    }

    // Get the current amount of fuel purchased from the FlowMeter server
    public String getFuelPurchased() {
        return flowMeterPort.get();
    }

    // Shutdown
    public void close() throws IOException {
        flowMeterPort.close();
        gasPumpPort.close();
    }
}
