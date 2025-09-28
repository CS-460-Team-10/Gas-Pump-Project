package interfaces;

// this is the gas station server
// role is only to tell hub what are the fuel prices and record how much cash is paid

import socketAPI.ioServer;

public class gasStation {
    private ioServer api; // communcation api
    // price list for all fuel types, made them up, although unleaded is pretty compareable with acutal NM gas prices
    String[] prices = {"Unleaded:3.25", "Premium:3.75", "PremiumPlus:4.00", "Gasoline:3.50"};

    public gasStation(int connector) throws Exception {
        // open the device on port
        api = new ioServer(connector);
        // send first price list to hub
        sendPriceList();
        // start the loop for gas station logic
        Transactions();
    }

    private void sendPriceList() {
        // build the price list message string
        StringBuilder sb = new StringBuilder();
        sb.append("price-list(");
        for (int i = 0; i < prices.length; i++) {
            sb.append(prices[i]);
            if (i < prices.length - 1) sb.append(",");
        }
        sb.append(")");
        // send the price list to hub
        api.send(sb.toString());
        System.out.println("GasStation Sending: " + sb.toString());
    }

    private void Transactions() throws Exception {
        // main loop keep running
        while (true) {
            // get message from hub
            String msg = api.get();
            if (msg != null) {
                System.out.println("GasStation Received: " + msg);
                // if hub send cash-paid then log it
                if (msg.contains("cash-paid(")) {
                    String d = msg.substring(msg.indexOf("(") + 1, msg.length() - 1);
                    System.out.println("Transaction recorded: $" + d);
                }
            }
            Thread.sleep(50); // small delay not to spin
        }
    }

    public static void main(String[] args) throws Exception {
        // start the gas station on connector 6 status port
        new gasStation(6007);
    }
}
