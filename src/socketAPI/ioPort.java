package socketAPI;
import java.io.*;
import java.net.*;

public class ioPort {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile String buffer;  // one-place buffer
    private Thread listener;

    public ioPort(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Start listener thread
        listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    synchronized (this) {
                        buffer = line;  // overwrite previous buffer
                    }
                }
            } catch (IOException e) {
                // connection closed or error
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    // (1) Send a string to the server
    public void send(String msg) {
        out.println(msg);
    }

    // (2) Get the buffered string and clear it
    public synchronized String get() {
        String temp = buffer;
        buffer = null;
        return temp;
    }

    // (3) Read the buffered string without clearing it
    public synchronized String read() {
        return buffer;
    }

    // Graceful shutdown
    public void close() throws IOException {
        if (listener != null) {
            listener.interrupt();
        }
        if (socket != null) {
            socket.close();
        }
    }
}
