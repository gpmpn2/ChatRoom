package engine;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import client.ClientConnection;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class ChatRoom {
	private static final int PORT = 14188;
	
	private static final int MAX_CONNECTIONS = 3;
	
	private ArrayList<ClientConnection> clients;
	
	public static ArrayList<User> validUsers = new ArrayList<>();
	
	public ArrayList<ClientConnection> getClients() {
		return this.clients;
	}
	
	public static void main(String[] args) {
		loadAllUsers();
		ChatRoom chatRoom = new ChatRoom();
		chatRoom.bootUp();
	}
	
	private void bootUp() {
		clients = new ArrayList<>();
		ServerSocket mainSocket = null;
		
		try {
			mainSocket = new ServerSocket(PORT);
			openToConnections(mainSocket);
		} catch (IOException io) {
			System.out.println("Failed to listen on port: " + PORT);
			io.printStackTrace();
		}
	}
	
	private void openToConnections(ServerSocket mainSocket) {
		System.out.println("Chat Room now listening on port: " + PORT);
		
		while(true) {
			try {
				if (clients.size() < MAX_CONNECTIONS) {
					System.out.println("SIZE: " + validUsers.size());
					Socket socket = mainSocket.accept();
					ClientConnection clientConnection = new ClientConnection(this, socket);
					Thread thread = new Thread(clientConnection);
					thread.start();
					clients.add(clientConnection);
				}
			} catch (IOException io) {
				System.out.println("Failed to accept client on port: " + PORT);
				io.printStackTrace();
			}
		}
	}
	
	private static void loadAllUsers() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("src/users.txt")));
			String len = "";
			while((len = br.readLine()) != null) {
				String values[] = len.split("\t");
				validUsers.add(new User(values[0], values[1]));
			}
			System.out.println("Found " + validUsers.size() + " users registered.");
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
