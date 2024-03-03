package Assignment3Starter;

import java.net.*;
import java.io.*;
import org.json.*;
import java.util.Random;
import java.util.Base64;
import java.util.HashMap;



/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
    static JSONObject hints;
    static String category;
    static String RandomImg;
    static String RandomImgName;
    static String RandomImgPath;
    static HashMap<String, Integer> userPointsMap = new HashMap<>();
    static int points = 0;
    static int hintsCount = 0;

    static {
        try {
            hints = readHints();
        } catch (FileNotFoundException e) {
            hints = new JSONObject();
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        Socket sock;
        try {
            //open socket
            int port = 8888;

            for (int i = 0; i < args.length; i++) 
            {
                if (args[i].equals("-p") && i < args.length - 1) 
                {
                    port = Integer.parseInt(args[i + 1]);
                }
            }
            
            @SuppressWarnings("resource")
            ServerSocket serv = new ServerSocket(port, 1);
            System.out.println("Server ready for connetion");
            points = 0;
            hintsCount = 0;
            userPointsMap.put("adam", 5);
            userPointsMap.put("steve", 4);
            loadFromFile();

            String name = "";

            // This is just a very simpe start with the project that establishes a basic client server connection and asks for a name
            // You can make any changes you like
            while (true) {
                sock = serv.accept(); // blocking wait

                // setup the object reading channel
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                //OutputStream out = sock.getOutputStream();
                String s = (String) in.readObject();
                JSONObject json = new JSONObject(s); // assume message is json
                JSONObject obj = new JSONObject(); // resp object
                System.out.println(json);


                switch (json.getString("type")) {
                    
                    case "start":
                        System.out.println("New connection");
                        obj.put("type", "hello");
                        obj.put("value", "Hello, please tell me your name.");
                        obj = sendImg("img/hi.png", obj);
                        break;
                    case "restart":
                        System.out.println("- Restart");
                        obj.put("type", "chooseCategory");
                        obj.put("value", "Please chose a category animals (a), cities (c), or leader board (l)"); // menu options send
                        break;
                    case "name":
                        System.out.println("- Got a name");
                        name = json.getString("value");
                        obj.put("type", "chooseCategory");
                        obj.put("value", "Hello " + name + ", please chose a category animals (a), cities (c), or leader board (l)"); // menu options send
                        break;
                    case "incorrect":
                        System.out.println("- Incorrect");
                        obj.put("type", "chooseCategory");
                        obj.put("value", "Hello " + name + ", please chose a category animals (a), cities (c), or leader board (l)"); // menu options send
                        break;
                    
                    case "category":
                        System.out.println("- Got a Category");
                        
                        //valid category option
                        if (!json.getString("value").equalsIgnoreCase("a") && !json.getString("value").equalsIgnoreCase("c") && !json.getString("value").equalsIgnoreCase("l") && !json.getString("value").equalsIgnoreCase("done")) 
                        {
                            obj.put("type", "chooseCategory");
                            obj.put("value", "Invalid category, please chose a category animals (a), cities (c), or leader board (l)"); // menu options send
                        }
                        else
                        {
                            category = json.getString("value");

                            switch (category) {
                                case "a":
                                    if (new Random().nextInt(100) < 10) 
                                    {
                                        obj.put("type", "resetPts");
                                        obj.put("value", 0);
                                    }
                                    else
                                    {
                                        RandomImg = getImage(new File("img/animal"));
                                        RandomImgPath = "img" + File.separator + "animal" + File.separator + RandomImg;
                                        RandomImgName = RandomImg.substring(0, RandomImg.length()-4);
                                        hintsCount = 0;

                                        obj.put("type", "RandomImg");
                                        obj.put("value", encodeImageToBase64(RandomImgPath));
                                    }
                                    
                                    break;
                                case "c":
                                    if (new Random().nextInt(100) < 10) 
                                    {
                                        obj.put("type", "resetPts");
                                        obj.put("value", 0);
                                    }
                                    else
                                    {
                                        RandomImg = getImage(new File("img/city"));
                                        RandomImgPath = "img" + File.separator + "city" + File.separator + RandomImg;
                                        RandomImgName = RandomImg.substring(0, RandomImg.length()-4);
                                        hintsCount = 0;

                                        obj.put("type", "RandomImg");
                                        obj.put("value", encodeImageToBase64(RandomImgPath));
                                    }
                                    break;
                                case "l":
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("LEADERBOARD\n--------------------------\n");

                                    for (String user : userPointsMap.keySet()) 
                                    {
                                        sb.append("User: ").append(user).append(", Points: ").append(userPointsMap.get(user)).append("\n");
                                    }

                                    sb.append("\nPress Enter To Continue\n");

                                    obj.put("type", "scoreboard");
                                    obj.put("value",sb.toString());
                                    break;
                                case "done":
                                    if (userPointsMap.containsKey(name)) 
                                    {
                                        if (userPointsMap.get(name) < points) 
                                        {
                                            userPointsMap.put(name, points);
                                            saveToFile();
                                        }
                                    } else 
                                    {
                                        userPointsMap.put(name, points);
                                        saveToFile();
                                    }

                                    
                                    obj.put("type", "gameover");
                                    obj.put("value","\nCongrats you ended with " + Integer.toString(points) + " points! Press enter to restart."+"\n");
                                    points = 0;
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case "guess":
                        System.out.println("- Got a guess");
                        String guess = json.getString("value");
                        
                        switch (guess.toLowerCase()) {
                            case "help":
                                if(hintsCount < 3)
                                {
                                    points-=20;
                                    obj.put("type", "hint");
                                    obj.put("value","\n" + hints.getJSONArray(RandomImgName.toLowerCase()).getString(hintsCount) + "\n");
                                    obj.put("points", Integer.toString(points));

                                    hintsCount+=1;
                                }
                                else
                                {
                                    obj.put("type", "nohint");
                                    obj.put("value","\nNo more hints\n");
                                }
                                break;
                            case "done":
                                if (userPointsMap.containsKey(name)) 
                                {
                                    if (userPointsMap.get(name) < points) 
                                    {
                                        userPointsMap.put(name, points);
                                        saveToFile();
                                    }
                                } 
                                else 
                                {
                                    userPointsMap.put(name, points);
                                    saveToFile();
                                }
                                
                                points = 0;
                                obj.put("type", "gameover");
                                obj.put("value", "\nCongrats you ended with " + Integer.toString(points) + " points! Press enter to restart." + "\n");
                                points = 0;
                                break;
                            
                            default:
                                if (guess.toLowerCase().equals(RandomImgName)) 
                                {
                                    points+=100;
                                    obj.put("type", "correctAnswer");
                                    obj.put("value",Integer.toString(points));
                                }
                                else
                                {
                                    points-=20;
                                    obj.put("type", "incorrectAnswer");
                                    obj.put("value",Integer.toString(points));
                                }
                                break;
                        }

                        break;
                    case "allinguess":
                        System.out.println("- Got a guess");
                        guess = json.getString("value");
                        
                        switch (guess.toLowerCase()) {
                            default:
                                if (guess.toLowerCase().equals(RandomImgName)) 
                                {
                                    points+=200;
                                    obj.put("type", "correctAnswer");
                                    obj.put("value",Integer.toString(points));
                                }
                                else
                                {
                                    points-=200;
                                    obj.put("type", "incorrectAnswer");
                                    obj.put("value",Integer.toString(points));
                                }
                                break;
                        }

                        break;
                    default:
                        System.out.println("not sure what you meant");
                        obj.put("type", "error");
                        obj.put("value", "unknown request");
                        break;
                }
                
                PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
                outWrite.println(obj.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //save the HashMap to a file
    public static void saveToFile() 
    {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("src" + File.separator + "main" + File.separator + "java" + File.separator + "Assignment3Starter" + File.separator + "scoreboard.ser"))) 
        {
            outputStream.writeObject(userPointsMap);
    
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadFromFile() 
    {
        File file = new File("src" + File.separator + "main" + File.separator + "java" + File.separator + "Assignment3Starter" + File.separator + "scoreboard.ser");
        
        if (file.exists() && file.length() > 0) 
        {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) 
            {
                userPointsMap = (HashMap<String, Integer>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) 
            {
                e.printStackTrace();
            }
        }
    }
    /*
     * resources for base64 conversion founf here:
     * https://www.baeldung.com/java-base64-image-string
     */
    private static String encodeImageToBase64(String imagePath) throws IOException 
    {  
        File file = new File(imagePath);
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) 
        {
            byte[] imageData = new byte[(int) file.length()];
            fileInputStreamReader.read(imageData);
            return Base64.getEncoder().encodeToString(imageData);
        }
    }

    /**
     * Method that gets a random filename from a file in a specific directory (e.g. img/animals).
     *
     * @param f which specifies the name of the directory
     * @return string of the image file
     */
    public static String getImage(File f) throws Exception {
        String[] list = f.list();

        Random rand = new Random(); //instance of random class
        int int_random = rand.nextInt(list.length - 1);

        String image = list[int_random + 1];
        System.out.println(image);

        if (image.equals(".DS_Store")) { // since Mac always has that
            image = getImage(f);
        }
        return image;
    }

    /**
     * Method that reads the hint list and returns it as JSONObject
     *
     * @return JSONObject including all the hints for the current game
     */
    public static JSONObject readHints() throws FileNotFoundException {
        FileInputStream in = new FileInputStream("img/hints.txt");
        JSONObject obj = new JSONObject(new JSONTokener(in));
        return obj;
    }

    /**
     * In my implementation this method gets a specific file name, opens it, manipulates it to be send over the network
     * and adds that manipulated image to the given obj which is basically my response to the client. You can do it differently of course
     *
     * @param filename with the image to open
     * @param obj      the current response that the server is creating to be send back to the client
     * @return json object that will be sent back to the client which includes the image
     */
    public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
        File file = new File(filename);

        if (file.exists()) {
            // import image
            // I did not use the Advanced Custom protocol
            // I read in the image and translated it into basically into a string and send it back to the client where I then decoded again
            obj.put("image", "Pretend I am this image: " + filename);
        }
        return obj;
    }
}
