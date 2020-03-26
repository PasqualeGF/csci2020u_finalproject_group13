package chatservice;
/*
* 										CSCI 2020 FinalProject
* 										Group 13
* 											
*/

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class Agent extends Application {

	// Creating Window Components and GUI Components
	// The VBox container will contain the window
	VBox container = new VBox();
	//upperSide nameLabel,AgentNameField and save button
	GridPane upperSide = new GridPane();
	//Scrollbar is created for the textarea
	ScrollPane chatBar = new ScrollPane();
	TextArea chatText = new TextArea();
	// lowerSide GridPane contains: reply button, messageBody
	GridPane lowerSide = new GridPane();
	// Create UI controls: reply button triggers response function.
	Button replyBtn = new Button("Reply");
	// saveBtn will save the chat text into a txt file.
	Button saveBtn = new Button("Save Chat");
	Label nameLbl = new Label(" Agent Name:");

	TextField messageBody = new TextField();
	// Method to get the textflied from user name
	TextField AgentNameField = new TextField();
	// Product info loads
	File loadFile;
	// Button to load product info
	Button loadBtn = new Button("Load Product");
	// Fil_chooser loads the product data from a txt file
	FileChooser fil_chooser = new FileChooser();
	// Target file to save the conversation.
	File saveAsFile;
	// File Chooser to save the file
	FileChooser fil2_chooser = new FileChooser();

	// Main start function
	@Override
	public void start(Stage primaryStage) throws Exception {
		// This will create and configure User Window
		primaryStage.setResizable(false);
		setupGUI(primaryStage);
		// Starts the chat server
		startChat();
	}
	// Override stop to ensure program exits successfully on window close
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
	private void setupGUI(Stage primaryStage) {
		// this method will configure the upperSide GridPane containing nameLabel, AgentNameField and save/load btns
		setupUpperSide(primaryStage);
		// This method will configure the lowerSide GridPane containing replyBtn,messageBody
		setupLowerSide();
		// This method will configure the ScrollBar and the TextArea
		setupChat();
		// Adding children to Vbox
		setupContainer();
		// Show the scene
		setupStage(primaryStage);
	}

	// This function will setup the stage settings
	private void setupStage(Stage primaryStage) {
		Scene scene = new Scene(container);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Agent");
		primaryStage.show();
	}

	// This function will setup the container area
	private void setupContainer() {
		// Add components to the window
		container.getChildren().addAll(upperSide, chatBar, lowerSide);
	}

	/*
	 * Function setup the chat scrollbar, adds the textarea to it
	 *
	 */
	private void setupChat() {
		// Set width
		chatText.setMaxWidth(1000);

		// Disables editing in the textarea 
		chatText.setEditable(false);
		// Add textarea to the scrollbar
		chatBar.setContent(chatText);

	}

	// Sets up the chat components
	private void setupLowerSide() {
		// Set number of cols to organize elements
		int numCols = 4;
		for (int i = 0; i < numCols; i++) { //loops through the number of columns
			ColumnConstraints colConst = new ColumnConstraints();
			colConst.setPrefWidth(110);
			if (i==0){
				colConst.setPrefWidth(103);
			}
			lowerSide.getColumnConstraints().add(colConst);
		}
		// Add the reply button and the messageBody to the LowerPane
		messageBody.setMaxWidth(2000);
		lowerSide.add(replyBtn, 4, 0);
		lowerSide.add(messageBody, 0, 0,4,1);
		//Adds the text to chat area and provides it to the Agent.
		replyBtn.setOnAction(
				e -> {// Check if any of the fields is empty and stop the process
					if (messageBody.getText().isEmpty() == false & AgentNameField.getText().isEmpty()==false)
						{chatText.appendText(
								"\n Agent (" + AgentNameField.getText() + "):" + "    " + messageBody.getText());
						// Send message to Client
							sendMessage("\n Agent (" + AgentNameField.getText() + "):" + "    " + messageBody.getText());
						// This will stop the Agent from changing name once set
						AgentNameField.setDisable(true);
						}
					
					else {// Stop the user from sending empty replies via alert Box
						setupErrorBox();
					}
					// Reset the reply text
					messageBody.setText("");
					
				});

	}

	// This function will stop the user from sending empty replies it uses alert Box
	private void setupErrorBox() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Box");
		alert.setContentText("Reply or Name is empty.");
		alert.showAndWait();
	}

	// This function will setup the name components
	private void setupUpperSide(Stage primaryStage) {
		//Set number of cols to organize elements
		int numCols = 5;
		for (int i = 0; i < numCols; i++) { //loops through the columns
			ColumnConstraints colConst = new ColumnConstraints();
			if (i==0) {
				colConst.setPrefWidth(80);
			}
			else if (i==1){
				colConst.setPrefWidth(100);
			}
			else if (i==4){
				colConst.setPrefWidth(90);
			}
			else{
				colConst.setPrefWidth(70);
			}
			upperSide.getColumnConstraints().add(colConst);
		}
		AgentNameField.setMaxWidth(1000);
		upperSide.add(nameLbl, 0, 0);
		upperSide.add(AgentNameField, 1, 0,2,1);
		upperSide.add(loadBtn, 4, 0);
		upperSide.add(saveBtn, 5, 0);
		// Load file on clicking load button
		loadBtn.setOnAction(e->{
			try {
				loadFile=fil_chooser.showOpenDialog(primaryStage);
				loadProduct();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		// Save chat on clicking save button
		saveBtn.setOnAction(r -> {
			// This will add filters when saving files it will save it as txt file
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text File", "*.txt");
			fil2_chooser.getExtensionFilters().add(extFilter);
			saveAsFile = fil2_chooser.showSaveDialog(primaryStage);
			saveAs();
		});


	}

	// Function to load product data from file upon clicking Load button
	
	private void loadProduct() throws Exception {

		try {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			// Create input stream
			Scanner inputStream = new Scanner(loadFile);
			// Update Agent chat
			chatText.appendText("\n Agent (" + AgentNameField.getText() + "):");
			// Send message to Client
			sendMessage("\n Agent (" + AgentNameField.getText() + "):");
			while (inputStream.hasNext()) {
				// Read in file, update Agent chat and send to client
				String data = inputStream.nextLine();
				chatText.appendText("\n " + data);
				sendMessage("\n " + data);
			}
			// This will show a confirmation that file has been loaded successfully
			alert.setTitle("Confirmation Box");
			alert.setContentText("File has been successfully loaded!");
			alert.showAndWait();
		} catch (FileNotFoundException e) {
			System.out.println("Something went wrong while reading the file");
		}

	}

	// Sends the conversation to the file using writer and bufferedwriter
	private void saveAs() {
		try (Writer writer = new BufferedWriter(new FileWriter(saveAsFile))) { //Creates buffer writer 
			String chatContent = chatText.getText(); //gets the textarea content
			writer.write(chatContent); //writes to the file
			writer.close(); //closes the connection
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Box");
			alert.setContentText("Conversation has been successfully saved!");
			alert.showAndWait();
			
		} catch (Exception e) {
			System.out.println("Problem while writing the file.");
			e.printStackTrace();
		}

	}

	/********************************************* SERVER CODE *********************************************/
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private String message = "";
	private Socket connection;
	// Function to start server and chat
	public void startChat(){
		// Create another thread for server to run on
		Runnable serverTask = new Runnable() {
			@Override
			public void run() {

				try {
					while (true) {
						// Establish server socket and max number of backlogged connected users
						server = new ServerSocket(3000, 50);
						try {
							// Wait for connection and run input and output stream functions to retrieve and send messages
							waitingToConnect();
							setupStreams();
							connectedChat();
						} catch (EOFException eofException) {
							// If server connection interrupted print error message
							showMessage("\n ERROR: Connection interrupted. ");
						} finally {
							// Close connection when done
							closeConnection();
						}
					}
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		};
		// Start server thread
		Thread serverThread = new Thread(serverTask);
		serverThread.start();
	}

	// Function to connect to server and client
	private void waitingToConnect() throws IOException{
		// Display waiting message then connection info when connected
		showMessage(" Waiting for a client to connect... \n");
		connection = server.accept();
		showMessage("\n Now connected to: " + connection.getInetAddress().getHostName());
	}
	// Function to setup streams
	private void setupStreams() throws IOException{
		// Setup input and output streams
		input = new ObjectInputStream(connection.getInputStream());
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		// Print confirmation message
		showMessage("\n Client is ready to chat. \n");
	}

	// Function to handle messages while chatting
	private void connectedChat() throws IOException{
		do{
			try{
				// Append incoming user's message
				message = (String) input.readObject();
				showMessage(message);
			}catch(ClassNotFoundException classNotFoundException){
				// If message sent is not valid, print error message
				showMessage("ERROR: User has sent an invalid object!");
			}
		}while(!message.equals("END CHAT"));


	}

	// Function to close the connections
	public void closeConnection(){
		showMessage("\n Client has left the chat.\n Connection terminated.\n");
		try{
			// Close path to and from client, as well as connection
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	// Send message to the client
	private void sendMessage(String message){
		try{
			output.writeObject(message);
			output.flush();
		}catch(IOException ioException){
			chatText.appendText("\n ERROR: message not sent, please retry.");
		}
	}

	// Update chat windows with message
	private void showMessage(final String text){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						// Show message in text box
						chatText.appendText(text);
					}
				}
		);
	}

}
