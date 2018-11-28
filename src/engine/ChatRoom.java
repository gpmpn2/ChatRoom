package engine;


import java.io.DataInputStream;
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
	
	private ArrayList<ClientConnection> clients;
	
	public ArrayList<ClientConnection> getClients() {
		return this.clients;
	}
	
	public static void main(String[] args) {
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
				Socket socket = mainSocket.accept();
				ClientConnection clientConnection = new ClientConnection(this, socket);
				Thread thread = new Thread(clientConnection);
				thread.start();
				System.out.println("CONNECTED!");
				clients.add(clientConnection);
			} catch (IOException io) {
				System.out.println("Failed to accept client on port: " + PORT);
				io.printStackTrace();
			}
		}
	}
}
