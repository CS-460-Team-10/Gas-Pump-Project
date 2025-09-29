import java.io.IOException;

import clients.Customer;
import clients.DispensingUnit;
import clients.GasStation;
import clients.HoseSensors;
import clients.PaymentSystem;

// DISCLAIMER *****(Must start all interfaces before running hub logic)*****                                    ************
public class hub {

    // UI Payloads
    private final String UI_WELCOME =
        "bp:b0/a0:b1/a0:b2/a0:b3/a0:b4/a0:b5/a0:b6/a0:b7/a0:b8/a0:b9/a0:" + // Button config
        "t01/s1B/f1/c5/\"Welcome!\":t23/s3R/f1/c5/\"Use the card reader to begin your transaction.\":t45/s1B/f1/c5/\"*\""; // Text config
    private String UI_SELECT_FUEL; // Defined later after the gas station provides the product list and prices
    private final String UI_ATTACH_HOSE =
        "bp:b0/a0:b1/a0:b2/a0:b3/a0:b4/a0:b5/a0:b6/a0:b7/a0:b8/a0:b9/a0:" +
        "t23/s2R/f1/c5/\"Attach the hose to your vehicle's \\n       gas tank to begin fueling.\":t45/s1B/f1/c5/\"*\"";
    private final String UI_FUELING =
        "bp:b0/a0:b1/a0:b2/a0:b3/a0:b4/a0:b5/a0:b6/a0:b7/a1:b8/a0:b9/a0:" +
        "t23/s2R/f1/c5/\"Fueling in progress...\":t45/s1B/f1/c5/\"*\":t7/s3I/f1/c5/\"STOP\"";
    private final String UI_FINAL =
        "bp:b0/a0:b1/a0:b2/a0:b3/a0:b4/a0:b5/a0:b6/a0:b7/a0:b8/a0:b9/a0:" +
        "t01/s2B/f1/c5/\"Transaction complete.\":t45/s3I/f1/c5/\"Thank you!\":t67/s1B/f1/c5/\"*\"";

    private final Customer customer;
    private final DispensingUnit dispensingUnit;
    private final HoseSensors hoseSensors;
    private final PaymentSystem paymentSystem;
    private final GasStation gasStation;
    private int counter = 0;
    private String[] ProductList = new String[4];

    public hub() throws IOException {

        // Instantiate client objects *****(Must start all interfaces before running hub logic)*****                                    ************
        this.customer  = new Customer("localhost", 6000, "localhost", 6001);
        this.dispensingUnit = new DispensingUnit("localhost", 6002, "localhost", 6003);
        this.hoseSensors = new HoseSensors("localhost", 6004, "localhost", 6005);
        this.paymentSystem = new PaymentSystem("localhost", 6006);
        this.gasStation = new GasStation("localhost", 6007);
    }

    public void run() throws InterruptedException {

        // Initial welcome screen
        customer.display(UI_WELCOME);

        // Get GasStation prices and fwd it to screen, pump, and flowmeter
        String msg = null;
        while (msg == null) { msg = pollInterfaceMessages("GasStation"); }
        if (msg.contains("Inactivity Timeout")) { msg = null; return; }
        else if(msg.contains("Product-List. - ")){
            dispensingUnit.sendProductList(msg);
            msg = msg.replace("Product-List. - ", "");
            ProductList = msg.split(":");

            UI_SELECT_FUEL = "bps/2b3:b0/a0:b1/a0:b2/a1:b3/a1:b4/a1:b5/a0:b6/a1:b7/a0:b8/a1:b9/a0:" +
                            "t01/s2R/f1/c5/\"Payment approved. Select fuel type ($)::\":t2/s3I/f1/c5/\"" + ProductList[0] + "\":"
                        + "t3/s3R/f1/c5/\"Confirm\":t4/s3I/f1/c5/\"" + ProductList[1] + "\":t6/s3I/f1/c5/\"" + ProductList[2] + 
                        "\":t8/s3I/f1/c5/\"" + ProductList[3] + "\"";
        }

        // Main running loop
        while (true) {
            msg = pollInterfaceMessages("");
            if (msg == null) { continue; }

            // Process Card Data
            if (msg.contains("Card-No. - ") && customer.screenState.equals(UI_WELCOME)) {
                processCard(msg);

            // Process Fuel Selection
            } else if (msg.contains("Fuel-Grade. - ") && customer.screenState.equals(UI_SELECT_FUEL)) {
                processFuelSelection(msg);

            // Process Hose Attachment
            } else if (msg.contains("Hose-Attached.") && customer.screenState.equals(UI_ATTACH_HOSE)) {
                processHoseAttachment();
            } else if(msg.contains("Gal Pumped: ")){
                hoseSensors.sendFuelFlowed(msg);

            // Process End of Transaction
            } else if ((msg.contains("Hose-Detached.") || msg.contains("Tank-Full.") || msg.contains("bp7" /*Stop Button*/)) && customer.screenState.equals(UI_FUELING)) {
                processEndOfTransaction();
            }
        }
    }

    // Poll interfaces for a desired message
    private String pollInterfaceMessages(String desiredServer) throws InterruptedException {
        String msg = null;

        switch (desiredServer) {
            case "GasStation" -> msg = gasStation.getFromStation();
            case "Bank"       -> msg = paymentSystem.getFromBank();
            case "Meter"      -> msg = dispensingUnit.getFuelPurchased();
            case "Hose-Attached" -> msg = hoseSensors.isHoseConnected();
            case "Hose-Tank"     -> msg = hoseSensors.isTankFull();
            case "Screen"          -> msg = customer.getScreenData();
            case "Card"          -> msg = customer.getCardData();
            default              -> msg = pollInterfaceMessages();
        }

        if (inactivityTimeout(msg)) { return "Inactivity Timeout"; }
        if(msg != null){ System.out.println("RECIEVING: " + msg); }

        return msg;
    }

    // Poll all interfaces for messages in order of priority
    private String pollInterfaceMessages() throws InterruptedException {
        String msg = gasStation.getFromStation();
        if (msg == null) msg = paymentSystem.getFromBank();
        if (msg == null) msg = customer.getScreenData();
        if (msg == null) msg = hoseSensors.isTankFull();
        if (msg == null) msg = hoseSensors.isHoseConnected();
        if (msg == null) msg = dispensingUnit.getFuelPurchased();
        if (msg == null) msg = customer.getCardData();

        if (msg == null) {
            Thread.sleep(100);
            if (++counter >= 100) { customer.display(UI_WELCOME); counter = 0; }
        } else {
            counter = 0;
            System.out.println("RECIEVING: " + msg);
        }
        return msg; // may be null
    }

    // Handle inactivity timeouts and null messages
    private boolean inactivityTimeout(String msg) throws InterruptedException {
        // Null message handling and inactivity timeout
        if (msg == null) {
            Thread.sleep(100);  // avoid busy-waiting
            counter += 1;

            if (counter >= 100) {
                customer.display(UI_WELCOME); // refresh welcome screen
                counter = 0;
                return true;
            }
        } else { counter = 0; }
        return false;
    }

    // Process card data
    private void processCard(String msg) throws InterruptedException{
        paymentSystem.sendToBank(msg); // send card data to bank

        msg = null;
        while (msg == null) { msg = pollInterfaceMessages("Bank"); } // wait for bank response
        if (msg.contains("Inactivity Timeout")) { msg = null; return; }

        if(msg.contains("Card-Approved.")){
            customer.sendCardApproved(); // card approved
            Thread.sleep(2000);
            customer.display(UI_SELECT_FUEL); // select fuel screen
        } else if(msg.contains("Card-Denied.")){
            customer.sendCardDenied(); // card denied
        }
    }

    // Process fuel selection
    private void processFuelSelection(String msg) {
        customer.display(UI_ATTACH_HOSE); // attach hose screen
        dispensingUnit.sendGradeSelection(msg);
    }

    // Process hose attachment
    private void processHoseAttachment() throws InterruptedException {
        customer.display(UI_FUELING);
        dispensingUnit.turnMeterOn();
        Thread.sleep(3000);
        dispensingUnit.turnPumpOn(); // Start fueling
    }

    // Process end of transaction
    private void processEndOfTransaction() throws InterruptedException {
        dispensingUnit.turnPumpOff();
        Thread.sleep(3000); // display final screen and amount fueled for few seconds
        customer.display(UI_FINAL);

        String msg = null;
        while (msg == null) { msg = pollInterfaceMessages("Meter"); } // get final amount fueled
        if (msg.contains("Inactivity Timeout")) { System.out.println("Error: Could not determine sale amount. Exiting..."); System.exit(1); } // critical error - exits

        paymentSystem.sendToBank(msg);
        gasStation.sendToStation(msg);
        Thread.sleep(5000);
        dispensingUnit.turnMeterOff();
        customer.display(UI_WELCOME);
    }

    // Close all connections
    public void close() {
        try { customer.close(); } catch (IOException ignored) {}
        try { dispensingUnit.close(); } catch (IOException ignored) {}
        try { hoseSensors.close(); } catch (IOException ignored) {}
        try { paymentSystem.close(); } catch (IOException ignored) {}
        try { gasStation.close(); } catch (IOException ignored) {}
    }

    public static void main(String[] args) throws Exception {

        hub hub = new hub();

        try { 
            hub.run(); 

        } finally {
             hub.close(); 
        }
    }
}
