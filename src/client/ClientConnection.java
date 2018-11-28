package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import engine.ChatRoom;

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
					
					if (message.startsWith("user")) {
						System.out.println(message.split(" ")[1] + " has logged into the Chat Room.");
					} else if (message.startsWith("send")) {
						for(ClientConnection connection : getChatRoom().getClients()) {
							PrintWriter messenger = connection.getMessenger();
							if (messenger != null) {
								messenger.write(message + "\r\n");
								messenger.flush();
							}
						}
					} else if (message.startsWith("logout")) {
						
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
}
