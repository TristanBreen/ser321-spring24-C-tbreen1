package Assignment3Starter;

import java.awt.Dimension;

import org.json.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;


import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements Assignment3Starter.OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	String currentMessage;
	Socket sock;
	OutputStream out;
	ObjectOutputStream os;
	BufferedReader bufferedReader;

	String host = "localhost";
	int port = 9000;

	boolean starting = true;
	String responseType = "";
	int points = 0;

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui(String host, int port, String id) throws IOException {
		this.host = host;
		this.port = port;

		// ---- GUI things you do not have to change/touch them ----
		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);

		picturePanel.newGame(1);

		// ---- GUI things end ----

		open(); // open connection to server
		outputPanel.setTask("Login");
		currentMessage = "{'type': 'start'}"; // sending a start request to the server
		try {
			os.writeObject(currentMessage); // send to server
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String string = this.bufferedReader.readLine(); // wait for answer
		JSONObject json = new JSONObject(string); // assumes answer is a JSON
		outputPanel.appendOutput(json.getString("value")); // write output value to output panel

		try {
			picturePanel.insertImage("img/hi.png", 0, 0); // hard coded to open this image -- image (not path) should be read from server message
		} catch (Exception e){
			System.out.println(e);
		}
		close(); // close connection to server
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Submit button handling
	 * 
	 * Change this to whatever you need, this is where the action happens. Tip outsource things to methods though so this method
	 * does not get too long
	 */
	@Override
	public void submitClicked() {
		try {
			open();
			System.out.println("submit clicked"); // server connection opened
			String input = outputPanel.getInputText(); // Pulls the input box text

				if (input.length() > 0) {
					outputPanel.appendOutput(input); // append input to the output panel
					outputPanel.setInputText(""); // clear input text box
				}

				if(starting == true)
				{
					starting = false;
					starting(input);
				}
				else
				{
					switch (responseType) 
					{
						case "chooseCategory":
							choosingCategory(input);
							outputPanel.setTask("Choose");
							break;
						case "scoreboard":
							restart();
							break;
						case "correctAnswer":
							restart();
							break;
						case "gameover":
							points = 0;
							outputPanel.setPoints(points);
							outputPanel.setTask("Game Over");
							restart();
							break;
						default:
							outputPanel.setTask("Choose");
							makeGuess(input);
							break;
					}
				}

			try {
				System.out.println("Waiting on response");
				String string = this.bufferedReader.readLine();
				JSONObject json = new JSONObject(string);

				if(json.getString("type").equals("RandomImg"))
				{
					byte[] imgData = Base64.getDecoder().decode(json.getString("value"));
					picturePanel.insertImage(new ByteArrayInputStream(imgData), 0, 0);
				}
				else if(json.getString("type").equals("resetPts"))
				{
					points = 0;
					outputPanel.setPoints(points);
					outputPanel.appendOutput("\nUNLUCKY...your points were reset to 0.\nPlease chose a category animals (a), cities (c), or leader board (l)\n");
				}
				else if(json.getString("type").equals("correctAnswer") || json.getString("type").equals("incorrectAnswer"))
				{
					points = Integer.parseInt(json.getString("value"));
					if(json.getString("type").equals("correctAnswer"))
					{
						outputPanel.appendOutput("\nCORRECT...please press enter to continue.\n");
					}
					else
					{
						outputPanel.appendOutput("\nINCORRECT...please make another guess\n");
					}
					
				} 
				else
				{
					if(json.getString("type").equals("hint"))
					{
						points = Integer.parseInt(json.getString("points"));
					}
					outputPanel.appendOutput(json.getString("value"));
				}

				responseType = json.getString("type");

				System.out.println("Got a response");
				System.out.println(json);

			} catch (Exception e) {
				e.printStackTrace();
			}

			outputPanel.setPoints(points);
			outputPanel.setInputText("");
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void starting(String input) 
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "name");
		obj.put("value", input);
	
		try {
			os.writeObject(obj.toString());// sending the current message to the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void choosingCategory(String input) 
	{
		if(input.toLowerCase().equals("exit"))
		{
			close();
			System.exit(1);
		}
		JSONObject obj = new JSONObject();
		obj.put("type", "category");
		obj.put("value", input);
	
		try {
			os.writeObject(obj.toString());// sending the current message to the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void restart() 
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "restart");
	
		try {
			os.writeObject(obj.toString());// sending the current message to the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void makeGuess(String input) 
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "guess");
		obj.put("value", input);
	
		try {
			os.writeObject(obj.toString());// sending the current message to the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makeAllInGuess(String input) 
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "allinguess");
		obj.put("value", input);
	
		try {
			os.writeObject(obj.toString());// sending the current message to the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}











	@Override
	public void allInClicked(){
		
		try {
			open();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String input = outputPanel.getInputText();

		if (input.length() > 0) {
			outputPanel.appendOutput(input); // append input to the output panel
			outputPanel.setInputText(""); // clear input text box
		}

		makeAllInGuess(input);
		try {
			System.out.println("Waiting on response");
			String string = this.bufferedReader.readLine();
			JSONObject json = new JSONObject(string);

			if(json.getString("type").equals("correctAnswer") || json.getString("type").equals("incorrectAnswer"))
			{
				points = Integer.parseInt(json.getString("value"));
				if(json.getString("type").equals("correctAnswer"))
				{
					outputPanel.appendOutput("\nCORRECT...please press enter to continue.\n");
				}
				else
				{
					outputPanel.appendOutput("\nINCORRECT...please make another guess\n");
				}
				
			} 
			
			responseType = json.getString("type");

			System.out.println("Got a response");
			System.out.println(json);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void open() throws UnknownHostException, IOException {
		this.sock = new Socket(host, port); // connect to host and socket on port 8888

		// get output channel
		this.out = sock.getOutputStream();
		// create an object output writer (Java only)
		this.os = new ObjectOutputStream(out);
		this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

	}
	
	public void close() {
		try {
			if (out != null)  out.close();
			if (bufferedReader != null)   bufferedReader.close(); 
			if (sock != null) sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
		public static void main(String[] args) throws IOException {
			try {
				// Default port value
				int port = 8888;
	
				// Check if there is a Gradle command-line argument for port
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-p") && i < args.length - 1) {
						port = Integer.parseInt(args[i + 1]);
					}
				}
	
				
				String host = "localhost";
	
				
				ClientGui main = new ClientGui(host, port, args[0]);
				main.show(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

