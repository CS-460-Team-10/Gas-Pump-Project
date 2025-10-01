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

    public boolean authorize(String cardNumber) throws InterruptedException {
        bankPort.send("Card-No. - " + cardNumber);

        String response = null;
        while (response == null) {
            response = bankPort.get();
        }

        return response.contains("Card-Approved.");
    }

    public void chargeRequest(String cardNum, double amount) {
        bankPort.send("Transaction-Complete. Amount: " + amount +
                "Card-No: " + cardNum);
    }
}