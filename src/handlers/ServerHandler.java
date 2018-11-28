package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import server.Server;
import server.User;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class ServerHandler extends Thread {
	private Socket socket;
	private int id;
	private PrintWriter output;
	private BufferedReader input;
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return this.id;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void setOutput(PrintWriter output) {
		this.output = output;
	}
	
	public PrintWriter getOutput() {
		return this.output;
	}
	
	public void setInput(BufferedReader input) {
		this.input = input;
	}
	
	public BufferedReader getInput() {
		return this.input;
	}
	
	public ServerHandler(Socket socket, int id) {
		setID(id);
		setSocket(socket);
		
		try {
			setOutput(new PrintWriter(getSocket().getOutputStream(), true));
			setInput(new BufferedReader(new InputStreamReader(getSocket().getInputStream())));
		} catch (IOException io) {
			System.out.println("Chat Room could not resolve host name.");
			io.printStackTrace();
		}
	}
	
	public void run() {
		getOutput().println("Welcome to the Chat Room!");
		
		try {
		while(true) {
			String message = getInput().readLine();
			
			if (message == null) {
				break;
			}
			
			String values[] = message.split(" ");
			String command = values[0];
			
			switch(command) {
			case "newuser":
				
				break;
			case "login":
				
				break;
			case "who":
				
				break;
			case "send":
				
				break;
			case "logout":
				
				break;
				default:
					getOutput().println("[Server] Invalid command.");
					break;
			}
		}
		} catch (IOException io) {
			
		} finally {
			try {
				getSocket().close();
				//logout();
				System.out.println("A user has left the Chat Room.");
			} catch (IOException io) {
				System.err.println("[Error] failure when attempting to remove user from the Chat Room.");
			}
		}
	}
	
	private static boolean checkNewUser(String username) {
		for(User user : Server.validUsers) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return false;
			}
		}
		return true;
	}
}
