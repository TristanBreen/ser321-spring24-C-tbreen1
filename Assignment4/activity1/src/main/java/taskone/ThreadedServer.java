package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {

    public static void main(String[] args) throws Exception {
        int port;

        if (args.length != 1) {
            System.out.println("Usage: java ThreadedServer <port>");
            System.exit(1);
        }

        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }

        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started...");

        while (true) {
            System.out.println("Accepting a Request...");
            Socket clientSocket = server.accept();

            // Create a new instance of StringList for each client
            Thread clientThread = new Thread(new ClientHandler(clientSocket));
            clientThread.start();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
