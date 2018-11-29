package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import server.Server;
import server.Member;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class ServerHandler extends Thread {
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private String username = "";
	private boolean logoutInitiated;
	
	/*
	 * Getters and Setters
	 */
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
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Constructor for the Server handler behind each member
	 * @param socket
	 * @param id
	 */
	public ServerHandler(Socket socket) {
		setSocket(socket);
		
		try {
			setOutput(new PrintWriter(getSocket().getOutputStream(), true));
			setInput(new BufferedReader(new InputStreamReader(getSocket().getInputStream())));
		} catch (IOException io) {
			System.out.println("Chat Room could not resolve host name.");
			io.printStackTrace();
		}
	}
	
	/**
	 * Handles any actions that a member may do while in the Chat Room
	 */
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
					if (values.length == 3) {
						createNewUser(values[1], values[2]);
					} else {
						getOutput().println("[Server] Incorrect syntax. Try \"newuser username password\".");
					}
					break;
				case "login":
					if (values.length == 3) {
						login(values[1], values[2]);
					} else {
						getOutput().println("[Server] Incorrect syntax. Try \"login username password\".");
					}
					break;
				case "who":
					if (values.length == 1) {
						who();
					} else {
						getOutput().println("[Server] Incorrect syntax. Try \"who\".");
					}
					break;
				case "send":
					if (values.length > 2) {
						String messageToSend = "";
						for(int i = 2; i < values.length ;i++) {
							messageToSend = messageToSend.concat((i == 2 ? "" : " ") + values[i]);
						}
						
						if (values[1].equalsIgnoreCase("all")) {
							sendAll(messageToSend);
						} else {
							send(values[1], messageToSend);
						}
					} else {
						getOutput().println("[Server] Incorrect syntax. Try \"send user message...\" or \"send all message\".");
					}
					break;
				case "logout":
					if (values.length == 1) {
						logoutInitiated = true;
						logout();
					} else {
						getOutput().println("[Server] Incorrect syntax. Try \"logout\".");
					}
					break;
					default:
						getOutput().println("[Server] Invalid command.");
						break;
				}
			}
		} catch (IOException io) {
			System.err.println("[Error] Reading input.");
			io.printStackTrace();
		} finally {
			try {
				getSocket().close();
				if (!logoutInitiated) {
					logout();
				}
			} catch (IOException io) {
				System.err.println("[Error] failure when attempting to remove user from the Chat Room.");
			}
		}
	}
	
	/**
	 * Handles creating a new user
	 * @param username
	 * @param password
	 */
	private void createNewUser(String username, String password) {
		if (!getUsername().equals("")) {
			getOutput().println("[Server] You can't do this while logged in.");
			return;
		}
		
		if (checkForActiveMember(username)) {
			getOutput().println("[Server] This member is already logged in.");
			return;
		}
		
		if (username.length() >= 32) {
			getOutput().println("[Server] Username must be less than 32 characters.");
			return;
		}
		
		if (username.length() == 0) {
			getOutput().println("[Server] Username cannot be blank.");
			return;
		}
		
		if (password.length() < 4 || password.length() > 8) {
			getOutput().println("[Server] Password must be between 4-8 characters.");
			return;
		}
		
		//Preventing use of all, because this username could cause conflict when sending a private message to a user named 'all'
		if (checkUsername(username) || username.equalsIgnoreCase("all")) {
			Member member = new Member(username, password);
			member.save();
			Server.members.add(member);
			login(username, password);
		} else {
			getOutput().println("[Server] This username is alreay in-use.");
		}
	}
	
	/**
	 * Handles logging a user in
	 * @param username
	 * @param password
	 */
	private void login(String username, String password) {
		if (!getUsername().equals("")) {
			getOutput().println("[Server] You can't do this while logged in.");
			return;
		}
		
		if (checkForActiveMember(username)) {
			getOutput().println("[Server] This member is already logged in.");
			return;
		}
		
		//Preventing use of all, because this username could cause conflict when sending a private message to a user named 'all'
		if (checkAccount(username, password) || username.equalsIgnoreCase("all")) {
			setUsername(username);
			getOutput().println("You are now logged in as " + username);
			for(ServerHandler handler : Server.clients) {
				if (!handler.getUsername().equalsIgnoreCase(getUsername())) {
					handler.getOutput().println("[Server] " + getUsername() + " has logged into the Chat Room!");
				}
			}
			Server.log(getUsername() + " has logged in.");
		} else {
			getOutput().println("[Server] Invalid username or password.");
		}
	}
	
	/**
	 * Handles displaying all current members (active) in the Chat Room
	 */
	private void who() {
		if (Server.activeMembers() == 0) {
			getOutput().println("[Server] There are currently no active members in the Chat Room!");
			return;
		}
		
		getOutput().println("[Chat Room Members]");
		for(ServerHandler handler : Server.clients) {
			getOutput().println("[Member] " + handler.getUsername());
		}
	}
	
	/**
	 * Handles sending a broadcast message to all members of the Chat Room
	 * @param message
	 */
	private void sendAll(String message) {
		if (getUsername().equals("")) {
			getOutput().println("[Server] Please login before doing this.");
			return;
		}
		
		for(ServerHandler handler : Server.clients) {
			if (!handler.getUsername().equalsIgnoreCase(getUsername())) {
				handler.getOutput().println(getUsername() + ": " + message);
			}
		}
	}
	
	/**
	 * Handles sending a private message to only a single other member in the Chat Room
	 * @param reciever
	 * @param message
	 */
	private void send(String reciever, String message) {
		if (getUsername().equals("")) {
			getOutput().println("[Server] Please login before doing this.");
			return;
		}
		
		if (reciever.equals("")) {
			getOutput().println("[Server] You can't send a message to an blank member name.");
			return;
		}
		
		boolean found = false;
		
		for(ServerHandler handler : Server.clients) {
			if (handler.getUsername().equalsIgnoreCase(reciever)) {
				found = true;
				handler.getOutput().println(getUsername() + ": " + message);
			}
		}
		
		if (!found) {
			getOutput().println("[Server] Member " + reciever + " is not currently in the Chat Room.");
		}
	}
	
	/**
	 * Handles logging out a member
	 */
	private void logout() {
		int index = -1;
		
		for(int i = 0; i < Server.clients.size();i++) {
			if (Server.clients.get(i).getUsername().equalsIgnoreCase(getUsername())) {
				index = i;
				break;
			}
		}
		
		if (index != -1) {
			getOutput().println("You have been removed from the Chat Room.");
			Server.clients.remove(index);
			
			if (!getUsername().equals("")) {
				for(ServerHandler handler : Server.clients) {
					handler.getOutput().println("[Server] " + getUsername() + " has logged out of the Chat Room.");
				}
				Server.log(getUsername() + " has logged out.");
			} else {
				Server.log("A unregistered member has left the lobby.");
			}
		} else {
			Server.log("Fatal issue while logging out member " + getUsername() + ".");
		}
		
	}
	
	/**
	 * Handles checking the given username against the current list of already registered members
	 * @param username
	 * @return
	 */
	private boolean checkUsername(String username) {
		for(Member member : Server.members) {
			if (member.getUsername().equalsIgnoreCase(username)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks the given username and password against all members to see if it is valid or not
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean checkAccount(String username, String password) {
		for(Member member : Server.members) {
			if (member.getUsername().equalsIgnoreCase(username) && member.getPassword().equals(password)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks to see if a given username is an active member in the Chat Room
	 * @param username
	 * @return
	 */
	private boolean checkForActiveMember(String username) {
		for(ServerHandler handler : Server.clients) {
			if (!handler.getUsername().equals("") && handler.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	}
}
