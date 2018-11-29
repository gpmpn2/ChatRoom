package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Member {

	private String username;
	private String password;
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public Member(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public void save() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") + "/Desktop/users.txt"), true));
			bw.write(getUsername() + "\t" + getPassword() + "\n");
			bw.flush();
			bw.close();
			System.out.println("Successfully saved " + username + ".");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
