package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import proto.RequestProtos.*;
import proto.ResponseProtos.*;

class SockBaseServer {
    static String logFilename = "logs.txt";

    ServerSocket socket = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    static Game game;
    static Map<String, Integer> nameScoreMap = new HashMap<>();
    static String taskAnswer;
    static String tempTask;
    static boolean gameMade = true;

    public SockBaseServer(Socket sock, Game game) {
        this.clientSocket = sock;
        SockBaseServer.game = game;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e) {
            System.out.println("Error in constructor: " + e);
        }
    }

    // Handles the communication right now it just accepts one input and then is
    // done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer.
    public void start() throws IOException {
        loadNameScoreMap();

        game.newGame();

        String name = "";

        System.out.println("Ready...");
        try {
            while (true) {
                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);
                System.out.println("\nSTART\nOP: " + op + "\nEND");
                String result = null;

                // if the operation is NAME (so the beginning then say there is a commention and
                // greet the client)
                if (op.getOperationType() == Request.OperationType.NAME) {
                    // get name from proto object
                    name = op.getName();

                    if (!nameScoreMap.containsKey(name)) {
                        nameScoreMap.put(name, 0);
                    }

                    // writing a connect message to the log with name and CONNENCT
                    writeToLog(name, Message.CONNECT);
                    System.out.println("Got a connection and a name: " + name);
                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.HELLO)
                            .setHello("Hello " + name
                                    + " and welcome. \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game")
                            .build();
                    response.writeDelimitedTo(out);
                }
                if (op.getOperationType() == Request.OperationType.LEADERBOARD) {

                    System.out.println("Requesting Leaderboard");

                    Response.Builder responseBuilder = Response.newBuilder()
                            .setResponseType(Response.ResponseType.LEADERBOARD);

                    for (Map.Entry<String, Integer> entry : nameScoreMap.entrySet()) {
                        Leader leader = Leader.newBuilder()
                                .setName(entry.getKey())
                                .setWins(entry.getValue())
                                .build();

                        responseBuilder.addLeaderboard(leader);
                    }

                    // Build the final Response object
                    Response response = responseBuilder.build();

                    response.writeDelimitedTo(out);
                    saveNameScoreMap();
                }
                if (op.getOperationType() == Request.OperationType.NEW) {

                    System.out.println("Joining Game");

                    if(!gameMade)
                    {
                        game = new Game();
                        game.newGame();
                        gameMade = true;
                    }

                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.TASK)
                            .setImage(game.getImage())
                            .setTask(task())
                            .build();
                    response.writeDelimitedTo(out);

                    saveNameScoreMap();

                }
                if (op.getOperationType() == Request.OperationType.ANSWER) {
                    if (op.getAnswer().equals(taskAnswer)) {
                        Response response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.TASK)
                                .setImage(replace(Game.imgIncrement))
                                .setTask(task())
                                .setEval(true)
                                .build();

                        Game.incrementNum++;
                        if (Game.incrementNum >= 8) {
                            response = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.WON)
                                    .setImage(game.getImage())
                                    .setMessage("Congrats you won!")
                                    .build();

                                    gameMade = false;
                            nameScoreMap.put(name, nameScoreMap.get(name) + 1);
                        }

                        response.writeDelimitedTo(out);

                    } else {

                        Response response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.TASK)
                                .setImage(game.getImage())
                                .setTask(tempTask)
                                .setEval(false)
                                .build();
                        response.writeDelimitedTo(out);
                    }

                }
                if (op.getOperationType() == Request.OperationType.QUIT) {

                    System.out.println("Quitting");
                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.BYE)
                            .setHello("Goodbye " + name)
                            .build();
                    response.writeDelimitedTo(out);

                    saveNameScoreMap();

                }
            }

            // Example how to start a new game and how to build a response with the image
            // which you could then send to the server
            // LINE 67-108 are just an example for Protobuf and how to work with the
            // differnt types. They DO NOT
            // belong into this code.
            // game.newGame(); // starting a new game

            // // adding the String of the game to
            // Response response2 = Response.newBuilder()
            // .setResponseType(Response.ResponseType.TASK)
            // .setImage(game.getImage())
            // .setTask("Great task goes here")
            // .build();

            // // On the client side you would receive a Response object which is the same
            // as the one in line 70, so now you could read the fields
            // System.out.println("Task: " + response2.getResponseType());
            // System.out.println("Image: \n" + response2.getImage());
            // System.out.println("Task: \n" + response2.getTask());

            // // Creating Leader entry and Leader response
            // Response.Builder res = Response.newBuilder()
            // .setResponseType(Response.ResponseType.LEADERBOARD);

            // // building a leader entry
            // Leader leader = Leader.newBuilder()
            // .setName("name")
            // .setWins(0)
            // .setLogins(0)
            // .build();

            // // building a leader entry
            // Leader leader2 = Leader.newBuilder()
            // .setName("name2")
            // .setWins(1)
            // .setLogins(1)
            // .build();

            // res.addLeaderboard(leader);
            // res.addLeaderboard(leader2);

            // Response response3 = res.build();

            // for (Leader lead: response3.getLeaderboardList()){
            // System.out.println(lead.getName() + ": " + lead.getWins());
            // }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (clientSocket != null)
                clientSocket.close();
        }
    }

    /**
     * Replaces num characters in the image. I used it to turn more than one x when
     * the task is fulfilled
     * 
     * @param num -- number of x to be turned
     * @return String of the new hidden image
     */
    public String replace(int num) {
        for (int i = 0; i < num; i++) {
            if (game.getIdx() < game.getIdxMax())
                game.replaceOneCharacter();
        }
        return game.getImage();
    }

    /**
     * Writing a new entry to our log
     * 
     * @param name    - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be
     *                written in the log (e.g. Connect)
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message) {
        try {
            // read old log file
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " + name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a
            // protobuf object
            // which has a repeated field "log"

            for (String log : logsObj.getLogList()) {

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        } catch (Exception e) {
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * 
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception {
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    public static void main(String args[]) throws Exception {
        Game game = new Game();

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099;
        int sleepDelay = 10000;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new thread for each client connection
                Thread clientThread = new Thread(new ClientHandler(clientSocket, game));
                clientThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Game game;

        public ClientHandler(Socket clientSocket, Game game) {
            this.clientSocket = clientSocket;
            this.game = game;
        }

        @Override
        public void run() {
            try {
                SockBaseServer server = new SockBaseServer(clientSocket, game);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveNameScoreMap() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("nameScoreMap.ser"))) {
            oos.writeObject(nameScoreMap);
            System.out.println("nameScoreMap saved to file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadNameScoreMap() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("nameScoreMap.ser"))) {
            Map<String, Integer> loadedMap = (Map<String, Integer>) ois.readObject();
            nameScoreMap.clear();
            nameScoreMap.putAll(loadedMap);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a random fruit from a predefined list of fruits, sorts the
     * characters
     * of the randomly chosen fruit in alphabetical order, and returns the sorted
     * fruit.
     * 
     * @return A string representing the sorted characters of a randomly chosen
     *         fruit.
     */
    public static String task() {
        String[] fruits = {
                "apple", "orange", "banana", "grape", "strawberry",
                "pineapple", "mango", "watermelon", "kiwi", "blueberry",
                "peach", "pear", "cherry", "raspberry", "plum"
        };

        Random random = new Random();
        String randomFruit = fruits[random.nextInt(fruits.length)];
        taskAnswer = randomFruit;

        char[] chars = randomFruit.toCharArray();
        Arrays.sort(chars);
        tempTask = new String(chars);

        return tempTask;
    }
}
