package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class ServerConnection implements Runnable {
	private Socket socket;
	private String userName;
	private final LinkedList<String> messagesToSend;
	private boolean hasMessageToLoad;
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public LinkedList<String> getMessagesToSend() {
		return this.messagesToSend;
	}
	
	public ServerConnection(Socket socket, String userName) {
		setSocket(socket);
		setUserName(userName);
		messagesToSend = new LinkedList<>();
		hasMessageToLoad = false;
	}
	
	public void pushMessage(String input) {
		String message = "";
		String command = "";
		
		String values[] = input.split(" ");
		if (values.length < 2) {
			System.out.println("[Server] Denied. Invalid input.");
			return;
		}
		
		command = message = values[0];
		
		for(int i = 1;i < values.length; i++) {
			message = message.concat((i == 1 ? "" : " ") + values[i]);
		}
		
		if (!checkCommand(command)) {
			System.out.println("[Server] Denied. Invalid input.");
			return;
		}
		
		switch(command) {
		case "send":
			synchronized (getMessagesToSend()) {
				hasMessageToLoad = true;
				getMessagesToSend().push(message);
			}
			return;
		case "logout":
			//TODO
			return;
		}
	}
	
	private static boolean checkCommand(String command) {
		switch (command) {
		case "send":
		case "logout":
			return true;
			default:
				return false;
		}
	}
	
	@Override
	public void run() {
		System.out.println("Successfully logged in as " + getUserName()
								+ "\nLocal Port: " + getSocket().getLocalPort()
								+ "\nServer Address: " + getSocket().getRemoteSocketAddress()
								+ "\nServer Port: " + getSocket().getPort());
		
		Scanner scanner = null;
		
		try {
			PrintWriter outputLocation = new PrintWriter(getSocket().getOutputStream(), false);
			InputStream inputLocation = getSocket().getInputStream();
			scanner = new Scanner(inputLocation);
			
			while(!getSocket().isClosed()) {
				if (inputLocation.available() > 0) {
					if (scanner.hasNextLine()) {
						System.out.println(scanner.nextLine());
					}
				}
				if (hasMessageToLoad) {
					String messageToSend = "";
					synchronized (getMessagesToSend()) {
						messageToSend = getMessagesToSend().pop();
						hasMessageToLoad = !getMessagesToSend().isEmpty();
					}
					outputLocation.println(getUserName() + ": " + messageToSend);
					outputLocation.flush();
				}
			}
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
}
