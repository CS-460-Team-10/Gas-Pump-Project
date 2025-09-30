package clients;
import java.io.IOException;

import socketAPI.ioPort;

public class PaymentSystem {
    private final ioPort bankPort;

    // PaymentSystem connects to the bank server
    public PaymentSystem(String bankHost, int bankPortNum) throws IOException {
        this.bankPort = new ioPort(bankHost, bankPortNum);
    }

    // Send a message to the bank server
    public void sendToBank(String msg) {
        bankPort.send(msg);
    }

    // Get the buffered card data and clear it
    public String getFromBank() {
        return bankPort.get();
    }

    // Shutdown
    public void close() throws IOException {
        bankPort.close();
    }
}