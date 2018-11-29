package server;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import handlers.ServerHandler;

/**
 * 
 * @author Grant Maloney | gpmpn2 | 11/26/18
 *
 */
public class Server {
	private static final int PORT = 14188;
	private static final int MAX_CONNECTIONS = 3;
	public static ArrayList<ServerHandler> clients = new ArrayList<>();
	public static ArrayList<Member> members = new ArrayList<>();
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
	
	public static void main(String[] args) {
		Server chatRoom = new Server();
		chatRoom.loadMembers();
		
		try {
			chatRoom.bootUp();
		} catch (IOException io) {
			System.err.println("[Error] Port already in use, or failed to bind port.");
			io.printStackTrace();
		}
	}
	
	private void bootUp() throws IOException {
		System.out.println("Server booting up...");
		ServerSocket mainSocket = new ServerSocket(PORT);
		System.out.println("Chat Room now listening on port: " + PORT);
		
		try {
			while(true) {
				if (clients.size() != MAX_CONNECTIONS) {
					ServerHandler connection = new ServerHandler(mainSocket.accept());
					connection.start();
					clients.add(connection);
				}
			}
		} finally {
			mainSocket.close();
		}
	}
	
	private void loadMembers() {
		try {
			File file = new File(System.getProperty("user.home") + "/Desktop/users.txt");
		
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			String len = "";
			while((len = br.readLine()) != null) {
				String values[] = len.split("\t");
				members.add(new Member(values[0], values[1]));
			}
			br.close();
			System.out.println("Found " + members.size() + " members registered.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int activeMembers() {
		int count = 0;
		
		for(ServerHandler handler : clients) {
			if (!handler.getUsername().equals("")) {
				count++;
			}
		}
		
		return count;
	}
	
	public static void log(String message) {
		LocalDateTime ldt = LocalDateTime.now();
		String formattedDate = formatter.format(ldt);
		System.out.println("[" + formattedDate + "] " + message);
	}
}
