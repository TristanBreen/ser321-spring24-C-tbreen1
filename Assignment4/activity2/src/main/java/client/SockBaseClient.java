
package client;

import java.net.*;
import java.io.*;
import java.util.Scanner;

import proto.RequestProtos.*;
import proto.ResponseProtos.*;
import proto.ResponseProtos.Response.ResponseType;

class SockBaseClient {

    public static void main(String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int port = 9099; // default port
        boolean name = false;
        String taskAnswer;
        boolean gameIsLive = false;

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        try {
            // Connect to the server
            serverSock = new Socket(host, port);
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();
            Scanner input = new Scanner(System.in);

            System.out.println("Please provide your name for the server. :-)");
            String strToSend = input.nextLine();

            // Build the request object including the name
            Request op = Request.newBuilder()
                    .setOperationType(Request.OperationType.NAME)
                    .setName(strToSend)
                    .build();
            
            // Write to the server
            op.writeDelimitedTo(out);

            // Read from the server
            Response response = Response.parseDelimitedFrom(in);

            // Print the server response
            System.out.println(response.getHello());

            while (true) {
                // Prompt user for input
                strToSend = input.nextLine();
        
                if(strToSend.equals("1"))
                {
                    // Build the request object
                    System.out.println("ENTERED1\n");
                    op = Request.newBuilder()
                            .setOperationType(Request.OperationType.LEADERBOARD)
                            .build();
            
                    // Write the request to the server
                    op.writeDelimitedTo(out);
            
                    // Read the response from the server
                    response = Response.parseDelimitedFrom(in);
            
                    // Print the server response
                    // Adjust this according to your protocol
                    for (Leader leader : response.getLeaderboardList())
                    {
                        System.out.println(leader.getName() + ": " + leader.getWins());
                    }
                    printMenu();
                }
                else if(strToSend.equals("2"))
                {
                    // Build the request object
                    //System.out.println("ENTERED2\n");
                    System.out.println("\nUnscrample the name of the FRUIT\n");
                    op = Request.newBuilder()
                            .setOperationType(Request.OperationType.NEW)
                            .build();
            
                    // Write the request to the server
                    op.writeDelimitedTo(out);
            
                    // Read the response from the server
                    response = Response.parseDelimitedFrom(in);
            
                    System.out.println(response.getImage());
                    System.out.println(response.getTask());
                    
                    gameIsLive = true;
                }
                else if(strToSend.equals("3"))
                {
                    // Build the request object
                    //System.out.println("ENTERED3\n");
                    op = Request.newBuilder()
                            .setOperationType(Request.OperationType.QUIT)
                            .build();
            
                    // Write to the server
                    op.writeDelimitedTo(out);

                    // Read from the server
                    response = Response.parseDelimitedFrom(in);

                    // Print the server response
                    System.out.println(response);
                    return;
                }
                else
                {
                    if(gameIsLive)
                    {
                        
                        op = Request.newBuilder()
                            .setOperationType(Request.OperationType.ANSWER)
                            .setAnswer(strToSend)
                            .build();
            
                        // Write the request to the server
                        op.writeDelimitedTo(out);
                        
                        // Read the response from the server
                        response = Response.parseDelimitedFrom(in);
                        
                        if (response.getResponseType() == Response.ResponseType.WON)
                        {
                            System.out.println(response.getImage() + "\n\n" + response.getMessage());
                            printMenu();
                        }
                        else if(response.getEval() == true)
                        {
                            System.out.println(response.getImage());
                            System.out.println(response.getTask());
                        }
                        else
                        {
                            System.out.println(response.getImage());
                            System.out.println(response.getTask());
                        }
                        
                    }
                    else
                    {
                        System.err.println("Unexpected Entry try again");
                        printMenu();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
            if (serverSock != null) serverSock.close();
        }
    }

    public static void printMenu()
    {
        System.out.println("\nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - Quit");
    }

}
