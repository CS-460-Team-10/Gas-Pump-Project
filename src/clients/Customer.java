package clients;
import java.io.IOException;

import socketAPI.ioPort;

public class Customer {
    private final ioPort screenPort;
    private final ioPort cardReaderPort;
    public String screenState = null;

    // Customer connects to the Screen and CardReader servers
    public Customer(String screenHost, int screenPortNum,
                    String cardReaderHost, int cardReaderPortNum) throws IOException {
        this.screenPort = new ioPort(screenHost, screenPortNum);
        this.cardReaderPort = new ioPort(cardReaderHost, cardReaderPortNum);
    }

    // Send a message to the Screen server
    public void display(String msg) {
        screenState = msg;
        screenPort.send(msg);
    }

    // Get the buffered card data and clear it
    public String getCardData() {
        return cardReaderPort.get();
    }

    // Send card approved to reader
    public void sendCardApproved() {
        cardReaderPort.send("C1");
    }
    
    // Send card denied to reader
    public void sendCardDenied() {
        cardReaderPort.send("C0");
    }

    // Shutdown
    public void close() throws IOException {
        screenPort.close();
        cardReaderPort.close();
    }
}
