package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

import server.ServerConnection;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class Client {
	private static final String HOST = "localhost";
	private static final int PORT = 14188;
	
	private String userName;
	private String password;
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public static void main(String args[]) {
		System.out.println("GPMPN2 - Chat Room");
		
		String input = null;
		Scanner scanner = null;
		
		try {
			scanner =  new Scanner(System.in);
			System.out.println("New user? Use command 'newuser name password'."
					+ "\nExisting user? Use command 'login name password'."
					+ "\nLogin:\n");
			
			boolean successfulLogin = false;
			String userName = "";
			String password = "";
			
			while(input == null || !successfulLogin) {
				input = scanner.nextLine();
				
				String values[] = input.split(" ");
				
				if (input.trim().equals("")) {
					System.out.println("You cannot have a blank username.");
				} else if (values.length != 3) {
					System.out.println("[Server] Denied. Please login first.");
				} else if (values.length == 3) {
				
					String command = values[0];
					userName = values[1];
					password = values[2];
					
					if (!checkLoginCommand(command)) {
						System.out.println("[Server] Denied. Please login first.");
					} else {
						successfulLogin = true;
					}
				}
				
				
			}
			
			Client client = new Client(userName, password);
			client.connectClient(scanner);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
	
	private Client(String userName, String password) {
		setUserName(userName);
		setPassword(userName);
	}
	
	private void connectClient(Scanner scanner) {
		try {
			Socket socket = new Socket(HOST, PORT);
			System.out.println("Establishing Connection...");
			Thread.sleep(1000);
			
			PrintWriter outputLocation = new PrintWriter(socket.getOutputStream(), false);
			outputLocation.println("user " + getUserName());
			outputLocation.flush();
			
			ServerConnection serverConnection = new ServerConnection(socket, getUserName());
			Thread thread = new Thread(serverConnection);
			thread.start();
			
			while(thread.isAlive()) {
				if (scanner.hasNextLine()) {
					serverConnection.pushMessage(scanner.nextLine());
				} else {
					Thread.sleep(200);
				}
			}
		} catch (ConnectException ce) {
			System.out.println("Unable to connect to port: " + PORT + " on host " + HOST);
			//ce.printStackTrace();
		} catch (IOException io) {
			io.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	private static boolean checkLoginCommand(String command) {
		switch (command) {
		case "login":
		case "newuser":
			return true;
			default:
				return false;
		}
	}
}
