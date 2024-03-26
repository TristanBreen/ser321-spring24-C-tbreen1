package taskone;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolServer {

    private static int connections = 2; 
    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        int port;

        if (args.length != 2) {
            System.out.println("Usage: gradle runServer -Pport=9099 -Cconnections=2 -q --console=plain");
            System.exit(1);
        }

        port = -1;
        try {
            port = Integer.parseInt(args[0]);
            connections = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] and [connections]must be an integer");
            System.exit(2);
        }

        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started...");

        while (true) {
            //System.out.println("Accepting a Request...");

            
            if (activeConnections.get() < connections) {
                
                Socket clientSocket = server.accept();

                // Increment the active connections counter
                activeConnections.incrementAndGet();

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            } else {
            
            }
        }
    }

    // ClientHandler class to handle client connections and requests
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                StringList strings = new StringList();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                // Perform the same logic as in the non-threaded version
                Performer performer = new Performer(clientSocket, strings);
                performer.doPerform();
                
                // Close the client socket when done
                System.out.println("Closing client socket.");
                clientSocket.close();
                
                // Decrement the active connections counter
                activeConnections.decrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
