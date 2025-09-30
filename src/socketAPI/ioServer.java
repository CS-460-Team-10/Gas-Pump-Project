package socketAPI;
import java.io.*;
import java.net.*;

public class ioServer {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile String buffer;   // one-place buffer
    private Thread listener;
    private ServerSocket serverSocket;

    public ioServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();  // wait for one client
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
                // client disconnected or error
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    // Send a string to the client
    public void send(String msg) {
        out.println(msg);
    }

    // Get the buffered string and clear it
    public synchronized String get() {
        String temp = buffer;
        buffer = null;
        return temp;
    }

    // Read the buffered string without clearing it
    public synchronized String read() {
        return buffer;
    }

    // Shutdown
    public void close() throws IOException {
        if (listener != null) {
            listener.interrupt();
        }
        if (socket != null) {
            socket.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
