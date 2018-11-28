package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import engine.ChatRoom;
import engine.User;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class ClientConnection implements Runnable {
	private Socket socket;
	private ChatRoom chatRoom;
	private PrintWriter messenger;
	
	public void setChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}
	
	public ChatRoom getChatRoom() {
		return this.chatRoom;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void setMessenger(PrintWriter messenger) {
		this.messenger = messenger;
	}
	
	public PrintWriter getMessenger() {
		return this.messenger;
	}
	
	public ClientConnection(ChatRoom chatRoom, Socket socket) {
		setChatRoom(chatRoom);
		setSocket(socket);
		
		try {
			setMessenger(new PrintWriter(getSocket().getOutputStream(), false));
		} catch (IOException io) {
			System.out.println("Chat Room could not resolve host name.");
			io.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Scanner scanner = null;
		
		try {
			scanner = new Scanner(getSocket().getInputStream());
			
			while(!socket.isClosed()) {
				if (scanner.hasNextLine()) {
					String message = scanner.nextLine();
					
					if (message.startsWith("send")) {
						String values[] = message.split(" ");
						String name = values[1];
						String messageToSend = "";
						
						for(int i = 2;i < values.length; i++) {
							messageToSend = messageToSend.concat((i == 2 ? "" : " ") + values[i]);
						}
						
						for(ClientConnection connection : getChatRoom().getClients()) {
							PrintWriter messenger = connection.getMessenger();
							if (messenger != null) {
								messenger.write(name + ": " + messageToSend + "\r\n");
								messenger.flush();
							}
						}
					} else if (message.startsWith("logout")) {
						
					} else if (message.startsWith("newuser")) {
						String values[] = message.split(" ");
						String username = values[1];
						String password = values[2];
						
						if (checkNewUser(username)) {
							User user = new User(username, password);
							user.save();
							ChatRoom.validUsers.add(user);
							System.out.println(message.split(" ")[1] + " has logged into the Chat Room.");
						} else {
							PrintWriter messenger = this.getMessenger();
							messenger.write("[Server] Denied. Account already exists.\r\n");
						}
					}
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
	
	private static boolean checkNewUser(String username) {
		for(User user : ChatRoom.validUsers) {
			if (user.getUsername().equalsIgnoreCase(username)) {
				return false;
			}
		}
		return true;
	}
}
