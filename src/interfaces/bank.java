package interfaces;

import socketAPI.ioServer;

public class bank {
    private final ioServer api; // communcation api
    private double balance = 0; // dummy balance variable

    public bank(int connector) throws Exception {
        // open the device on port
        api = new ioServer(connector);

        // start the loop for bank logic
        authorization();
    }

    private boolean validCard(){
        balance = Math.random() * 100; // Balance of the card

        // 90% chance of being valid - balance must be atleast $10
        return balance > 10;
    }

    public void authorization() throws Exception {
        // main loop keep running
        while (true) {
            // get messages from hub
            String msg = api.get();
            if (msg != null) {
                System.out.println("Bank Received: " + msg);

                // Process card data
                if (msg.contains("Card-No. - ") && validCard()) {
                    api.send("Card-Approved.");
                    System.out.println("Bank approved");

                } else if (msg.contains("Card-No. - ")){
                    api.send("Card-Denied.");
                    System.out.println("Bank denied");
                }

                // Charge card for transaction
                if (msg.contains("Transaction-Complete. Amount: $")) {
                    msg = msg.trim().replace("Transaction-Complete. Amount: $", "");
                    balance -= Double.parseDouble(msg);
                    String bal = String.format("$%.2f", balance);
                    System.out.println("Bank charged card. New balance: " + bal);
                }
            }
            Thread.sleep(50); // small delay not to spin
        }
    }

    public static void main(String[] args) throws Exception {
        new bank(6006);
    }
}

