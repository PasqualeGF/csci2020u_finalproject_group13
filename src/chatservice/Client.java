package chatservice;/*
* 										CSCI2020 FinalProject
* 										Group 13
* 											
*/

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
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

public class Client extends Application {

	// creating Window Components
	// creating GUI Components
	// this will contain the window
	VBox container = new VBox();
	// contains nameLabel,ClientNameField and save button
	GridPane upperSide = new GridPane();
	// this is create scrollbar for the textarea
	ScrollPane chatBar = new ScrollPane();
	TextArea chatText = new TextArea();
	// this will contain the replybutton, messageBody
	GridPane lowerSide = new GridPane();
	// create UI controls
	// this will create reply button which triggers the response function.
	Button replyBtn = new Button("Reply");
	// this button will save the chat text into a txt file.
	Button saveBtn = new Button("Save Chat");
	Label nameLbl = new Label(" Client Name:");
	//reply Body
	TextField messageBody = new TextField();
	// this will add the user name from the textfield
	TextField ClientNameField = new TextField();
	// Target file to save the conversation.
	File saveAsFile;
	// File Chooser to save the file
	FileChooser fil2_chooser = new FileChooser();

	// Main start function
	@Override
	public void start(Stage primaryStage) throws Exception {
		// this will create and configure User Window
		primaryStage.setResizable(false);
		setupGUI(primaryStage);
		// This will start the chat server, see near bottom of program for server code
		startChat();

	}

	private void setupGUI(Stage primaryStage) {
		// this method will configure the upperSide GridPane
		// contains nameLabel,ClientNameField and save button
		setupUpperside(primaryStage);
		// this method will configure the lowerSide GridPane 
		//contains replyBtn,messageBody
		setupLowerSide();
		// this method will configure the ScrollBar and the TextArea
		setupChat();
		// adding childern to Vbox
		setupContainer();
		// show the scene
		setupStage(primaryStage);
	}

	// this function will setup the stage settings
	private void setupStage(Stage primaryStage) {
		Scene scene = new Scene(container);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.show();
	}

	// this function will setup the container area
	private void setupContainer() {
		// add components in order to fill the window
		container.getChildren().addAll(upperSide, chatBar, lowerSide);
	}

	/*
	 * this function will setup the the chat scroll bar it will add the textarea to
	 * it
	 */
	private void setupChat() {
		// this will disable the textarea using SetEditable();
		chatText.setEditable(false);
		// this will add the textarea to the scrollbar
		chatBar.setContent(chatText);
		// this will change the default font color to black

	}

	// this function will setup the chat components
	private void setupLowerSide() {
		//Set number of cols to organize elements
		int numCols = 4;
		for (int i = 0; i < numCols; i++) {
			ColumnConstraints colConst = new ColumnConstraints();
			colConst.setPrefWidth(110);
			if (i==0){
				colConst.setPrefWidth(104);
			}
			lowerSide.getColumnConstraints().add(colConst);
		}
		// set message body max width
		messageBody.setMaxWidth(2000);
		// this will add the reply button and the messageBody to the LowerPane
		lowerSide.add(replyBtn, 4, 0);
		lowerSide.add(messageBody, 0, 0,4,1);
		replyBtn.setOnAction(
				// setup the reply by adding the text to chat area and write it to the client.
				
				e -> {//check if any of the fields is empty and stop the process
					if (messageBody.getText().isEmpty() == false & ClientNameField.getText().isEmpty()==false)
						{chatText.appendText(
								"\nClient (" + ClientNameField.getText() + "):" + "    " + messageBody.getText());
						// Send message to Agent
							sendMessage("\nClient (" + ClientNameField.getText() + "):" + "    " + messageBody.getText());
						// will stop the client from changing name
						ClientNameField.setDisable(true);
						}
					
					else {// this code will stop the user from sending empty replies it uses alert Box
						setupErrorBox();
					}
					// reset the reply text
					messageBody.setText("");
					// send the response to the client
					// -----
					
				});

	}

	// this code will stop the user from sending empty replies it uses alert Box
	private void setupErrorBox() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Box");
		alert.setContentText("Reply or Name is empty.");
		alert.showAndWait();
	}

	// this function will setup the name components
	private void setupUpperside(Stage primaryStage) {
		//Set number of cols to organize elements
		int numCols = 4;
		for (int i = 0; i < numCols; i++) {
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
		ClientNameField.setMaxWidth(1000);
		upperSide.add(nameLbl, 0, 0);
		upperSide.add(ClientNameField, 1, 0,2,1);
		upperSide.add(saveBtn, 4, 0);
		saveBtn.setOnAction(r -> {
			saveAsFile = fil2_chooser.showSaveDialog(primaryStage);
			saveAs();
		});

	}

	// Creates buffer writer, get the textarea content, write to the file, then it closes the connection
	private void saveAs() {
		try (Writer writer = new BufferedWriter(new FileWriter(saveAsFile))) {
			String chatContent = chatText.getText();
			writer.write(chatContent);
			writer.close();
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
					// Establish connection and run input and output stream functions to retrieve and send messages
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
		showMessage("\n Closing connections... \n");
		try{
			// Close path to and from Agent, as well as connection
			output.close();
			input.close();
			connection.close();
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
