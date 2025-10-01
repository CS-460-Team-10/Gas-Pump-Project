# Gas Pump Simulator

A graphical user interface (GUI) application that simulates the operation of a modern gas pump, developed using Java and JavaFX.

## Table of Contents

- [About The Project](#about-the-project)
- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation and Execution](#installation-and-execution)
- [Usage](#usage)
- [Contributors](#contributors)

## About The Project

This project is a simulation of a gas pump interface created for a software engineering course at the University of New Mexico. It allows a user to interact with a digital pump, select fuel types, prepay, dispense fuel, and view the transaction details in real-time. The goal is to model the state and logic of a real-world gas pump system in a software application using a developed socket API.

## Features

* **Fuel Selection**: Choose from multiple grades of fuel (i.e. Regular, Mid-Grade, Premium).
* **Dynamic Pricing and Products**: Prices are displayed and used for calculations and can be modified in the gas station gui.
* **RFID Payment Scanning**: Use a card reader to scan a credit card to pay.
* **Account Simulation**: A bank account and balance is simulated.
* **Fuel Tank Fullness Simulation**: A fuel tank level is simulated and will never overfill due to a simulated fullness sensor.
* **Real-Time Dispensing**: A "Start" button initiates the fuel flow, with the display updating the volume and cost in real-time. A "Stop" button halts the process.
* **Live Transaction Display**: The interface continuously shows the number of gallons pumped.
* **Receipt / Summary**: At the end of the transaction, a summary is displayed.

## Technologies

* **[Java](https://www.oracle.com/java/)**: Core application logic.
* **[JavaFX](https://openjfx.io/)**: Framework for the graphical user interface.

## Project Structure

The project is organized into the following main directories:

* `src/`: Contains all the Java source code (`.java` files).
    * `interfaces/`: Contains each device interface i.e. the screen, pump, bank, station, hose, etc.
    * `clients/`: Contains all object handlers for the device interfaces which hub.java oversees.
    * `socketAPI/`: Contains the server and client socket API's which are used in all communication.
* `res/`: Contains application resources, such as custom developed images.
* `out/`: The default output directory for compiled Java bytecode (`.class` files).

## Getting Started

Follow these instructions to get a local copy up and running.

### Prerequisites

1.  **Java Development Kit (JDK) 21**
    * Make sure you have JDK 21 installed and your `JAVA_HOME` environment variable is set.

2.  **JavaFX SDK 21**
    * Download the JavaFX SDK from the [JavaFX website](https://gluonhq.com/products/javafx/).
    * Unzip the SDK to a known location on your computer.
    * It is highly recommended to create an environment variable `PATH_TO_FX` that points to the `lib` folder inside your JavaFX SDK directory.
        * Example on macOS/Linux: `export PATH_TO_FX=/path/to/javafx-sdk-21/lib`
        * Example on Windows: `set PATH_TO_FX="C:\path\to\javafx-sdk-21\lib"`

### Installation and Execution

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/CS-460-Team-10/Gas-Pump-Project.git](https://github.com/CS-460-Team-10/Gas-Pump-Project.git)
    cd Gas-Pump-Project
    ```

2.  **Compile the source code:**  
    Compile all java files:
    ```sh
    javac *.java
    ```
4.  **Run the application:**  
    Open multiple terminal windows and run each interface module and then lastly run the hub.java file. Each is a standalone interface that runs separately.


## Usage

Once the application is running, you can use it as you would a real gas pump:

1.  **Scan Card**: Click the card reader to scan your card.
2.  **Select Fuel Grade**: Click on one of the fuel grade buttons and then click confirm.
4.  **Connect Hose**: Click the hose to connect it to the car. Fueling will begin shortly when you do.
5.  **Fueling**: Live fuel flow can be seen in the flow meter.
6.  **Stop Pumping**: Click the "Stop" button at any time to finish the transaction. The pumping will also stop automatically if the hose becomes disconnected.
7.  **View End**: A final screen will be displayed and gallons purchased can be seen in the flowmeter.

## Contributors

This project was developed by **Team 10** for the UNM-CS-460 course.  
- Alex Maynes (Project Manager)  
- Jackie Javier  
- Ricardo Rangel Valencia  
- Momen Katba Bader  
  
---
