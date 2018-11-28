package server;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import handlers.ServerHandler;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class Server {
	private static final int PORT = 14188;
	
	private static final int MAX_CONNECTIONS = 3;
	
	private ArrayList<ServerHandler> clients;
	
	public static ArrayList<User> validUsers = new ArrayList<>();
	
	public ArrayList<ServerHandler> getClients() {
		return this.clients;
	}
	
	public static void main(String[] args) {
		loadAllUsers();
		Server chatRoom = new Server();
		
		try {
			chatRoom.bootUp();
		} catch (IOException io) {
			System.err.println("[Error] Port already in use, or failed to bind port.");
			io.printStackTrace();
		}
	}
	
	private void bootUp() throws IOException {
		clients = new ArrayList<>();
		ServerSocket mainSocket = new ServerSocket(PORT);
		System.out.println("Chat Room now listening on port: " + PORT);
		
		try {
			while(true) {
				ServerHandler connection = new ServerHandler(mainSocket.accept(), clients.size() + 1);
				connection.start();
				clients.add(connection);
			}
		} finally {
			mainSocket.close();
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
			br.close();
			System.out.println("Found " + validUsers.size() + " users registered.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
