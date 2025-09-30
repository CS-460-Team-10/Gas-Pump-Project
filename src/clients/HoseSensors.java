package clients;
import java.io.IOException;

import socketAPI.ioPort;

public class HoseSensors {
    private final ioPort hosePort;
    private final ioPort tankFullnessSensorPort;

    // HoseSensors connects to the hose and tank fullness sensor servers
    public HoseSensors(String hoseHost, int hosePortNum,
        String tankFullnessSensorHost, int tankFullnessSensorPortNum) throws IOException {
        this.hosePort = new ioPort(hoseHost, hosePortNum);
        this.tankFullnessSensorPort = new ioPort(tankFullnessSensorHost, tankFullnessSensorPortNum);
    }

    // Check hose connection from server
    public String isHoseConnected() {
        return hosePort.get();
    }

    // Check tank fullness from server
    public String isTankFull() {
        return tankFullnessSensorPort.get();
    }

    // Check tank fullness from server
    public void sendFuelFlowed(String msg) {
        tankFullnessSensorPort.send(msg);
    }

    // Shutdown
    public void close() throws IOException {
        hosePort.close();
        tankFullnessSensorPort.close();
    }
}