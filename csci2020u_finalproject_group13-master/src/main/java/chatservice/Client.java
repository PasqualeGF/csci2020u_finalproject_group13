package chatservice;
/*
 * 										CSCI2020 FinalProject
 * 										Group 13
 *
 */

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
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

public class Client extends Application {

	// Creating Window Components and GUI Components
	VBox container = new VBox();
	// upperSide creates the pane: containing nameLabel, ClientNameField and save button
	GridPane upperSide = new GridPane();
	// chatBar: scrollbar and textarea
	ScrollPane chatBar = new ScrollPane();
	TextArea chatText = new TextArea();
	// lowerSide:  replybutton, messageBody
	GridPane lowerSide = new GridPane();
	// Create UI controls: reply button which triggering the response function.
	Button replyBtn = new Button("Reply");
	// This button saves the chat text into a txt file.
	Button saveBtn = new Button("Save Chat");
	// Client name label
	Label nameLbl = new Label(" Client Name:");
	// Reply body
	TextField messageBody = new TextField();
	// Adds the user name from the textfield
	TextField ClientNameField = new TextField();
	// Saves the conversation
	File saveAsFile;
	// File Chooser to save the file
	FileChooser fil2_chooser = new FileChooser();

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Creates and configure User Window
		primaryStage.setResizable(false);
		setupGUI(primaryStage);
		// Start the chat server
		startChat();

	}
	// Override stop to ensure program exits successfully on window close
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
	private void setupGUI(Stage primaryStage) {
		// Configure the upperSide GridPane: nameLabel, ClientNameField and save button
		setupUpperside(primaryStage);
		// Configure the lowerSide GridPane: replyBtn, messageBody
		setupLowerSide();
		// Configure the ScrollBar and the TextArea
		setupChat();
		// Adds children to Vbox
		setupContainer();
		// Show the scene
		setupStage(primaryStage);
	}

	// This function will setup the stage settings
	private void setupStage(Stage primaryStage) {
		Scene scene = new Scene(container);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.show();
	}

	// This function will setup the container area
	private void setupContainer() {
		// Add components in order to fill the window
		container.getChildren().addAll(upperSide, chatBar, lowerSide);
	}

	// This function will setup the chat scroll bar including the text area
	private void setupChat() {
		// Disable editing the textarea using SetEditable();
		chatText.setEditable(false);
		// Add the textarea to the scrollbar
		chatBar.setContent(chatText);

	}

	// This function will setup the chat components
	private void setupLowerSide() {
		// Set number of columns to organize elements
		int numCols = 4;
		for (int i = 0; i < numCols; i++) { // Loops through the columns
			ColumnConstraints colConst = new ColumnConstraints();
			colConst.setPrefWidth(110);
			if (i==0){
				colConst.setPrefWidth(104);
			}
			lowerSide.getColumnConstraints().add(colConst);
		}
		// Set message body max width
		messageBody.setMaxWidth(2000);
		// This will add the reply button and the messageBody to the LowerPane
		lowerSide.add(replyBtn, 4, 0);
		lowerSide.add(messageBody, 0, 0,4,1);
		replyBtn.setOnAction(
				// Setup the reply by adding the text to chat area and writes it to the client.

				e -> {// If any of the fields are empty and stop the process
					if (messageBody.getText().isEmpty() == false & ClientNameField.getText().isEmpty()==false)
					{chatText.appendText(
							"\n Client (" + ClientNameField.getText() + "):" + "    " + messageBody.getText());
						// Send message to Agent
						sendMessage("\n Client (" + ClientNameField.getText() + "):" + "    " + messageBody.getText());
						// Stops the client from changing the name once set
						ClientNameField.setDisable(true);
					}

					else {// This code will stop the user from sending empty replies: alert Box
						setupErrorBox();
					}
					// Reset the reply text
					messageBody.setText("");
				});

	}

	// This function will stop the user from sending empty replies: alert box
	private void setupErrorBox() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Box");
		alert.setContentText("Reply or Name is empty.");
		alert.showAndWait();
	}

	// This function will setup the name components
	private void setupUpperside(Stage primaryStage) {
		// Set number of columns to organize elements
		int numCols = 4;
		for (int i = 0; i < numCols; i++) { //loops through the cols
			ColumnConstraints colConst = new ColumnConstraints();
			if (i==0) {
				colConst.setPrefWidth(80);
			}
			else if (i==2){
				colConst.setPrefWidth(130);
			}
			else{
				colConst.setPrefWidth(100);
			}
			upperSide.getColumnConstraints().add(colConst);
		}
		// Add elements to the columns
		ClientNameField.setMaxWidth(1000);
		upperSide.add(nameLbl, 0, 0);
		upperSide.add(ClientNameField, 1, 0,2,1);
		upperSide.add(saveBtn, 4, 0);
		// Save chat on save button click
		saveBtn.setOnAction(r -> {
			saveAsFile = fil2_chooser.showSaveDialog(primaryStage);
			saveAs();
		});

	}

	// Sends the conversation to the file using writer and bufferedwriter
	private void saveAs() {
		try (Writer writer = new BufferedWriter(new FileWriter(saveAsFile))) { // Create buffered writer
			// Get textarea context
			String chatContent = chatText.getText();
			// Write to the file
			writer.write(chatContent);
			// Closes the buffered writer
			writer.close();
			// Alert user to confirmation of file being saved
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
	private String message = "";
	private String serverIP = "127.0.0.1";
	private Socket connection;
	// Function to connect to server and start chat
	public void startChat(){
		// Create another thread for server to run on
		Runnable serverTask = new Runnable() {
			@Override
			public void run() {
				try {
					// Establish connection and run input and output stream functions: retrieves and send messages
					connectToAgent();
					setupStreams();
					connectedChat();
				} catch (EOFException eofException) {
					// If server connection interrupted print error message
					showMessage("\n ERROR: Connection interrupted.");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				} finally {
					// Close connection when done
					closeConnection();
				}
			}
		};
		// Start server thread
		Thread serverThread = new Thread(serverTask);
		serverThread.start();
	}

	// Function to connect to Agent on server
	private void connectToAgent() throws IOException{
		showMessage(" Attempting to connect to an Agent... \n");
		// Get server connection
		connection = new Socket(InetAddress.getByName(serverIP), 3000);
		showMessage("\n Now connected to: " + connection.getInetAddress().getHostName());
	}

	// Function to setup streams
	private void setupStreams() throws IOException{
		// Setup input and output streams
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		// Print confirmation message
		showMessage("\n Agent is ready to chat. \n");
	}

	// Function to handle messages while chatting
	private void connectedChat() throws IOException{
		do{
			try{
				// Append user's message
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
		showMessage("\n Agent has left the chat.\n Connection terminated. \n");
		try{
			// Close path to and from Agent
			output.close();
			input.close();
			connection.close(); //close connection
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	// Send message to the Agent
	private void sendMessage(String message){
		try{
			output.writeObject(message);
			output.flush();
		}catch(IOException ioException){
			chatText.appendText("\n ERROR: Message not sent, please retry.");
		}
	}

	// Update chat window with message
	private void showMessage(final String message){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						// Show message in text box
						chatText.appendText(message);
					}
				}
		);
	}



}
