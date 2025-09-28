import java.io.IOException;

public class hub {

    // UI Payloads
    private static final String UI_WELCOME =
        "t01/s1B/f1/c5/\"Welcome!\":t23/s3R/f1/c5/\"Use the card reader to begin your transaction.\":t45/s1B/f1/c5/\"*\"";
    private static final String UI_SELECT_FUEL =
        "t01/s2R/f1/c5/\"Payment approved. Select fuel type:\":t2/s3I/f1/c5/\"Unleaded\":"
      + "t3/s3R/f1/c5/\"Confirm\":t4/s3I/f1/c5/\"Premium\":t6/s3I/f1/c5/\"Premium Plus\":t8/s3I/f1/c5/\"Gasoline\"";
    private static final String UI_ATTACH_HOSE =
        "t23/s2R/f1/c5/\"Attach the hose to your vehicle's \\n       gas tank to begin fueling.\":t45/s1B/f1/c5/\"*\"";
    private static final String UI_FUELING =
        "t23/s2R/f1/c5/\"Fueling in progress...\":t45/s1B/f1/c5/\"*\"";
    private static final String UI_FINAL =
        "t01/s2B/f1/c5/\"Transaction complete.\":t45/s3I/f1/c5/\"Thank you!\":t67/s1B/f1/c5/\"*\"";

    private final Customer customer;
    private final DispensingUnit dispensingUnit;
    private final HoseSensors hoseSensors;
    private final PaymentSystem paymentSystem;
    private final GasStation gasStation;
    private int counter = 0;

    public hub() throws IOException {

        // Instantiate client objects
        this.customer  = new Customer("localhost", 6000, "localhost", 6001);
        this.dispensingUnit = new DispensingUnit("localhost", 6002, "localhost", 6003);
        this.hoseSensors = new HoseSensors("localhost", 6004, "localhost", 6005);
        this.paymentSystem = new PaymentSystem("localhost", 6006);
        this.gasStation = new GasStation("localhost", 6007);
    }

    public void run() throws InterruptedException {

        // Initial welcome screen
        customer.display(UI_WELCOME);

        // Main running loop
        while (true) {
            String msg = pollInterfaceMessages("");
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

            // Process End of Transaction
            } else if ((msg.contains("Hose-Detached.") || msg.contains("Tank-Full.") || msg.contains("Emergency-Stop.")) && customer.screenState.equals(UI_FUELING)) {
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
            case "Card"          -> msg = customer.getCardData();
            default              -> msg = pollInterfaceMessages();
        }

        if (inactivityTimeout(msg)) { return "Inactivity Timeout"; }

        return msg;
    }

    // Poll all interfaces for messages in order of priority
    private String pollInterfaceMessages() throws InterruptedException {
        String msg = gasStation.getFromStation();
        if (msg == null) msg = paymentSystem.getFromBank();
        if (msg == null) msg = dispensingUnit.getFuelPurchased();
        if (msg == null) msg = hoseSensors.isTankFull();
        if (msg == null) msg = hoseSensors.isHoseConnected();
        if (msg == null) msg = customer.getCardData();

        if (msg == null) {
            Thread.sleep(100);
            if (++counter >= 100) { customer.display(UI_WELCOME); counter = 0; }
        } else {
            counter = 0;
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
    private void processHoseAttachment() {
        customer.display(UI_FUELING);
        dispensingUnit.turnMeterOn();
        dispensingUnit.turnPumpOn(); // Start fueling
    }

    // Process end of transaction
    private void processEndOfTransaction() throws InterruptedException {
        dispensingUnit.turnPumpOff();
        customer.display(UI_FINAL);
        Thread.sleep(5000); // display final screen and amount fueled for few seconds
        dispensingUnit.turnMeterOff();

        String msg = null;
        while (msg == null) { msg = pollInterfaceMessages("Meter"); } // get final amount fueled
        if (msg.contains("Inactivity Timeout")) { System.out.println("Error: Could not determine sale amount. Exiting..."); System.exit(1); } // critical error - exits

        gasStation.sendToStation("Transaction complete. Amount: " + msg);
        paymentSystem.sendToBank("Transaction complete. Amount: " + msg);
        customer.display(UI_WELCOME);
    }


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
