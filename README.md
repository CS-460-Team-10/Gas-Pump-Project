
### Button Sequencer

When `bps/…` is active, the Screen records button presses and only sends the final domain message upon confirmation. For fuel selection the Hub typically sends:

- `bps/1b3` — one selection press, then **Confirm** on button id `3` (row 2 right).

The Screen will reply with:

- `Fuel-Grade. - <GradeName>`

### Business Messages

- **Products and prices**
  - From Gas Station → Hub:  
    `Product-List. - Unleaded/3.25:Premium/3.75:PremiumPlus/4.00:Gasoline/3.50`
  - Hub forwards to devices that need it (e.g., Flow Meter).

- **Fuel selection**
  - From Screen → Hub:  
    `Fuel-Grade. - Unleaded`

- **Card flow**
  - From Card Reader → Hub:  
    `Card-No. - 4111-1111-1111-1111`
  - From Bank → Hub:  
    `C1` (approved) or `C0` (declined)
  - Hub → Bank (charge after fueling):  
    `Charge-Card. - 12.34`  (numeric only)

- **Hose / tank**
  - Hose → Hub: `Hose-Attached.` / `Hose-Detached.`
  - Tank Sensor → Hub: `Tank-Full.` / `Tank-Not-Full.`

- **Flow meter**
  - Start/stop control (Hub → Meter/Pump): `FM1` (on), `FM0` (off)
  - Recommended final total (Meter → Hub):  
    `Total-Cost. - 12.34`  
    or, if sending a human summary, Hub extracts the numeric cost.

---

## Build & Run

### Prerequisites

- **JDK 21** (tested with Azul Zulu 21)
- **JavaFX 21** SDK on your classpath for UI modules (`screen`, `cardReader`, `hose`)

### Compile

You can compile with your IDE or plain `javac`. Example (non-modular classpath):

```bash
javac -cp "path/to/javafx/lib/*" *.java
