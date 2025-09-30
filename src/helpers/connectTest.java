package helpers;

import socketAPI.ioPort;

public class connectTest {

    public static void main(String[] args) {
        try {
            // connect to server at localhost:6007
            ioPort port = new ioPort("localhost", 6006); // Desired connection port

            System.out.println("Connected. Type Ctrl+C to stop.");

            // loop forever: send + receive
            while (true) {
                // send a simple heartbeat/test message
                port.send("ping");

                // check for responses
                String msg = port.get();
                if (msg != null) {
                    System.out.println("Received: " + msg);
                }

                Thread.sleep(1000); // 1 second delay between iterations
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}